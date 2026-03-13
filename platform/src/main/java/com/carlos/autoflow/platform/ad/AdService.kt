package com.carlos.autoflow.platform.ad

import android.app.Application

object AdService {
    private var adManager: AdManager? = null

    fun initialize(application: Application) {
        if (adManager == null) {
            adManager = UmengAdManager(application)
            adManager?.initialize()
        }
    }

    fun getAdManager(): AdManager {
        return adManager ?: throw IllegalStateException("AdService not initialized. Call initialize() first.")
    }
}
