package com.carlos.autoflow.platform.task.config

import android.content.Context
import com.google.gson.Gson

private const val PREFS_NAME = "daily_checkin_config"
private const val KEY_CONFIG_JSON = "config_json"

class DailyCheckInConfigStore(
    context: Context,
    private val gson: Gson = Gson()
) {
    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun loadConfig(): DailyCheckInConfig? {
        val json = prefs.getString(KEY_CONFIG_JSON, null) ?: return null
        return try {
            gson.fromJson(json, DailyCheckInConfig::class.java)
        } catch (_: Throwable) {
            null
        }
    }

    fun saveConfig(config: DailyCheckInConfig) {
        prefs.edit()
            .putString(KEY_CONFIG_JSON, gson.toJson(config))
            .apply()
    }
}
