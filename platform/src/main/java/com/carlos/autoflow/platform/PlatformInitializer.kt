package com.carlos.autoflow.platform

import android.app.Application
import android.util.Log
import com.carlos.autoflow.platform.ad.AdService
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

    fun init(application: Application) {
        if (initialized) return

        analyticsTracker = UmengAnalyticsTracker()
        pushService = JPushService()

        analyticsTracker.initialize(application)
        pushService.initialize(application)
        AdService.initialize(application)

        initialized = true

        if (BuildConfig.ENABLE_PLATFORM_LOGGING) {
            Log.d("PlatformInitializer", "Platform SDKs initialized")
        }
    }
}
