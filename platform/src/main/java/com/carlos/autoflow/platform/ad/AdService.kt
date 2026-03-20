package com.carlos.autoflow.platform.ad

import android.app.Application
import com.carlos.autoflow.platform.ad.umeng.UmengAdManager

object AdService {
    private var adManager: AdManager? = null

    fun initialize(application: Application) {
        if (adManager == null) {
            val umengManager = UmengAdManager(application)
            adManager = AdRouter(
                listOf(
                    AdPlatformConfig(
                        platformName = "umeng",
                        manager = umengManager,
                        enabledTypes = AdType.values().toSet(),
                        priority = 0
                    )
                )
            )
            adManager?.initialize()
        }
    }

    fun getAdManager(): AdManager {
        return adManager ?: throw IllegalStateException("AdService not initialized. Call initialize() first.")
    }
}
