package com.airtimebot.app.utils

import android.util.Log

class MessageProcessor {
    private val TAG = "MessageProcessor"

    fun processMpesaMessage(message: String): ProcessedMessage? {
        try {
            val amountRegex = "Ksh\\s*(\\d+(?:\\.\\d{2})?)".toRegex()
            val phoneRegex = "(0[17]\\d{8})".toRegex()
            
            val amountMatch = amountRegex.find(message)
            val phoneMatch = phoneRegex.find(message)
            
            if (amountMatch != null && phoneMatch != null) {
                val amount = amountMatch.groupValues[1].toFloat()
                val phone = phoneMatch.groupValues[1]
                
                Log.d(TAG, "Extracted amount: $amount from phone: $phone")
                return ProcessedMessage(amount, phone)
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
    val phone: String
)