package com.carlos.autoflow.foundation.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.webkit.WebView
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.carlos.autoflow.foundation.ui.components.FoundationTopAppBar
import com.carlos.autoflow.foundation.web.FoundationWebViewHelper

class WebViewActivity : ComponentActivity() {
    companion object {
        private const val EXTRA_URL = "extra_url"
        private const val EXTRA_TITLE = "extra_title"

        fun createIntent(context: Context, url: String, title: String): Intent {
            return Intent(context, WebViewActivity::class.java).apply {
                putExtra(EXTRA_URL, url)
                putExtra(EXTRA_TITLE, title)
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val url = intent?.getStringExtra(EXTRA_URL) ?: ""
        val title = intent?.getStringExtra(EXTRA_TITLE) ?: ""

        setContent {
            var progress by remember { mutableStateOf(0) }
            val webViewHelper = remember { mutableStateOf<FoundationWebViewHelper?>(null) }

            BackHandler { finish() }

            Scaffold(
                topBar = {
                    FoundationTopAppBar(
                        title = title,
                        onNavigationClick = { finish() }
                    )
                }
            ) { padding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    AndroidView(
                        factory = { context ->
                            WebView(context).apply {
                                webViewHelper.value = FoundationWebViewHelper(this).also { helper ->
                                    helper.configure(
                                        chromeClient = object : android.webkit.WebChromeClient() {
                                            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                                progress = newProgress
                                            }
                                        }
                                    )
                                    helper.load(url)
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxSize()
                    )
                    if (progress < 100) {
                        LinearProgressIndicator(
                            progress = progress / 100f,
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.TopCenter)
                        )
                    }
                }
            }

            DisposableEffect(Unit) {
                onDispose {
                    webViewHelper.value?.destroy()
                }
            }
        }
    }
}
