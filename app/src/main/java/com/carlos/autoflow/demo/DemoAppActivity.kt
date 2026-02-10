package com.carlos.autoflow.demo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.carlos.autoflow.ui.theme.AutoFlowTheme

class DemoAppActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AutoFlowTheme {
                DemoAppScreen()
            }
        }
    }
}
