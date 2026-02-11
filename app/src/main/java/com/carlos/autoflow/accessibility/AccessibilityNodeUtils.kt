package com.carlos.autoflow.accessibility

import android.view.accessibility.AccessibilityNodeInfo
import android.graphics.Rect // Add this import
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
        // First, try the system's findAccessibilityNodeInfosByText
        val systemFoundNode = root.findAccessibilityNodeInfosByText(text).firstOrNull()
        if (systemFoundNode != null) {
            AutoFlowLogger.d(TAG, "findNodeByText: System found node by text: $text")
            return systemFoundNode
        }

        // If system method fails, try our recursive method
        val customFoundNode = findNodeByTextRecursive(root, text)
        if (customFoundNode != null) {
            AutoFlowLogger.d(TAG, "findNodeByText: Custom recursive method found node by text: $text")
            return customFoundNode
        }

        AutoFlowLogger.d(TAG, "findNodeByText: No node found by text: $text")
        return null
    }

    fun findNodesByText(root: AccessibilityNodeInfo, text: String, results: MutableList<AccessibilityNodeInfo>) {
        results.addAll(root.findAccessibilityNodeInfosByText(text))
    }

    /**
     * 递归搜索AccessibilityNodeInfo，在其text、contentDescription或hintText属性中查找包含指定文本的节点（不区分大小写）。
     * 此方法执行深度优先搜索。
     *
     * @param node 开始搜索的当前节点。
     * @param text 要搜索的文本。
     * @return 找到的第一个匹配的AccessibilityNodeInfo，如果未找到则返回null。
     */
    fun findNodeByTextRecursive(node: AccessibilityNodeInfo, text: String): AccessibilityNodeInfo? {
        // Check current node
        val lowerCaseSearchText = text.lowercase()
        val nodeText = node.text?.toString()?.lowercase()
        val nodeContentDescription = node.contentDescription?.toString()?.lowercase()
        val nodeHintText = node.hintText?.toString()?.lowercase()

        if (nodeText?.contains(lowerCaseSearchText) == true ||
            nodeContentDescription?.contains(lowerCaseSearchText) == true ||
            nodeHintText?.contains(lowerCaseSearchText) == true) {
            return node
        }

        // Search children
        for (i in 0 until node.childCount) {
            val child = node.getChild(i)
            if (child != null) {
                val found = findNodeByTextRecursive(child, text)
                if (found != null) {
                    child.recycle() // Recycle child before returning from recursive call
                    return found
                }
                child.recycle() // Recycle child after use
            }
        }
        return null
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

    /**
     * 递归遍历并打印AccessibilityNodeInfo树的详细信息，用于调试目的。
     *
     * @param node 当前要打印的AccessibilityNodeInfo节点。
     * @param indent 字符串，用于控制打印输出的缩进，以显示树的层级结构。
     */
    fun printAccessibilityNodeTree(node: AccessibilityNodeInfo?, indent: String = "") {
        if (node == null) {
            AutoFlowLogger.d(TAG, "${indent}Node is null")
            return
        }

        val parts = mutableListOf<String>()
        parts.add("ClassName: ${node.className}")
        node.viewIdResourceName?.let { parts.add("ID: $it") }
        node.text?.let { parts.add("Text: \"$it\"") }
        node.contentDescription?.let { parts.add("ContentDesc: \"$it\"") }
        node.hintText?.let { parts.add("HintText: \"$it\"") }
        parts.add("Clickable: ${node.isClickable}")

        val bounds = Rect()
        node.getBoundsInScreen(bounds)
        parts.add("Bounds: [${bounds.left}, ${bounds.top}]-[${bounds.right}, ${bounds.bottom}]")

        AutoFlowLogger.d(TAG, "$indent${parts.joinToString(", ")}")

        for (i in 0 until node.childCount) {
            val child = node.getChild(i)
            printAccessibilityNodeTree(child, "$indent  ")
            child?.recycle() // 回收子节点
        }
    }
}
