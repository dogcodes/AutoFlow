package com.carlos.autoflow.platform.ad

import android.app.Activity
import android.util.Log
import java.util.EnumMap

class AdRouter(
    configs: List<AdPlatformConfig>,
    val preferenceStore: AdPreferenceStore
) : AdManager {
    companion object {
        private const val TAG = "AdRouter"
    }

    private val platformConfigs = configs.sortedBy { it.priority }
    private val loadedManagers = EnumMap<AdType, AdManager>(AdType::class.java)

    override fun initialize() {
        platformConfigs.forEach { it.manager.initialize() }
    }

    override fun loadSplashAd(activity: Activity, adId: String, callback: AdCallback) {
        dispatchWithFallback(
            adType = AdType.SPLASH,
            activity = activity,
            adId = adId,
            callback = callback
        ) { manager, adCallback ->
            manager.loadSplashAd(activity, adId, adCallback)
        }
    }

    override fun loadRewardedAd(activity: Activity, adId: String, callback: AdCallback) {
        dispatchWithFallback(
            adType = AdType.REWARDED,
            activity = activity,
            adId = adId,
            callback = callback
        ) { manager, adCallback ->
            manager.loadRewardedAd(activity, adId, adCallback)
        }
    }

    override fun loadInterstitialAd(activity: Activity, adId: String, callback: AdCallback) {
        dispatchWithFallback(
            adType = AdType.INTERSTITIAL,
            activity = activity,
            adId = adId,
            callback = callback
        ) { manager, adCallback ->
            manager.loadInterstitialAd(activity, adId, adCallback)
        }
    }

    override fun loadBannerAd(activity: Activity, adId: String, callback: AdCallback) {
        dispatchWithFallback(
            adType = AdType.BANNER,
            activity = activity,
            adId = adId,
            callback = callback
        ) { manager, adCallback ->
            manager.loadBannerAd(activity, adId, adCallback)
        }
    }

    override fun loadFloatingAd(activity: Activity, adId: String, callback: AdCallback) {
        dispatchWithFallback(
            adType = AdType.FLOATING,
            activity = activity,
            adId = adId,
            callback = callback
        ) { manager, adCallback ->
            manager.loadFloatingAd(activity, adId, adCallback)
        }
    }

    override fun loadFloatingBallAd(activity: Activity, adId: String, callback: AdCallback) {
        dispatchWithFallback(
            adType = AdType.FLOATING_BALL,
            activity = activity,
            adId = adId,
            callback = callback
        ) { manager, adCallback ->
            manager.loadFloatingBallAd(activity, adId, adCallback)
        }
    }

    override fun loadFeedAd(activity: Activity, adId: String, callback: AdCallback) {
        dispatchWithFallback(
            adType = AdType.FEED,
            activity = activity,
            adId = adId,
            callback = callback
        ) { manager, adCallback ->
            manager.loadFeedAd(activity, adId, adCallback)
        }
    }

    private fun dispatchWithFallback(
        adType: AdType,
        activity: Activity,
        adId: String,
        callback: AdCallback,
        loadAction: (AdManager, AdCallback) -> Unit
    ) {
        if (!preferenceStore.isEnabled(adType)) {
            callback.onAdFailed("Ad type $adType disabled via preferences")
            return
        }
        val candidates = platformConfigs.filter { adType in it.enabledTypes }
        if (candidates.isEmpty()) {
            callback.onAdFailed("Ad type $adType disabled")
            return
        }

        fun attempt(index: Int) {
            if (index >= candidates.size) {
                callback.onAdFailed("All platforms failed for $adType")
                return
            }
            val platform = candidates[index]
            loadAction(platform.manager, object : AdCallback {
                override fun onAdLoaded() {
                    loadedManagers[adType] = platform.manager
                    callback.onAdLoaded()
                }

                override fun onAdFailed(error: String?) {
                    Log.d(TAG, "Platform [${platform.platformName}] failed for $adType: $error")
                    if (index == candidates.lastIndex) {
                        callback.onAdFailed(error)
                    } else {
                        attempt(index + 1)
                    }
                }

                override fun onAdShown() = callback.onAdShown()
                override fun onAdClicked() = callback.onAdClicked()
                override fun onAdClosed() = callback.onAdClosed()
                override fun onAdRewarded() = callback.onAdRewarded()
            })
        }

        attempt(0)
    }

    override fun showSplashAd(activity: Activity) {
        showForType(AdType.SPLASH) { manager -> manager.showSplashAd(activity) }
    }

    override fun showRewardedAd(activity: Activity) {
        showForType(AdType.REWARDED) { manager -> manager.showRewardedAd(activity) }
    }

    override fun showInterstitialAd(activity: Activity) {
        showForType(AdType.INTERSTITIAL) { manager -> manager.showInterstitialAd(activity) }
    }

    override fun showBannerAd(activity: Activity) {
        showForType(AdType.BANNER) { manager -> manager.showBannerAd(activity) }
    }

    override fun showFloatingAd(activity: Activity) {
        showForType(AdType.FLOATING) { manager -> manager.showFloatingAd(activity) }
    }

    override fun showFloatingBallAd(activity: Activity) {
        showForType(AdType.FLOATING_BALL) { manager -> manager.showFloatingBallAd(activity) }
    }

    override fun showFeedAd(activity: Activity) {
        showForType(AdType.FEED) { manager -> manager.showFeedAd(activity) }
    }

    private inline fun showForType(adType: AdType, action: (AdManager) -> Unit) {
        val manager = loadedManagers[adType]
        if (manager == null) {
            Log.w(TAG, "No loaded manager for $adType")
            return
        }
        action(manager)
    }
}
