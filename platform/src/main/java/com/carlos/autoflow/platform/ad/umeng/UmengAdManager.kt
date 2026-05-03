package com.carlos.autoflow.platform.ad.umeng

import android.R
import android.app.Activity
import android.app.AlertDialog
import android.app.Application
import android.graphics.Color
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.setPadding
import coil.ImageLoader
import coil.request.ImageRequest
import coil.size.Size
import com.carlos.autoflow.platform.ad.AdCallback
import com.carlos.autoflow.platform.ad.AdManager
import com.carlos.autoflow.platform.ad.SplashAdActivity
import com.carlos.autoflow.platform.analytics.AnalyticsTracker
import com.umeng.union.UMFloatingIconAD
import com.umeng.union.UMNativeAD
import com.umeng.union.UMRewardAD
import com.umeng.union.UMSplashAD
import com.umeng.union.UMUnionSdk
import com.umeng.union.api.UMAdConfig
import com.umeng.union.api.UMUnionApi
import com.umeng.union.widget.UMNativeLayout
import java.util.UUID
import android.os.Handler
import android.os.Looper
import coil.transform.RoundedCornersTransformation
import com.carlos.autoflow.utils.AutoFlowLogger

class UmengAdManager(private val application: Application, private val analyticsTracker: AnalyticsTracker? = null) : AdManager {
    private var splashAd: UMSplashAD? = null
    private var splashCallback: AdCallback? = null
    private val handler = Handler(Looper.getMainLooper())
    private var loadTimeout: Runnable? = null
    private companion object {
        private const val SPLASH_TIMEOUT_MS = 5_000L
        private const val TAG = "UmengAdManager"
    }
    private var interstitialAd: UMUnionApi.AdDisplay? = null
    private var bannerAd: UMNativeAD? = null
    private var floatingAd: UMUnionApi.AdDisplay? = null
    private var floatingBallAd: UMFloatingIconAD? = null
    private var feedAd: UMNativeAD? = null
    private var rewardAd: UMRewardAD? = null
    private val userId: String by lazy { fetchUserId() }

    override fun initialize() {
        runCatching {
            UMUnionSdk.init(application)
            Log.d(TAG, "Umeng monetization initialized for ${application.packageName}")
        }.onFailure {
            Log.e(TAG, "Failed to initialize Umeng monetization", it)
        }
    }

    override fun loadSplashAd(activity: Activity, adId: String, callback: AdCallback) {
        splashCallback = callback
        loadTimeout?.let { handler.removeCallbacks(it) }
        loadTimeout = Runnable {
            callback.onAdFailed("Splash load timeout")
            splashAd = null
        }.also { handler.postDelayed(it, SPLASH_TIMEOUT_MS) }
        val config = UMAdConfig.Builder()
            .setSlotId(adId)
            .build()
        UMUnionSdk.loadSplashAd(
            config,
            object : UMUnionApi.AdLoadListener<UMSplashAD> {
                override fun onSuccess(type: UMUnionApi.AdType, display: UMSplashAD) {
                    loadTimeout?.let { handler.removeCallbacks(it) }
                    splashAd = display
                    callback.onAdLoaded()
                    trackAdEvent("ad_load", "splash", adId, "success")
                }

                override fun onFailure(type: UMUnionApi.AdType, error: String?) {
                    loadTimeout?.let { handler.removeCallbacks(it) }
                    callback.onAdFailed(error ?: "Splash ad load failed")
                    splashAd = null
                    trackAdEvent("ad_load", "splash", adId, "failure", error)
                }
            },
            SPLASH_TIMEOUT_MS.toInt()
        )
    }

