package com.carlos.autoflow.platform

import android.app.Application
import android.util.Log
import com.carlos.autoflow.foundation.network.FoundationNetworkClient
import com.carlos.autoflow.platform.ad.AdPreferenceStore
import com.carlos.autoflow.platform.ad.AdService
import com.carlos.autoflow.platform.ad.config.AdConfigurationManager
import com.carlos.autoflow.platform.analytics.AnalyticsTracker
import com.carlos.autoflow.platform.analytics.UmengAnalyticsTracker
import com.carlos.autoflow.platform.push.JPushService
import com.carlos.autoflow.platform.push.PushService

object PlatformInitializer {

    @Volatile
    private var initialized = false

    lateinit var analyticsTracker: AnalyticsTracker
        private set

    lateinit var pushService: PushService
        private set

    private val networkClient = FoundationNetworkClient()

    fun init(application: Application) {
        if (initialized) return

        analyticsTracker = UmengAnalyticsTracker()
        pushService = JPushService()

        val adPreferenceStore = AdPreferenceStore(application)
        val adConfigurationManager = AdConfigurationManager(application, networkClient)
        val cachedConfig = adConfigurationManager.loadCachedConfig()
        cachedConfig?.let { adConfigurationManager.applyConfig(it, adPreferenceStore) }
        AdService.setGlobalAdsEnabled(adConfigurationManager.shouldEnableAds(cachedConfig))

        analyticsTracker.initialize(application)
        pushService.initialize(application)
        AdService.initialize(application)

        adConfigurationManager.fetchRemoteConfig { remote ->
            remote?.let {
                adConfigurationManager.applyConfig(it, adPreferenceStore)
                AdService.setGlobalAdsEnabled(adConfigurationManager.shouldEnableAds(it))
            }
        }

        initialized = true

        if (BuildConfig.ENABLE_PLATFORM_LOGGING) {
            Log.d("PlatformInitializer", "Platform SDKs initialized")
        }
    }
}
