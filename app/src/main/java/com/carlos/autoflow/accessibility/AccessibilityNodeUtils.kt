package com.carlos.autoflow.accessibility

import android.view.accessibility.AccessibilityNodeInfo
import com.carlos.autoflow.utils.AutoFlowLogger
import com.carlos.autoflow.workflow.models.ElementSelector

object AccessibilityNodeUtils {

    private const val TAG = "AccessibilityNodeUtils"

    fun findClickableParent(node: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        var parent = node.parent
        while (parent != null) {
            if (parent.isClickable) {
                return parent
            }
            val grandParent = parent.parent
            parent.recycle() // 回收当前父节点
            parent = grandParent
        }
        return null
    }

    fun findClickableChild(node: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        for (i in 0 until node.childCount) {
            val child = node.getChild(i)
            if (child != null) {
                if (child.isClickable) {
                    return child
                }
                val clickableChild = findClickableChild(child) // 递归查找
                if (clickableChild != null) {
                    child.recycle() // Recycle child before returning from recursive call
                    return clickableChild
                }
                child.recycle() // Recycle child after use
            }
        }
        return null
    }

    fun findElement(rootInActiveWindow: AccessibilityNodeInfo?, selector: ElementSelector): AccessibilityNodeInfo? {
        AutoFlowLogger.d(TAG, "尝试查找单个元素，选择器: $selector")
        val root = rootInActiveWindow ?: run {
            AutoFlowLogger.e(TAG, "findElement: rootInActiveWindow 为空，无法查找元素。")
            return null
        }

        val foundNode = when (selector) {
            is ElementSelector.ById -> findNodeById(root, selector.resourceId)
            is ElementSelector.ByText -> findNodeByText(root, selector.text)
            is ElementSelector.ByDescription -> findNodeByDescription(root, selector.description)
            is ElementSelector.ByClassName -> findNodeByClassName(root, selector.className)
            is ElementSelector.ByCoordinate -> findNodeByCoordinate(root, selector.x, selector.y)
        }

        if (foundNode == null) {
            AutoFlowLogger.d(TAG, "findElement: 未找到选择器对应的元素: $selector")
        } else {
            AutoFlowLogger.d(TAG, "findElement: 已找到选择器对应的元素: $selector -> ${foundNode.viewIdResourceName ?: foundNode.className}")
        }
        return foundNode
    }

    fun findElements(rootInActiveWindow: AccessibilityNodeInfo?, selector: ElementSelector): List<AccessibilityNodeInfo> {
        AutoFlowLogger.d(TAG, "尝试查找多个元素，选择器: $selector")
        val root = rootInActiveWindow ?: run {
            AutoFlowLogger.e(TAG, "findElements: rootInActiveWindow 为空，无法查找元素。")
            return emptyList()
        }
        val results = mutableListOf<AccessibilityNodeInfo>()

        when (selector) {
            is ElementSelector.ByText -> findNodesByText(root, selector.text, results)
            else -> findElement(root, selector)?.let { results.add(it) } // Use the new findElement
        }

        AutoFlowLogger.d(TAG, "findElements: 找到 ${results.size} 个选择器对应的元素: $selector")
        return results
    }

    fun findNodeById(root: AccessibilityNodeInfo, resourceId: String): AccessibilityNodeInfo? {
        return root.findAccessibilityNodeInfosByViewId(resourceId).firstOrNull()
    }

    fun findNodeByText(root: AccessibilityNodeInfo, text: String): AccessibilityNodeInfo? {
        return root.findAccessibilityNodeInfosByText(text).firstOrNull()
    }

    fun findNodesByText(root: AccessibilityNodeInfo, text: String, results: MutableList<AccessibilityNodeInfo>) {
        results.addAll(root.findAccessibilityNodeInfosByText(text))
    }

    fun findNodeByDescription(root: AccessibilityNodeInfo, description: String): AccessibilityNodeInfo? {
        if (root.contentDescription?.toString() == description) return root

        for (i in 0 until root.childCount) {
            root.getChild(i)?.let { child ->
                findNodeByDescription(child, description)?.let {
                    child.recycle() // Recycle child before returning from recursive call
                    return it
                }
                child.recycle() // Recycle child after use
            }
        }
        return null
    }

    fun findNodeByClassName(root: AccessibilityNodeInfo, className: String): AccessibilityNodeInfo? {
        if (root.className?.toString() == className) return root

        for (i in 0 until root.childCount) {
            root.getChild(i)?.let { child ->
                findNodeByClassName(child, className)?.let {
                    child.recycle() // Recycle child before returning from recursive call
                    return it
                }
                child.recycle() // Recycle child after use
            }
        }
        return null
    }

    fun findNodeByCoordinate(root: AccessibilityNodeInfo, x: Float, y: Float): AccessibilityNodeInfo? {
        // 坐标查找需要遍历所有节点检查边界
        // This is a simplified implementation, a full implementation would involve traversing the tree
        // and checking if the node's bounds contain the coordinate.
        return null
    }
}
