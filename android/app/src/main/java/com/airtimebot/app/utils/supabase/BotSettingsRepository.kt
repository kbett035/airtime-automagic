package com.airtimebot.app.utils.supabase

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.json.JsonObject

object BotSettingsRepository {
    private const val TAG = "BotSettingsRepository"
    private const val TIMEOUT_MS = 30000L

    suspend fun fetchBotSettings(): Map<String, Any>? = withContext(Dispatchers.IO) {
        try {
            withTimeout(TIMEOUT_MS) {
                val response = SupabaseClient.client.postgrest["bot_settings"].select()
                val result = response.decodeList<JsonObject>().firstOrNull()
                result?.let { JsonUtils.convertJsonObjectToMap(it) }
            }
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "Timeout fetching bot settings")
            null
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching bot settings: ${e.message}")
            null
        }
    }
}