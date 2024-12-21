package com.airtimebot.app.utils

import android.util.Log
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern

data class ProcessedMessage(
    val amount: Float,
    val phone: String,
    val transactionId: String,
    val isScheduled: Boolean = false,
    val scheduledTime: Date? = null
)

class MessageProcessor {
    private val TAG = "MessageProcessor"
    private val MPESA_PATTERN = "(?i)(.*?)Ksh([\\d,]+\\.?\\d*)\\s+received\\s+from\\s+(\\d+).*"
    
    fun processMpesaMessage(message: String): ProcessedMessage? {
        try {
            val pattern = Pattern.compile(MPESA_PATTERN)
            val matcher = pattern.matcher(message)
            
            if (matcher.find()) {
                val transactionId = matcher.group(1)?.trim() ?: ""
                val amount = matcher.group(2)?.replace(",", "")?.toFloat() ?: 0f
                var phone = matcher.group(3) ?: ""
                
                // Convert phone number format if needed
                if (phone.startsWith("254")) {
                    phone = "0" + phone.substring(3)
                }
                
                Log.d(TAG, "Extracted: Amount=$amount, Phone=$phone, TxID=$transactionId")
                return ProcessedMessage(amount, phone, transactionId)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error processing message: ${e.message}", e)
        }
        return null
    }
}