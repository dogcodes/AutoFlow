package com.carlos.autoflow.foundation.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.core.view.WindowCompat
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb

abstract class BaseComposeActivity : ComponentActivity() {
    protected open val useImmersiveStatusBar: Boolean = true
    protected open val initialStatusBarColor: Color = Color.Transparent
    protected open val preferredStatusBarColor: Color? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (useImmersiveStatusBar) {
            WindowCompat.setDecorFitsSystemWindows(window, false)
            applyStatusBarColor(initialStatusBarColor, initialStatusBarColor.luminance() > 0.5f)
        }
    }

    protected fun applyStatusBarColor(color: Color, useDarkIcons: Boolean) {
        window.statusBarColor = color.toArgb()
        WindowCompat.getInsetsController(window, window.decorView)
            ?.isAppearanceLightStatusBars = useDarkIcons
    }

    @Composable
    protected fun AutoFlowStatusBarColor(color: Color? = preferredStatusBarColor) {
        val resolvedColor = color ?: MaterialTheme.colorScheme.surface
        LaunchedEffect(resolvedColor) {
            applyStatusBarColor(resolvedColor, resolvedColor.luminance() > 0.5f)
        }
    }
}
