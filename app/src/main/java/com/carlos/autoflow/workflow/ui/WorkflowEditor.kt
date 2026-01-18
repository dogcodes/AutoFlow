package com.carlos.autoflow.workflow.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.carlos.autoflow.workflow.models.NodeType
import com.carlos.autoflow.workflow.viewmodel.WorkflowViewModel

@Composable
fun WorkflowEditor() {
    val workflowViewModel: WorkflowViewModel = viewModel()
    val workflow by workflowViewModel.workflow.collectAsState()
    val selectedNodeId by workflowViewModel.selectedNodeId.collectAsState()
    val connectingNodeId by workflowViewModel.connectingNodeId.collectAsState()
    
    Row(modifier = Modifier.fillMaxSize()) {
        // 左侧节点面板 - 添加阴影和圆角
        Surface(
            modifier = Modifier.width(200.dp),
            color = Color.White,
            shadowElevation = 8.dp,
            shape = RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp)
        ) {
            NodeSelectionPanel(
                onNodeSelected = { nodeType ->
                    workflowViewModel.addNode(nodeType, 100f, 100f)
                }
            )
        }
        
        // 主画布区域 - 优化背景和布局
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .background(
                    brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFF8F9FA),
                            Color(0xFFE3F2FD)
                        )
                    )
                )
        ) {
            // 网格背景和连线
            Canvas(
                modifier = Modifier.fillMaxSize()
            ) {
                // 绘制网格
                val gridSize = 20.dp.toPx()
                val strokeWidth = 0.5.dp.toPx()
                
                val numVerticalLines = (size.width / gridSize).toInt() + 1
                val numHorizontalLines = (size.height / gridSize).toInt() + 1
                
                for (i in 0..numVerticalLines) {
                    drawLine(
                        color = Color(0xFFE0E0E0).copy(alpha = 0.5f),
                        start = androidx.compose.ui.geometry.Offset(i * gridSize, 0f),
                        end = androidx.compose.ui.geometry.Offset(i * gridSize, size.height),
                        strokeWidth = strokeWidth
                    )
                }
                
                for (i in 0..numHorizontalLines) {
                    drawLine(
                        color = Color(0xFFE0E0E0).copy(alpha = 0.5f),
                        start = androidx.compose.ui.geometry.Offset(0f, i * gridSize),
                        end = androidx.compose.ui.geometry.Offset(size.width, i * gridSize),
                        strokeWidth = strokeWidth
                    )
                }
            }
            
            // 单独的连线层，支持点击删除
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures { offset ->
                            // 检测点击的连接线
                            workflow.connections.forEach { connection ->
                                val sourceNode = workflow.nodes.find { it.id == connection.sourceNodeId }
                                val targetNode = workflow.nodes.find { it.id == connection.targetNodeId }
                                
                                if (sourceNode != null && targetNode != null) {
                                    val nodeWidth = 160.dp.toPx()
                                    val nodeHeight = 100.dp.toPx()
                                    
                                    val startX = sourceNode.x.dp.toPx() + nodeWidth + 5.dp.toPx()
                                    val startY = sourceNode.y.dp.toPx() + nodeHeight / 2
                                    val endX = targetNode.x.dp.toPx() - 5.dp.toPx()
                                    val endY = targetNode.y.dp.toPx() + nodeHeight / 2
                                    
                                    // 简单的线段点击检测（检测中点附近）
                                    val midX = (startX + endX) / 2
                                    val midY = (startY + endY) / 2
                                    val distance = kotlin.math.sqrt(
                                        (offset.x - midX) * (offset.x - midX) + 
                                        (offset.y - midY) * (offset.y - midY)
                                    ).toFloat()
                                    
                                    if (distance < 30.dp.toPx()) {
                                        workflowViewModel.removeConnection(connection.id)
                                        return@detectTapGestures
                                    }
                                }
                            }
                        }
                    }
            ) {
                workflow.connections.forEach { connection ->
                    val sourceNode = workflow.nodes.find { it.id == connection.sourceNodeId }
                    val targetNode = workflow.nodes.find { it.id == connection.targetNodeId }
                    
                    if (sourceNode != null && targetNode != null) {
                        // 节点尺寸
                        val nodeWidth = 160.dp.toPx()
                        val nodeHeight = 100.dp.toPx()
                        
                        // 连接点位置
                        val startX = sourceNode.x.dp.toPx() + nodeWidth + 5.dp.toPx()
                        val startY = sourceNode.y.dp.toPx() + nodeHeight / 2
                        val endX = targetNode.x.dp.toPx() - 5.dp.toPx()
                        val endY = targetNode.y.dp.toPx() + nodeHeight / 2
                        
                        // 贝塞尔曲线
                        val controlOffset = 60.dp.toPx()
                        val path = androidx.compose.ui.graphics.Path().apply {
                            moveTo(startX, startY)
                            cubicTo(
                                startX + controlOffset, startY,
                                endX - controlOffset, endY,
                                endX, endY
                            )
                        }
                        
                        // 绘制连接线
                        drawPath(
                            path = path,
                            color = Color(0xFF1976D2),
                            style = androidx.compose.ui.graphics.drawscope.Stroke(
                                width = 3.dp.toPx(),
                                cap = androidx.compose.ui.graphics.StrokeCap.Round
                            )
                        )
                        
                        // 连接点
                        drawCircle(
                            color = Color(0xFF2196F3),
                            radius = 5.dp.toPx(),
                            center = androidx.compose.ui.geometry.Offset(startX, startY)
                        )
                        drawCircle(
                            color = Color.White,
                            radius = 2.dp.toPx(),
                            center = androidx.compose.ui.geometry.Offset(startX, startY)
                        )
                        
                        drawCircle(
                            color = Color(0xFF42A5F5),
                            radius = 5.dp.toPx(),
                            center = androidx.compose.ui.geometry.Offset(endX, endY)
                        )
                        drawCircle(
                            color = Color.White,
                            radius = 2.dp.toPx(),
                            center = androidx.compose.ui.geometry.Offset(endX, endY)
                        )
                        
                        // 在连接线中点绘制删除按钮
                        val midX = (startX + endX) / 2
                        val midY = (startY + endY) / 2
                        drawCircle(
                            color = Color(0xFFE53E3E),
                            radius = 8.dp.toPx(),
                            center = androidx.compose.ui.geometry.Offset(midX, midY)
                        )
                        drawCircle(
                            color = Color.White,
                            radius = 3.dp.toPx(),
                            center = androidx.compose.ui.geometry.Offset(midX, midY)
                        )
                    }
                }
            }
            
            // 渲染所有节点 - 拖拽时禁用动画
            workflow.nodes.forEach { node ->
                key(node.id) {
                    Box(
                        modifier = Modifier
                            .offset(
                                x = node.x.dp,
                                y = node.y.dp
                            )
                    ) {
                        WorkflowNodeView(
                            node = node,
                            isSelected = selectedNodeId == node.id,
                            isConnecting = connectingNodeId == node.id,
                            onMove = { deltaX, deltaY ->
                                workflowViewModel.moveNode(node.id, deltaX, deltaY)
                            },
                            onSelect = { 
                                if (connectingNodeId != null) {
                                    workflowViewModel.finishConnection(node.id)
                                } else {
                                    workflowViewModel.selectNode(node.id)
                                }
                            },
                            onDelete = { workflowViewModel.deleteNode(node.id) },
                            onDoubleClick = {
                                if (connectingNodeId == null) {
                                    if (node.type == NodeType.END) {
                                        // 结束节点双击时显示提示
                                        workflowViewModel.selectNode("end_tip_${node.id}")
                                    } else {
                                        workflowViewModel.startConnection(node.id)
                                    }
                                } else {
                                    workflowViewModel.cancelConnection()
                                }
                            }
                        )
                    }
                }
            }
            
            // 状态指示器
            if (workflow.nodes.isEmpty()) {
                Card(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(32.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 0.9f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "🎯 开始创建工作流",
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color(0xFF1976D2)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "从左侧面板选择节点开始",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }
                }
            }
            
            // 连接状态提示
            if (connectingNodeId != null) {
                val connectingNode = workflow.nodes.find { it.id == connectingNodeId }
                Card(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFF9800).copy(alpha = 0.9f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = when (connectingNode?.type) {
                            NodeType.START -> "🔗 从【开始】连接：点击其他节点完成连接（开始节点不能被连接）"
                            else -> "🔗 连接模式：点击目标节点完成连接"
                        },
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White
                    )
                }
            }
            
            // 结束节点双击提示
            if (selectedNodeId?.startsWith("end_tip_") == true) {
                Card(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFE53E3E).copy(alpha = 0.9f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "❌ 结束节点无法向外连接，只能接收来自其他节点的连接",
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White
                    )
                }
                
                // 2秒后自动清除提示
                LaunchedEffect(selectedNodeId) {
                    kotlinx.coroutines.delay(2000)
                    workflowViewModel.selectNode(null)
                }
            }
        }
    }
}
