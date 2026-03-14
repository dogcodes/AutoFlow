package com.carlos.autoflow.platform.ad

import android.app.Application
import android.app.Activity
import android.util.Log
import com.umeng.commonsdk.UMConfigure
import com.umeng.union.UMFloatingIconAD
import com.umeng.union.UMNativeAD
import com.umeng.union.UMRewardAD
import com.umeng.union.UMSplashAD
import com.umeng.union.UMUnionSdk
import com.umeng.union.api.UMAdConfig
import com.umeng.union.api.UMUnionApi

class UmengAdManager(private val application: Application) : AdManager {
    private var splashAd: UMSplashAD? = null
    private var interstitialAd: UMUnionApi.AdDisplay? = null
    private var bannerAd: UMUnionApi.AdDisplay? = null
    private var floatingAd: UMUnionApi.AdDisplay? = null
    private var floatingBallAd: UMFloatingIconAD? = null
    private var feedAd: UMNativeAD? = null
    private var rewardAd: UMRewardAD? = null

    override fun initialize() {
        runCatching {
            UMUnionSdk.init(application)
            Log.d(TAG, "Umeng monetization initialized for ${application.packageName}")
        }.onFailure {
            Log.e(TAG, "Failed to initialize Umeng monetization", it)
        }
    }

    override fun loadSplashAd(activity: Activity, adId: String, callback: AdCallback) {
        UMUnionSdk.getApi().loadSplashAd(
            adConfig(adId),
            object : UMUnionApi.AdLoadListener<UMSplashAD> {
                override fun onSuccess(type: UMUnionApi.AdType, ad: UMSplashAD) {
                    splashAd = ad.apply {
                        setAdEventListener(object : UMUnionApi.SplashAdListener {
                            override fun onExposed() = callback.onAdShown()

                            override fun onClicked(view: android.view.View) = callback.onAdClicked()

                            override fun onError(code: Int, message: String) =
                                callback.onAdFailed("$code:$message")

                            override fun onDismissed() = callback.onAdClosed()
                        })
                    }
                    callback.onAdLoaded()
                }

                override fun onFailure(type: UMUnionApi.AdType, error: String) {
                    callback.onAdFailed(error)
                }
            },
            5
        )
    }

    override fun loadRewardedAd(activity: Activity, adId: String, callback: AdCallback) {
        UMUnionSdk.getApi().loadRewardAd(
            adConfig(adId),
            object : UMUnionApi.AdLoadListener<UMRewardAD> {
                override fun onSuccess(type: UMUnionApi.AdType, ad: UMRewardAD) {
                    rewardAd = ad.apply {
                        setAdCloseListener { callback.onAdClosed() }
                        setAdEventListener(object : UMUnionApi.AdEventListener {
                            override fun onExposed() = callback.onAdShown()

                            override fun onClicked(view: android.view.View) = callback.onAdClicked()

                            override fun onError(code: Int, message: String) =
                                callback.onAdFailed("$code:$message")
                        })
                    }
                    callback.onAdLoaded()
                }

                override fun onFailure(type: UMUnionApi.AdType, error: String) {
                    callback.onAdFailed(error)
                }
            }
        )
    }

    override fun loadInterstitialAd(activity: Activity, adId: String, callback: AdCallback) {
        UMUnionSdk.getApi().loadInterstitialAd(
            activity,
            adConfig(adId),
            object : UMUnionApi.AdLoadListener<UMUnionApi.AdDisplay> {
                override fun onSuccess(type: UMUnionApi.AdType, ad: UMUnionApi.AdDisplay) {
                    interstitialAd = ad.apply {
                        setAdCloseListener { callback.onAdClosed() }
                        setAdEventListener(object : UMUnionApi.AdEventListener {
                            override fun onExposed() = callback.onAdShown()

                            override fun onClicked(view: android.view.View) = callback.onAdClicked()

                            override fun onError(code: Int, message: String) =
                                callback.onAdFailed("$code:$message")
                        })
                    }
                    callback.onAdLoaded()
                }

                override fun onFailure(type: UMUnionApi.AdType, error: String) {
                    callback.onAdFailed(error)
                }
            }
        )
    }

    override fun showRewardedAd(activity: Activity) {
        rewardAd?.show() ?: Log.w(TAG, "Rewarded ad is not ready")
    }

    override fun showSplashAd(activity: Activity) {
        splashAd?.show(activity) ?: Log.w(TAG, "Splash ad is not ready")
    }

