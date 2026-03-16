package com.carlos.autoflow.foundation.web

import android.annotation.SuppressLint
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient

class FoundationWebViewHelper(private val webView: WebView) {

    @SuppressLint("SetJavaScriptEnabled")
    fun configure(
        enableJavaScript: Boolean = true,
        client: WebViewClient = WebViewClient(),
        chromeClient: WebChromeClient? = null,
        settingsConfig: WebSettings.() -> Unit = {}
    ) {
        webView.webViewClient = client
        chromeClient?.let {
            webView.webChromeClient = it
        }
        webView.settings.apply {
            javaScriptEnabled = enableJavaScript
            domStorageEnabled = true
            loadWithOverviewMode = true
            useWideViewPort = true
            setSupportZoom(true)
            builtInZoomControls = true
            displayZoomControls = false
            settingsConfig()
        }
    }

    fun load(url: String) {
        webView.loadUrl(url)
    }

    fun destroy() {
        webView.apply {
            stopLoading()
            webChromeClient = null
            destroy()
        }
    }
}
