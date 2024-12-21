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
        const val ERROR_CHANNEL_ID = "TransactionErrors"
        const val ERROR_NOTIFICATION_ID = 3
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
        
        val errorChannel = NotificationChannel(
            ERROR_CHANNEL_ID,
            context.getString(R.string.error_channel_name),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = context.getString(R.string.error_channel_description)
            enableVibration(true)
            enableLights(true)
        }
        
        notificationManager.createNotificationChannel(serviceChannel)
        notificationManager.createNotificationChannel(statusChannel)
        notificationManager.createNotificationChannel(errorChannel)
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
        val channelId = if (isError) ERROR_CHANNEL_ID else STATUS_CHANNEL_ID
        val notificationId = if (isError) ERROR_NOTIFICATION_ID else STATUS_NOTIFICATION_ID
        val title = context.getString(
            if (isError) R.string.transaction_failed 
            else R.string.transaction_update
        )
        
        val notification = NotificationCompat.Builder(context, channelId)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_notification)
            .setPriority(if (isError) NotificationCompat.PRIORITY_HIGH else NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .build()

        notificationManager.notify(notificationId, notification)
    }

    fun cancelNotification(notificationId: Int) {
        notificationManager.cancel(notificationId)
    }
}