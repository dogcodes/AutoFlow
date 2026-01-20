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
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.carlos.autoflow.workflow.models.NodeType
import com.carlos.autoflow.workflow.viewmodel.CanvasViewModel
import com.carlos.autoflow.workflow.viewmodel.WorkflowViewModel
import kotlinx.coroutines.delay
import kotlin.math.floor
import kotlin.math.pow
import kotlin.math.sqrt

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

    // 控制节点选择面板的显示/隐藏状态
    var isPanelVisible by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTransformGestures { centroid, pan, zoom, _ ->
                        val oldScale = canvasState.scale
                        val newScale = (oldScale * zoom).coerceIn(canvasState.minScale, canvasState.maxScale)
                        val effectiveZoom = newScale / oldScale

                        val newOffsetX = canvasState.offsetX + pan.x + (centroid.x - canvasState.offsetX) * (1 - effectiveZoom)
                        val newOffsetY = canvasState.offsetY + pan.y + (centroid.y - canvasState.offsetY) * (1 - effectiveZoom)

                        canvasViewModel.updateScale(newScale)
                        canvasViewModel.updateOffset(newOffsetX, newOffsetY)
                    }
                }
                .graphicsLayer(
                    scaleX = canvasState.scale,
                    scaleY = canvasState.scale,
                    translationX = canvasState.offsetX,
                    translationY = canvasState.offsetY
                )
        ) {
            // 背景网格、连接线和节点都在这个被统一变换的Box中
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
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

                                    // offset 坐标已由父级 graphicsLayer 自动转换为世界坐标，无需再次转换
                                    val distance = sqrt(
                                        (offset.x - midX).pow(2) + (offset.y - midY).pow(2)
                                    )

                                    if (distance < 12.dp.toPx()) {
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
                val worldLeft = -canvasState.offsetX / canvasState.scale
                val worldTop = -canvasState.offsetY / canvasState.scale
                val worldRight = worldLeft + size.width / canvasState.scale
                val worldBottom = worldTop + size.height / canvasState.scale

                // 绘制垂直线
                val firstVerticalLineX = (floor(worldLeft / gridSize) * gridSize)
                var currentX = firstVerticalLineX
                while (currentX <= worldRight) {
                    drawLine(
                        color = Color.Gray.copy(alpha = 0.2f),
                        start = Offset(currentX, worldTop),
                        end = Offset(currentX, worldBottom),
                        strokeWidth = (1.dp.toPx()) / canvasState.scale
                    )
                    currentX += gridSize
                }

                // 绘制水平线
                val firstHorizontalLineY = (floor(worldTop / gridSize) * gridSize)
                var currentY = firstHorizontalLineY
                while (currentY <= worldBottom) {
                    drawLine(
                        color = Color.Gray.copy(alpha = 0.2f),
                        start = Offset(worldLeft, currentY),
                        end = Offset(worldRight, currentY),
                        strokeWidth = (1.dp.toPx()) / canvasState.scale
                    )
                    currentY += gridSize
                }

                // 绘制连接线 (使用世界坐标，无需手动应用变换)
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

                        val controlOffset = 60.dp.toPx()
                        val path = Path().apply {
                            moveTo(startX, startY)
                            cubicTo(startX + controlOffset, startY, endX - controlOffset, endY, endX, endY)
                        }

                        drawPath(
                            path = path,
                            color = Color(0xFF1976D2),
                            style = Stroke(width = 3.dp.toPx())
                        )

                        drawCircle(color = Color(0xFF2196F3), radius = 5.dp.toPx(), center = Offset(startX, startY))
                        drawCircle(color = Color.White, radius = 2.dp.toPx(), center = Offset(startX, startY))
                        drawCircle(color = Color(0xFF42A5F5), radius = 5.dp.toPx(), center = Offset(endX, endY))
                        drawCircle(color = Color.White, radius = 2.dp.toPx(), center = Offset(endX, endY))

                        val midX = (startX + endX) / 2
                        val midY = (startY + endY) / 2
                        drawCircle(color = Color(0xFFE53E3E), radius = 8.dp.toPx(), center = Offset(midX, midY))
                        drawCircle(color = Color.White, radius = 3.dp.toPx(), center = Offset(midX, midY))
                    }
                }
            }

            // 渲染所有节点 (不再需要单独的graphicsLayer)
            workflow.nodes.forEach { node ->
                key(node.id) {
                    Box(
                        modifier = Modifier.offset(x = node.x.dp, y = node.y.dp)
                    ) {
                        WorkflowNodeView(
                            node = node,
                            isSelected = selectedNodeId == node.id,
                            isConnecting = connectingNodeId == node.id,
                                                        onMove = { deltaX, deltaY ->
                                                            workflowViewModel.moveNode(
                                                                node.id,
                                                                deltaX,
                                                                deltaY
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
        }

        // UI元素（面板、按钮等）保持在最上层，不受缩放影响
        AnimatedVisibility(
            visible = isPanelVisible,
            enter = slideInHorizontally(),
            exit = slideOutHorizontally(),
            modifier = Modifier.align(Alignment.CenterStart)
        ) {
            NodeSelectionPanel(
                onNodeSelected = { nodeType ->
                    workflowViewModel.addNode(nodeType, 100f, 100f)
                }
            )
        }
        
        // 切换节点选择面板可见性的FAB
        FloatingActionButton(
            onClick = { isPanelVisible = !isPanelVisible },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp),
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            Icon(
                imageVector = if (isPanelVisible) Icons.Default.Close else Icons.Default.Menu,
                contentDescription = if (isPanelVisible) "关闭节点面板" else "打开节点面板",
                tint = Color.White
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            FloatingActionButton(
                onClick = { canvasViewModel.zoomIn() },
                modifier = Modifier.size(48.dp),
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "放大")
            }

            Spacer(modifier = Modifier.height(8.dp))

            FloatingActionButton(
                onClick = { canvasViewModel.zoomOut() },
                modifier = Modifier.size(48.dp),
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Remove, contentDescription = "缩小")
            }

            Spacer(modifier = Modifier.height(8.dp))

            FloatingActionButton(
                onClick = { canvasViewModel.resetZoom() },
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
                delay(2000)
                workflowViewModel.selectNode(null)
            }
        }
    }
}
