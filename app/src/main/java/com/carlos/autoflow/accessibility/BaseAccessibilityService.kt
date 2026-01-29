package com.carlos.autoflow.accessibility

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.util.Log

abstract class BaseAccessibilityService : AccessibilityService() {
    
    companion object {
        private const val TAG = "BaseAccessibility"
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d(TAG, "${javaClass.simpleName} 已连接")
        onAccessibilityServiceConnected()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "${javaClass.simpleName} 已断开")
        onAccessibilityServiceDestroyed()
    }

    override fun onInterrupt() {
        Log.d(TAG, "${javaClass.simpleName} 被中断")
        onAccessibilityServiceInterrupted()
    }

    // 通用方法：获取所有节点
    protected fun getAllNodes(root: AccessibilityNodeInfo? = rootInActiveWindow): List<AccessibilityNodeInfo> {
        val nodes = mutableListOf<AccessibilityNodeInfo>()
        root?.let { extractNodesRecursively(it, nodes) }
        return nodes
    }

    // 通用方法：根据坐标查找节点
    protected fun findNodeAtCoordinates(x: Int, y: Int, root: AccessibilityNodeInfo? = rootInActiveWindow): AccessibilityNodeInfo? {
        return root?.let { findNodeAtCoordinatesRecursively(it, x, y) }
    }

    // 通用方法：根据文本查找节点
    protected fun findNodeByText(text: String, root: AccessibilityNodeInfo? = rootInActiveWindow): AccessibilityNodeInfo? {
        return root?.let { findNodeByTextRecursively(it, text) }
    }

    // 通用方法：根据ID查找节点
    protected fun findNodeById(resourceId: String, root: AccessibilityNodeInfo? = rootInActiveWindow): AccessibilityNodeInfo? {
        return root?.let { findNodeByIdRecursively(it, resourceId) }
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
