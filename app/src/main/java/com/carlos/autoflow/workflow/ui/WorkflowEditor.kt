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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
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
import com.carlos.autoflow.workflow.ui.JsonExamplesDialog
import com.carlos.autoflow.workflow.models.NodeType
import com.carlos.autoflow.workflow.models.WorkflowNode
import com.carlos.autoflow.workflow.viewmodel.CanvasViewModel
import com.carlos.autoflow.workflow.viewmodel.WorkflowViewModel
import com.carlos.autoflow.accessibility.AccessibilityPermissionCard
import com.carlos.autoflow.workflow.ui.ExecutionStatusOverlay
import com.carlos.autoflow.BuildConfig
import com.carlos.autoflow.billing.BannerAdView
import com.carlos.autoflow.license.FeatureManager
import com.carlos.autoflow.compliance.ComplianceConfig
import com.carlos.autoflow.demo.DemoAppActivity
import com.carlos.autoflow.ui.SideDrawer
import com.carlos.autoflow.ui.screens.HistoryActivity
import com.carlos.autoflow.ui.screens.SettingsActivity
import com.carlos.autoflow.ui.screens.AboutActivity
import com.carlos.autoflow.ui.screens.NodeMonitorActivity
import com.carlos.autoflow.ui.screens.LicenseActivity
import com.carlos.autoflow.demo.DemoAppScreen
import com.carlos.autoflow.recorder.ui.RecordingControlPanel
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.floor
import kotlin.math.pow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkflowEditor(
    workflowViewModel: WorkflowViewModel = viewModel(),
    canvasViewModel: CanvasViewModel = viewModel(),
    showSideDrawerButton: Boolean = true,
    onSaveWorkflow: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val featureManager = remember { FeatureManager(context, BuildConfig.FORCE_PREMIUM) }
    val workflow by workflowViewModel.workflow.collectAsState()
    val canvasState by canvasViewModel.canvasState.collectAsState()
    val connectingNodeId by workflowViewModel.connectingNodeId.collectAsState()
    val selectedNodeId by workflowViewModel.selectedNodeId.collectAsState()
    val executingNodes by workflowViewModel.executingNodes.collectAsState()

    // 对话框状态
    var showImportDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var showExecuteDialog by remember { mutableStateOf(false) }
    var showAccessibilityExamples by remember { mutableStateOf(false) }
    var showSideDrawer by remember { mutableStateOf(false) }
    var currentScreen by remember { mutableStateOf("workflow") }
    var executeResult by remember { mutableStateOf("") }
    var importError by remember { mutableStateOf<String?>(null) }
    var configNode by remember { mutableStateOf<WorkflowNode?>(null) }
    var showJsonExamplesDialog by remember { mutableStateOf(false) } // 新增状态变量
    var showPremiumDialog by remember { mutableStateOf(false) }
    var showTimeAbnormalDialog by remember { mutableStateOf(false) }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME && showTimeAbnormalDialog) {
                if (!featureManager.isSystemTimeAbnormal()) showTimeAbnormalDialog = false
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Box(modifier = modifier.fillMaxSize()) {
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

                if (!ComplianceConfig.isComplianceMode) {
                    AccessibilityPermissionCard(
                        modifier = Modifier.align(Alignment.TopCenter)
                    )
                }

                WorkflowControls(
                    workflowViewModel = workflowViewModel,
                    canvasViewModel = canvasViewModel,
                    onShowImportDialog = { showImportDialog = true },
                    onShowExportDialog = {
                        if (!featureManager.canUseAdvancedNodes()) showPremiumDialog = true
                        else showExportDialog = true
                    },
                    onShowExecuteDialog = {
                        workflowViewModel.executeWorkflow(context) { result ->
                            when (result) {
                                "REQUIRE_PREMIUM" -> showPremiumDialog = true
                                "SYSTEM_TIME_INVALID" -> showTimeAbnormalDialog = true
                                else -> { showExecuteDialog = true; executeResult = result }
                            }
                        }
                    },
                    onShowAccessibilityExamples = { showAccessibilityExamples = true },
                    onShowJsonExamplesDialog = { showJsonExamplesDialog = true },
                    onShowAdDebug = {
                        runCatching {
                            context.startActivity(
                                Intent().setClassName(
                                    context,
                                    "com.carlos.autoflow.platform.ad.AdDebugActivity"
                                )
                            )
                        }.onFailure { throwable ->
                            Log.w("WorkflowEditor", "广告测试页面不可用", throwable)
                        }
                    },
                    onSaveWorkflow = onSaveWorkflow,
                    onOpenLicense = {
                        context.startActivity(Intent(context, LicenseActivity::class.java))
                    },
                    onShowSideDrawer = { showSideDrawer = true },
                    showSideDrawerButton = showSideDrawerButton,
                    isDebug = BuildConfig.DEBUG
                )

                WorkflowStatusMessages(
                    workflow = workflow,
                    connectingNodeId = connectingNodeId,
                    selectedNodeId = selectedNodeId,
                    workflowViewModel = workflowViewModel
                )

                ExecutionStatusOverlay(
                    executingNodes = executingNodes,
                    modifier = Modifier.align(Alignment.BottomCenter)
                )

                if (featureManager.shouldShowAds()) {
                    BannerAdView(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(16.dp)
                    )
                }
            }
            "demo" -> {
                context.startActivity(Intent(context, DemoAppActivity::class.java))
                currentScreen = "workflow"
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

                    if (!ComplianceConfig.isComplianceMode) {
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
        }
        
        // 侧滑栏
        SideDrawer(
            isVisible = showSideDrawer,
            onDismiss = { showSideDrawer = false },
            onItemSelected = { screen ->
                when (screen) {
                    "workflow" -> currentScreen = "workflow"
                    "recording" -> currentScreen = "recording"
                    "demo" -> {
                        context.startActivity(Intent(context, DemoAppActivity::class.java))
                        currentScreen = "workflow"
                    }
                    "monitor" -> context.startActivity(Intent(context, NodeMonitorActivity::class.java))
                    "history" -> context.startActivity(Intent(context, HistoryActivity::class.java))
                    "settings" -> context.startActivity(Intent(context, SettingsActivity::class.java))
                    "about" -> context.startActivity(Intent(context, AboutActivity::class.java))
                }
                showSideDrawer = false
            },
            modifier = Modifier.fillMaxSize()
        )
    }

    // 对话框处理
    if (showTimeAbnormalDialog) {
        val date = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showTimeAbnormalDialog = false },
            title = { androidx.compose.material3.Text("系统时间异常") },
            text = { androidx.compose.material3.Text("系统日期异常（$date），请在系统设置中校准后重试。") },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = { showTimeAbnormalDialog = false }) {
                    androidx.compose.material3.Text("知道了")
                }
            }
        )
    }

    if (showPremiumDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showPremiumDialog = false },
            title = { androidx.compose.material3.Text("需要激活使用时长") },
            text = { androidx.compose.material3.Text("执行工作流需要激活使用时长，请前往激活页面。") },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = {
                    showPremiumDialog = false
                    context.startActivity(android.content.Intent(context, LicenseActivity::class.java))
                }) { androidx.compose.material3.Text("去激活") }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { showPremiumDialog = false }) {
                    androidx.compose.material3.Text("取消")
                }
            }
        )
    }

    if (showImportDialog) {
        ImportDialog(
            isDebug = BuildConfig.DEBUG,
            onDismiss = { 
                showImportDialog = false
                importError = null
            },
            onImportAfw = { afw ->
                val success = workflowViewModel.importFromText(afw)
                if (success) {
                    showImportDialog = false
                    importError = null
                } else {
                    importError = "AFW格式错误"
                }
            },
            onImportJson = { json ->
                val success = workflowViewModel.importFromText(json)
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
            isDebug = BuildConfig.DEBUG,
            afw = workflowViewModel.exportToAfw(),
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
    
}
