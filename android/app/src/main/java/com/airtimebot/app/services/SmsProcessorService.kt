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
import com.airtimebot.app.utils.UssdQueue
import kotlinx.coroutines.*
import java.util.*
import java.util.concurrent.TimeUnit

class SmsProcessorService : Service() {
    private val TAG = "SmsProcessorService"
    private lateinit var notificationHelper: NotificationHelper
    private lateinit var ussdHandler: UssdHandler
    private lateinit var messageProcessor: MessageProcessor
    private lateinit var telephonyManager: TelephonyManager
    private val serviceScope = CoroutineScope(Dispatchers.IO + Job())
    private val ussdQueue = UssdQueue.getInstance()

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
                        handleTransaction(processed)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error processing message: ${e.message}", e)
                }
            }
        }
        return START_STICKY
    }

    private suspend fun handleTransaction(processed: ProcessedMessage) {
        withContext(Dispatchers.IO) {
            val calendar = Calendar.getInstance()
            val currentTime = calendar.time
            
            // Check if sender has already made a transaction today
            if (hasTransactionToday(processed.phone)) {
                // Schedule for next day 12:05 AM
                calendar.add(Calendar.DAY_OF_MONTH, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 5)
                calendar.set(Calendar.SECOND, 0)
                
                val scheduledTime = calendar.time
                Log.d(TAG, "Transaction scheduled for: $scheduledTime")
                
                // Queue the transaction
                ussdQueue.enqueue(UssdRequest(
                    processed.amount,
                    processed.phone,
                    processed.transactionId,
                    scheduledTime = scheduledTime
                ))
                
                notificationHelper.showNotification(
                    "Transaction Scheduled",
                    "Transaction for ${processed.phone} will be processed at 12:05 AM"
                )
            } else {
                // Process immediately
                ussdHandler.dialUssdWithRetry(
                    processed.amount,
                    processed.phone,
                    processed.transactionId
                )
            }
        }
    }

    private suspend fun hasTransactionToday(phone: String): Boolean {
        // Check if there's any transaction for this phone number today
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        
        // Implementation would check the daily_transactions table
        return false // Placeholder for now
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