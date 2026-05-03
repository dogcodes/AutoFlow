package com.carlos.autoflow.platform.ad

import android.app.Application
import com.carlos.autoflow.platform.ad.umeng.UmengAdManager
import com.carlos.autoflow.platform.analytics.AnalyticsTracker

object AdService {
    private var adManager: AdManager? = null
    private var globalAdsEnabled = true
    private var analyticsTracker: AnalyticsTracker? = null

    fun initialize(application: Application, analyticsTracker: AnalyticsTracker? = null) {
        if (adManager == null) {
            this.analyticsTracker = analyticsTracker
            val preferenceStore = AdPreferenceStore(application)
            val umengManager = UmengAdManager(application, analyticsTracker)
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
