package com.airtimebot.app.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.util.Log
import com.airtimebot.app.services.SmsProcessorService

class SmsReceiver : BroadcastReceiver() {
    private val TAG = "SmsReceiver"
    
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
            messages?.forEach { message ->
                val messageBody = message.displayMessageBody
                Log.d(TAG, "SMS received: $messageBody")
                
                if (messageBody.contains("M-PESA") || 
                    messageBody.contains("Confirmed") || 
                    messageBody.contains("received")) {
                    Log.d(TAG, "M-Pesa message detected")
                    val serviceIntent = Intent(context, SmsProcessorService::class.java).apply {
                        putExtra("sms_body", messageBody)
                    }
                    context.startService(serviceIntent)
                }
            }
        }
    }
}