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
        val gridSizePx = Dimens.WorkflowEditor.GridSize.toPx()

        // Calculate the effective grid size on screen
        val effectiveGridSizeScreen = gridSizePx * canvasState.scale

        // Determine the visible screen area
        val screenWidth = size.width
        val screenHeight = size.height

        // Calculate the offset for the grid lines based on canvasState.offsetX/Y
        // This makes the grid appear to move with the canvas.
        val gridOffsetX = canvasState.offsetX % effectiveGridSizeScreen
        val gridOffsetY = canvasState.offsetY % effectiveGridSizeScreen


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
            currentXScreen += effectiveGridSizeScreen
        }

        // Draw vertical lines for negative offset
        currentXScreen = gridOffsetX - effectiveGridSizeScreen
        while (currentXScreen >= 0f) {
            drawLine(
                color = Dimens.WorkflowEditorColors.GridLine,
                start = Offset(currentXScreen, 0f),
                end = Offset(currentXScreen, screenHeight),
                strokeWidth = (1.dp.toPx())
            )
            currentXScreen -= effectiveGridSizeScreen
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
            currentYScreen += effectiveGridSizeScreen
        }

        // Draw horizontal lines for negative offset
        currentYScreen = gridOffsetY - effectiveGridSizeScreen
        while (currentYScreen >= 0f) {
            drawLine(
                color = Dimens.WorkflowEditorColors.GridLine,
                start = Offset(0f, currentYScreen),
                end = Offset(screenWidth, currentYScreen),
                strokeWidth = (1.dp.toPx())
            )
            currentYScreen -= effectiveGridSizeScreen
        }
    }
}