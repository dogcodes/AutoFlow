package com.carlos.autoflow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.carlos.autoflow.ui.theme.AutoFlowTheme
import com.carlos.autoflow.workflow.ui.WorkflowEditor
import com.carlos.autoflow.demo.DemoAppScreen
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
                    MainScreen()
                }
            }
        }
    }
}

@Composable
private fun MainScreen() {
    var currentScreen by remember { mutableStateOf("workflow") }
    
    Column(modifier = Modifier.fillMaxSize()) {
        // 顶部切换栏
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1976D2)),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(0.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                TabButton(
                    text = "工作流编辑器",
                    icon = Icons.Default.AccountTree,
                    isSelected = currentScreen == "workflow",
                    onClick = { currentScreen = "workflow" },
                    modifier = Modifier.weight(1f)
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                TabButton(
                    text = "示例应用",
                    icon = Icons.Default.Apps,
                    isSelected = currentScreen == "demo",
                    onClick = { currentScreen = "demo" },
                    modifier = Modifier.weight(1f)
                )
            }
        }
        
        // 内容区域
        when (currentScreen) {
            "workflow" -> WorkflowEditor()
            "demo" -> DemoAppScreen()
        }
    }
}

@Composable
private fun TabButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) 
                Color.White.copy(alpha = 0.2f) 
            else Color.Transparent
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}
