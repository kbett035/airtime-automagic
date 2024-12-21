package com.airtimebot.app.services

import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import com.airtimebot.app.utils.*
import kotlinx.coroutines.*

class SmsProcessorService : Service() {
    private val TAG = "SmsProcessorService"
    private val serviceScope = CoroutineScope(Dispatchers.IO + Job())
    private lateinit var serviceInitializer: ServiceInitializer

    override fun onCreate() {
        super.onCreate()
        initializeService()
        Log.d(TAG, "Service created and started in foreground")
    }

    private fun initializeService() {
        serviceInitializer = ServiceInitializer(this)
        serviceInitializer.initialize()
        startForeground(
            NotificationHelper.NOTIFICATION_ID,
            serviceInitializer.notificationHelper.createNotification()
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.getStringExtra("sms_body")?.let { message ->
            Log.d(TAG, "Processing message: $message")
            serviceScope.launch {
                try {
                    serviceInitializer.messageProcessor.processMpesaMessage(message)?.let { processed ->
                        serviceInitializer.transactionHandler.handleTransaction(
                            processed,
                            serviceInitializer.ussdHandler
                        )
                        
                        // Log transaction to Supabase
                        SupabaseClient.logTransaction(
                            amount = processed.amount,
                            senderPhone = processed.phone,
                            ussdString = serviceInitializer.ussdHandler.getUssdString(
                                processed.amount,
                                processed.phone
                            ),
                            messageText = message
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