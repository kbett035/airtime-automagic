package com.airtimebot.app.utils

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import com.airtimebot.app.R

class NotificationHelper(private val context: Context) {
    companion object {
        const val CHANNEL_ID = "AirtimeBotService"
        const val NOTIFICATION_ID = 1
    }

    fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Airtime Bot Service",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Background service for processing M-Pesa messages"
            setShowBadge(false)
        }
        
        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }

    fun createNotification(): Notification {
        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("Airtime Bot Active")
            .setContentText("Monitoring for M-Pesa messages")
            .setSmallIcon(R.drawable.ic_notification)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
    }
}