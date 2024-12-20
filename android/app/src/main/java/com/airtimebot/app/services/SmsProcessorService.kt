package com.airtimebot.app.services

import android.app.*
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import android.provider.Telephony
import android.net.Uri
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import android.util.Log
import com.airtimebot.app.R
import android.os.Handler
import android.os.Looper
import android.telephony.TelephonyManager
import android.content.Context
import android.os.Build

class SmsProcessorService : Service() {
    private val TAG = "SmsProcessorService"
    private val CHANNEL_ID = "AirtimeBotService"
    private val NOTIFICATION_ID = 1
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var telephonyManager: TelephonyManager

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())
        telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        Log.d(TAG, "Service created and started in foreground")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.getStringExtra("sms_body")?.let { message ->
            Log.d(TAG, "Processing message: $message")
            processMpesaMessage(message)
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Airtime Bot Service",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Background service for processing M-Pesa messages"
            setShowBadge(false)
        }
        
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Airtime Bot Active")
            .setContentText("Monitoring for M-Pesa messages")
            .setSmallIcon(R.drawable.ic_notification)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
    }

    private fun processMpesaMessage(message: String) {
        try {
            val amountRegex = "Ksh\\s*(\\d+(?:\\.\\d{2})?)".toRegex()
            val phoneRegex = "(0[17]\\d{8})".toRegex()
            
            val amountMatch = amountRegex.find(message)
            val phoneMatch = phoneRegex.find(message)
            
            if (amountMatch != null && phoneMatch != null) {
                val amount = amountMatch.groupValues[1].toFloat()
                val phone = phoneMatch.groupValues[1]
                
                Log.d(TAG, "Extracted amount: $amount from phone: $phone")
                
                handler.postDelayed({
                    dialUssdWithRetry(amount, phone)
                }, 1000)
            } else {
                Log.e(TAG, "Failed to extract amount or phone from message: $message")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error processing M-Pesa message: ${e.message}", e)
        }
    }

    private fun dialUssdWithRetry(amount: Float, senderPhone: String, retryCount: Int = 0) {
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
                putExtra("com.android.phone.extra.slot", 0) // Use SIM slot 0
                putExtra("simSlot", 0)
            }
            
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) 
                == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Dialing USSD: $ussdCode")
                startActivity(intent)

                // Schedule a check to verify if the USSD call was successful
                handler.postDelayed({
                    if (!isUssdCallSuccessful()) {
                        Log.d(TAG, "USSD call might have failed, retrying... Attempt: ${retryCount + 1}")
                        dialUssdWithRetry(amount, senderPhone, retryCount + 1)
                    }
                }, 5000) // Wait 5 seconds before checking
            } else {
                Log.e(TAG, "Missing CALL_PHONE permission")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error dialing USSD: ${e.message}", e)
            // Retry after a delay
            handler.postDelayed({
                dialUssdWithRetry(amount, senderPhone, retryCount + 1)
            }, 2000)
        }
    }

    private fun isUssdCallSuccessful(): Boolean {
        // This is a basic implementation. You might want to add more sophisticated checks
        return telephonyManager.callState == TelephonyManager.CALL_STATE_IDLE
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Service destroyed")
        // Restart service if it's destroyed
        val restartIntent = Intent(this, SmsProcessorService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(restartIntent)
        } else {
            startService(restartIntent)
        }
    }
}