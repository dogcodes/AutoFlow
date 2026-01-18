package com.carlos.autoflow.workflow.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.carlos.autoflow.workflow.models.NodeType
import com.carlos.autoflow.workflow.viewmodel.CanvasViewModel
import com.carlos.autoflow.workflow.viewmodel.WorkflowViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkflowEditor(
    workflowViewModel: WorkflowViewModel = viewModel(),
    canvasViewModel: CanvasViewModel = viewModel()
) {
    val workflow by workflowViewModel.workflow.collectAsState()
    val canvasState by canvasViewModel.canvasState.collectAsState()
    val connectingNodeId by workflowViewModel.connectingNodeId.collectAsState()
    val selectedNodeId by workflowViewModel.selectedNodeId.collectAsState()
    val density = LocalDensity.current

    Box(modifier = Modifier.fillMaxSize()) {
        // 背景网格和手势检测
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        canvasViewModel.updateScale(canvasState.scale * zoom)
                        canvasViewModel.updateOffset(
                            canvasState.offsetX + pan.x,
                            canvasState.offsetY + pan.y
                        )
                    }
                }
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        // 检测连接线删除按钮点击
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
            // 绘制网格
            val gridSize = 20.dp.toPx()
            val startX = (-canvasState.offsetX / canvasState.scale) % gridSize
            val startY = (-canvasState.offsetY / canvasState.scale) % gridSize
            
            for (x in (startX.toInt() until size.width.toInt() step gridSize.toInt())) {
                drawLine(
                    color = Color.Gray.copy(alpha = 0.2f),
                    start = Offset(x.toFloat(), 0f),
                    end = Offset(x.toFloat(), size.height),
                    strokeWidth = 1.dp.toPx()
                )
            }
            
            for (y in (startY.toInt() until size.height.toInt() step gridSize.toInt())) {
                drawLine(
                    color = Color.Gray.copy(alpha = 0.2f),
                    start = Offset(0f, y.toFloat()),
                    end = Offset(size.width, y.toFloat()),
                    strokeWidth = 1.dp.toPx()
                )
            }
            
            // 绘制连接线（不缩放）
            workflow.connections.forEach { connection ->
                val sourceNode = workflow.nodes.find { it.id == connection.sourceNodeId }
                val targetNode = workflow.nodes.find { it.id == connection.targetNodeId }
                
                if (sourceNode != null && targetNode != null) {
                    // 节点尺寸
                    val nodeWidth = 160.dp.toPx()
                    val nodeHeight = 100.dp.toPx()
                    
                    // 连接点位置（应用缩放和偏移）
                    val startX = (sourceNode.x.dp.toPx() + nodeWidth + 5.dp.toPx()) * canvasState.scale + canvasState.offsetX
                    val startY = (sourceNode.y.dp.toPx() + nodeHeight / 2) * canvasState.scale + canvasState.offsetY
                    val endX = (targetNode.x.dp.toPx() - 5.dp.toPx()) * canvasState.scale + canvasState.offsetX
                    val endY = (targetNode.y.dp.toPx() + nodeHeight / 2) * canvasState.scale + canvasState.offsetY
                    
                    // 贝塞尔曲线
                    val controlOffset = 60.dp.toPx() * canvasState.scale
                    val path = Path().apply {
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
                        center = Offset(startX, startY)
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 2.dp.toPx(),
                        center = Offset(startX, startY)
                    )
                    
                    drawCircle(
                        color = Color(0xFF42A5F5),
                        radius = 5.dp.toPx(),
                        center = Offset(endX, endY)
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 2.dp.toPx(),
                        center = Offset(endX, endY)
                    )
                    
                    // 在连接线中点绘制删除按钮
                    val midX = (startX + endX) / 2
                    val midY = (startY + endY) / 2
                    drawCircle(
                        color = Color(0xFFE53E3E),
                        radius = 8.dp.toPx(),
                        center = Offset(midX, midY)
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 3.dp.toPx(),
                        center = Offset(midX, midY)
                    )
                }
            }
        }
        
        // 渲染所有节点 - 支持缩放
        workflow.nodes.forEach { node ->
            key(node.id) {
                Box(
                    modifier = Modifier
                        .offset(
                            x = node.x.dp,
                            y = node.y.dp
                        )
                        .graphicsLayer(
                            scaleX = canvasState.scale,
                            scaleY = canvasState.scale,
                            translationX = canvasState.offsetX,
                            translationY = canvasState.offsetY
                        )
                ) {
                    WorkflowNodeView(
                        node = node,
                        isSelected = selectedNodeId == node.id,
                        isConnecting = connectingNodeId == node.id,
                        onMove = { deltaX, deltaY ->
                            workflowViewModel.moveNode(
                                node.id, 
                                deltaX / canvasState.scale, 
                                deltaY / canvasState.scale
                            )
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
        
        // 节点选择面板
        NodeSelectionPanel(
            modifier = Modifier.align(Alignment.CenterStart),
            onNodeSelected = { nodeType ->
                workflowViewModel.addNode(nodeType, 100f, 100f)
            }
        )
        
        // 缩放控制和重置按钮
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            FloatingActionButton(
                onClick = { canvasViewModel.updateScale(canvasState.scale * 1.2f) },
                modifier = Modifier.size(48.dp),
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "放大")
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            FloatingActionButton(
                onClick = { canvasViewModel.updateScale(canvasState.scale * 0.8f) },
                modifier = Modifier.size(48.dp),
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Remove, contentDescription = "缩小")
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            FloatingActionButton(
                onClick = { 
                    canvasViewModel.updateScale(1.0f)
                    canvasViewModel.updateOffset(0f, 0f)
                },
                modifier = Modifier.size(48.dp),
                containerColor = MaterialTheme.colorScheme.secondary
            ) {
                Text(
                    text = "重置",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White
                )
            }
        }
        
        // 连接状态提示
        if (connectingNodeId != null) {
            val connectingNode = workflow.nodes.find { it.id == connectingNodeId }
            Card(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2196F3))
            ) {
                Text(
                    text = when (connectingNode?.type) {
                        NodeType.START -> "🔗 从【开始】连接：点击其他节点完成连接"
                        else -> "🔗 连接模式：点击目标节点完成连接"
                    },
                    modifier = Modifier.padding(12.dp),
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
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE53E3E))
            ) {
                Text(
                    text = "❌ 结束节点无法向外连接",
                    modifier = Modifier.padding(12.dp),
                    color = Color.White
                )
            }
            
            LaunchedEffect(selectedNodeId) {
                kotlinx.coroutines.delay(2000)
                workflowViewModel.selectNode(null)
            }
        }
    }
}
