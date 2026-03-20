package com.carlos.autoflow

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.carlos.autoflow.foundation.network.FoundationNetworkClient
import com.carlos.autoflow.foundation.privacy.PrivacyConsentDialog
import com.carlos.autoflow.foundation.privacy.PrivacyConsentManager
import com.carlos.autoflow.foundation.upgrade.UpgradeManager
import com.carlos.autoflow.foundation.upgrade.ui.AutoUpgradeChecker
import com.carlos.autoflow.platform.ad.AdService
import com.carlos.autoflow.platform.ad.SplashAdCoordinator
import com.carlos.autoflow.platform.ad.SplashAdCooldownManager
import com.carlos.autoflow.ui.screens.MainHomeScreen
import com.carlos.autoflow.ui.theme.AutoFlowTheme
import com.carlos.autoflow.utils.PerformanceMonitor

class MainActivity : ComponentActivity() {
    private lateinit var privacyConsentManager: PrivacyConsentManager
    private lateinit var splashCoordinator: SplashAdCoordinator
    private val isColdStartLaunch by lazy { isColdStart() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestNotificationPermissionIfNeeded()
        privacyConsentManager = PrivacyConsentManager(this)
        splashCoordinator = SplashAdCoordinator(
            this,
            AdService.getAdManager(),
            SplashAdCooldownManager(AdService.preferenceStore(), this)
        )
        if (privacyConsentManager.hasConsent()) {
            splashCoordinator.maybeShowSplash(
                isColdStartLaunch,
                true
            )
        }

        // 初始化性能监控
        PerformanceMonitor.initialize(this)

        setContent {
            var showPrivacyConsentDialog by remember {
                mutableStateOf(!privacyConsentManager.hasConsent())
            }
            val upgradeManager = remember { UpgradeManager(FoundationNetworkClient()) }

            AutoFlowTheme {
                if (showPrivacyConsentDialog) {
                    PrivacyConsentDialog(
                        onAgree = {
                            privacyConsentManager.grantConsent()
                            showPrivacyConsentDialog = false
                        },
                        onDecline = { finish() }
                    )
                } else {
                    AutoUpgradeChecker(
                        versionCode = BuildConfig.VERSION_CODE,
                        upgradeManager = upgradeManager
                    )
                    LaunchedEffect(showPrivacyConsentDialog) {
                        if (!showPrivacyConsentDialog) {
                            splashCoordinator.maybeShowSplash(
                                isColdStartLaunch,
                                privacyConsentManager.hasConsent()
                            )
                        }
                    }
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        MainHomeScreen()
                    }
                }
            }
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.POST_NOTIFICATIONS),
            REQUEST_POST_NOTIFICATIONS
        )
    }

    companion object {
        private const val REQUEST_POST_NOTIFICATIONS = 1001
    }

    private fun isColdStart(): Boolean {
        val fromHistory = intent?.flags?.and(Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) != 0
        return isTaskRoot && !fromHistory
    }

}
