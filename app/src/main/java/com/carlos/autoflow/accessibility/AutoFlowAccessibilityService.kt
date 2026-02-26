package com.carlos.autoflow.accessibility

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.os.Bundle
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.carlos.autoflow.utils.AutoFlowLogger // Added
import com.carlos.autoflow.workflow.models.ElementSelector
import com.carlos.autoflow.recorder.OperationRecorder
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class AutoFlowAccessibilityService : AccessibilityService() {
    
    companion object {
        private var instance: AutoFlowAccessibilityService? = null
        fun getInstance(): AutoFlowAccessibilityService? = instance
        fun isServiceEnabled(): Boolean = instance != null
        private const val TAG = "AutoFlowAccessibility"
    }

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val operationRecorder = OperationRecorder()

    private val _accessibilityEvents = MutableSharedFlow<AccessibilityEvent>(extraBufferCapacity = 64)
    val accessibilityEvents = _accessibilityEvents.asSharedFlow()

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        AutoFlowLogger.d(TAG, "无障碍服务已连接")
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        instance = null
        AutoFlowLogger.d(TAG, "无障碍服务已断开")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event?.let { 
            handleAccessibilityEvent(it)
            // 将事件拷贝一份发送到流中，防止事件被系统回收
            serviceScope.launch {
                _accessibilityEvents.emit(AccessibilityEvent.obtain(it))
            }
            // 将事件传递给录制器
            operationRecorder.handleAccessibilityEvent(it, rootInActiveWindow)
            //打印所有节点
            AccessibilityNodeUtils.printAccessibilityNodeTree(rootInActiveWindow)
        }
    }

    override fun onInterrupt() {
        AutoFlowLogger.d(TAG, "无障碍服务被中断")
    }

    private fun handleAccessibilityEvent(event: AccessibilityEvent) {
        when (event.eventType) {
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                AutoFlowLogger.d(TAG, "窗口变化: ${event.packageName}")
            }
        }
    }

    // 执行操作 - 带重试机制
    suspend fun executeOperation(operation: AccessibilityOperation): OperationResult {
        AutoFlowLogger.d(TAG, "正在执行操作: ${operation::class.simpleName}, 选择器: ${operation.selector}")
        val result = withContext(Dispatchers.IO) {
            retryOperation(operation.retryCount) {
                when (operation) {
                    is ClickOperation -> performClick(operation)
                    is InputOperation -> performInput(operation)
                    is ScrollOperation -> performScroll(operation)
                    is FindOperation -> performFind(operation)
                    is WaitOperation -> performWait(operation)
                    is SwipeOperation -> performSwipe(operation)
                    is GetTextOperation -> performGetText(operation)
                    is CheckStateOperation -> performCheckState(operation)
                }
            }
        }
        when (result) {
            is OperationResult.Success -> AutoFlowLogger.d(TAG, "操作 ${operation::class.simpleName} 成功: ${result.data}")
            is OperationResult.Error -> AutoFlowLogger.e(TAG, "操作 ${operation::class.simpleName} 失败: ${result.message}")
        }
        return result
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
        AutoFlowLogger.d(TAG, "尝试对选择器执行点击操作: ${operation.selector}")
        
        var nodeToClick = if (operation.childConditions.isNotEmpty()) {
            // 如果存在子约束，使用高级查找
            AccessibilityNodeUtils.findNodeWithConstraints(
                rootInActiveWindow, 
                operation.selector, 
                operation.childConditions
            )
        } else {
            // 否则使用普通查找
            AccessibilityNodeUtils.findElement(rootInActiveWindow, operation.selector)
        }
        
        if (nodeToClick == null) {
            AutoFlowLogger.e(TAG, "点击操作失败: 未找到选择器对应的元素: ${operation.selector}")
            return OperationResult.Error("元素未找到")
        }

        AutoFlowLogger.d(TAG, "已找到点击操作的元素: ${nodeToClick.viewIdResourceName ?: nodeToClick.className}")

        // 根据点击策略调整要点击的节点
        when (operation.clickStrategy) {
            com.carlos.autoflow.workflow.models.ClickStrategy.FIND_CLICKABLE_PARENT -> {
                if (!nodeToClick.isClickable) {
                    val originalNode = nodeToClick
                    nodeToClick = AccessibilityNodeUtils.findClickableParent(nodeToClick) ?: nodeToClick
                    if (originalNode != nodeToClick) {
                        AutoFlowLogger.d(TAG, "ClickStrategy: 查找可点击父级，最终点击节点: ${nodeToClick.viewIdResourceName ?: nodeToClick.className}")
                    } else {
                        AutoFlowLogger.d(TAG, "ClickStrategy: 查找可点击父级，但未找到更合适的父级，仍点击原始节点。")
                    }
                }
            }
            com.carlos.autoflow.workflow.models.ClickStrategy.FIND_CLICKABLE_CHILD -> {
                if (!nodeToClick.isClickable) {
                    val originalNode = nodeToClick
                    nodeToClick = AccessibilityNodeUtils.findClickableChild(nodeToClick) ?: nodeToClick
                    if (originalNode != nodeToClick) {
                        AutoFlowLogger.d(TAG, "ClickStrategy: 查找可点击子级，最终点击节点: ${nodeToClick.viewIdResourceName ?: nodeToClick.className}")
                    } else {
                        AutoFlowLogger.d(TAG, "ClickStrategy: 查找可点击子级，但未找到更合适的子级，仍点击原始节点。")
                    }
                }
            }
            com.carlos.autoflow.workflow.models.ClickStrategy.DEFAULT -> {
                // 默认行为，如果元素不可点击，则尝试点击自身
                if (!nodeToClick.isClickable) {
                    AutoFlowLogger.d(TAG, "ClickStrategy: 默认，但目标节点不可点击。尝试点击自身。")
                }
            }
        }
        
        val success = when (operation.clickType) {
            ClickType.SINGLE -> {
                AutoFlowLogger.d(TAG, "正在对节点执行单击操作: ${nodeToClick.viewIdResourceName ?: nodeToClick.className}")
                nodeToClick.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            }
            ClickType.LONG -> {
                AutoFlowLogger.d(TAG, "正在对节点执行长按操作: ${nodeToClick.viewIdResourceName ?: nodeToClick.className}")
                nodeToClick.performAction(AccessibilityNodeInfo.ACTION_LONG_CLICK)
            }
        }
        
        return if (success) {
            AutoFlowLogger.d(TAG, "点击操作成功，选择器: ${operation.selector}")
            OperationResult.Success(mapOf("clicked" to true))
        } else {
            AutoFlowLogger.e(TAG, "点击操作失败，选择器: ${operation.selector}")
            OperationResult.Error("点击操作失败")
        }
    }

    private fun findClickableParent(node: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        var parent = node.parent
        while (parent != null) {
            if (parent.isClickable) {
                return parent
            }
            parent = parent.parent
        }
        return null
    }

    private fun findClickableChild(node: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        for (i in 0 until node.childCount) {
            val child = node.getChild(i)
            if (child != null) {
                if (child.isClickable) {
                    return child
                }
                val clickableChild = findClickableChild(child) // 递归查找
                if (clickableChild != null) {
                    return clickableChild
                }
            }
        }
        return null
    }

    private fun findInputtableParent(node: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        var parent = node.parent
        while (parent != null) {
            if (parent.isEditable) {
                return parent
            }
            parent = parent.parent
        }
        return null
    }

    private fun performInput(operation: InputOperation): OperationResult {
        AutoFlowLogger.d(TAG, "尝试对选择器执行输入操作: ${operation.selector}，文本: ${operation.text}")
        var nodeToInput = AccessibilityNodeUtils.findElement(rootInActiveWindow, operation.selector)
        if (nodeToInput == null) {
            AutoFlowLogger.e(TAG, "输入操作失败: 未找到选择器对应的元素: ${operation.selector}")
            return OperationResult.Error("元素未找到")
        }
        AutoFlowLogger.d(TAG, "已找到初始元素: ${nodeToInput.viewIdResourceName ?: nodeToInput.className}")

        // Apply input strategy
        when (operation.inputStrategy) {
            com.carlos.autoflow.workflow.models.InputStrategy.FIND_INPUTTABLE_PARENT -> {
                if (!nodeToInput.isEditable) {
                    val originalNode = nodeToInput
                    nodeToInput = AccessibilityNodeUtils.findEditableNode(nodeToInput) ?: nodeToInput
                    if (originalNode != nodeToInput) {
                        AutoFlowLogger.d(TAG, "InputStrategy: 智能查找可编辑节点，最终输入节点: ${nodeToInput.viewIdResourceName ?: nodeToInput.className}")
                    } else {
                        AutoFlowLogger.d(TAG, "InputStrategy: 未找到可编辑节点，使用原始节点")
                    }
                }
            }
            com.carlos.autoflow.workflow.models.InputStrategy.DEFAULT -> {
                // 默认策略也尝试智能查找
                if (!nodeToInput.isEditable) {
                    AutoFlowLogger.d(TAG, "默认策略: 节点不可编辑，尝试智能查找")
                    val originalNode = nodeToInput
                    nodeToInput = AccessibilityNodeUtils.findEditableNode(nodeToInput) ?: nodeToInput
                    if (originalNode != nodeToInput) {
                        AutoFlowLogger.d(TAG, "默认策略: 找到可编辑节点: ${nodeToInput.viewIdResourceName ?: nodeToInput.className}")
                    }
                }
            }
        }

        if (!nodeToInput.isEditable) {
             AutoFlowLogger.e(TAG, "输入操作失败: 最终目标节点不可编辑: ${nodeToInput.viewIdResourceName ?: nodeToInput.className}")
             return OperationResult.Error("目标不可编辑")
        }

        if (operation.clearFirst) {
            AutoFlowLogger.d(TAG, "正在清空输入框文本。")
            val selectAllArgs = Bundle().apply {
                putInt(AccessibilityNodeInfo.ACTION_ARGUMENT_SELECTION_START_INT, 0)
                putInt(AccessibilityNodeInfo.ACTION_ARGUMENT_SELECTION_END_INT, nodeToInput.text?.length ?: 0)
            }
            nodeToInput.performAction(AccessibilityNodeInfo.ACTION_SET_SELECTION, selectAllArgs)
        }
        
        val arguments = Bundle().apply {
            putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, operation.text)
        }
        val success = nodeToInput.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)
        
        return if (success) {
            AutoFlowLogger.d(TAG, "输入操作成功，选择器: ${operation.selector}")
            OperationResult.Success(mapOf("inputText" to operation.text))
        } else {
            AutoFlowLogger.e(TAG, "输入操作失败，选择器: ${operation.selector}")
            OperationResult.Error("输入操作失败")
        }
    }

    private fun performScroll(operation: ScrollOperation): OperationResult {
        AutoFlowLogger.d(TAG, "尝试执行滚动操作，方向: ${operation.direction}")
        val action = when (operation.direction) {
            "up" -> AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD
            "down" -> AccessibilityNodeInfo.ACTION_SCROLL_FORWARD
            else -> {
                AutoFlowLogger.e(TAG, "滚动操作失败: 不支持的滚动方向: ${operation.direction}")
                return OperationResult.Error("不支持的滚动方向")
            }
        }
        
        val success = rootInActiveWindow?.performAction(action) ?: false
        return if (success) {
            AutoFlowLogger.d(TAG, "滚动操作成功，方向: ${operation.direction}")
            OperationResult.Success(mapOf("scrolled" to true))
        } else {
            AutoFlowLogger.e(TAG, "滚动操作失败，方向: ${operation.direction}")
            OperationResult.Error("滚动操作失败")
        }
    }

    private fun performFind(operation: FindOperation): OperationResult {
        AutoFlowLogger.d(TAG, "尝试查找选择器对应的元素: ${operation.selector}")
        val elements = AccessibilityNodeUtils.findElements(rootInActiveWindow, operation.selector)
        AutoFlowLogger.d(TAG, "找到 ${elements.size} 个选择器对应的元素: ${operation.selector}")
        return OperationResult.Success(mapOf(
            "elements" to elements,
            "count" to elements.size
        ))
    }

    private suspend fun performWait(operation: WaitOperation): OperationResult {
        AutoFlowLogger.d(TAG, "尝试等待选择器对应的元素: ${operation.selector}，最大等待时间 ${operation.maxWaitTime}ms")
        val startTime = System.currentTimeMillis()
        
        while (System.currentTimeMillis() - startTime < operation.maxWaitTime) {
            if (AccessibilityNodeUtils.findElement(rootInActiveWindow, operation.selector) != null) {
                AutoFlowLogger.d(TAG, "等待成功: 已找到选择器对应的元素: ${operation.selector}")
                return OperationResult.Success(mapOf("found" to true))
            }
            delay(operation.checkInterval)
        }
        
        AutoFlowLogger.e(TAG, "等待失败: 等待选择器对应的元素超时: ${operation.selector}")
        return OperationResult.Error("等待超时")
    }

    private suspend fun performSwipe(operation: SwipeOperation): OperationResult {
        AutoFlowLogger.d(TAG, "尝试执行滑动操作，从 (${operation.startX}, ${operation.startY}) 到 (${operation.endX}, ${operation.endY})，持续时间 ${operation.duration}ms")
        val path = Path().apply {
            moveTo(operation.startX, operation.startY)
            lineTo(operation.endX, operation.endY)
        }
        
        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, operation.duration))
            .build()
        
        return withContext(Dispatchers.Main) {
            val success = dispatchGesture(gesture, null, null)
            if (success) {
                AutoFlowLogger.d(TAG, "滑动操作成功。")
                OperationResult.Success(mapOf("swiped" to true))
            } else {
                AutoFlowLogger.e(TAG, "滑动操作失败。")
                OperationResult.Error("滑动操作失败")
            }
        }
    }

    private fun performGetText(operation: GetTextOperation): OperationResult {
        AutoFlowLogger.d(TAG, "尝试获取选择器对应的文本: ${operation.selector}")
        val node = AccessibilityNodeUtils.findElement(rootInActiveWindow, operation.selector)
        if (node == null) {
            AutoFlowLogger.e(TAG, "获取文本操作失败: 未找到选择器对应的元素: ${operation.selector}")
            return OperationResult.Error("元素未找到")
        }
        val text = node.text?.toString() ?: ""
        AutoFlowLogger.d(TAG, "成功提取文本: '$text'，选择器: ${operation.selector}")
        return OperationResult.Success(mapOf("text" to text))
    }

    private fun performCheckState(operation: CheckStateOperation): OperationResult {
        AutoFlowLogger.d(TAG, "尝试检查选择器对应的状态 '${operation.expectedState}': ${operation.selector}")
        val node = AccessibilityNodeUtils.findElement(rootInActiveWindow, operation.selector)
        if (node == null) {
            AutoFlowLogger.e(TAG, "检查状态操作失败: 未找到选择器对应的元素: ${operation.selector}")
            return OperationResult.Error("元素未找到")
        }
        
        val actualState = when (operation.expectedState) {
            "checked" -> node.isChecked
            "enabled" -> node.isEnabled
            "visible" -> node.isVisibleToUser
            else -> {
                AutoFlowLogger.e(TAG, "检查状态操作失败: 不支持的状态检查: ${operation.expectedState}")
                return OperationResult.Error("不支持的状态检查: ${operation.expectedState}")
            }
        }
        AutoFlowLogger.d(TAG, "检查选择器对应的状态: ${operation.selector}. 期望: ${operation.expectedState}, 实际: $actualState, 匹配: $actualState")
        
        return OperationResult.Success(mapOf(
            "state" to operation.expectedState,
            "actual" to actualState,
            "matches" to actualState
        ))
    }

    // 录制功能相关方法
    fun startRecording() {
        operationRecorder.startRecording()
        AutoFlowLogger.d(TAG, "开始录制操作")
    }
    
    fun stopRecording() = operationRecorder.stopRecording()
    
    fun isRecording() = operationRecorder.isRecording()
    
    fun getRecordedCount() = operationRecorder.getRecordedCount()

    }

