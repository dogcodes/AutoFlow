package com.carlos.autoflow.platform.ad.config

import android.content.Context
import com.google.gson.Gson

class AdConfigStore(
    context: Context,
    private val gson: Gson = Gson()
) {
    companion object {
        private const val PREFS_NAME = "ad_remote_config"
        private const val KEY_CONFIG_JSON = "config_json"
    }

    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun loadConfig(): RemoteAdConfiguration? {
        val json = prefs.getString(KEY_CONFIG_JSON, null) ?: return null
        return try {
            gson.fromJson(json, RemoteAdConfiguration::class.java)
        } catch (t: Throwable) {
            null
        }
    }

    fun saveConfig(config: RemoteAdConfiguration) {
        val json = gson.toJson(config)
        prefs.edit().putString(KEY_CONFIG_JSON, json).apply()
    }

    fun clear() {
        prefs.edit().remove(KEY_CONFIG_JSON).apply()
    }
}
