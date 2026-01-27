package com.carlos.autoflow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.carlos.autoflow.ui.theme.AutoFlowTheme
import com.carlos.autoflow.workflow.ui.WorkflowEditor
import com.carlos.autoflow.utils.PerformanceMonitor

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 初始化性能监控
        PerformanceMonitor.initialize(this)
        
        setContent {
            AutoFlowTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    WorkflowEditor()
                }
            }
        }
    }
}
