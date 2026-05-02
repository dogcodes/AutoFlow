package com.carlos.autoflow.platform.ad.config

import android.content.Context
import com.carlos.autoflow.foundation.network.ApiRoutes
import com.carlos.autoflow.foundation.network.FoundationNetworkClient
import com.carlos.autoflow.foundation.network.NetworkResult
import com.carlos.autoflow.platform.ad.AdPreferenceStore
import com.carlos.autoflow.platform.ad.AdType
import com.carlos.autoflow.platform.ad.DeviceIdentifiers
import com.carlos.autoflow.platform.ad.config.RemoteAdConfiguration
import com.google.gson.Gson
import java.util.Locale

class AdConfigurationManager(
    private val context: Context,
    private val networkClient: FoundationNetworkClient,
    private val store: AdConfigStore = AdConfigStore(context),
    private val gson: Gson = Gson()
) {
    companion object {
        val DEFAULT_REWARDED_POLICY = RemoteRewardedAdPolicy()
    }

    fun loadCachedConfig(): RemoteAdConfiguration? = store.loadConfig()

    fun shouldEnableAds(config: RemoteAdConfiguration?): Boolean {
        if (config == null) return true
        if (!config.globalEnabled) return false
        if (config.requireOaid && !DeviceIdentifiers.hasOaid(context)) return false
        return true
    }

    fun getRewardedPolicy(config: RemoteAdConfiguration?): RemoteRewardedAdPolicy {
        return config?.rewardedPolicy ?: DEFAULT_REWARDED_POLICY
    }

    fun applyConfig(config: RemoteAdConfiguration, preferenceStore: AdPreferenceStore) {
        config.slots.forEach { (remoteName, slotConfig) ->
            remoteName.toAdType()?.let { type ->
                preferenceStore.setEnabled(type, slotConfig.enabled)
                slotConfig.cooldownMs?.let { preferenceStore.setCooldown(type, it) }
                slotConfig.dailyLimit?.let { preferenceStore.setDailyLimit(type, it) }
            }
        }
        config.hotStartupCooldownMs?.let { preferenceStore.setHotStartupCooldown(it) }
        store.saveConfig(config)
    }

    private fun String.toAdType(): AdType? = when (lowercase(Locale.getDefault())) {
        "splash" -> AdType.SPLASH
        "rewarded" -> AdType.REWARDED
        "interstitial" -> AdType.INTERSTITIAL
        "banner" -> AdType.BANNER
        "floating" -> AdType.FLOATING
        "floatingball", "floating_ball" -> AdType.FLOATING_BALL
        "feed" -> AdType.FEED
        else -> null
    }

    fun fetchRemoteConfig(onFetched: (RemoteAdConfiguration?) -> Unit) {
        networkClient.get(ApiRoutes.AD_CONFIG) { result ->
            when (result) {
                is NetworkResult.Success -> {
                    val config = try {
                        gson.fromJson(result.body, RemoteAdConfiguration::class.java)
                    } catch (_: Throwable) {
                        null
                    }
                    onFetched(config)
                }
                is NetworkResult.Error -> onFetched(null)
            }
        }
    }
}
