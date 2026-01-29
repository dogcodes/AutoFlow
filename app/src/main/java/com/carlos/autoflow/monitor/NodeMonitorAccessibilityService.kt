package com.carlos.autoflow.monitor

import android.view.accessibility.AccessibilityEvent
import com.carlos.autoflow.accessibility.BaseAccessibilityService

class NodeMonitorAccessibilityService : BaseAccessibilityService() {
    
    companion object {
        private var instance: NodeMonitorAccessibilityService? = null
        fun getInstance(): NodeMonitorAccessibilityService? = instance
        fun isServiceEnabled(): Boolean = instance != null
    }

    override fun onAccessibilityServiceConnected() {
        instance = this
    }

    override fun onAccessibilityServiceDestroyed() {
        instance = null
    }

    override fun onAccessibilityServiceInterrupted() {
        // 处理中断
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Monitor服务不需要处理事件，只需要提供节点查询能力
    }

    // 获取当前页面所有节点信息
    fun getCurrentPageNodes(): List<NodeInfo> {
        return getAllNodes().map { NodeInfo.fromAccessibilityNode(it) }
    }

    // 根据坐标查找节点
    fun findNodeAt(x: Int, y: Int): NodeInfo? {
        return findNodeAtCoordinates(x, y)?.let { NodeInfo.fromAccessibilityNode(it) }
    }
}