    override fun loadRewardedAd(activity: Activity, adId: String, callback: AdCallback) {
        UMUnionSdk.getApi().loadRewardAd(
            rewardAdConfig(adId),
            object : UMUnionApi.AdLoadListener<UMRewardAD> {
                override fun onSuccess(type: UMUnionApi.AdType, ad: UMRewardAD) {
                    rewardAd = ad.apply {
                        setAdCloseListener {
                            callback.onAdClosed()
                            clearRewardAd()
                        }
                        setAdEventListener(object : UMUnionApi.RewardAdListener {
                            override fun onReward(success: Boolean, rewardInfo: Map<String, Any>) {
                                if (success) {
                                    callback.onAdRewarded()
                                } else {
                                    callback.onAdFailed("Reward verification failed")
                                }
                            }

                            override fun onExposed() = callback.onAdShown()

                            override fun onClicked(view: View) = callback.onAdClicked()

                            override fun onError(code: Int, message: String) =
                                callback.onAdFailed("$code:$message")

                            override fun onDismissed() {
                                callback.onAdClosed()
                                clearRewardAd()
                            }
                        })
                    }
                    callback.onAdLoaded()
                    trackAdEvent("ad_load", "rewarded", adId, "success")
                }

                override fun onFailure(type: UMUnionApi.AdType, error: String?) {
                    callback.onAdFailed(error ?: "Reward ad load failed")
                    clearRewardAd()
                    trackAdEvent("ad_load", "rewarded", adId, "failure", error)
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

                            override fun onClicked(view: View) = callback.onAdClicked()

                            override fun onError(code: Int, message: String) =
                                callback.onAdFailed("$code:$message")
                        })
                    }
                    callback.onAdLoaded()
                    trackAdEvent("ad_load", "interstitial", adId, "success")
                }

