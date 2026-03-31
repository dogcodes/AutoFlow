package com.carlos.autoflow.platform.task.config

import android.content.Context
import android.os.Handler
import android.os.Looper
import com.carlos.autoflow.foundation.network.FoundationNetworkClient
import com.carlos.autoflow.foundation.network.NetworkResult
import com.google.gson.Gson

private const val CHECKIN_CONFIG_URL = "http://autoflow.xbdcc.cn/checkin-config.json"

class DailyCheckInConfigManager(
    private val context: Context,
    private val networkClient: FoundationNetworkClient,
    private val store: DailyCheckInConfigStore = DailyCheckInConfigStore(context),
    private val gson: Gson = Gson()
) {
    private val mainHandler = Handler(Looper.getMainLooper())

    fun loadCachedConfig(): DailyCheckInConfig? = store.loadConfig()

    fun fetchRemoteConfig(onFetched: (DailyCheckInConfig?) -> Unit) {
        networkClient.get(CHECKIN_CONFIG_URL) { result ->
            when (result) {
                is NetworkResult.Success -> {
                    val config = try {
                        gson.fromJson(result.body, DailyCheckInConfig::class.java)
                    } catch (_: Throwable) {
                        null
                    }
                    config?.let { store.saveConfig(it) }
                    mainHandler.post {
                        onFetched(config ?: loadCachedConfig())
                    }
                }
                is NetworkResult.Error -> {
                    mainHandler.post {
                        onFetched(loadCachedConfig())
                    }
                }
            }
        }
    }
}
