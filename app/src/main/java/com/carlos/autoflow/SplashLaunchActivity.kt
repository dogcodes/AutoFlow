package com.carlos.autoflow

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.carlos.autoflow.foundation.privacy.PrivacyConsentManager
import com.carlos.autoflow.platform.ad.AdService
import com.carlos.autoflow.platform.ad.SplashAdCoordinator
import com.carlos.autoflow.platform.ad.SplashAdCooldownManager
import com.carlos.autoflow.ui.theme.AutoFlowTheme

class SplashLaunchActivity : ComponentActivity() {
    private lateinit var privacyConsentManager: PrivacyConsentManager
    private var mainLaunched = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestNotificationPermissionIfNeeded()
        privacyConsentManager = PrivacyConsentManager(this)

        setContent {
            AutoFlowTheme {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "AutoFlow",
                        color = Color.White,
                        fontSize = 32.sp
                    )
                }
            }
        }

        if (!privacyConsentManager.hasConsent()) {
            navigateToMain()
            return
        }

        val splashCoordinator = SplashAdCoordinator(
            this,
            AdService.getAdManager(),
            SplashAdCooldownManager(AdService.preferenceStore(), this)
        )

        val handled = splashCoordinator.maybeShowSplash(
            isColdStart = true,
            hasConsent = true
        ) {
            navigateToMain()
        }

        if (!handled) {
            navigateToMain()
        }
    }

    private fun navigateToMain() {
        if (mainLaunched) return
        mainLaunched = true
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.POST_NOTIFICATIONS),
            MainActivity.REQUEST_POST_NOTIFICATIONS
        )
    }
}
