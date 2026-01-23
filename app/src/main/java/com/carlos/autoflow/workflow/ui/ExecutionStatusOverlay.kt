package com.carlos.autoflow.workflow.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.carlos.autoflow.workflow.models.WorkflowNode
import kotlinx.coroutines.delay

enum class ExecutionStatus {
    IDLE, RUNNING, SUCCESS, ERROR, WARNING
}

data class NodeExecutionState(
    val nodeId: String,
    val status: ExecutionStatus,
    val message: String = "",
    val progress: Float = 0f
)

@Composable
fun ExecutionStatusOverlay(
    executingNodes: Map<String, NodeExecutionState>,
    modifier: Modifier = Modifier
) {
    if (executingNodes.isNotEmpty()) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        "工作流执行中",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                executingNodes.values.forEach { nodeState ->
                    NodeExecutionItem(nodeState)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun NodeExecutionItem(
    nodeState: NodeExecutionState
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 状态指示器
        StatusIndicator(nodeState.status)
        
        // 节点信息
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "节点 ${nodeState.nodeId}",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
            if (nodeState.message.isNotEmpty()) {
                Text(
                    text = nodeState.message,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        // 进度指示
        if (nodeState.status == ExecutionStatus.RUNNING && nodeState.progress > 0) {
            CircularProgressIndicator(
                progress = nodeState.progress,
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp
            )
        }
    }
}

@Composable
private fun StatusIndicator(status: ExecutionStatus) {
    val (icon, color) = when (status) {
        ExecutionStatus.IDLE -> Icons.Default.RadioButtonUnchecked to Color.Gray
        ExecutionStatus.RUNNING -> Icons.Default.Refresh to MaterialTheme.colorScheme.primary
        ExecutionStatus.SUCCESS -> Icons.Default.CheckCircle to Color(0xFF4CAF50)
        ExecutionStatus.ERROR -> Icons.Default.Error to Color(0xFFF44336)
        ExecutionStatus.WARNING -> Icons.Default.Warning to Color(0xFFFF9800)
    }
    
    Box(
        modifier = Modifier
            .size(24.dp)
            .clip(CircleShape)
            .background(color.copy(alpha = 0.1f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
fun NodeExecutionIndicator(
    node: WorkflowNode,
    executionState: NodeExecutionState?,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = executionState != null,
        enter = scaleIn() + fadeIn(),
        exit = scaleOut() + fadeOut(),
        modifier = modifier
    ) {
        executionState?.let { state ->
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(
                        when (state.status) {
                            ExecutionStatus.RUNNING -> MaterialTheme.colorScheme.primary
                            ExecutionStatus.SUCCESS -> Color(0xFF4CAF50)
                            ExecutionStatus.ERROR -> Color(0xFFF44336)
                            ExecutionStatus.WARNING -> Color(0xFFFF9800)
                            else -> Color.Transparent
                        }
                    )
            ) {
                if (state.status == ExecutionStatus.RUNNING) {
                    var rotation by remember { mutableStateOf(0f) }
                    
                    LaunchedEffect(Unit) {
                        while (true) {
                            rotation += 10f
                            if (rotation >= 360f) rotation = 0f
                            delay(50)
                        }
                    }
                    
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier
                            .size(8.dp)
                            .align(Alignment.Center)
                    )
                }
            }
        }
    }
}

// 执行状态管理器
class ExecutionStatusManager {
    private val _executingNodes = mutableStateMapOf<String, NodeExecutionState>()
    val executingNodes: Map<String, NodeExecutionState> = _executingNodes
    
    fun updateNodeStatus(nodeId: String, status: ExecutionStatus, message: String = "", progress: Float = 0f) {
        _executingNodes[nodeId] = NodeExecutionState(nodeId, status, message, progress)
    }
    
    fun removeNode(nodeId: String) {
        _executingNodes.remove(nodeId)
    }
    
    fun clear() {
        _executingNodes.clear()
    }
    
    suspend fun executeWithStatus(
        nodeId: String,
        message: String,
        operation: suspend () -> Unit
    ) {
        try {
            updateNodeStatus(nodeId, ExecutionStatus.RUNNING, message)
            operation()
            updateNodeStatus(nodeId, ExecutionStatus.SUCCESS, "执行成功")
            delay(1000) // 显示成功状态1秒
        } catch (e: Exception) {
            updateNodeStatus(nodeId, ExecutionStatus.ERROR, "执行失败: ${e.message}")
            delay(2000) // 显示错误状态2秒
        } finally {
            removeNode(nodeId)
        }
    }
}
