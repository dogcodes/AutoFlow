package com.carlos.autoflow.accessibility

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.os.Bundle
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.carlos.autoflow.workflow.models.ElementSelector
import kotlinx.coroutines.*

class AutoFlowAccessibilityService : AccessibilityService() {
    
    companion object {
        private var instance: AutoFlowAccessibilityService? = null
        fun getInstance(): AutoFlowAccessibilityService? = instance
        fun isServiceEnabled(): Boolean = instance != null
        private const val TAG = "AutoFlowAccessibility"
    }

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        Log.d(TAG, "无障碍服务已连接")
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        instance = null
        Log.d(TAG, "无障碍服务已断开")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event?.let { handleAccessibilityEvent(it) }
    }

    override fun onInterrupt() {
        Log.d(TAG, "无障碍服务被中断")
    }

    private fun handleAccessibilityEvent(event: AccessibilityEvent) {
        when (event.eventType) {
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                Log.d(TAG, "窗口变化: ${event.packageName}")
            }
        }
    }

    // 执行操作 - 带重试机制
    suspend fun executeOperation(operation: AccessibilityOperation): OperationResult {
        return withContext(Dispatchers.IO) {
            retryOperation(operation.retryCount) {
                when (operation) {
                    is ClickOperation -> performClick(operation)
                    is InputOperation -> performInput(operation)
                    is ScrollOperation -> performScroll(operation)
                    is FindOperation -> performFind(operation)
                    is WaitOperation -> performWait(operation)
                }
            }
        }
    }

    private suspend fun <T> retryOperation(maxRetries: Int, operation: suspend () -> T): T {
        repeat(maxRetries - 1) {
            try {
                return operation()
            } catch (e: Exception) {
                delay(1000)
            }
        }
        return operation()
    }

    private fun performClick(operation: ClickOperation): OperationResult {
        val node = findElement(operation.selector) ?: return OperationResult.Error("元素未找到")
        
        val success = when (operation.clickType) {
            ClickType.SINGLE -> node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            ClickType.LONG -> node.performAction(AccessibilityNodeInfo.ACTION_LONG_CLICK)
        }
        
        return if (success) OperationResult.Success(mapOf("clicked" to true))
        else OperationResult.Error("点击操作失败")
    }

    private fun performInput(operation: InputOperation): OperationResult {
        val node = findElement(operation.selector) ?: return OperationResult.Error("输入框未找到")
        
        if (operation.clearFirst) {
            val selectAllArgs = Bundle().apply {
                putInt(AccessibilityNodeInfo.ACTION_ARGUMENT_SELECTION_START_INT, 0)
                putInt(AccessibilityNodeInfo.ACTION_ARGUMENT_SELECTION_END_INT, node.text?.length ?: 0)
            }
            node.performAction(AccessibilityNodeInfo.ACTION_SET_SELECTION, selectAllArgs)
        }
        
        val arguments = Bundle().apply {
            putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, operation.text)
        }
        val success = node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)
        
        return if (success) OperationResult.Success(mapOf("inputText" to operation.text))
        else OperationResult.Error("输入操作失败")
    }

    private fun performScroll(operation: ScrollOperation): OperationResult {
        val action = when (operation.direction) {
            "up" -> AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD
            "down" -> AccessibilityNodeInfo.ACTION_SCROLL_FORWARD
            else -> return OperationResult.Error("不支持的滚动方向")
        }
        
        val success = rootInActiveWindow?.performAction(action) ?: false
        return if (success) OperationResult.Success(mapOf("scrolled" to true))
        else OperationResult.Error("滚动操作失败")
    }

    private fun performFind(operation: FindOperation): OperationResult {
        val elements = findElements(operation.selector)
        return OperationResult.Success(mapOf(
            "elements" to elements,
            "count" to elements.size
        ))
    }

    private suspend fun performWait(operation: WaitOperation): OperationResult {
        val startTime = System.currentTimeMillis()
        
        while (System.currentTimeMillis() - startTime < operation.maxWaitTime) {
            if (findElement(operation.selector) != null) {
                return OperationResult.Success(mapOf("found" to true))
            }
            delay(operation.checkInterval)
        }
        
        return OperationResult.Error("等待超时")
    }

    private fun findElement(selector: ElementSelector): AccessibilityNodeInfo? {
        val root = rootInActiveWindow ?: return null
        
        return when (selector) {
            is ElementSelector.ById -> findNodeById(root, selector.resourceId)
            is ElementSelector.ByText -> findNodeByText(root, selector.text)
            is ElementSelector.ByDescription -> findNodeByDescription(root, selector.description)
            is ElementSelector.ByClassName -> findNodeByClassName(root, selector.className)
            is ElementSelector.ByCoordinate -> findNodeByCoordinate(selector.x, selector.y)
        }
    }

    private fun findElements(selector: ElementSelector): List<AccessibilityNodeInfo> {
        val root = rootInActiveWindow ?: return emptyList()
        val results = mutableListOf<AccessibilityNodeInfo>()
        
        when (selector) {
            is ElementSelector.ByText -> findNodesByText(root, selector.text, results)
            else -> findElement(selector)?.let { results.add(it) }
        }
        
        return results
    }

    private fun findNodeById(root: AccessibilityNodeInfo, resourceId: String): AccessibilityNodeInfo? {
        return root.findAccessibilityNodeInfosByViewId(resourceId).firstOrNull()
    }

    private fun findNodeByText(root: AccessibilityNodeInfo, text: String): AccessibilityNodeInfo? {
        return root.findAccessibilityNodeInfosByText(text).firstOrNull()
    }

    private fun findNodesByText(root: AccessibilityNodeInfo, text: String, results: MutableList<AccessibilityNodeInfo>) {
        results.addAll(root.findAccessibilityNodeInfosByText(text))
    }

    private fun findNodeByDescription(root: AccessibilityNodeInfo, description: String): AccessibilityNodeInfo? {
        if (root.contentDescription?.toString() == description) return root
        
        for (i in 0 until root.childCount) {
            root.getChild(i)?.let { child ->
                findNodeByDescription(child, description)?.let { return it }
            }
        }
        return null
    }

    private fun findNodeByClassName(root: AccessibilityNodeInfo, className: String): AccessibilityNodeInfo? {
        if (root.className?.toString() == className) return root
        
        for (i in 0 until root.childCount) {
            root.getChild(i)?.let { child ->
                findNodeByClassName(child, className)?.let { return it }
            }
        }
        return null
    }

    private fun findNodeByCoordinate(x: Float, y: Float): AccessibilityNodeInfo? {
        // 坐标查找需要遍历所有节点检查边界
        return null // 简化实现
    }
}

