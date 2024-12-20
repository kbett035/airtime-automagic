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
        const val STATUS_CHANNEL_ID = "TransactionStatus"
        const val STATUS_NOTIFICATION_ID = 2
    }

    private val notificationManager: NotificationManager by lazy {
        context.getSystemService(NotificationManager::class.java)
    }

    fun createNotificationChannel() {
        val serviceChannel = NotificationChannel(
            CHANNEL_ID,
            "Airtime Bot Service",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Background service for processing M-Pesa messages"
            setShowBadge(false)
        }
        
        val statusChannel = NotificationChannel(
            STATUS_CHANNEL_ID,
            "Transaction Status",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Updates about transaction processing"
        }
        
        notificationManager.createNotificationChannel(serviceChannel)
        notificationManager.createNotificationChannel(statusChannel)
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

    fun showTransactionNotification(message: String, isError: Boolean = false) {
        val notification = NotificationCompat.Builder(context, STATUS_CHANNEL_ID)
            .setContentTitle(if (isError) "Transaction Failed" else "Transaction Update")
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_notification)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(STATUS_NOTIFICATION_ID, notification)
    }
}