    override fun showInterstitialAd(activity: Activity) {
        interstitialAd?.show(activity)
            ?: Log.w(TAG, "Interstitial ad is not ready or no longer valid")
    }

    override fun loadBannerAd(activity: Activity, adId: String, callback: AdCallback) {
        UMUnionSdk.getApi().loadFloatingBannerAd(
            activity,
            adConfig(adId),
            object : UMUnionApi.AdLoadListener<UMUnionApi.AdDisplay> {
                override fun onSuccess(type: UMUnionApi.AdType, ad: UMUnionApi.AdDisplay) {
                    bannerAd = ad.apply {
                        setAdEventListener(object : UMUnionApi.AdEventListener {
                            override fun onExposed() = callback.onAdShown()
                            override fun onClicked(view: android.view.View) = callback.onAdClicked()
                            override fun onError(code: Int, message: String) =
                                callback.onAdFailed("$code:$message")
                        })
                    }
                    callback.onAdLoaded()
                }

                override fun onFailure(type: UMUnionApi.AdType, error: String) {
                    callback.onAdFailed(error)
                }
            }
        )
    }

    override fun showBannerAd(activity: Activity) {
        bannerAd?.show(activity)
            ?: Log.w(TAG, "Banner ad is not ready")
    }

    override fun loadFloatingAd(activity: Activity, adId: String, callback: AdCallback) {
        UMUnionSdk.getApi().loadFloatingBannerAd(
            activity,
            adConfig(adId),
            object : UMUnionApi.AdLoadListener<UMUnionApi.AdDisplay> {
                override fun onSuccess(type: UMUnionApi.AdType, ad: UMUnionApi.AdDisplay) {
                    floatingAd = ad.apply {
                        setAdEventListener(object : UMUnionApi.AdEventListener {
                            override fun onExposed() = callback.onAdShown()
                            override fun onClicked(view: android.view.View) = callback.onAdClicked()
                            override fun onError(code: Int, message: String) =
                                callback.onAdFailed("$code:$message")
                        })
                    }
                    callback.onAdLoaded()
                }

                override fun onFailure(type: UMUnionApi.AdType, error: String) {
                    callback.onAdFailed(error)
                }
            }
        )
    }

    override fun showFloatingAd(activity: Activity) {
        floatingAd?.show(activity)
            ?: Log.w(TAG, "Floating ad is not ready")
    }

    override fun loadFloatingBallAd(activity: Activity, adId: String, callback: AdCallback) {
        UMUnionSdk.getApi().loadFloatingIconAd(
            activity,
            adConfig(adId),
            object : UMUnionApi.AdLoadListener<UMFloatingIconAD> {
                override fun onSuccess(type: UMUnionApi.AdType, ad: UMFloatingIconAD) {
                    floatingBallAd = ad.apply {
                        setAdEventListener(object : UMUnionApi.AdEventListener {
                            override fun onExposed() = callback.onAdShown()
                            override fun onClicked(view: android.view.View) = callback.onAdClicked()
                            override fun onError(code: Int, message: String) =
                                callback.onAdFailed("$code:$message")
                        })
                    }
                    callback.onAdLoaded()
                }

                override fun onFailure(type: UMUnionApi.AdType, error: String) {
                    callback.onAdFailed(error)
                }
            }
        )
    }

    override fun showFloatingBallAd(activity: Activity) {
        floatingBallAd?.show(activity)
            ?: Log.w(TAG, "Floating ball ad is not ready")
    }

    override fun loadFeedAd(activity: Activity, adId: String, callback: AdCallback) {
        UMUnionSdk.getApi().loadFeedAd(
            adConfig(adId),
            object : UMUnionApi.AdLoadListener<UMNativeAD> {
                override fun onSuccess(type: UMUnionApi.AdType, ad: UMNativeAD) {
                    feedAd = ad
                    callback.onAdLoaded()
                }

                override fun onFailure(type: UMUnionApi.AdType, error: String) {
                    callback.onAdFailed(error)
                }
            }
        )
    }

    override fun showFeedAd(activity: Activity) {
        feedAd?.let {
            Log.d(TAG, "Feed ad loaded; bind it in UI manually")
        } ?: Log.w(TAG, "Feed ad is not ready")
    }

    private fun adConfig(slotId: String): UMAdConfig {
        return UMAdConfig.Builder()
            .setSlotId(slotId)
            .build()
    }

    private companion object {
        private const val TAG = "UmengAdManager"
    }
}
