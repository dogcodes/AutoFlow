package com.carlos.autoflow.workflow.ui

import android.content.Intent
import android.util.Log
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
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.carlos.autoflow.ui.theme.Dimens
import com.carlos.autoflow.workflow.ui.WorkflowStatusMessages
import com.carlos.autoflow.workflow.ui.WorkflowControls
import com.carlos.autoflow.workflow.ui.WorkflowNodes
import com.carlos.autoflow.workflow.ui.WorkflowConnections
import com.carlos.autoflow.workflow.ui.CanvasBackground
import com.carlos.autoflow.workflow.ui.ImportDialog
import com.carlos.autoflow.workflow.ui.ExportDialog
import com.carlos.autoflow.workflow.ui.ExecuteResultDialog
import com.carlos.autoflow.workflow.ui.NodeConfigDialog
import com.carlos.autoflow.workflow.ui.AccessibilityExamplesDialog
import com.carlos.autoflow.workflow.models.NodeType
import com.carlos.autoflow.workflow.models.WorkflowNode
import com.carlos.autoflow.workflow.viewmodel.CanvasViewModel
import com.carlos.autoflow.workflow.viewmodel.WorkflowViewModel
import com.carlos.autoflow.accessibility.AccessibilityPermissionCard
import com.carlos.autoflow.workflow.ui.ExecutionStatusOverlay
import com.carlos.autoflow.billing.ui.LicenseDialog
import com.carlos.autoflow.billing.BannerAdView
import com.carlos.autoflow.billing.FeatureManager
import com.carlos.autoflow.demo.DemoAppActivity
import com.carlos.autoflow.ui.SideDrawer
import com.carlos.autoflow.ui.screens.HistoryScreen
import com.carlos.autoflow.ui.screens.SettingsScreen
import com.carlos.autoflow.ui.screens.AboutScreen
import com.carlos.autoflow.demo.DemoAppScreen
import com.carlos.autoflow.recorder.ui.RecordingControlPanel
import kotlinx.coroutines.delay
import kotlin.math.floor
import kotlin.math.pow
import com.carlos.autoflow.monitor.NodeMonitorDemo
import com.carlos.autoflow.BuildConfig

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkflowEditor(
    workflowViewModel: WorkflowViewModel = viewModel(),
    canvasViewModel: CanvasViewModel = viewModel()
) {
    val context = LocalContext.current
    val featureManager = remember { FeatureManager(context) }
    val workflow by workflowViewModel.workflow.collectAsState()
    val canvasState by canvasViewModel.canvasState.collectAsState()
    val connectingNodeId by workflowViewModel.connectingNodeId.collectAsState()
    val selectedNodeId by workflowViewModel.selectedNodeId.collectAsState()
    val executingNodes by workflowViewModel.executingNodes.collectAsState()
    val density = LocalDensity.current

    // 对话框状态
    var showImportDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var showExecuteDialog by remember { mutableStateOf(false) }
    var showAccessibilityExamples by remember { mutableStateOf(false) }
    var showLicenseDialog by remember { mutableStateOf(false) }
    var showSideDrawer by remember { mutableStateOf(false) }
    var currentScreen by remember { mutableStateOf("workflow") }
    var executeResult by remember { mutableStateOf("") }
    var importError by remember { mutableStateOf<String?>(null) }
    var configNode by remember { mutableStateOf<WorkflowNode?>(null) }
    var showJsonExamplesDialog by remember { mutableStateOf(false) } // 新增状态变量

    Box(modifier = Modifier.fillMaxSize()) {
        when (currentScreen) {
            "workflow" -> {
                CanvasBackground(
                    modifier = Modifier.fillMaxSize(),
                    canvasState = canvasState
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .onSizeChanged { size ->
                            canvasViewModel.updateViewportSize(
                                width = size.width.toFloat(),
                                height = size.height.toFloat()
                            )
                        }
                        .pointerInput(Unit) {
                            detectTransformGestures { centroid, pan, zoom, _ ->
                                canvasViewModel.applyTransform(
                                    anchorX = centroid.x,
                                    anchorY = centroid.y,
                                    panX = pan.x,
                                    panY = pan.y,
                                    zoomFactor = zoom
                                )
                            }
                        }
                        .graphicsLayer(
                            transformOrigin = TransformOrigin(0f, 0f),
                            scaleX = canvasState.scale,
                            scaleY = canvasState.scale,
                            translationX = canvasState.offsetX,
                            translationY = canvasState.offsetY
                        )
                ) {
                    WorkflowConnections(
                        workflow = workflow,
                        workflowViewModel = workflowViewModel,
                        canvasState = canvasState
                    )

                    WorkflowNodes(
                        workflow = workflow,
                        selectedNodeId = selectedNodeId,
                        connectingNodeId = connectingNodeId,
                        canvasScale = canvasState.scale,
                        workflowViewModel = workflowViewModel,
                        onShowNodeConfig = { node -> configNode = node }
                    )
                }

                // 无障碍权限状态卡片
                AccessibilityPermissionCard(
                    modifier = Modifier.align(Alignment.TopCenter)
                )

                WorkflowControls(
                    workflowViewModel = workflowViewModel,
                    canvasViewModel = canvasViewModel,
                    onShowImportDialog = { showImportDialog = true },
                    onShowExportDialog = { showExportDialog = true },
                    onShowExecuteDialog = {
                        showExecuteDialog = true
                        workflowViewModel.executeWorkflow(context) { result ->
                            executeResult = result
                        }
                    },
                    onShowAccessibilityExamples = { showAccessibilityExamples = true },
                    onShowJsonExamplesDialog = { showJsonExamplesDialog = true }, // 传递新的lambda
                    onShowLicenseDialog = { showLicenseDialog = true },
                    onShowSideDrawer = { showSideDrawer = true },
                    isDebug = BuildConfig.DEBUG
                )

                WorkflowStatusMessages(
                    workflow = workflow,
                    connectingNodeId = connectingNodeId,
                    selectedNodeId = selectedNodeId,
                    workflowViewModel = workflowViewModel
                )
                
                // 执行状态覆盖层
                ExecutionStatusOverlay(
                    executingNodes = executingNodes,
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
                
                // 底部广告 (仅免费版)
                if (featureManager.shouldShowAds()) {
                    BannerAdView(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(16.dp)
                    )
                }
            }
            "monitor" -> {
                Box(modifier = Modifier.fillMaxSize()) {
                    NodeMonitorDemo()
                    
                    // 侧滑栏按钮
                    FloatingActionButton(
                        onClick = { showSideDrawer = true },
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(16.dp)
                            .size(48.dp),
                        containerColor = MaterialTheme.colorScheme.secondary
                    ) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "打开侧滑栏",
                            tint = Color.White
                        )
                    }
                }
            }
            "demo" -> {
                val intent = Intent(context, DemoAppActivity::class.java)
                context.startActivity(intent)
                currentScreen = "workflow" // Reset to workflow after launching
            }
            "recording" -> {
                Box(modifier = Modifier.fillMaxSize()) {
                    RecordingControlPanel(
                        onWorkflowGenerated = { json ->
                            workflowViewModel.importFromJson(json)
                            currentScreen = "workflow"
                        },
                        modifier = Modifier.fillMaxSize().padding(16.dp)
                    )
                    
                    // 侧滑栏按钮
                    FloatingActionButton(
                        onClick = { showSideDrawer = true },
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(16.dp)
                            .size(48.dp),
                        containerColor = MaterialTheme.colorScheme.secondary
                    ) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "打开侧滑栏",
                            tint = Color.White
                        )
                    }
                }
            }
            "history" -> {
                Box(modifier = Modifier.fillMaxSize()) {
                    HistoryScreen()
                    
                    // 侧滑栏按钮
                    FloatingActionButton(
                        onClick = { showSideDrawer = true },
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(16.dp)
                            .size(48.dp),
                        containerColor = MaterialTheme.colorScheme.secondary
                    ) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "打开侧滑栏",
                            tint = Color.White
                        )
                    }
                }
            }
            "settings" -> {
                Box(modifier = Modifier.fillMaxSize()) {
                    SettingsScreen()
                    
                    // 侧滑栏按钮
                    FloatingActionButton(
                        onClick = { showSideDrawer = true },
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(16.dp)
                            .size(48.dp),
                        containerColor = MaterialTheme.colorScheme.secondary
                    ) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "打开侧滑栏",
                            tint = Color.White
                        )
                    }
                }
            }
            "about" -> {
                Box(modifier = Modifier.fillMaxSize()) {
                    AboutScreen()
                    
                    // 侧滑栏按钮
                    FloatingActionButton(
                        onClick = { showSideDrawer = true },
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(16.dp)
                            .size(48.dp),
                        containerColor = MaterialTheme.colorScheme.secondary
                    ) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "打开侧滑栏",
                            tint = Color.White
                        )
                    }
                }
            }
        }
        
        // 侧滑栏
        SideDrawer(
            isVisible = showSideDrawer,
            onDismiss = { showSideDrawer = false },
            onItemSelected = { screen ->
                if (screen == "demo") {
                    val intent = Intent(context, DemoAppActivity::class.java)
                    context.startActivity(intent)
                    currentScreen = "workflow" // Reset to workflow after launching
                    // Do NOT dismiss drawer here, it will remain open when returning
                } else {
                    currentScreen = screen
                    showSideDrawer = false // Dismiss drawer for internal screen changes
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }

    // 对话框处理
    if (showImportDialog) {
        ImportDialog(
            onDismiss = { 
                showImportDialog = false
                importError = null
            },
            onImport = { json ->
                val success = workflowViewModel.importFromJson(json)
                if (success) {
                    showImportDialog = false
                    importError = null
                } else {
                    importError = "JSON格式错误"
                }
            },
            onImportFromUrl = { url ->
                workflowViewModel.loadFromUrl(url) { success, error ->
                    if (success) {
                        showImportDialog = false
                        importError = null
                    } else {
                        importError = error ?: "未知错误"
                    }
                }
            },
            errorMessage = importError
        )
    }

    if (showExportDialog) {
        ExportDialog(
            json = workflowViewModel.exportToJson(),
            onDismiss = { showExportDialog = false }
        )
    }

    if (showExecuteDialog) {
        ExecuteResultDialog(
            result = executeResult,
            onDismiss = { showExecuteDialog = false }
        )
    }

    // 节点配置对话框
    configNode?.let { node ->
        NodeConfigDialog(
            node = node,
            onDismiss = { configNode = null },
            onSave = { config ->
                workflowViewModel.updateNodeConfig(node.id, config)
                configNode = null
            }
        )
    }
    
    // 无障碍示例对话框
    if (showAccessibilityExamples) {
        AccessibilityExamplesDialog(
            onDismiss = { showAccessibilityExamples = false },
            onExampleSelected = { example ->
                workflowViewModel.loadWorkflow(example)
                showAccessibilityExamples = false
            }
        )
    }

    // 无障碍JSON示例对话框
    if (showJsonExamplesDialog) { // 新增的对话框调用
        JsonExamplesDialog(
            workflowViewModel = workflowViewModel,
            onDismiss = { showJsonExamplesDialog = false }
        )
    }
    
    // 许可证管理对话框
    if (showLicenseDialog) {
        LicenseDialog(
            onDismiss = { showLicenseDialog = false }
        )
    }
}
