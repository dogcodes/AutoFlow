package com.carlos.autoflow.ui.screens

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.carlos.autoflow.foundation.ui.components.FoundationTopAppBar
import com.carlos.autoflow.foundation.ui.BaseComposeActivity
import com.carlos.autoflow.ui.theme.AutoFlowTheme

class HistoryActivity : BaseComposeActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AutoFlowTheme {
                AutoFlowStatusBarColor()
                Scaffold(
                    topBar = {
                        FoundationTopAppBar(
                            title = "历史记录",
                            onNavigationClick = { finish() }
                        )
                    }
                ) { padding ->
                    HistoryScreen(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding),
                        showTitleBar = false
                    )
                }
            }
        }
    }
}
