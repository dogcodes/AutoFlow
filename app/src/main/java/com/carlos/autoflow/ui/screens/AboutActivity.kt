package com.carlos.autoflow.ui.screens

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.carlos.autoflow.foundation.ui.components.FoundationTopAppBar
import com.carlos.autoflow.ui.theme.AutoFlowTheme

class AboutActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AutoFlowTheme {
                Scaffold(
                    topBar = {
                        FoundationTopAppBar(
                            title = "关于",
                            onNavigationClick = { finish() }
                        )
                    }
                ) { padding ->
                    AboutScreen(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                    )
                }
            }
        }
    }
}
