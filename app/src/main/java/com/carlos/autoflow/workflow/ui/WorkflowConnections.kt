package com.carlos.autoflow.workflow.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.carlos.autoflow.ui.theme.Dimens
import com.carlos.autoflow.workflow.models.Workflow
import com.carlos.autoflow.workflow.viewmodel.WorkflowViewModel
import com.carlos.autoflow.workflow.viewmodel.CanvasState

@Composable
fun WorkflowConnections(
    workflow: Workflow,
    workflowViewModel: WorkflowViewModel,
    canvasState: CanvasState
) {
    val density = LocalDensity.current
    
    // 动画进度，用于流动效果（只在启用时创建）
    val animationProgress by if (canvasState.showFlowAnimation) {
        rememberInfiniteTransition(label = "connection_flow").animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(2000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "flow_progress"
        )
    } else {
        remember { mutableStateOf(0f) }
    }

    // 绘制连接线 (使用世界坐标，无需手动应用变换)
    Canvas(
        modifier = Modifier
            .fillMaxSize()
    ) {
        workflow.connections.forEach { connection ->
            val sourceNode = workflow.nodes.find { it.id == connection.sourceNodeId }
            val targetNode = workflow.nodes.find { it.id == connection.targetNodeId }

            if (sourceNode != null && targetNode != null) {
                val nodeWidth = Dimens.WorkflowEditor.NodeWidth.toPx()
                val nodeHeight = Dimens.WorkflowEditor.NodeHeight.toPx()

                val startX = sourceNode.x.dp.toPx() + nodeWidth + Dimens.WorkflowEditor.ConnectionCircleRadiusOuter.toPx()
                val startY = sourceNode.y.dp.toPx() + nodeHeight / 2
                val endX = targetNode.x.dp.toPx() - Dimens.WorkflowEditor.ConnectionCircleRadiusOuter.toPx()
                val endY = targetNode.y.dp.toPx() + nodeHeight / 2

                val controlOffset = Dimens.WorkflowEditor.ConnectionControlOffset.toPx()
                val path = Path().apply {
                    moveTo(startX, startY)
                    cubicTo(startX + controlOffset, startY, endX - controlOffset, endY, endX, endY)
                }

                // 绘制连接线
                drawPath(
                    path = path,
                    color = Dimens.WorkflowEditorColors.ConnectionLine,
                    style = Stroke(width = Dimens.WorkflowEditor.ConnectionStrokeWidth.toPx())
                )

                // 绘制流动的小点（仅在启用时）
                if (canvasState.showFlowAnimation) {
                    val pathMeasure = PathMeasure()
                    pathMeasure.setPath(path, false)
                    val pathLength = pathMeasure.length
                    
                    // 绘制3个流动点
                    for (i in 0..2) {
                        val offset = (animationProgress + i * 0.33f) % 1f
                        val distance = offset * pathLength
                        val position = pathMeasure.getPosition(distance)
                        
                        drawCircle(
                            color = Color(0xFF4CAF50),
                            radius = 4.dp.toPx(),
                            center = position
                        )
                    }
                }

                // 绘制端点圆圈
                drawCircle(color = Dimens.WorkflowEditorColors.ConnectionCircleOutline, radius = Dimens.WorkflowEditor.ConnectionCircleRadiusOuter.toPx(), center = Offset(startX, startY))
                drawCircle(color = Color.White, radius = Dimens.WorkflowEditor.ConnectionCircleRadiusInner.toPx(), center = Offset(startX, startY))
                drawCircle(color = Dimens.WorkflowEditorColors.ConnectionCircleTarget, radius = Dimens.WorkflowEditor.ConnectionCircleRadiusOuter.toPx(), center = Offset(endX, endY))
                drawCircle(color = Color.White, radius = Dimens.WorkflowEditor.ConnectionCircleRadiusInner.toPx(), center = Offset(endX, endY))
            }
        }
    }

    // 渲染所有可点击的删除按钮
    workflow.connections.forEach { connection ->
        val sourceNode = workflow.nodes.find { it.id == connection.sourceNodeId }
        val targetNode = workflow.nodes.find { it.id == connection.targetNodeId }
        if (sourceNode != null && targetNode != null) {
            // In world coordinates
            val nodeWidthPx = with(density) { Dimens.WorkflowEditor.NodeWidth.toPx() }
            val nodeHeightPx = with(density) { Dimens.WorkflowEditor.NodeHeight.toPx() }
            val startXWorld = with(density) { sourceNode.x.dp.toPx() } + nodeWidthPx + with(density) { Dimens.WorkflowEditor.ConnectionCircleRadiusOuter.toPx() }
            val startYWorld = with(density) { sourceNode.y.dp.toPx() } + nodeHeightPx / 2
            val endXWorld = with(density) { targetNode.x.dp.toPx() } - with(density) { Dimens.WorkflowEditor.ConnectionCircleRadiusOuter.toPx() }
            val endYWorld = with(density) { targetNode.y.dp.toPx() } + nodeHeightPx / 2
            val midXWorld = (startXWorld + endXWorld) / 2
            val midYWorld = (startYWorld + endYWorld) / 2

            val buttonSize = Dimens.WorkflowEditor.ConnectionDeleteButtonSize
            val buttonRadius = Dimens.WorkflowEditor.ConnectionDeleteButtonRadius

            Box(
                modifier = Modifier
                    // Use world coordinates directly, graphicsLayer will transform
                    .offset(
                        x = with(density) { midXWorld.toDp() } - buttonRadius,
                        y = with(density) { midYWorld.toDp() } - buttonRadius
                    )
                    .size(buttonSize)
            ) {
                IconButton(
                    onClick = { workflowViewModel.removeConnection(connection.id) },
                    modifier = Modifier.fillMaxSize(),
                    colors = IconButtonDefaults.iconButtonColors(containerColor = Dimens.WorkflowEditorColors.ConnectionDeleteButtonBackground)
                ) {
                    Icon(Icons.Default.Remove, contentDescription = "删除连接", tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}