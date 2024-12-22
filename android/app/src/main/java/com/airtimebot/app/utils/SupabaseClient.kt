package com.airtimebot.app.utils

import android.util.Log
import com.airtimebot.app.BuildConfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.PostgrestResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

object SupabaseClient {
    private const val TAG = "SupabaseClient"
    private const val TIMEOUT_MS = 30000L // 30 seconds timeout
    
    private val client = createSupabaseClient(
        supabaseUrl = BuildConfig.SUPABASE_URL,
        supabaseKey = BuildConfig.SUPABASE_ANON_KEY
    ) {
        install(Auth)
        install(Postgrest)
    }

    suspend fun fetchBotSettings(): Map<String, Any>? = withContext(Dispatchers.IO) {
        try {
            withTimeout(TIMEOUT_MS) {
                val response: PostgrestResult = client.postgrest["bot_settings"].select()
                val result = response.decodeList<JsonObject>().firstOrNull()
                result?.let { convertJsonObjectToMap(it) }
            }
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "Timeout fetching bot settings")
            null
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching bot settings: ${e.message}")
            null
        }
    }

    suspend fun fetchUssdPatterns(): List<Map<String, Any>>? = withContext(Dispatchers.IO) {
        try {
            withTimeout(TIMEOUT_MS) {
                val response: PostgrestResult = client.postgrest["ussd_patterns"].select()
                val results = response.decodeList<JsonObject>()
                results.map { convertJsonObjectToMap(it) }
            }
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "Timeout fetching USSD patterns")
            null
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching USSD patterns: ${e.message}")
            null
        }
    }

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
                
                client.postgrest["transaction_logs"].insert(data)
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
                client.postgrest["transaction_logs"]
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

    private fun convertJsonObjectToMap(jsonObject: JsonObject): Map<String, Any> {
        return jsonObject.entries.associate { (key, value) ->
            key to when {
                value.isString -> value.toString()
                value.isNumber -> value.toString().toDoubleOrNull() ?: value.toString()
                value.isBoolean -> value.toString().toBoolean()
                else -> value.toString()
            }
        }
    }
}