package com.carlos.autoflow.ui.screens

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.carlos.autoflow.billing.ui.LicenseDialog
import com.carlos.autoflow.foundation.ui.BaseComposeActivity
import com.carlos.autoflow.ui.theme.AutoFlowTheme

class LicenseActivity : BaseComposeActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AutoFlowTheme {
                AutoFlowStatusBarColor(Color.Transparent)
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Transparent
                ) {
                    LicenseDialog(onDismiss = { finish() })
                }
            }
        }
    }
}
