package com.carlos.autoflow.workflow.ui

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.carlos.autoflow.ui.theme.Dimens
import com.carlos.autoflow.workflow.viewmodel.CanvasState
import androidx.compose.ui.platform.LocalDensity
import kotlin.math.ceil
import kotlin.math.floor

@Composable
fun CanvasBackground(
    modifier: Modifier = Modifier,
    canvasState: CanvasState
) {
    val density = LocalDensity.current
    Canvas(modifier = modifier) {
        val worldGridStepPx = Dimens.WorkflowEditor.GridSize.toPx()
        val scaledGridStepPx = worldGridStepPx * canvasState.scale
        val minGridLinePx = with(density) { 20.dp.toPx() }
        val skipFactor = if (canvasState.isAdaptiveGridEnabled) {
            if (scaledGridStepPx < minGridLinePx) {
                ceil(minGridLinePx / scaledGridStepPx).toInt()
            } else {
                1
            }
        } else {
            1
        }
        val stepPx = scaledGridStepPx * skipFactor
        if (stepPx <= 0f || !stepPx.isFinite()) return@Canvas

        val screenWidth = size.width
        val screenHeight = size.height
        val strokeWidth = 1.dp.toPx()

        val firstX = floor((-canvasState.offsetX) / stepPx) * stepPx + canvasState.offsetX
        var x = firstX
        while (x <= screenWidth) {
            drawLine(
                color = Dimens.WorkflowEditorColors.GridLine,
                start = Offset(x, 0f),
                end = Offset(x, screenHeight),
                strokeWidth = strokeWidth
            )
            x += stepPx
        }

        val firstY = floor((-canvasState.offsetY) / stepPx) * stepPx + canvasState.offsetY
        var y = firstY
        while (y <= screenHeight) {
            drawLine(
                color = Dimens.WorkflowEditorColors.GridLine,
                start = Offset(0f, y),
                end = Offset(screenWidth, y),
                strokeWidth = strokeWidth
            )
            y += stepPx
        }
    }
}
