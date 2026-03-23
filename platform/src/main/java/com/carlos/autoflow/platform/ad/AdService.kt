package com.carlos.autoflow.platform.ad

import android.app.Application
import com.carlos.autoflow.platform.ad.umeng.UmengAdManager

object AdService {
    private var adManager: AdManager? = null
    private var globalAdsEnabled = true

    fun initialize(application: Application) {
        if (adManager == null) {
            val preferenceStore = AdPreferenceStore(application)
            val umengManager = UmengAdManager(application)
            adManager = AdRouter(
                listOf(
                    AdPlatformConfig(
                        platformName = "umeng",
                        manager = umengManager,
                        enabledTypes = AdType.values().toSet(),
                        priority = 0
                    )
                ),
                preferenceStore,
                shouldLoadAds = { globalAdsEnabled }
            )
            adManager?.initialize()
        }
    }

    fun getAdManager(): AdManager {
        return adManager ?: throw IllegalStateException("AdService not initialized. Call initialize() first.")
    }

    fun preferenceStore(): AdPreferenceStore =
        (adManager as? AdRouter)?.let { it.preferenceStore } ?: throw IllegalStateException("Router not initialized")

    fun setGlobalAdsEnabled(enabled: Boolean) {
        globalAdsEnabled = enabled
    }
}