// 操作定义
sealed class AccessibilityOperation {
    abstract val timeout: Long
    abstract val retryCount: Int
    open val selector: ElementSelector? = null
}

data class ClickOperation(
    override val selector: ElementSelector,
    val clickType: ClickType = ClickType.SINGLE,
    val clickStrategy: com.carlos.autoflow.workflow.models.ClickStrategy = com.carlos.autoflow.workflow.models.ClickStrategy.DEFAULT,
    val childConditions: List<ElementSelector> = emptyList(), // 新增子约束
    override val timeout: Long = 5000,
    override val retryCount: Int = 3
) : AccessibilityOperation()

data class InputOperation(
    override val selector: ElementSelector,
    val text: String,
    val clearFirst: Boolean = true,
    val inputStrategy: com.carlos.autoflow.workflow.models.InputStrategy = com.carlos.autoflow.workflow.models.InputStrategy.DEFAULT,
    override val timeout: Long = 5000,
    override val retryCount: Int = 3
) : AccessibilityOperation()

data class ScrollOperation(
    val direction: String,
    val distance: Int = 1,
    override val timeout: Long = 3000,
    override val retryCount: Int = 2,
    override val selector: ElementSelector? = null
) : AccessibilityOperation()

data class FindOperation(
    override val selector: ElementSelector,
    override val timeout: Long = 3000,
    override val retryCount: Int = 1
) : AccessibilityOperation()

data class WaitOperation(
    override val selector: ElementSelector,
    val maxWaitTime: Long = 10000,
    val checkInterval: Long = 500,
    override val timeout: Long = 10000,
    override val retryCount: Int = 1
) : AccessibilityOperation()

data class SwipeOperation(
    val startX: Float,
    val startY: Float,
    val endX: Float,
    val endY: Float,
    val duration: Long = 500,
    override val timeout: Long = 3000,
    override val retryCount: Int = 2,
    override val selector: ElementSelector? = null
) : AccessibilityOperation()

data class GetTextOperation(
    override val selector: ElementSelector,
    override val timeout: Long = 3000,
    override val retryCount: Int = 2
) : AccessibilityOperation()

data class CheckStateOperation(
    override val selector: ElementSelector,
    val expectedState: String, // "checked", "enabled", "visible"
    override val timeout: Long = 3000,
    override val retryCount: Int = 2
) : AccessibilityOperation()

enum class ClickType { SINGLE, LONG }

sealed class OperationResult {
    data class Success(val data: Map<String, Any>) : OperationResult()
    data class Error(val message: String) : OperationResult()
}