                override fun onFailure(type: UMUnionApi.AdType, error: String?) {
                    callback.onAdFailed(error ?: "Reward ad load failed")
                    trackAdEvent("ad_load", "interstitial", adId, "failure", error)
                }
            }
        )
    }

    override fun showRewardedAd(activity: Activity) {
        val ad = rewardAd
        if (ad == null) {
            Log.w(TAG, "Rewarded ad is not ready")
            return
        }
        ad.show(activity)
    }

    override fun showSplashAd(activity: Activity) {
        val ad = splashAd
        if (ad == null) {
            Log.w(TAG, "Splash ad is not ready")
            return
        }
        SplashAdActivity.show(activity, ad, splashCallback)
        splashAd = null
        splashCallback = null
    }

    override fun showInterstitialAd(activity: Activity) {
        interstitialAd?.show(activity)
            ?: Log.w(TAG, "Interstitial ad is not ready or no longer valid")
    }

    override fun loadBannerAd(activity: Activity, adId: String, callback: AdCallback) {
        UMUnionSdk.getApi().loadNativeBannerAd(
            adConfig(adId),
            object : UMUnionApi.AdLoadListener<UMNativeAD> {
                override fun onSuccess(type: UMUnionApi.AdType, ad: UMNativeAD) {
                    bannerAd = ad
                    callback.onAdLoaded()
                    trackAdEvent("ad_load", "banner", adId, "success")
                }

                override fun onFailure(type: UMUnionApi.AdType, error: String?) {
                    callback.onAdFailed(error ?: "Banner ad load failed")
                    trackAdEvent("ad_load", "banner", adId, "failure", error)
                }
            }
        )
    }

    override fun showBannerAd(activity: Activity) {
        val ad = bannerAd
        if (ad == null) {
            Log.w(TAG, "Banner ad is not ready")
            return
        }
        showNativeAdDialog(activity, ad, "Banner")
        bannerAd = null
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
                            override fun onClicked(view: View) = callback.onAdClicked()
                            override fun onError(code: Int, message: String) =
                                callback.onAdFailed("$code:$message")
                        })
                    }
                    callback.onAdLoaded()
                    trackAdEvent("ad_load", "floating", adId, "success")
                }

                override fun onFailure(type: UMUnionApi.AdType, error: String?) {
                    callback.onAdFailed(error ?: "Banner ad load failed")
                    trackAdEvent("ad_load", "floating", adId, "failure", error)
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
                            override fun onClicked(view: View) = callback.onAdClicked()
                            override fun onError(code: Int, message: String) =
                                callback.onAdFailed("$code:$message")
                        })
                    }
                    callback.onAdLoaded()
                    trackAdEvent("ad_load", "floating_ball", adId, "success")
                }

                override fun onFailure(type: UMUnionApi.AdType, error: String?) {
                    callback.onAdFailed(error ?: "Floating ad load failed")
                    trackAdEvent("ad_load", "floating_ball", adId, "failure", error)
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
                    trackAdEvent("ad_load", "feed", adId, "success")
                }

                override fun onFailure(type: UMUnionApi.AdType, error: String?) {
                    callback.onAdFailed(error ?: "Feed ad load failed")
                    trackAdEvent("ad_load", "feed", adId, "failure", error)
                }
            }
        )
    }

    override fun showFeedAd(activity: Activity) {
        val ad = feedAd
        if (ad == null) {
            Log.w(TAG, "Feed ad is not ready")
            return
        }

        val contentLayout = UMNativeLayout(activity)
        contentLayout.setBackgroundColor(Color.WHITE)
        contentLayout.layoutParams = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        contentLayout.setPadding(24)

        val title = TextView(activity).apply {
            text = ad.title ?: "信息流广告"
            textSize = 16f
            setTextColor(Color.BLACK)
            setPadding(0, 0, 0, 8)
        }
        val body = TextView(activity).apply {
            text = ad.content ?: ""
            textSize = 14f
            setTextColor(Color.DKGRAY)
        }
        val imageView = ImageView(activity).apply {
            setBackgroundColor(Color.LTGRAY)
            adjustViewBounds = true
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        contentLayout.addView(title)
        contentLayout.addView(body)
        contentLayout.addView(imageView)

        val request = ImageRequest.Builder(activity)
            .data(ad.imageUrl)
            .size(Size.Companion.ORIGINAL)
            .placeholder(R.color.darker_gray)
            .transformations(RoundedCornersTransformation(8f))
            .target(imageView)
            .build()
        ImageLoader(activity).enqueue(request)

        ad.bindView(activity, contentLayout, listOf(contentLayout))

        AlertDialog.Builder(activity)
            .setView(contentLayout)
            .setPositiveButton("关闭") { _, _ -> }
            .show()

        feedAd = null
    }

    private fun showNativeAdDialog(activity: Activity, ad: UMNativeAD, label: String) {
        val layout = UMNativeLayout(activity).apply {
            setBackgroundColor(Color.WHITE)
            setPadding(32)
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        val title = TextView(activity).apply {
            text = ad.title ?: label
            textSize = 18f
            setTextColor(Color.BLACK)
        }
        val body = TextView(activity).apply {
            text = ad.content ?: ""
            textSize = 14f
            setTextColor(Color.DKGRAY)
        }
        val imageView = ImageView(activity).apply {
            adjustViewBounds = true
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        layout.addView(title)
        layout.addView(body)
        layout.addView(imageView)

        val request = ImageRequest.Builder(activity)
            .data(ad.imageUrl)
            .size(Size.Companion.ORIGINAL)
            .placeholder(R.color.darker_gray)
            .transformations(RoundedCornersTransformation(8f))
            .target(imageView)
            .build()
        ImageLoader(activity).enqueue(request)

        ad.bindView(activity, layout, listOf(layout))

        AlertDialog.Builder(activity)
            .setTitle(label)
            .setView(layout)
            .setPositiveButton("关闭") { dialog, _ -> dialog.dismiss() }
            .setOnDismissListener {
                Log.d(TAG, "$label dismissed")
            }
            .show()
    }

    private fun adConfig(slotId: String): UMAdConfig {
        return UMAdConfig.Builder()
            .setSlotId(slotId)
            .build()
    }

    private fun rewardAdConfig(slotId: String): UMAdConfig {
        return UMAdConfig.Builder()
            .setSlotId(slotId)
            .setUserId(userId)
            .setCustomData(buildCustomData())
            .build()
    }

    private fun buildCustomData(): String {
        val packageName = application.packageName
        return "{\"pkg\":\"$packageName\"}"
    }

    private fun fetchUserId(): String {
        val androidId = Settings.Secure.getString(application.contentResolver, Settings.Secure.ANDROID_ID)
        return androidId ?: UUID.randomUUID().toString()
    }

    private fun clearRewardAd() {
        rewardAd = null
    }

    private fun trackAdEvent(eventName: String, adType: String, slotId: String, result: String, errorMessage: String? = null) {
        AutoFlowLogger.d(TAG, "Tracking ad event: $eventName, type: $adType, slot: $slotId, result: $result, error: $errorMessage")
        analyticsTracker?.trackEvent(eventName, mapOf(
            "ad_type" to adType,
            "slot_id" to slotId,
            "result" to result
        ).apply {
            errorMessage?.let { plus("error_message" to it) }
        })
    }
}
