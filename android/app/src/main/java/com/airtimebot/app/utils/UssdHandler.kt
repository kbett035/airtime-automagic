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

class UssdHandler(
    private val context: Context,
    private val telephonyManager: TelephonyManager
) {
    private val handler = Handler(Looper.getMainLooper())
    private val TAG = "UssdHandler"

    fun dialUssdWithRetry(amount: Float, senderPhone: String, retryCount: Int = 0) {
        try {
            if (retryCount >= 3) {
                Log.e(TAG, "Max retry attempts reached for USSD dialing")
                return
            }

            val ussdCode = "*544*4*6*0700396314#"
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

                handler.postDelayed({
                    if (!isUssdCallSuccessful()) {
                        Log.d(TAG, "USSD call might have failed, retrying... Attempt: ${retryCount + 1}")
                        dialUssdWithRetry(amount, senderPhone, retryCount + 1)
                    }
                }, 5000)
            } else {
                Log.e(TAG, "Missing CALL_PHONE permission")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error dialing USSD: ${e.message}", e)
            handler.postDelayed({
                dialUssdWithRetry(amount, senderPhone, retryCount + 1)
            }, 2000)
        }
    }

    private fun isUssdCallSuccessful(): Boolean {
        return telephonyManager.callState == TelephonyManager.CALL_STATE_IDLE
    }
}