// 操作定义
sealed class AccessibilityOperation {
    abstract val timeout: Long
    abstract val retryCount: Int
}

data class ClickOperation(
    val selector: ElementSelector,
    val clickType: ClickType = ClickType.SINGLE,
    override val timeout: Long = 5000,
    override val retryCount: Int = 3
) : AccessibilityOperation()

data class InputOperation(
    val selector: ElementSelector,
    val text: String,
    val clearFirst: Boolean = true,
    override val timeout: Long = 5000,
    override val retryCount: Int = 3
) : AccessibilityOperation()

data class ScrollOperation(
    val direction: String,
    val distance: Int = 1,
    override val timeout: Long = 3000,
    override val retryCount: Int = 2
) : AccessibilityOperation()

data class FindOperation(
    val selector: ElementSelector,
    override val timeout: Long = 3000,
    override val retryCount: Int = 1
) : AccessibilityOperation()

data class WaitOperation(
    val selector: ElementSelector,
    val maxWaitTime: Long = 10000,
    val checkInterval: Long = 500,
    override val timeout: Long = 10000,
    override val retryCount: Int = 1
) : AccessibilityOperation()

enum class ClickType { SINGLE, LONG }

sealed class OperationResult {
    data class Success(val data: Map<String, Any>) : OperationResult()
    data class Error(val message: String) : OperationResult()
}
