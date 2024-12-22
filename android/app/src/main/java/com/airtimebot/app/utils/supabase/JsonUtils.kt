package com.airtimebot.app.utils.supabase

import kotlinx.serialization.json.JsonObject

object JsonUtils {
    fun convertJsonObjectToMap(jsonObject: JsonObject): Map<String, Any> {
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