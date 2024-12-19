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

class SmsProcessorService : Service() {
    private val TAG = "SmsProcessorService"
    private val CHANNEL_ID = "AirtimeBotService"
    private val NOTIFICATION_ID = 1

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Service started")
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
            .build()
    }

    private fun processMpesaMessage(message: String) {
        // Extract amount using regex
        val amountRegex = "Ksh(\\d+\\.?\\d*)".toRegex()
        val phoneRegex = "(07\\d{8})".toRegex()
        
        val amountMatch = amountRegex.find(message)
        val phoneMatch = phoneRegex.find(message)
        
        if (amountMatch != null && phoneMatch != null) {
            val amount = amountMatch.groupValues[1].toFloat()
            val phone = phoneMatch.groupValues[1]
            
            // Dial USSD
            val ussdCode = "*544*4*6*0700396314#"
            val ussdUri = Uri.parse("tel:$ussdCode")
            val intent = Intent(Intent.ACTION_CALL, ussdUri)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) 
                == PackageManager.PERMISSION_GRANTED) {
                startActivity(intent)
            }
            
            // Log transaction
            Log.d(TAG, "Processing payment: $amount from $phone")
        }
    }
}