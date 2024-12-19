package com.airtimebot.app.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.util.Log

class SmsReceiver : BroadcastReceiver() {
    private val TAG = "SmsReceiver"
    
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
            messages?.forEach { message ->
                if (message.displayMessageBody.contains("M-PESA")) {
                    Log.d(TAG, "M-Pesa message received")
                    val serviceIntent = Intent(context, SmsProcessorService::class.java)
                    serviceIntent.putExtra("sms_body", message.displayMessageBody)
                    context.startService(serviceIntent)
                }
            }
        }
    }
}