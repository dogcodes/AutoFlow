package com.carlos.autoflow.platform.ad

import android.app.Activity

interface AdManager {
    fun initialize()
    fun loadSplashAd(activity: Activity, adId: String, callback: AdCallback)
    fun loadRewardedAd(activity: Activity, adId: String, callback: AdCallback)
    fun loadInterstitialAd(activity: Activity, adId: String, callback: AdCallback)
    fun loadBannerAd(activity: Activity, adId: String, callback: AdCallback)
    fun loadFloatingAd(activity: Activity, adId: String, callback: AdCallback)
    fun loadFloatingBallAd(activity: Activity, adId: String, callback: AdCallback)
    fun loadFeedAd(activity: Activity, adId: String, callback: AdCallback)
    fun showSplashAd(activity: Activity)
    fun showRewardedAd(activity: Activity)
    fun showInterstitialAd(activity: Activity)
    fun showBannerAd(activity: Activity)
    fun showFloatingAd(activity: Activity)
    fun showFloatingBallAd(activity: Activity)
    fun showFeedAd(activity: Activity)
}

interface AdCallback {
    fun onAdLoaded()
    fun onAdFailed(error: String?)
    fun onAdShown()
    fun onAdClicked()
    fun onAdClosed()
    fun onAdRewarded()
}
