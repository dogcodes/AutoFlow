package com.carlos.autoflow.workflow.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.carlos.autoflow.ui.theme.Dimens
import com.carlos.autoflow.workflow.viewmodel.CanvasViewModel
import com.carlos.autoflow.workflow.viewmodel.WorkflowViewModel

@Composable
fun WorkflowControls(
    workflowViewModel: WorkflowViewModel = viewModel(),
    canvasViewModel: CanvasViewModel = viewModel(),
    onShowImportDialog: () -> Unit = {},
    onShowExportDialog: () -> Unit = {},
    onShowExecuteDialog: () -> Unit = {}
) {
    val canvasState by canvasViewModel.canvasState.collectAsState()
    var isPanelVisible by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        // 节点选择面板
        AnimatedVisibility(
            visible = isPanelVisible,
            enter = slideInHorizontally(),
            exit = slideOutHorizontally(),
            modifier = Modifier.align(Alignment.CenterStart)
        ) {
            NodeSelectionPanel(
                onNodeSelected = { nodeType ->
                    workflowViewModel.addNode(nodeType, 100f, 100f)
                },
                onClose = { isPanelVisible = false }
            )
        }
        
        // 左上角：节点面板按钮
        AnimatedVisibility(
            visible = !isPanelVisible,
            enter = slideInHorizontally(),
            exit = slideOutHorizontally(),
            modifier = Modifier.align(Alignment.TopStart)
        ) {
            FloatingActionButton(
                onClick = { isPanelVisible = true },
                modifier = Modifier.padding(Dimens.WorkflowEditor.DefaultPadding),
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "打开节点面板",
                    tint = Color.White
                )
            }
        }

        // 右上角：功能菜单
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(Dimens.WorkflowEditor.DefaultPadding)
        ) {
            var showMenu by remember { mutableStateOf(false) }
            
            FloatingActionButton(
                onClick = { showMenu = true },
                modifier = Modifier.size(48.dp),
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.MoreVert, "菜单", tint = Color.White)
            }
            
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false }
            ) {
                DropdownMenuItem(
                    text = { Text("📥 导入工作流") },
                    onClick = {
                        showMenu = false
                        onShowImportDialog()
                    }
                )
                DropdownMenuItem(
                    text = { Text("💡 加载示例") },
                    onClick = {
                        showMenu = false
                        workflowViewModel.loadSampleWorkflow()
                    }
                )
                DropdownMenuItem(
                    text = { Text("📤 导出工作流") },
                    onClick = {
                        showMenu = false
                        onShowExportDialog()
                    }
                )
                DropdownMenuItem(
                    text = { Text("▶️ 执行工作流") },
                    onClick = {
                        showMenu = false
                        onShowExecuteDialog()
                    }
                )
                DropdownMenuItem(
                    text = { 
                        Text(if (canvasState.showFlowAnimation) "🔄 关闭流动动画" else "🔄 开启流动动画") 
                    },
                    onClick = {
                        showMenu = false
                        canvasViewModel.toggleFlowAnimation()
                    }
                )
            }
        }

        // 右下角：缩放控制
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
