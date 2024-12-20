package com.airtimebot.app.utils

import android.util.Log
import java.util.UUID

class MessageProcessor {
    private val TAG = "MessageProcessor"

    fun processMpesaMessage(message: String): ProcessedMessage? {
        try {
            // Enhanced regex patterns for M-Pesa messages
            val amountRegex = "Ksh[\\s.]*(\\d+(?:[.,]\\d{2})?)".toRegex(RegexOption.IGNORE_CASE)
            val phoneRegex = "(254\\d{9}|0[17]\\d{8})".toRegex()
            val transactionIdRegex = "([A-Z0-9]{8,12})".toRegex()
            
            val amountMatch = amountRegex.find(message)
            val phoneMatch = phoneRegex.find(message)
            val transactionIdMatch = transactionIdRegex.find(message)
            
            if (amountMatch != null && phoneMatch != null) {
                // Clean and format the amount
                val amount = amountMatch.groupValues[1]
                    .replace(",", "")
                    .toFloat()
                
                // Format phone number to local format
                var phone = phoneMatch.groupValues[1]
                if (phone.startsWith("254")) {
                    phone = "0${phone.substring(3)}"
                }
                
                // Get transaction ID or generate one
                val transactionId = transactionIdMatch?.groupValues?.get(1) 
                    ?: UUID.randomUUID().toString()
                
                Log.d(TAG, "Processed message - Amount: $amount, Phone: $phone, TransactionId: $transactionId")
                return ProcessedMessage(amount, phone, transactionId)
            } else {
                Log.e(TAG, "Failed to extract amount or phone from message: $message")
                return null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error processing M-Pesa message: ${e.message}", e)
            return null
        }
    }
}

data class ProcessedMessage(
    val amount: Float,
    val phone: String,
    val transactionId: String
)