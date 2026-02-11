package com.carlos.autoflow.accessibility

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.util.Log // Keep android.util.Log for now to replace it
import com.carlos.autoflow.utils.AutoFlowLogger // Added
import com.carlos.autoflow.utils.logTag // Added

abstract class BaseAccessibilityService : AccessibilityService() {
    
    companion object {
        private const val TAG = "BaseAccessibility"
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        AutoFlowLogger.d(TAG, "${javaClass.simpleName} 已连接")
        onAccessibilityServiceConnected()
    }

    override fun onDestroy() {
        super.onDestroy()
        AutoFlowLogger.d(TAG, "${javaClass.simpleName} 已断开")
        onAccessibilityServiceDestroyed()
    }

    override fun onInterrupt() {
        AutoFlowLogger.d(TAG, "${javaClass.simpleName} 被中断")
        onAccessibilityServiceInterrupted()
    }

    // 通用方法：获取所有节点
    protected fun getAllNodes(root: AccessibilityNodeInfo? = rootInActiveWindow): List<AccessibilityNodeInfo> {
        AutoFlowLogger.d(TAG, "获取所有节点")
        val nodes = mutableListOf<AccessibilityNodeInfo>()
        root?.let { extractNodesRecursively(it, nodes) }
        AutoFlowLogger.d(TAG, "共找到 ${nodes.size} 个节点")
        return nodes
    }

    // 通用方法：根据坐标查找节点
    protected fun findNodeAtCoordinates(x: Int, y: Int, root: AccessibilityNodeInfo? = rootInActiveWindow): AccessibilityNodeInfo? {
        AutoFlowLogger.d(TAG, "根据坐标 ($x, $y) 查找节点")
        val foundNode = root?.let { findNodeAtCoordinatesRecursively(it, x, y) }
        if (foundNode == null) {
            AutoFlowLogger.d(TAG, "未找到坐标 ($x, $y) 处的节点")
        } else {
            AutoFlowLogger.d(TAG, "在坐标 ($x, $y) 处找到节点: ${foundNode.className} (${foundNode.viewIdResourceName})")
        }
        return foundNode
    }

    // 通用方法：根据文本查找节点
    protected fun findNodeByText(text: String, root: AccessibilityNodeInfo? = rootInActiveWindow): AccessibilityNodeInfo? {
        AutoFlowLogger.d(TAG, "根据文本 '$text' 查找节点")
        val foundNode = root?.let { findNodeByTextRecursively(it, text) }
        if (foundNode == null) {
            AutoFlowLogger.d(TAG, "未找到文本 '$text' 的节点")
        } else {
            AutoFlowLogger.d(TAG, "找到文本 '$text' 的节点: ${foundNode.className} (${foundNode.viewIdResourceName})")
        }
        return foundNode
    }

    // 通用方法：根据ID查找节点
    protected fun findNodeById(resourceId: String, root: AccessibilityNodeInfo? = rootInActiveWindow): AccessibilityNodeInfo? {
        AutoFlowLogger.d(TAG, "根据ID '$resourceId' 查找节点")
        val foundNode = root?.let { findNodeByIdRecursively(it, resourceId) }
        if (foundNode == null) {
            AutoFlowLogger.d(TAG, "未找到ID '$resourceId' 的节点")
        } else {
            AutoFlowLogger.d(TAG, "找到ID '$resourceId' 的节点: ${foundNode.className}")
        }
        return foundNode
    }

    private fun extractNodesRecursively(node: AccessibilityNodeInfo, nodes: MutableList<AccessibilityNodeInfo>) {
        nodes.add(node)
        for (i in 0 until node.childCount) {
            node.getChild(i)?.let { child ->
                extractNodesRecursively(child, nodes)
            }
        }
    }

    private fun findNodeAtCoordinatesRecursively(node: AccessibilityNodeInfo, x: Int, y: Int): AccessibilityNodeInfo? {
        val rect = android.graphics.Rect()
        node.getBoundsInScreen(rect)
        
        if (rect.contains(x, y)) {
            for (i in 0 until node.childCount) {
                node.getChild(i)?.let { child ->
                    findNodeAtCoordinatesRecursively(child, x, y)?.let { return it }
                }
            }
            return node
        }
        return null
    }

    private fun findNodeByTextRecursively(node: AccessibilityNodeInfo, text: String): AccessibilityNodeInfo? {
        if (node.text?.toString()?.contains(text, ignoreCase = true) == true) {
            return node
        }
        for (i in 0 until node.childCount) {
            node.getChild(i)?.let { child ->
                findNodeByTextRecursively(child, text)?.let { return it }
            }
        }
        return null
    }

    private fun findNodeByIdRecursively(node: AccessibilityNodeInfo, resourceId: String): AccessibilityNodeInfo? {
        if (node.viewIdResourceName == resourceId) {
            return node
        }
        for (i in 0 until node.childCount) {
            node.getChild(i)?.let { child ->
                findNodeByIdRecursively(child, resourceId)?.let { return it }
            }
        }
        return null
    }

    // 子类需要实现的方法
    protected abstract fun onAccessibilityServiceConnected()
    protected abstract fun onAccessibilityServiceDestroyed()
    protected abstract fun onAccessibilityServiceInterrupted()
}
