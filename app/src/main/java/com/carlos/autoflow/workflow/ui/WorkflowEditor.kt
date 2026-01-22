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
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
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
import com.carlos.autoflow.ui.theme.Dimens
import com.carlos.autoflow.workflow.ui.WorkflowStatusMessages
import com.carlos.autoflow.workflow.ui.WorkflowControls
import com.carlos.autoflow.workflow.ui.WorkflowNodes
import com.carlos.autoflow.workflow.ui.WorkflowConnections
import com.carlos.autoflow.workflow.ui.CanvasBackground
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
        CanvasBackground(
            modifier = Modifier.fillMaxSize(),
            canvasState = canvasState
        )

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

            WorkflowConnections(
                workflow = workflow,
                workflowViewModel = workflowViewModel
            )

            WorkflowNodes(
                workflow = workflow,
                selectedNodeId = selectedNodeId,
                connectingNodeId = connectingNodeId,
                workflowViewModel = workflowViewModel
            )
        }



        WorkflowControls(
            workflowViewModel = workflowViewModel,
            canvasViewModel = canvasViewModel
        )


        WorkflowStatusMessages(
            workflow = workflow,
            connectingNodeId = connectingNodeId,
            selectedNodeId = selectedNodeId,
            workflowViewModel = workflowViewModel
        )
    }
}
