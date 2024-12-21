package com.airtimebot.app.utils

import android.content.Context
import android.telephony.TelephonyManager

class ServiceInitializer(private val context: Context) {
    lateinit var notificationHelper: NotificationHelper
    lateinit var ussdHandler: UssdHandler
    lateinit var messageProcessor: MessageProcessor
    lateinit var telephonyManager: TelephonyManager
    lateinit var transactionHandler: TransactionHandler

    fun initialize() {
        notificationHelper = NotificationHelper(context)
        telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        ussdHandler = UssdHandler(context, telephonyManager)
        messageProcessor = MessageProcessor()
        transactionHandler = TransactionHandler()
        
        initializeNotifications()
    }

    private fun initializeNotifications() {
        notificationHelper.createNotificationChannel()
    }
}