package com.airtimebot.app.utils.supabase

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

object TransactionRepository {
    private const val TAG = "TransactionRepository"
    private const val TIMEOUT_MS = 30000L

    suspend fun logTransaction(
        amount: Float,
        senderPhone: String,
        ussdString: String,
        messageText: String,
        status: String = "pending"
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            withTimeout(TIMEOUT_MS) {
                val data = buildJsonObject {
                    put("amount", amount)
                    put("sender_phone", senderPhone)
                    put("ussd_string", ussdString)
                    put("message_text", messageText)
                    put("status", status)
                }
                
                SupabaseClient.client.postgrest["transaction_logs"].insert(data)
                Log.d(TAG, "Transaction logged successfully")
                true
            }
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "Timeout logging transaction")
            false
        } catch (e: Exception) {
            Log.e(TAG, "Error logging transaction: ${e.message}")
            false
        }
    }

    suspend fun updateTransactionStatus(
        transactionId: String,
        status: String
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            withTimeout(TIMEOUT_MS) {
                val data = buildJsonObject {
                    put("status", status)
                }
                SupabaseClient.client.postgrest["transaction_logs"]
                    .update(data)
                    .eq("id", transactionId)
                Log.d(TAG, "Transaction status updated successfully")
                true
            }
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "Timeout updating transaction status")
            false
        } catch (e: Exception) {
            Log.e(TAG, "Error updating transaction status: ${e.message}")
            false
        }
    }
}