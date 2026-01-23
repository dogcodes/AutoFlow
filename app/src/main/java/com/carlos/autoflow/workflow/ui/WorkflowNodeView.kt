package com.carlos.autoflow.workflow.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
    onDoubleClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var isDragging by remember { mutableStateOf(false) }
    val density = LocalDensity.current

    Card(
        modifier = modifier
            .size(160.dp, 100.dp)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { isDragging = true },
                    onDragEnd = { isDragging = false }
                ) { _, dragAmount ->
                    // 将像素转换为dp
                    val deltaXDp = with(density) { dragAmount.x.toDp().value }
                    val deltaYDp = with(density) { dragAmount.y.toDp().value }
                    onMove(deltaXDp, deltaYDp)
                }
            }
            .combinedClickable(
                onClick = onSelect,
                onDoubleClick = onDoubleClick
            ),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isConnecting -> Color(0xFFFFEB3B).copy(alpha = 0.3f) // 黄色连接状态
                isSelected -> Color(0xFFE3F2FD) // 蓝色选中状态
                else -> Color.White
            }
        ),
        border = when {
            isConnecting -> BorderStroke(3.dp, Color(0xFFFF9800)) // 橙色连接边框
            isSelected -> BorderStroke(2.dp, Color(0xFF1976D2)) // 蓝色选中边框
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
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(20.dp)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "删除",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
            
            Text(
                text = node.title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 2
            )
        }
    }
}

private fun getNodeIcon(type: NodeType): ImageVector = when (type) {
    NodeType.START -> Icons.Default.PlayArrow
    NodeType.END -> Icons.Default.Stop
    NodeType.HTTP_REQUEST -> Icons.Default.Send
    NodeType.DATA_TRANSFORM -> Icons.Default.Build
    NodeType.CONDITION -> Icons.Default.Info
    NodeType.LOOP -> Icons.Default.Refresh
    NodeType.DELAY -> Icons.Default.DateRange
    NodeType.SCRIPT -> Icons.Default.Edit
}
