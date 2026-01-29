package com.carlos.autoflow.monitor

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import android.content.Intent
import com.carlos.autoflow.utils.PermissionUtils
import androidx.compose.ui.unit.sp
import com.carlos.autoflow.monitor.NodeMonitorAccessibilityService

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NodeMonitorDemo() {
    val context = LocalContext.current
    var isServiceEnabled by remember { mutableStateOf(NodeMonitorAccessibilityService.isServiceEnabled()) }
    val isMonitoringActive by NodeMonitorManager.isMonitoringActive
    
    // 监听生命周期变化来更新服务状态
    LaunchedEffect(Unit) {
        while (true) {
            isServiceEnabled = NodeMonitorAccessibilityService.isServiceEnabled()
            kotlinx.coroutines.delay(1000) // 每秒检查一次
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 标题
        Text(
            text = "Node Monitor 演示",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        
        // 状态卡片
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "当前状态",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                
                StatusItem(
                    label = "无障碍服务",
                    status = if (isServiceEnabled) "已启用" else "未启用",
                    isEnabled = isServiceEnabled
                )
                
                StatusItem(
                    label = "监控状态",
                    status = if (isMonitoringActive) "监控中" else "已停止",
                    isEnabled = isMonitoringActive
                )
            }
        }
        
        // 操作按钮
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "操作控制",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                
                if (!isServiceEnabled) {
                    Button(
                        onClick = {
                            PermissionUtils.openAccessibilitySettings(context)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("开启无障碍服务")
                    }
                } else {
                    Button(
                        onClick = {
                            try {
                                if (isMonitoringActive) {
                                    NodeMonitorManager.stopMonitoring(context)
                                } else {
                                    if (!NodeMonitorManager.startMonitoring(context)) {
                                        // 服务未启用，重新检查状态
                                        isServiceEnabled = NodeMonitorAccessibilityService.isServiceEnabled()
                                    }
                                }
                            } catch (e: Exception) {
                                // 处理异常，避免崩溃
                                e.printStackTrace()
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (isMonitoringActive) "停止监控" else "开始监控")
                    }
                }
            }
        }
        
        // 使用说明
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = "信息",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "使用说明",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
                
                LazyColumn(
                    modifier = Modifier.height(200.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(usageInstructions) { instruction ->
                        Text(
                            text = "• $instruction",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatusItem(
    label: String,
    status: String,
    isEnabled: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Surface(
            color = if (isEnabled) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer,
            shape = MaterialTheme.shapes.small
        ) {
            Text(
                text = status,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = if (isEnabled) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer
            )
        }
    }
}

private val usageInstructions = listOf(
    "首先确保无障碍服务已启用",
    "点击开始监控按钮启动节点监控",
    "屏幕右上角会出现浮动按钮",
    "主按钮显示监控状态，可点击切换",
    "数字显示当前页面节点总数",
    "列表按钮打开侧滑面板查看详情",
    "在侧滑面板中可以浏览所有节点",
    "点击具体节点查看详细属性信息",
    "包括位置、大小、交互属性等",
    "使用完毕后记得关闭监控节省资源"
)
