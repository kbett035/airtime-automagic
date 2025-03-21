package com.airtimebot.app.utils

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.telephony.TelephonyManager
import android.util.Log
import androidx.core.app.ActivityCompat
import kotlinx.coroutines.*

class UssdHandler(
    private val context: Context,
    private val telephonyManager: TelephonyManager
) {
    private val handler = Handler(Looper.getMainLooper())
    private val TAG = "UssdHandler"
    private val queue = UssdQueue.getInstance()
    private val scope = CoroutineScope(Dispatchers.IO + Job())
    private var ussdFormat = "*544*4*6*{phone}#" // Default format

    init {
        // Fetch USSD format from Supabase
        scope.launch {
            try {
                SupabaseClient.fetchBotSettings()?.let { settings ->
                    settings["ussd_format"]?.toString()?.let { format ->
                        ussdFormat = format
                        Log.d(TAG, "USSD format updated: $format")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching USSD format: ${e.message}")
            }
        }
    }

    fun getUssdString(amount: Float, phone: String): String {
        return ussdFormat.replace("{phone}", phone)
    }

    fun dialUssdWithRetry(amount: Float, senderPhone: String, messageId: String = "", retryCount: Int = 0) {
        val request = UssdRequest(amount, senderPhone, messageId, retryCount)
        queue.enqueue(request)
        
        try {
            if (retryCount >= 3) {
                Log.e(TAG, "Max retry attempts reached for USSD dialing")
                updateTransactionStatus(messageId, "failed")
                return
            }

            val ussdCode = getUssdString(amount, senderPhone)
            val ussdUri = Uri.parse("tel:$ussdCode")
            val intent = Intent(Intent.ACTION_CALL, ussdUri).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                putExtra("com.android.phone.extra.slot", 0)
                putExtra("simSlot", 0)
            }
            
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) 
                == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Dialing USSD: $ussdCode")
                context.startActivity(intent)
                
                // Monitor call state
                scope.launch {
                    delay(5000) // Wait for call to complete
                    if (!isUssdCallSuccessful()) {
                        Log.d(TAG, "USSD call failed, retrying... Attempt: ${retryCount + 1}")
                        updateTransactionStatus(messageId, "retrying")
                        dialUssdWithRetry(amount, senderPhone, messageId, retryCount + 1)
                    } else {
                        updateTransactionStatus(messageId, "completed")
                    }
                }
            } else {
                Log.e(TAG, "Missing CALL_PHONE permission")
                updateTransactionStatus(messageId, "failed")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error dialing USSD: ${e.message}")
            handler.postDelayed({
                updateTransactionStatus(messageId, "retrying")
                dialUssdWithRetry(amount, senderPhone, messageId, retryCount + 1)
            }, 2000)
        }
    }

    private fun isUssdCallSuccessful(): Boolean {
        return telephonyManager.callState == TelephonyManager.CALL_STATE_IDLE
    }

    private fun updateTransactionStatus(messageId: String, status: String) {
        scope.launch {
            try {
                // Update status in Supabase
                Log.d(TAG, "Updating transaction status: $messageId to $status")
            } catch (e: Exception) {
                Log.e(TAG, "Error updating transaction status: ${e.message}")
            }
        }
    }

    fun cleanup() {
        scope.cancel()
        queue.clear()
    }
}