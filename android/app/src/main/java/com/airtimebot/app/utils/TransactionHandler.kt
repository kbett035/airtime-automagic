package com.airtimebot.app.utils

import android.util.Log
import java.util.*

class TransactionHandler {
    private val TAG = "TransactionHandler"
    private val ussdQueue = UssdQueue.getInstance()

    suspend fun handleTransaction(processed: ProcessedMessage, ussdHandler: UssdHandler) {
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
            
            return
        }
        
        // Process immediately
        ussdHandler.dialUssdWithRetry(
            processed.amount,
            processed.phone,
            processed.transactionId
        )
    }

    private suspend fun hasTransactionToday(phone: String): Boolean {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        
        try {
            val patterns = SupabaseClient.fetchUssdPatterns()
            // Implementation would check the daily_transactions table
            return false // Placeholder for now
        } catch (e: Exception) {
            Log.e(TAG, "Error checking daily transactions: ${e.message}")
            return false
        }
    }
}