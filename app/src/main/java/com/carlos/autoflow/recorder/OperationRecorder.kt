package com.carlos.autoflow.recorder

import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.carlos.autoflow.workflow.models.ElementSelector

/**
 * 录制的操作数据类
 */
data class RecordedOperation(
    val type: OperationType,
    val timestamp: Long,
    val element: ElementInfo?,
    val text: String? = null,
    val coordinates: Pair<Float, Float>? = null,
    val scrollDirection: String? = null
)

/**
 * 操作类型枚举
 */
enum class OperationType {
    CLICK,
    LONG_CLICK,
    INPUT,
    SCROLL,
    SWIPE
}

/**
 * 操作录制引擎
 */
class OperationRecorder {
    
    private val elementDetector = ElementDetector()
    private val recordedOperations = mutableListOf<RecordedOperation>()
    private var isRecording = false
    
    /**
     * 开始录制
     */
    fun startRecording() {
        isRecording = true
        recordedOperations.clear()
    }
    
    /**
     * 停止录制
     */
    fun stopRecording(): List<RecordedOperation> {
        isRecording = false
        return recordedOperations.toList()
    }
    
    /**
     * 处理无障碍事件
     */
    fun handleAccessibilityEvent(event: AccessibilityEvent, rootNode: AccessibilityNodeInfo?) {
        if (!isRecording) return
        
        when (event.eventType) {
            AccessibilityEvent.TYPE_VIEW_CLICKED -> {
                recordClickOperation(event, rootNode)
            }
            AccessibilityEvent.TYPE_VIEW_LONG_CLICKED -> {
                recordLongClickOperation(event, rootNode)
            }
            AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED -> {
                recordInputOperation(event, rootNode)
            }
            AccessibilityEvent.TYPE_VIEW_SCROLLED -> {
                recordScrollOperation(event, rootNode)
            }
        }
    }
    
    /**
     * 手动记录点击操作（用于手势识别）
     */
    fun recordClick(x: Float, y: Float, rootNode: AccessibilityNodeInfo?) {
        if (!isRecording) return
        
        val element = elementDetector.findElementAt(rootNode, x, y)
        val operation = RecordedOperation(
            type = OperationType.CLICK,
            timestamp = System.currentTimeMillis(),
            element = element,
            coordinates = Pair(x, y)
        )
        
        recordedOperations.add(operation)
    }
    
    private fun recordClickOperation(event: AccessibilityEvent, rootNode: AccessibilityNodeInfo?) {
        val sourceNode = event.source
        val element = sourceNode?.let { node ->
            val bounds = android.graphics.Rect()
            node.getBoundsInScreen(bounds)
            
            ElementInfo(
                id = node.viewIdResourceName,
                text = node.text?.toString(),
                description = node.contentDescription?.toString(),
                className = node.className?.toString(),
                bounds = bounds,
                isClickable = node.isClickable,
                isEditable = node.isEditable,
                isScrollable = node.isScrollable,
                selector = elementDetector.generateSelector(
                    ElementInfo(
                        id = node.viewIdResourceName,
                        text = node.text?.toString(),
                        description = node.contentDescription?.toString(),
                        className = node.className?.toString(),
                        bounds = bounds,
                        isClickable = node.isClickable,
                        isEditable = node.isEditable,
                        isScrollable = node.isScrollable,
                        selector = ElementSelector.ById("temp") // 临时值
                    )
                )
            )
        }
        
        val operation = RecordedOperation(
            type = OperationType.CLICK,
            timestamp = System.currentTimeMillis(),
            element = element
        )
        
        recordedOperations.add(operation)
    }
    
    private fun recordLongClickOperation(event: AccessibilityEvent, rootNode: AccessibilityNodeInfo?) {
        val sourceNode = event.source
        val element = sourceNode?.let { createElementInfo(it) }
        
        val operation = RecordedOperation(
            type = OperationType.LONG_CLICK,
            timestamp = System.currentTimeMillis(),
            element = element
        )
        
        recordedOperations.add(operation)
    }
    
    private fun recordInputOperation(event: AccessibilityEvent, rootNode: AccessibilityNodeInfo?) {
        val sourceNode = event.source
        val element = sourceNode?.let { createElementInfo(it) }
        val inputText = event.text?.joinToString("") ?: ""
        
        val operation = RecordedOperation(
            type = OperationType.INPUT,
            timestamp = System.currentTimeMillis(),
            element = element,
            text = inputText
        )
        
        recordedOperations.add(operation)
    }
    
    private fun recordScrollOperation(event: AccessibilityEvent, rootNode: AccessibilityNodeInfo?) {
        val sourceNode = event.source
        val element = sourceNode?.let { createElementInfo(it) }
        
        // 简单判断滚动方向
        val scrollDirection = if (event.scrollY > 0) "down" else "up"
        
        val operation = RecordedOperation(
            type = OperationType.SCROLL,
            timestamp = System.currentTimeMillis(),
            element = element,
            scrollDirection = scrollDirection
        )
        
        recordedOperations.add(operation)
    }
    
    private fun createElementInfo(node: AccessibilityNodeInfo): ElementInfo {
        val bounds = android.graphics.Rect()
        node.getBoundsInScreen(bounds)
        
        return ElementInfo(
            id = node.viewIdResourceName,
            text = node.text?.toString(),
            description = node.contentDescription?.toString(),
            className = node.className?.toString(),
            bounds = bounds,
            isClickable = node.isClickable,
            isEditable = node.isEditable,
            isScrollable = node.isScrollable,
            selector = elementDetector.generateSelector(
                ElementInfo(
                    id = node.viewIdResourceName,
                    text = node.text?.toString(),
                    description = node.contentDescription?.toString(),
                    className = node.className?.toString(),
                    bounds = bounds,
                    isClickable = node.isClickable,
                    isEditable = node.isEditable,
                    isScrollable = node.isScrollable,
                    selector = ElementSelector.ById("temp") // 临时值，会被重新生成
                )
            )
        )
    }
    
    /**
     * 获取当前录制状态
     */
    fun isRecording(): Boolean = isRecording
    
    /**
     * 获取已录制操作数量
     */
    fun getRecordedCount(): Int = recordedOperations.size
}
