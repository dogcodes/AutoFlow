package com.carlos.autoflow.platform.ad

import android.app.Activity
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast

class AdDebugActivity : Activity() {
    private val adManager by lazy { AdService.getAdManager() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = "广告测试"
        setContentView(buildContentView())
    }

    private fun buildContentView(): ScrollView {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(16), dp(16), dp(16))
        }

        val splashIdInput = adIdInput("开屏广告位", DEFAULT_SPLASH_ID)
        val interstitialIdInput = adIdInput("插屏广告位", DEFAULT_INTERSTITIAL_ID)
        val infoIdInput = adIdInput("信息流广告位", DEFAULT_INFO_ID)
        val bannerIdInput = adIdInput("Banner广告位", DEFAULT_BANNER_ID)
        val floatIdInput = adIdInput("浮窗广告位", DEFAULT_FLOAT_ID)
        val floatingBallIdInput = adIdInput("悬浮球广告位", DEFAULT_FLOATING_BALL_ID)
        val rewardedIdInput = adIdInput("激励广告位", DEFAULT_REWARDED_ID)

        root.addView(splashIdInput.label)
        root.addView(splashIdInput.input)
        root.addView(button("加载并展示开屏广告") {
            val slotId = splashIdInput.input.text.toString().trim()
            if (slotId.isEmpty()) return@button toast("请输入开屏广告位")
            adManager.loadSplashAd(this, slotId, debugCallback("开屏") {
                adManager.showSplashAd(this)
            })
        })

        root.addView(spacer())
        root.addView(interstitialIdInput.label)
        root.addView(interstitialIdInput.input)
        root.addView(button("加载插屏广告") {
            val slotId = interstitialIdInput.input.text.toString().trim()
            if (slotId.isEmpty()) return@button toast("请输入插屏广告位")
            adManager.loadInterstitialAd(this, slotId, debugCallback("插屏"))
        })
        root.addView(button("展示插屏广告") {
            adManager.showInterstitialAd(this)
        })

        root.addView(spacer())
        root.addView(infoIdInput.label)
        root.addView(infoIdInput.input)
        root.addView(button("加载信息流广告") {
            val slotId = infoIdInput.input.text.toString().trim()
            if (slotId.isEmpty()) return@button toast("请输入信息流广告位")
            adManager.loadFeedAd(this, slotId, debugCallback("信息流"))
        })
        root.addView(button("展示信息流广告") {
            adManager.showFeedAd(this)
        })

        root.addView(spacer())
        root.addView(bannerIdInput.label)
        root.addView(bannerIdInput.input)
        root.addView(button("加载Banner广告") {
            val slotId = bannerIdInput.input.text.toString().trim()
            if (slotId.isEmpty()) return@button toast("请输入Banner广告位")
            adManager.loadBannerAd(this, slotId, debugCallback("Banner"))
        })
        root.addView(button("展示Banner广告") {
            adManager.showBannerAd(this)
        })

        root.addView(spacer())
        root.addView(floatIdInput.label)
        root.addView(floatIdInput.input)
        root.addView(button("加载浮窗广告") {
            val slotId = floatIdInput.input.text.toString().trim()
            if (slotId.isEmpty()) return@button toast("请输入浮窗广告位")
            adManager.loadFloatingAd(this, slotId, debugCallback("浮窗"))
        })
        root.addView(button("展示浮窗广告") {
            adManager.showFloatingAd(this)
        })

        root.addView(spacer())
        root.addView(floatingBallIdInput.label)
        root.addView(floatingBallIdInput.input)
        root.addView(button("加载悬浮球广告") {
            val slotId = floatingBallIdInput.input.text.toString().trim()
            if (slotId.isEmpty()) return@button toast("请输入悬浮球广告位")
            adManager.loadFloatingBallAd(this, slotId, debugCallback("悬浮球"))
        })
        root.addView(button("展示悬浮球广告") {
            adManager.showFloatingBallAd(this)
        })

        root.addView(spacer())
        root.addView(rewardedIdInput.label)
        root.addView(rewardedIdInput.input)
        root.addView(button("加载激励广告") {
            val slotId = rewardedIdInput.input.text.toString().trim()
            if (slotId.isEmpty()) return@button toast("请输入激励广告位")
            adManager.loadRewardedAd(this, slotId, debugCallback("激励"))
        })
        root.addView(button("展示激励广告") {
            adManager.showRewardedAd(this)
        })

        return ScrollView(this).apply {
            addView(
                root,
                ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            )
        }
    }

    private fun adIdInput(labelText: String, defaultValue: String): AdIdInput {
        val label = TextView(this).apply { text = labelText }
        val input = EditText(this).apply {
            hint = "请输入广告位 ID"
            inputType = InputType.TYPE_CLASS_TEXT
            setText(defaultValue)
        }
        return AdIdInput(label, input)
    }

    private fun button(text: String, onClick: () -> Unit): Button {
        return Button(this).apply {
            this.text = text
            setOnClickListener { onClick() }
        }
    }

    private fun spacer(): TextView {
        return TextView(this).apply { height = dp(16) }
    }

    private fun debugCallback(name: String, onLoaded: (() -> Unit)? = null): AdCallback {
        return object : AdCallback {
            override fun onAdLoaded() {
                log("$name 广告加载成功")
                onLoaded?.invoke()
            }

            override fun onAdFailed(error: String?) {
                log("$name 广告加载失败: ${error ?: "未知错误"}")
            }

            override fun onAdShown() {
                log("$name 广告已展示")
            }

            override fun onAdClicked() {
                log("$name 广告已点击")
            }

            override fun onAdClosed() {
                log("$name 广告已关闭")
            }

            override fun onAdRewarded() {
                log("$name 广告已激励")
            }
        }
    }

    private fun log(message: String) {
        Log.d(TAG, message)
        toast(message)
    }

    private fun toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    private data class AdIdInput(val label: TextView, val input: EditText)

    private companion object {
        private const val TAG = "AdDebugActivity"
        private const val DEFAULT_SPLASH_ID = "100007398"
        private const val DEFAULT_INTERSTITIAL_ID = "100007402"
        private const val DEFAULT_INFO_ID = "100007396"
        private const val DEFAULT_BANNER_ID = "100007404"
        private const val DEFAULT_FLOAT_ID = "100007397"
        private const val DEFAULT_FLOATING_BALL_ID = "100007405"
        private const val DEFAULT_REWARDED_ID = "100007403"
    }
}
