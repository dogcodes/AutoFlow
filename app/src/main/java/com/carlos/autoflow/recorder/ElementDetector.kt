package com.carlos.autoflow.recorder

import android.view.accessibility.AccessibilityNodeInfo
import com.carlos.autoflow.workflow.models.ElementSelector

/**
 * 元素信息数据类
 */
data class ElementInfo(
    val id: String?,
    val text: String?,
    val description: String?,
    val className: String?,
    val bounds: android.graphics.Rect,
    val isClickable: Boolean,
    val isEditable: Boolean,
    val isScrollable: Boolean,
    val selector: ElementSelector
)

/**
 * 页面元素识别核心类
 */
class ElementDetector {
    
    /**
     * 检测页面所有可交互元素
     */
    fun detectElements(rootNode: AccessibilityNodeInfo?): List<ElementInfo> {
        val elements = mutableListOf<ElementInfo>()
        rootNode?.let { traverseNode(it, elements) }
        return elements
    }
    
    /**
     * 根据坐标查找元素
     */
    fun findElementAt(rootNode: AccessibilityNodeInfo?, x: Float, y: Float): ElementInfo? {
        return detectElements(rootNode).find { element ->
            element.bounds.contains(x.toInt(), y.toInt())
        }
    }
    
    /**
     * 生成最优选择器
     */
    fun generateSelector(element: ElementInfo): ElementSelector {
        // 优先级：ID > 文本 > 描述 > 类名
        return when {
            !element.id.isNullOrEmpty() -> ElementSelector.ById(element.id)
            !element.text.isNullOrEmpty() -> ElementSelector.ByText(element.text)
            !element.description.isNullOrEmpty() -> ElementSelector.ByDescription(element.description)
            !element.className.isNullOrEmpty() -> ElementSelector.ByClassName(element.className)
            else -> ElementSelector.ByCoordinate(
                element.bounds.centerX().toFloat(),
                element.bounds.centerY().toFloat()
            )
        }
    }
    
    private fun traverseNode(node: AccessibilityNodeInfo, elements: MutableList<ElementInfo>) {
        // 只收集可交互的元素
        if (isInteractiveElement(node)) {
            val bounds = android.graphics.Rect()
            node.getBoundsInScreen(bounds)
            
            val elementInfo = ElementInfo(
                id = node.viewIdResourceName,
                text = node.text?.toString(),
                description = node.contentDescription?.toString(),
                className = node.className?.toString(),
                bounds = bounds,
                isClickable = node.isClickable,
                isEditable = node.isEditable,
                isScrollable = node.isScrollable,
                selector = createSelector(node)
            )
            
            elements.add(elementInfo)
        }
        
        // 递归遍历子节点
        for (i in 0 until node.childCount) {
            node.getChild(i)?.let { child ->
                traverseNode(child, elements)
            }
        }
    }
    
    private fun isInteractiveElement(node: AccessibilityNodeInfo): Boolean {
        return node.isClickable || 
               node.isLongClickable || 
               node.isEditable || 
               node.isScrollable ||
               node.isCheckable
    }
    
    private fun createSelector(node: AccessibilityNodeInfo): ElementSelector {
        val id = node.viewIdResourceName
        val text = node.text?.toString()
        val description = node.contentDescription?.toString()
        val className = node.className?.toString()
        
        return when {
            !id.isNullOrEmpty() -> ElementSelector.ById(id)
            !text.isNullOrEmpty() -> ElementSelector.ByText(text)
            !description.isNullOrEmpty() -> ElementSelector.ByDescription(description)
            !className.isNullOrEmpty() -> ElementSelector.ByClassName(className)
            else -> {
                val bounds = android.graphics.Rect()
                node.getBoundsInScreen(bounds)
                ElementSelector.ByCoordinate(
                    bounds.centerX().toFloat(),
                    bounds.centerY().toFloat()
                )
            }
        }
    }
}
