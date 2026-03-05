package com.carlos.autoflow.workflow.ui

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.derivedStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
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
    val worldGridStepPx = with(density) { Dimens.WorkflowEditor.GridSize.toPx() }
    val minGridLinePx = with(density) { 20.dp.toPx() }
    val gridStepWorld by remember(canvasState.scale, canvasState.isAdaptiveGridEnabled, worldGridStepPx, minGridLinePx) {
        derivedStateOf {
            if (!canvasState.isAdaptiveGridEnabled) return@derivedStateOf worldGridStepPx
            val scaledGridStepPx = worldGridStepPx * canvasState.scale
            val skipFactor = if (scaledGridStepPx < minGridLinePx) {
                ceil(minGridLinePx / scaledGridStepPx).toInt().coerceAtLeast(1)
            } else {
                1
            }
            worldGridStepPx * skipFactor
        }
    }

    Canvas(modifier = modifier) {
        if (gridStepWorld <= 0f || !gridStepWorld.isFinite()) return@Canvas

        val screenWidth = size.width
        val screenHeight = size.height
        val strokeWidth = 1.dp.toPx()

        var worldX = floor(canvasState.screenToWorldX(0f) / gridStepWorld) * gridStepWorld
        while (true) {
            val x = canvasState.worldToScreenX(worldX)
            if (x > screenWidth) break
            drawLine(
                color = Dimens.WorkflowEditorColors.GridLine,
                start = Offset(x, 0f),
                end = Offset(x, screenHeight),
                strokeWidth = strokeWidth
            )
            worldX += gridStepWorld
        }

        var worldY = floor(canvasState.screenToWorldY(0f) / gridStepWorld) * gridStepWorld
        while (true) {
            val y = canvasState.worldToScreenY(worldY)
            if (y > screenHeight) break
            drawLine(
                color = Dimens.WorkflowEditorColors.GridLine,
                start = Offset(0f, y),
                end = Offset(screenWidth, y),
                strokeWidth = strokeWidth
            )
            worldY += gridStepWorld
        }
    }
}
