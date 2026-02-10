package com.carlos.autoflow.workflow.models

import java.util.*

// 元素选择器系统
sealed class ElementSelector {
    data class ById(val resourceId: String) : ElementSelector()
    data class ByText(val text: String, val exact: Boolean = false) : ElementSelector()
    data class ByDescription(val description: String) : ElementSelector()
    data class ByClassName(val className: String) : ElementSelector()
    data class ByCoordinate(val x: Float, val y: Float) : ElementSelector()
    
    companion object {
        fun parse(selector: String): ElementSelector {
            return when {
                selector.startsWith("id=") -> ById(selector.substring(3))
                selector.startsWith("text=") -> ByText(selector.substring(5))
                selector.startsWith("desc=") -> ByDescription(selector.substring(5))
                selector.startsWith("class=") -> ByClassName(selector.substring(6))
                selector.contains(",") -> {
                    val coords = selector.split(",")
                    ByCoordinate(coords[0].toFloat(), coords[1].toFloat())
                }
                else -> ByText(selector) // 默认按文本查找
            }
        }
    }
}

data class WorkflowNode(
    val id: String = UUID.randomUUID().toString(),
    val type: NodeType,
    val title: String,
    val x: Float = 0f,
    val y: Float = 0f,
    val inputs: List<NodeInput> = emptyList(),
    val outputs: List<NodeOutput> = emptyList(),
    val config: Map<String, Any> = emptyMap()
)

data class NodeInput(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val type: String,
    val required: Boolean = false
)

data class NodeOutput(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val type: String
)

data class WorkflowConnection(
    val id: String = UUID.randomUUID().toString(),
    val sourceNodeId: String,
    val sourceOutputId: String,
    val targetNodeId: String,
    val targetInputId: String
)

data class Workflow(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String = "",
    val nodes: List<WorkflowNode>,
    val connections: List<WorkflowConnection>,
    val version: String = "1.0",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

enum class NodeType(val displayName: String, val category: NodeCategory, val description: String) {
    // 控制流节点
    START("开始", NodeCategory.CONTROL_FLOW, "工作流起点"),
    END("结束", NodeCategory.CONTROL_FLOW, "工作流终点"),
    CONDITION("条件判断", NodeCategory.CONTROL_FLOW, "分支逻辑控制"),
    LOOP("循环", NodeCategory.CONTROL_FLOW, "重复执行操作"),
    DELAY("延时", NodeCategory.CONTROL_FLOW, "等待指定时间"),
    SCRIPT("脚本执行", NodeCategory.CONTROL_FLOW, "自定义代码执行"),
    
    // UI交互节点
    UI_CLICK("点击操作", NodeCategory.UI_INTERACTION, "模拟点击UI元素"),
    UI_INPUT("文本输入", NodeCategory.UI_INTERACTION, "输入文本内容"),
    UI_SCROLL("滚动操作", NodeCategory.UI_INTERACTION, "滚动页面或列表"),
    UI_LONG_CLICK("长按操作", NodeCategory.UI_INTERACTION, "长按UI元素"),
    UI_SWIPE("滑动操作", NodeCategory.UI_INTERACTION, "滑动手势操作"),
    
    // UI检测节点
    UI_FIND("查找元素", NodeCategory.UI_DETECTION, "查找页面元素"),
    UI_WAIT("等待元素", NodeCategory.UI_DETECTION, "等待元素出现"),
    UI_GET_TEXT("获取文本", NodeCategory.UI_DETECTION, "获取元素文本"),
    UI_CHECK("检查状态", NodeCategory.UI_DETECTION, "检查元素状态"),
    
    // 系统事件节点
    APP_LAUNCH("应用启动", NodeCategory.SYSTEM_EVENT, "检测应用启动"),
    LAUNCH_ACTIVITY("启动应用Activity", NodeCategory.SYSTEM_EVENT, "启动指定的Android Activity"),
    NOTIFICATION("通知处理", NodeCategory.SYSTEM_EVENT, "处理系统通知"),
    SCREEN_STATE("屏幕状态", NodeCategory.SYSTEM_EVENT, "检测屏幕开关"),
    
    // 网络节点
    HTTP_REQUEST("HTTP请求", NodeCategory.NETWORK, "发送网络请求"),
    DATA_TRANSFORM("数据转换", NodeCategory.NETWORK, "处理响应数据")
}

enum class NodeCategory(val displayName: String, val color: String) {
    CONTROL_FLOW("控制流", "#2196F3"),      // 蓝色
    UI_INTERACTION("UI交互", "#4CAF50"),    // 绿色
    UI_DETECTION("UI检测", "#FF9800"),      // 橙色
    SYSTEM_EVENT("系统事件", "#9C27B0"),    // 紫色
    NETWORK("网络", "#F44336")              // 红色
}
