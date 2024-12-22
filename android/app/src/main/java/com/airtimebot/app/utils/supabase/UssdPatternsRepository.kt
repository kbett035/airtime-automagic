package com.airtimebot.app.utils.supabase

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.json.JsonObject

object UssdPatternsRepository {
    private const val TAG = "UssdPatternsRepository"
    private const val TIMEOUT_MS = 30000L

    suspend fun fetchUssdPatterns(): List<Map<String, Any>>? = withContext(Dispatchers.IO) {
        try {
            withTimeout(TIMEOUT_MS) {
                val response = SupabaseClient.client.postgrest["ussd_patterns"].select()
                val results = response.decodeList<JsonObject>()
                results.map { JsonUtils.convertJsonObjectToMap(it) }
            }
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "Timeout fetching USSD patterns")
            null
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching USSD patterns: ${e.message}")
            null
        }
    }
}