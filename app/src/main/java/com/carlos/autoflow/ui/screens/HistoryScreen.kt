package com.carlos.autoflow.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.offset
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.carlos.autoflow.workflow.models.ExecutionStatus
import com.carlos.autoflow.workflow.models.WorkflowExecution
import com.carlos.autoflow.workflow.repository.ExecutionHistoryRepository

@Composable
fun HistoryScreen() {
    val context = LocalContext.current
    val historyRepository = remember { ExecutionHistoryRepository(context) }
    val executions by historyRepository.executions.collectAsState()
    var selectedExecution by remember { mutableStateOf<WorkflowExecution?>(null) }
    var showClearDialog by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // 标题栏
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "执行历史",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            
            if (executions.isNotEmpty()) {
                IconButton(onClick = { showClearDialog = true }) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "清空全部",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (executions.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.History,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "暂无执行记录",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn {
                items(executions) { execution ->
                    ExecutionHistoryItem(
                        execution = execution,
                        onClick = { selectedExecution = execution },
                        onDelete = { historyRepository.deleteExecution(execution.id) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
    
    // 详情对话框
    selectedExecution?.let { execution ->
        ExecutionDetailDialog(
            execution = execution,
            onDismiss = { selectedExecution = null }
        )
    }
    
    // 清空确认对话框
    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("清空历史记录") },
            text = { Text("确定要清空所有执行历史记录吗？此操作不可撤销。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        historyRepository.clearAllExecutions()
                        showClearDialog = false
                    }
                ) {
                    Text("确定", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
}

@Composable
private fun ExecutionHistoryItem(
    execution: WorkflowExecution,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    var offsetX by remember { mutableFloatStateOf(0f) }
    val maxOffset = -120f // 左滑最大距离
    
    Box(
        modifier = Modifier.fillMaxWidth()
    ) {
        // 删除按钮背景
        if (offsetX < 0) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .background(MaterialTheme.colorScheme.error),
                contentAlignment = Alignment.CenterEnd
            ) {
                Row (
                    modifier = Modifier.padding(horizontal = 16.dp).clickable(onClick = onDelete),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "删除",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "删除",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
        
        // 主内容卡片
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .offset(x = offsetX.dp)
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            // 滑动超过一半时停留在删除位置，否则回弹
                            offsetX = if (offsetX < maxOffset / 2) {
                                maxOffset
                            } else {
                                0f
                            }
                        }
                    ) { _, dragAmount ->
                        val newOffset = (offsetX + dragAmount).coerceIn(maxOffset, 0f)
                        offsetX = newOffset
                    }
                }
                .clickable { 
                    if (offsetX == 0f) {
                        onClick()
                    } else {
                        // 如果处于滑动状态，点击收回
                        offsetX = 0f
                    }
                },
            colors = CardDefaults.cardColors(
                containerColor = when (execution.status) {
                    ExecutionStatus.SUCCESS -> Color(0xFFE8F5E8)
                    ExecutionStatus.FAILED -> Color(0xFFFFEBEE)
                    ExecutionStatus.STOPPED -> Color(0xFFFFF3E0)
                    ExecutionStatus.RUNNING -> Color(0xFFE3F2FD)
                }
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = execution.workflowName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f)
                    )
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = when (execution.status) {
                                ExecutionStatus.SUCCESS -> Icons.Default.CheckCircle
                                ExecutionStatus.FAILED -> Icons.Default.Error
                                ExecutionStatus.STOPPED -> Icons.Default.Stop
                                ExecutionStatus.RUNNING -> Icons.Default.PlayArrow
                            },
                            contentDescription = null,
                            tint = when (execution.status) {
                                ExecutionStatus.SUCCESS -> Color(0xFF4CAF50)
                                ExecutionStatus.FAILED -> Color(0xFFF44336)
                                ExecutionStatus.STOPPED -> Color(0xFFFF9800)
                                ExecutionStatus.RUNNING -> Color(0xFF2196F3)
                            },
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = when (execution.status) {
                                ExecutionStatus.SUCCESS -> "成功"
                                ExecutionStatus.FAILED -> "失败"
                                ExecutionStatus.STOPPED -> "已停止"
                                ExecutionStatus.RUNNING -> "运行中"
                            },
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = execution.formattedStartTime,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    execution.endTime?.let {
                        Text(
                            text = "耗时: ${execution.duration}ms",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ExecutionDetailDialog(
    execution: WorkflowExecution,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.8f),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // 标题栏
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "执行详情",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, "关闭")
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 详情内容
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    // 基本信息
                    DetailInfoCard(
                        title = "基本信息",
                        content = {
                            DetailRow("工作流名称", execution.workflowName)
                            DetailRow("执行状态", when (execution.status) {
                                ExecutionStatus.SUCCESS -> "成功"
                                ExecutionStatus.FAILED -> "失败"
                                ExecutionStatus.STOPPED -> "已停止"
                                ExecutionStatus.RUNNING -> "运行中"
                            })
                            DetailRow("开始时间", execution.formattedStartTime)
                            execution.endTime?.let {
                                DetailRow("耗时", "${execution.duration}ms")
                            }
                        }
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // 执行结果
                    DetailInfoCard(
                        title = "执行结果",
                        content = {
                            Text(
                                text = execution.result,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailInfoCard(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            content()
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}
