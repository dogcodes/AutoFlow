package com.carlos.autoflow.platform.ad

import android.app.Activity
import com.carlos.autoflow.utils.AutoFlowLogger

class SplashAdCoordinator(
    private val activity: Activity,
    private val adManager: AdManager,
    private val cooldownManager: SplashAdCooldownManager
) {
    companion object {
        const val SPLASH_AD_SLOT_ID = "100007398"
    }

    private var attempted = false

    fun maybeShowSplash(
        isColdStart: Boolean,
        hasConsent: Boolean,
        onCompleted: () -> Unit = {}
    ): Boolean {
        if (attempted || !hasConsent) return false
        if (!cooldownManager.shouldShowSplash(isColdStart)) return false
        attempted = true
        AutoFlowLogger.d("SplashAdCoordinator", "准备拉取开屏广告（coldStart=$isColdStart）")
        adManager.loadSplashAd(
            activity,
            SPLASH_AD_SLOT_ID,
            object : AdCallback {
                override fun onAdLoaded() {
                    cooldownManager.markSplashShown(isColdStart)
                    adManager.showSplashAd(activity)
                }

                override fun onAdFailed(error: String?) {
                    AutoFlowLogger.d("SplashAdCoordinator", "开屏广告加载失败：${error ?: "未知"}")
                    onCompleted()
                }

                override fun onAdShown() {}
                override fun onAdClicked() {}

                override fun onAdClosed() {
                    AutoFlowLogger.d("SplashAdCoordinator", "开屏广告已关闭")
                    onCompleted()
                }

                override fun onAdRewarded() {}
            }
        )
        return true
    }
}
