package com.carlos.autoflow.platform.ad

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.umeng.union.UMSplashAD
import com.umeng.union.api.UMUnionApi

class SplashAdActivity : Activity() {
    companion object {
        private var pendingSplashAd: UMSplashAD? = null
        private var pendingCallback: AdCallback? = null

        fun show(activity: Activity, ad: UMSplashAD, callback: AdCallback?) {
            pendingSplashAd = ad
            pendingCallback = callback
            val intent = Intent(activity, SplashAdActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            activity.startActivity(intent)
        }
    }

    private lateinit var container: FrameLayout
    private var splashAd: UMSplashAD? = null
    private var adCallback: AdCallback? = null
    private var canJump = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        splashAd = pendingSplashAd ?: run {
            finish()
            return
        }
        adCallback = pendingCallback
        pendingSplashAd = null
        pendingCallback = null

        container = FrameLayout(this).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
        setContentView(container)
        splashAd?.setAdEventListener(object : com.umeng.union.api.UMUnionApi.SplashAdListener {
            override fun onDismissed() {
                adCallback?.onAdClosed()
                finish()
            }

            override fun onExposed() {
                adCallback?.onAdShown()
            }

            override fun onClicked(view: View) {
                adCallback?.onAdClicked()
            }

            override fun onError(code: Int, message: String) {
                adCallback?.onAdFailed(message ?: "Splash error")
                finish()
            }
        })
        splashAd?.show(container)
    }

    override fun onResume() {
        super.onResume()
        if (canJump) {
            finish()
        }
        canJump = true
    }

    override fun onPause() {
        super.onPause()
        canJump = false
    }

    override fun onDestroy() {
        super.onDestroy()
        splashAd = null
        adCallback = null
    }

    override fun onBackPressed() {
        // ignore back
    }
}
