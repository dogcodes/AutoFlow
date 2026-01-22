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
import kotlin.math.roundToInt

@Composable
fun CanvasBackground(
    modifier: Modifier = Modifier,
    canvasState: CanvasState
) {
    val density = LocalDensity.current
    Canvas(modifier = modifier) {
        val gridSizePx = Dimens.WorkflowEditor.GridSize.toPx()

        // Calculate the effective grid size on screen
        val effectiveGridSizeScreen = gridSizePx * canvasState.scale

        // Determine the visible screen area
        val screenWidth = size.width
        val screenHeight = size.height

        // Minimum pixel distance between grid lines to prevent overcrowding
        val minGridLinePx = with(density) { 20.dp.toPx() } // Example: 20dp minimum distance

        // Calculate skip factor to ensure lines are not too close
        val skipFactor = if (canvasState.isAdaptiveGridEnabled) {
            if (effectiveGridSizeScreen < minGridLinePx) {
                ceil(minGridLinePx / effectiveGridSizeScreen).toInt()
            } else {
                1
            }
        } else {
            1 // If adaptive grid is disabled, always draw every grid line
        }
        val actualGridStep = effectiveGridSizeScreen * skipFactor

        // Calculate the offset for the grid lines based on canvasState.offsetX/Y
        // This makes the grid appear to move with the canvas.
        val gridOffsetX = canvasState.offsetX % actualGridStep
        val gridOffsetY = canvasState.offsetY % actualGridStep


        // Draw vertical lines
        var currentXScreen = gridOffsetX
        while (currentXScreen < screenWidth) {
            if (currentXScreen >= 0f) { // Only draw if line is within screen bounds
                drawLine(
                    color = Dimens.WorkflowEditorColors.GridLine,
                    start = Offset(currentXScreen, 0f),
                    end = Offset(currentXScreen, screenHeight),
                    strokeWidth = (1.dp.toPx())
                )
            }
            currentXScreen += actualGridStep
        }

        // Draw vertical lines for negative offset
        currentXScreen = gridOffsetX - actualGridStep
        while (currentXScreen >= -actualGridStep) { // Draw one extra line off-screen to handle edge cases
            drawLine(
                color = Dimens.WorkflowEditorColors.GridLine,
                start = Offset(currentXScreen, 0f),
                end = Offset(currentXScreen, screenHeight),
                strokeWidth = (1.dp.toPx())
            )
            currentXScreen -= actualGridStep
        }


        // Draw horizontal lines
        var currentYScreen = gridOffsetY
        while (currentYScreen < screenHeight) {
            if (currentYScreen >= 0f) { // Only draw if line is within screen bounds
                drawLine(
                    color = Dimens.WorkflowEditorColors.GridLine,
                    start = Offset(0f, currentYScreen),
                    end = Offset(screenWidth, currentYScreen),
                    strokeWidth = (1.dp.toPx())
                )
            }
            currentYScreen += actualGridStep
        }

        // Draw horizontal lines for negative offset
        currentYScreen = gridOffsetY - actualGridStep
        while (currentYScreen >= -actualGridStep) { // Draw one extra line off-screen to handle edge cases
            drawLine(
                color = Dimens.WorkflowEditorColors.GridLine,
                start = Offset(0f, currentYScreen),
                end = Offset(screenWidth, currentYScreen),
                strokeWidth = (1.dp.toPx())
            )
            currentYScreen -= actualGridStep
        }
    }
}