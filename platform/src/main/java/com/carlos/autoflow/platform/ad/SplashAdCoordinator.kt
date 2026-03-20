package com.carlos.autoflow.platform.ad

import android.app.Activity

class SplashAdCoordinator(
    private val activity: Activity,
    private val adManager: AdManager,
    private val cooldownManager: SplashAdCooldownManager
) {
    companion object {
        const val SPLASH_AD_SLOT_ID = "100007398"
    }

    private var attempted = false

    fun maybeShowSplash(isColdStart: Boolean, hasConsent: Boolean) {
        if (attempted || !hasConsent) return
        if (!cooldownManager.shouldShowSplash(isColdStart)) return
        attempted = true
        adManager.loadSplashAd(
            activity,
            SPLASH_AD_SLOT_ID,
            object : AdCallback {
                override fun onAdLoaded() {
                    cooldownManager.markSplashShown(isColdStart)
                    adManager.showSplashAd(activity)
                }

                override fun onAdFailed(error: String?) {}
                override fun onAdShown() {}
                override fun onAdClicked() {}
                override fun onAdClosed() {}
                override fun onAdRewarded() {}
            }
        )
    }
}
