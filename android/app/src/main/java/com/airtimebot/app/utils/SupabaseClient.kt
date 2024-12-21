package com.airtimebot.app.utils

import android.util.Log
import com.airtimebot.app.BuildConfig
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.GoTrue
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object SupabaseClient {
    private val TAG = "SupabaseClient"
    
    private val client = createSupabaseClient(
        supabaseUrl = BuildConfig.SUPABASE_URL,
        supabaseKey = BuildConfig.SUPABASE_ANON_KEY
    ) {
        install(GoTrue)
        install(Postgrest)
    }

    suspend fun fetchBotSettings(): Map<String, Any>? = withContext(Dispatchers.IO) {
        try {
            val response = client.postgrest["bot_settings"].select()
            Log.d(TAG, "Bot settings fetched successfully")
            response.decodeSingle<Map<String, Any>>()
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching bot settings: ${e.message}")
            null
        }
    }

    suspend fun fetchUssdPatterns(): List<Map<String, Any>>? = withContext(Dispatchers.IO) {
        try {
            val response = client.postgrest["ussd_patterns"].select()
            Log.d(TAG, "USSD patterns fetched successfully")
            response.decodeList<Map<String, Any>>()
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching USSD patterns: ${e.message}")
            null
        }
    }

    suspend fun logTransaction(
        amount: Float,
        senderPhone: String,
        ussdString: String,
        messageText: String
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val data = mapOf(
                "amount" to amount,
                "sender_phone" to senderPhone,
                "ussd_string" to ussdString,
                "message_text" to messageText
            )
            
            client.postgrest["transaction_logs"].insert(data)
            Log.d(TAG, "Transaction logged successfully")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error logging transaction: ${e.message}")
            false
        }
    }
}