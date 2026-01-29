package com.carlos.autoflow.monitor

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.WindowManager
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import com.carlos.autoflow.monitor.NodeMonitorAccessibilityService
import com.carlos.autoflow.ui.theme.AutoFlowTheme
import com.carlos.autoflow.utils.PermissionUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class NodeMonitorService : Service() {
    
    private var windowManager: WindowManager? = null
    private var floatingView: ComposeView? = null
    private var overlayView: ComposeView? = null
    private var isMonitoring by mutableStateOf(false)
    private var currentNodes by mutableStateOf<List<NodeInfo>>(emptyList())
    private var selectedNode by mutableStateOf<NodeInfo?>(null)
    private var showSidePanel by mutableStateOf(false)
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    
    companion object {
        private var instance: NodeMonitorService? = null
        
        fun start(context: Context) {
            if (!PermissionUtils.canDrawOverlays(context)) {
                PermissionUtils.requestOverlayPermission(context, "请授予悬浮窗权限以使用节点监控器")
                return
            }
            context.startService(Intent(context, NodeMonitorService::class.java))
        }
        
        fun canDrawOverlays(context: Context) = PermissionUtils.canDrawOverlays(context)
        fun requestOverlayPermission(context: Context) = PermissionUtils.requestOverlayPermission(context)
        
        fun stop(context: Context) {
            context.stopService(Intent(context, NodeMonitorService::class.java))
        }
    }
    
    override fun onCreate() {
        super.onCreate()
        try {
            instance = this
            windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
            createFloatingButton()
            createOverlayView()
        } catch (e: Exception) {
            e.printStackTrace()
            stopSelf()
        }
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        floatingView?.let { windowManager?.removeView(it) }
        overlayView?.let { windowManager?.removeView(it) }
        instance = null
    }
    
    private fun createFloatingButton() {
        try {
            floatingView = ComposeView(this).apply {
                setContent {
                    AutoFlowTheme {
                        FloatingMonitorButton(
                            isMonitoring = isMonitoring,
                            nodeCount = currentNodes.size,
                            onToggleMonitoring = { toggleMonitoring() },
                            onShowSidePanel = { showSidePanel = true }
                        )
                    }
                }
            }
            
            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                } else {
                    WindowManager.LayoutParams.TYPE_PHONE
                },
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.TOP or Gravity.END
                x = 20
                y = 100
            }
            
            windowManager?.addView(floatingView, params)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    private fun createOverlayView() {
        overlayView = ComposeView(this).apply {
            setContent {
                AutoFlowTheme {
                    if (showSidePanel) {
                        NodeSidePanel(
                            nodes = currentNodes,
                            selectedNode = selectedNode,
                            onNodeSelected = { selectedNode = it },
                            onDismiss = { showSidePanel = false }
                        )
                    }
                }
            }
        }
        
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                WindowManager.LayoutParams.TYPE_PHONE
            },
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )
        
        windowManager?.addView(overlayView, params)
    }
    
    private fun toggleMonitoring() {
        isMonitoring = !isMonitoring
        if (isMonitoring) {
            startMonitoring()
        } else {
            stopMonitoring()
        }
    }
    
    private fun startMonitoring() {
        val service = NodeMonitorAccessibilityService.getInstance()
        if (service == null) {
            Toast.makeText(this, "请先启用Monitor无障碍服务", Toast.LENGTH_SHORT).show()
            isMonitoring = false
            return
        }
        
        serviceScope.launch {
            while (isMonitoring) {
                currentNodes = service.getCurrentPageNodes()
                delay(1000)
            }
        }
    }
    
    private fun stopMonitoring() {
        currentNodes = emptyList()
        selectedNode = null
    }
    
    private fun extractAllNodes(root: AccessibilityNodeInfo): List<NodeInfo> {
        val nodes = mutableListOf<NodeInfo>()
        extractNodesRecursively(root, nodes, 0)
        return nodes
    }
    
    private fun extractNodesRecursively(node: AccessibilityNodeInfo, nodes: MutableList<NodeInfo>, depth: Int) {
        nodes.add(NodeInfo.fromAccessibilityNode(node, depth))
        
        for (i in 0 until node.childCount) {
            node.getChild(i)?.let { child ->
                extractNodesRecursively(child, nodes, depth + 1)
            }
        }
    }
}

data class NodeInfo(
    val className: String,
    val text: String,
    val contentDescription: String,
    val resourceId: String,
    val bounds: android.graphics.Rect,
    val isClickable: Boolean,
    val isScrollable: Boolean,
    val isEnabled: Boolean,
    val depth: Int
) {
    companion object {
        fun fromAccessibilityNode(node: AccessibilityNodeInfo, depth: Int = 0): NodeInfo {
            val bounds = android.graphics.Rect()
            node.getBoundsInScreen(bounds)
            
            return NodeInfo(
                className = node.className?.toString() ?: "",
                text = node.text?.toString() ?: "",
                contentDescription = node.contentDescription?.toString() ?: "",
                resourceId = node.viewIdResourceName ?: "",
                bounds = bounds,
                isClickable = node.isClickable,
                isScrollable = node.isScrollable,
                isEnabled = node.isEnabled,
                depth = depth
            )
        }
    }
}
