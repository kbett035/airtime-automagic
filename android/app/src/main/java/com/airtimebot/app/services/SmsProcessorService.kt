package com.airtimebot.app.services

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.telephony.TelephonyManager
import android.util.Log
import com.airtimebot.app.utils.MessageProcessor
import com.airtimebot.app.utils.NotificationHelper
import com.airtimebot.app.utils.UssdHandler
import kotlinx.coroutines.*

class SmsProcessorService : Service() {
    private val TAG = "SmsProcessorService"
    private lateinit var notificationHelper: NotificationHelper
    private lateinit var ussdHandler: UssdHandler
    private lateinit var messageProcessor: MessageProcessor
    private lateinit var telephonyManager: TelephonyManager
    private val serviceScope = CoroutineScope(Dispatchers.IO + Job())

    override fun onCreate() {
        super.onCreate()
        initializeComponents()
        startForegroundService()
        Log.d(TAG, "Service created and started in foreground")
    }

    private fun initializeComponents() {
        notificationHelper = NotificationHelper(this)
        telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        ussdHandler = UssdHandler(this, telephonyManager)
        messageProcessor = MessageProcessor()
    }

    private fun startForegroundService() {
        notificationHelper.createNotificationChannel()
        startForeground(NotificationHelper.NOTIFICATION_ID, notificationHelper.createNotification())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.getStringExtra("sms_body")?.let { message ->
            Log.d(TAG, "Processing message: $message")
            serviceScope.launch {
                try {
                    messageProcessor.processMpesaMessage(message)?.let { processed ->
                        ussdHandler.dialUssdWithRetry(
                            processed.amount,
                            processed.phone,
                            processed.transactionId
                        )
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error processing message: ${e.message}", e)
                }
            }
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Service destroyed")
        serviceScope.cancel()
        
        // Restart service if it's destroyed
        val restartIntent = Intent(this, SmsProcessorService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(restartIntent)
        } else {
            startService(restartIntent)
        }
    }
}