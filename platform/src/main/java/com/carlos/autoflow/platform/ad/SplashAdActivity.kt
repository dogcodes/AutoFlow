package com.carlos.autoflow.platform.ad

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.umeng.union.UMSplashAD
import com.umeng.union.UMUnionSdk
import com.umeng.union.api.UMAdConfig
import com.umeng.union.api.UMUnionApi
class SplashAdActivity : Activity() {
    companion object {
        private const val EXTRA_SLOT_ID = "slot_id"
        private const val TIMEOUT_MS = 5_000L
        private const val DEFAULT_SPLASH_ID = "100007398"
        private var callback: AdCallback? = null

        fun start(context: Context, slotId: String, adCallback: AdCallback) {
            callback = adCallback
            val intent = Intent(context, SplashAdActivity::class.java).apply {
                putExtra(EXTRA_SLOT_ID, slotId)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }

        private fun log(message: String) {
            Log.d("SplashAdActivity", message)
        }
    }

    private lateinit var container: FrameLayout
    private val handler = Handler(Looper.getMainLooper())
    private val timeoutRunnable = Runnable {
        log("skipping splash due to timeout")
        finishWithFallback()
    }
    private var splashAd: UMSplashAD? = null
    private var canJump = false

    private val adLoadListener = object : UMUnionApi.AdLoadListener<UMSplashAD> {
        override fun onSuccess(type: UMUnionApi.AdType, display: UMSplashAD) {
            log("splash load success")
            handler.removeCallbacks(timeoutRunnable)
            splashAd = display
            if (isFinishing) {
                finishWithFallback()
                return
            }
            display.setAdEventListener(object : UMUnionApi.SplashAdListener {
                override fun onDismissed() {
                    log("splash dismissed")
                    callback?.onAdClosed()
                    finishWithFallback()
                }

                override fun onExposed() {
                    callback?.onAdShown()
                }

                override fun onClicked(view: View) {
                    callback?.onAdClicked()
                }

                override fun onError(code: Int, message: String) {
                    log("splash error code:$code msg:$message")
                    callback?.onAdFailed(message ?: "Splash error")
                }
            })
            display.show(container)
            callback?.onAdLoaded()
        }

        override fun onFailure(type: UMUnionApi.AdType, message: String) {
            log("splash onFailure ${message ?: "unknown"}")
            callback?.onAdFailed(message ?: "Splash load failed")
            finishWithFallback()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        container = FrameLayout(this).apply {
            setBackgroundColor(Color.BLACK)
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
        setContentView(container)
        val slotId = intent?.getStringExtra(EXTRA_SLOT_ID) ?: DEFAULT_SPLASH_ID
        val config = UMAdConfig.Builder()
            .setSlotId(slotId)
            .build()
        UMUnionSdk.loadSplashAd(config, adLoadListener, TIMEOUT_MS.toInt())
        handler.postDelayed(timeoutRunnable, TIMEOUT_MS)
    }

    override fun onResume() {
        super.onResume()
        if (canJump) {
            finishWithFallback()
        }
        canJump = true
    }

    override fun onPause() {
        super.onPause()
        canJump = false
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(timeoutRunnable)
        splashAd = null
        callback = null
    }

    override fun onBackPressed() {
        // ignore back
    }

    private fun finishWithFallback() {
        if (isFinishing) return
        finish()
    }

    private fun log(message: String) {
        if (!TextUtils.isEmpty(message)) {
            Log.d("SplashAdActivity", message)
        }
    }

}
