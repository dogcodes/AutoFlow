package com.carlos.autoflow.workflow.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.carlos.autoflow.ui.theme.Dimens
import com.carlos.autoflow.workflow.viewmodel.CanvasViewModel
import com.carlos.autoflow.workflow.viewmodel.WorkflowViewModel

import androidx.compose.runtime.collectAsState

@Composable
fun WorkflowControls(
    workflowViewModel: WorkflowViewModel = viewModel(),
    canvasViewModel: CanvasViewModel = viewModel()
) {
    // Collect canvasState to get the current scale
    val canvasState by canvasViewModel.canvasState.collectAsState()

    // 控制节点选择面板的显示/隐藏状态
    var isPanelVisible by remember { mutableStateOf(false) }

    androidx.compose.foundation.layout.Box(modifier = Modifier.fillMaxSize()) {
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
                .padding(Dimens.WorkflowEditor.DefaultPadding),
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
                .padding(Dimens.WorkflowEditor.DefaultPadding)
        ) {
            FloatingActionButton(
                onClick = { canvasViewModel.zoomIn() },
                modifier = Modifier.size(48.dp),
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "放大")
            }

            // Zoom percentage display
            Spacer(modifier = Modifier.height(Dimens.WorkflowEditor.SmallPadding))
            Text(
                text = "${(canvasState.scale * 100).toInt()}%",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(Dimens.WorkflowEditor.SmallPadding))

            FloatingActionButton(
                onClick = { canvasViewModel.zoomOut() },
                modifier = Modifier.size(48.dp),
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Remove, contentDescription = "缩小")
            }

            Spacer(modifier = Modifier.height(Dimens.WorkflowEditor.SmallPadding))

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
    }
}