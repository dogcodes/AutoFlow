package com.carlos.autoflow.monitor

import android.content.Context
import androidx.compose.runtime.*
import com.carlos.autoflow.monitor.NodeMonitorAccessibilityService

class NodeMonitorManager {
    companion object {
        private var _isMonitoringActive = mutableStateOf(false)
        val isMonitoringActive: State<Boolean> = _isMonitoringActive
        
        fun startMonitoring(context: Context): Boolean {
            if (!NodeMonitorAccessibilityService.isServiceEnabled()) {
                return false
            }
            
            // 检查浮动窗口权限
            if (!NodeMonitorService.canDrawOverlays(context)) {
                NodeMonitorService.requestOverlayPermission(context)
                return false
            }
            
            NodeMonitorService.start(context)
            _isMonitoringActive.value = true
            return true
        }
        
        fun stopMonitoring(context: Context) {
            NodeMonitorService.stop(context)
            _isMonitoringActive.value = false
        }
        
        fun isAccessibilityServiceEnabled(): Boolean {
            return NodeMonitorAccessibilityService.isServiceEnabled()
        }
    }
}
