package com.carlos.autoflow.workflow.ui

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.carlos.autoflow.ui.theme.Dimens
import com.carlos.autoflow.workflow.viewmodel.CanvasState
import kotlin.math.floor

@Composable
fun CanvasBackground(
    modifier: Modifier = Modifier,
    canvasState: CanvasState
) {
    Canvas(modifier = modifier) {
        // 绘制网格
        val gridSizePx = Dimens.WorkflowEditor.GridSize.toPx()
        val worldLeft = -canvasState.offsetX / canvasState.scale
        val worldTop = -canvasState.offsetY / canvasState.scale
        val worldRight = worldLeft + size.width / canvasState.scale
        val worldBottom = worldTop + size.height / canvasState.scale

        // 绘制垂直线
        val firstVerticalLineX = (floor(worldLeft / gridSizePx) * gridSizePx)
        var currentX = firstVerticalLineX
        while (currentX <= worldRight) {
            drawLine(
                color = Dimens.WorkflowEditorColors.GridLine,
                start = Offset(currentX, worldTop),
                end = Offset(currentX, worldBottom),
                strokeWidth = (1.dp.toPx()) / canvasState.scale
            )
            currentX += gridSizePx
        }

        // 绘制水平线
        val firstHorizontalLineY = (floor(worldTop / gridSizePx) * gridSizePx)
        var currentY = firstHorizontalLineY
        while (currentY <= worldBottom) {
            drawLine(
                color = Dimens.WorkflowEditorColors.GridLine,
                start = Offset(worldLeft, currentY),
                end = Offset(worldRight, currentY),
                strokeWidth = (1.dp.toPx()) / canvasState.scale
            )
            currentY += gridSizePx
        }
    }
}