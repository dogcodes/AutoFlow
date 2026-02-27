package com.carlos.autoflow.workflow.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.sp
import com.carlos.autoflow.workflow.models.NodeType
import com.carlos.autoflow.workflow.models.WorkflowNode

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WorkflowNodeView(
    node: WorkflowNode,
    isSelected: Boolean,
    isConnecting: Boolean = false,
    onMove: (Float, Float) -> Unit,
    onSelect: () -> Unit,
    onDelete: () -> Unit,
    onConfig: () -> Unit = {},
    onStartConnection: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var isDragging by remember { mutableStateOf(false) }
    var showContextMenu by remember { mutableStateOf(false) }
    val density = LocalDensity.current

    Box {
        Card(
            modifier = modifier
                .size(160.dp, 100.dp)
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { isDragging = true },
                        onDragEnd = { isDragging = false }
                    ) { _, dragAmount ->
                        val deltaXDp = with(density) { dragAmount.x.toDp().value }
                        val deltaYDp = with(density) { dragAmount.y.toDp().value }
                        onMove(deltaXDp, deltaYDp)
                    }
                }
                .combinedClickable(
                    onClick = onSelect,
                    onDoubleClick = onConfig,
                    onLongClick = { showContextMenu = true }
                ),
            colors = CardDefaults.cardColors(
                containerColor = when {
                    isConnecting -> Color(0xFFFFEB3B).copy(alpha = 0.3f)
                    isSelected -> Color(0xFFE3F2FD)
                    else -> Color.White
                }
            ),
            border = when {
                isConnecting -> BorderStroke(3.dp, Color(0xFFFF9800))
                isSelected -> BorderStroke(2.dp, Color(0xFF1976D2))
                else -> null
            },
            elevation = CardDefaults.cardElevation(defaultElevation = if (isDragging) 8.dp else 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = getNodeIcon(node.type),
                        contentDescription = null,
                        tint = Color(0xFF1976D2),
                        modifier = Modifier.size(20.dp)
                    )
                    
                    if (isSelected) {
                        Row {
                            // 连接按钮（除了END节点）
                            if (node.type != NodeType.END) {
                                IconButton(
                                    onClick = onStartConnection,
                                    modifier = Modifier.size(20.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Link,
                                        contentDescription = "连接",
                                        modifier = Modifier.size(14.dp),
                                        tint = if (isConnecting) Color(0xFFFF9800) else Color(0xFF4CAF50)
                                    )
                                }
                            }
                            
                            // 删除按钮
                            IconButton(
                                onClick = onDelete,
                                modifier = Modifier.size(20.dp)
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "删除",
                                    modifier = Modifier.size(14.dp),
                                    tint = Color(0xFFE57373)
                                )
                            }
                        }
                    }
                }
                
                Column {
                    Text(
                        text = node.title,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1
                    )
                    
                    // 显示操作提示
                    val config = node.config
                    if (config != null && config.isNotEmpty()) {
                        Text(
                            text = "已配置",
                            fontSize = 10.sp,
                            color = Color(0xFF4CAF50)
                        )
                    } else if (node.type != NodeType.START && node.type != NodeType.END) {
                        Text(
                            text = if (isSelected) "双击配置" else "点击选择",
                            fontSize = 10.sp,
                            color = if (isSelected) Color(0xFFFF9800) else Color.Gray
                        )
                    }
                }
            }
        }
        
        // 右键菜单
        DropdownMenu(
            expanded = showContextMenu,
            onDismissRequest = { showContextMenu = false }
        ) {
            if (node.type != NodeType.START && node.type != NodeType.END) {
                DropdownMenuItem(
                    text = { Text("⚙️ 配置节点") },
                    onClick = {
                        showContextMenu = false
                        onConfig()
                    }
                )
            }
            if (node.type != NodeType.END) {
                DropdownMenuItem(
                    text = { Text("🔗 开始连接") },
                    onClick = {
                        showContextMenu = false
                        onStartConnection()
                    }
                )
            }
            DropdownMenuItem(
                text = { Text("🗑️ 删除节点") },
                onClick = {
                    showContextMenu = false
                    onDelete()
                }
            )
        }
    }
}

private fun getNodeIcon(type: NodeType): ImageVector = when (type) {
    NodeType.START -> Icons.Default.PlayArrow
    NodeType.EVENT_TRIGGER -> Icons.Default.Notifications
    NodeType.END -> Icons.Default.Stop
    NodeType.HTTP_REQUEST -> Icons.Default.Send
    NodeType.DATA_TRANSFORM -> Icons.Default.Build
    NodeType.CONDITION -> Icons.Default.Info
    NodeType.LOOP -> Icons.Default.Refresh
    NodeType.DELAY -> Icons.Default.DateRange
    NodeType.SCRIPT -> Icons.Default.Edit
    // UI交互节点图标
    NodeType.UI_CLICK -> Icons.Default.TouchApp
    NodeType.UI_INPUT -> Icons.Default.Keyboard
    NodeType.UI_SCROLL -> Icons.Default.SwipeUp
    NodeType.UI_LONG_CLICK -> Icons.Default.TouchApp
    NodeType.UI_SWIPE -> Icons.Default.SwipeLeft
    // UI检测节点图标
    NodeType.UI_FIND -> Icons.Default.Search
    NodeType.UI_WAIT -> Icons.Default.HourglassEmpty
    NodeType.UI_GET_TEXT -> Icons.Default.TextFields
    NodeType.UI_CHECK -> Icons.Default.CheckCircle
    // 系统事件节点图标
    NodeType.APP_LAUNCH -> Icons.Default.Launch
    NodeType.LAUNCH_ACTIVITY -> Icons.Default.OpenInNew
    NodeType.NOTIFICATION -> Icons.Default.Notifications
    NodeType.SCREEN_STATE -> Icons.Default.ScreenLockPortrait
    NodeType.SYSTEM_GLOBAL_ACTION -> Icons.Default.SettingsApplications
}
