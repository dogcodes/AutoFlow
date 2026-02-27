package com.carlos.autoflow.accessibility

import android.view.accessibility.AccessibilityNodeInfo
import android.graphics.Rect
import com.carlos.autoflow.utils.AutoFlowLogger
import com.carlos.autoflow.workflow.models.ElementSelector
import com.carlos.autoflow.workflow.models.MatchType // Import MatchType

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

    fun findEditableNode(node: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        // 1. 检查当前节点
        if (node.isEditable) {
            return node
        }

        // 2. 检查父节点
        var parent = node.parent
        while (parent != null) {
            if (parent.isEditable) {
                return parent
            }
            parent = parent.parent
        }

        // 3. 检查兄弟节点
        val nodeParent = node.parent
        if (nodeParent != null) {
            for (i in 0 until nodeParent.childCount) {
                val sibling = nodeParent.getChild(i)
                if (sibling != null && sibling != node && sibling.isEditable) {
                    return sibling
                }
            }
        }

        // 4. 检查子节点（递归）
        for (i in 0 until node.childCount) {
            val child = node.getChild(i)
            if (child != null) {
                if (child.isEditable) {
                    return child
                }
                val editableDescendant = findEditableNode(child)
                if (editableDescendant != null) {
                    return editableDescendant
                }
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
            is ElementSelector.ByText -> findNodeByText(root, selector.text, selector.matchType)
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

    /**
     * 带子节点约束的查找：
     * 1. 先根据主选择器找到候选节点列表
     * 2. 检查候选节点的子树中是否满足所有子约束的节点
     * 3. 支持 exclude 逻辑：如果 isExclude 为 true，则该子树中必须不存在满足条件的节点
     */
    fun findNodeWithConstraints(
        rootInActiveWindow: AccessibilityNodeInfo?,
        mainSelector: ElementSelector,
        childSelectors: List<ConstraintSelector>
    ): AccessibilityNodeInfo? {
        val root = rootInActiveWindow ?: return null
        
        // 1. 找到所有匹配主选择器的候选者
        val candidates = mutableListOf<AccessibilityNodeInfo>()
        when (mainSelector) {
            is ElementSelector.ById -> {
                candidates.addAll(root.findAccessibilityNodeInfosByViewId(mainSelector.resourceId))
            }
            is ElementSelector.ByText -> {
                findNodesByTextRecursive(root, mainSelector.text, mainSelector.matchType, candidates)
            }
            else -> {
                findElement(root, mainSelector)?.let { candidates.add(it) }
            }
        }

        if (candidates.isEmpty()) return null

        // 2. 遍历候选者，验证子节点约束
        for (candidate in candidates) {
            val allMatch = childSelectors.all { constraint ->
                val found = findElement(candidate, constraint.selector) != null
                if (constraint.isExclude) {
                    !found // 如果是排除模式，找不到才算匹配
                } else {
                    found  // 否则，找到才算匹配
                }
            }

            if (allMatch) {
                // 找到满足所有条件的第一个候选者，回收其他候选者并返回
                candidates.filter { it != candidate }.forEach { it.recycle() }
                return candidate
            }
            candidate.recycle()
        }
        
        return null
    }

    fun findElements(rootInActiveWindow: AccessibilityNodeInfo?, selector: ElementSelector): List<AccessibilityNodeInfo> {
        AutoFlowLogger.d(TAG, "尝试查找多个元素，选择器: $selector")
        val root = rootInActiveWindow ?: run {
            AutoFlowLogger.e(TAG, "findElements: rootInActiveWindow 为空，无法查找元素。")
            return emptyList()
        }
        val results = mutableListOf<AccessibilityNodeInfo>()

        when (selector) {
            is ElementSelector.ByText -> findNodesByText(root, selector.text, selector.matchType, results)
            else -> findElement(root, selector)?.let { results.add(it) } // Use the new findElement
        }

        AutoFlowLogger.d(TAG, "findElements: 找到 ${results.size} 个选择器对应的元素: $selector")
        return results
    }

    fun findNodeById(root: AccessibilityNodeInfo, resourceId: String): AccessibilityNodeInfo? {
        return root.findAccessibilityNodeInfosByViewId(resourceId).firstOrNull()
    }

    fun findNodeByText(root: AccessibilityNodeInfo, text: String, matchType: MatchType): AccessibilityNodeInfo? {
        // For CONTAINS, try system method first as it might be optimized
        if (matchType == MatchType.CONTAINS) {
            val systemFoundNodes = root.findAccessibilityNodeInfosByText(text)
            if (systemFoundNodes.isNotEmpty()) {
                // If system found multiple, we'd need to pick one or apply further filtering.
                // For a single find, just take the first.
                return systemFoundNodes.firstOrNull()
            }
        }

        // Otherwise, or if system method failed for CONTAINS, use our precise recursive method
        val customFoundNode = findNodeByTextRecursive(root, text, matchType)
        if (customFoundNode != null) {
            AutoFlowLogger.d(TAG, "findNodeByText: Custom recursive method found node by text: $text (MatchType: $matchType)")
            return customFoundNode
        }

        AutoFlowLogger.d(TAG, "findNodeByText: No node found by text: $text (MatchType: $matchType)")
        return null
    }

    fun findNodesByText(root: AccessibilityNodeInfo, text: String, matchType: MatchType, results: MutableList<AccessibilityNodeInfo>) {
        if (matchType == MatchType.CONTAINS) {
            results.addAll(root.findAccessibilityNodeInfosByText(text))
        } else {
            findNodesByTextRecursive(root, text, matchType, results)
        }
    }

    /**
     * 递归搜索AccessibilityNodeInfo，在其text、contentDescription或hintText属性中查找匹配指定文本的节点（不区分大小写）。
     * 此方法执行深度优先搜索。
     *
     * @param node 开始搜索的当前节点。
     * @param text 要搜索的文本。
     * @param matchType 文本匹配类型。
     * @return 找到的第一个匹配的AccessibilityNodeInfo，如果未找到则返回null。
     */
    fun findNodeByTextRecursive(node: AccessibilityNodeInfo, text: String, matchType: MatchType): AccessibilityNodeInfo? {
        // Check current node
        val lowerCaseSearchText = text.lowercase()
        val nodeText = node.text?.toString()?.lowercase()
        val nodeContentDescription = node.contentDescription?.toString()?.lowercase()
        val nodeHintText = node.hintText?.toString()?.lowercase()

        val matches = when (matchType) {
            MatchType.EXACT ->
                nodeText == lowerCaseSearchText ||
                nodeContentDescription == lowerCaseSearchText ||
                nodeHintText == lowerCaseSearchText
            MatchType.STARTS_WITH ->
                nodeText?.startsWith(lowerCaseSearchText) == true ||
                nodeContentDescription?.startsWith(lowerCaseSearchText) == true ||
                nodeHintText?.startsWith(lowerCaseSearchText) == true
            MatchType.ENDS_WITH ->
                nodeText?.endsWith(lowerCaseSearchText) == true ||
                nodeContentDescription?.endsWith(lowerCaseSearchText) == true ||
                nodeHintText?.endsWith(lowerCaseSearchText) == true
            MatchType.CONTAINS -> // Fallback for CONTAINS, though system method is preferred
                nodeText?.contains(lowerCaseSearchText) == true ||
                nodeContentDescription?.contains(lowerCaseSearchText) == true ||
                nodeHintText?.contains(lowerCaseSearchText) == true
        }

        if (matches) {
            return node
        }

        // Search children
        for (i in 0 until node.childCount) {
            val child = node.getChild(i)
            if (child != null) {
                val found = findNodeByTextRecursive(child, text, matchType)
                if (found != null) {
                    child.recycle() // Recycle child before returning from recursive call
                    return found
                }
                child.recycle() // Recycle child after use
            }
        }
        return null
    }

    /**
     * 递归搜索AccessibilityNodeInfo，在其text、contentDescription或hintText属性中查找所有匹配指定文本的节点（不区分大小写）。
     * 此方法执行深度优先搜索。
     *
     * @param node 开始搜索的当前节点。
     * @param text 要搜索的文本。
     * @param matchType 文本匹配类型。
     * @param results 存储匹配到的AccessibilityNodeInfo的列表。
     */
    fun findNodesByTextRecursive(node: AccessibilityNodeInfo, text: String, matchType: MatchType, results: MutableList<AccessibilityNodeInfo>) {
        val lowerCaseSearchText = text.lowercase()
        val nodeText = node.text?.toString()?.lowercase()
        val nodeContentDescription = node.contentDescription?.toString()?.lowercase()
        val nodeHintText = node.hintText?.toString()?.lowercase()

        val matches = when (matchType) {
            MatchType.EXACT ->
                nodeText == lowerCaseSearchText ||
                nodeContentDescription == lowerCaseSearchText ||
                nodeHintText == lowerCaseSearchText
            MatchType.STARTS_WITH ->
                nodeText?.startsWith(lowerCaseSearchText) == true ||
                nodeContentDescription?.startsWith(lowerCaseSearchText) == true ||
                nodeHintText?.startsWith(lowerCaseSearchText) == true
            MatchType.ENDS_WITH ->
                nodeText?.endsWith(lowerCaseSearchText) == true ||
                nodeContentDescription?.endsWith(lowerCaseSearchText) == true ||
                nodeHintText?.endsWith(lowerCaseSearchText) == true
            MatchType.CONTAINS -> // Fallback for CONTAINS, though system method is preferred
                nodeText?.contains(lowerCaseSearchText) == true ||
                nodeContentDescription?.contains(lowerCaseSearchText) == true ||
                nodeHintText?.contains(lowerCaseSearchText) == true
        }

        if (matches) {
            results.add(node)
        }

        for (i in 0 until node.childCount) {
            node.getChild(i)?.let { child ->
                findNodesByTextRecursive(child, text, matchType, results)
                child.recycle()
            }
        }
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
        parts.add("Editable: ${node.isEditable}") // 添加可编辑信息

        val bounds = Rect()
        node.getBoundsInScreen(bounds)
        parts.add("Bounds: [${bounds.left}, ${bounds.top}]-[${bounds.right}, ${bounds.bottom}]")

        AutoFlowLogger.d(TAG, "$indent${parts.joinToString(", ")}")

        for (i in 0 until node.childCount) {
            val child = node.getChild(i)
            printAccessibilityNodeTree(child, "$indent  ")
            // child?.recycle() // 移除不正确的回收子节点调用
        }
    }
}
