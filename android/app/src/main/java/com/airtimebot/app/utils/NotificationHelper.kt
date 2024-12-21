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
            context.getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = context.getString(R.string.notification_channel_description)
            setShowBadge(false)
        }
        
        val statusChannel = NotificationChannel(
            STATUS_CHANNEL_ID,
            context.getString(R.string.status_channel_name),
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = context.getString(R.string.status_channel_description)
        }
        
        notificationManager.createNotificationChannel(serviceChannel)
        notificationManager.createNotificationChannel(statusChannel)
    }

    fun createNotification(): Notification {
        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(context.getString(R.string.service_running))
            .setContentText(context.getString(R.string.monitoring_messages))
            .setSmallIcon(R.drawable.ic_notification)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setStyle(NotificationCompat.BigTextStyle())
            .build()
    }

    fun showTransactionNotification(message: String, isError: Boolean = false) {
        val notification = NotificationCompat.Builder(context, STATUS_CHANNEL_ID)
            .setContentTitle(context.getString(if (isError) R.string.transaction_failed else R.string.transaction_update))
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_notification)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .build()

        notificationManager.notify(STATUS_NOTIFICATION_ID, notification)
    }
}