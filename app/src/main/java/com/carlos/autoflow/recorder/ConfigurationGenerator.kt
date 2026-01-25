package com.carlos.autoflow.recorder

import com.carlos.autoflow.workflow.models.*
import com.google.gson.Gson
import java.util.*

/**
 * 配置文件生成器
 */
class ConfigurationGenerator {
    
    private val gson = Gson()
    
    /**
     * 将录制的操作转换为工作流配置
     */
    fun generateWorkflow(
        operations: List<RecordedOperation>,
        workflowName: String = "录制的工作流"
    ): Workflow {
        val nodes = mutableListOf<WorkflowNode>()
        val connections = mutableListOf<WorkflowConnection>()
        
        // 添加开始节点
        val startNode = createStartNode()
        nodes.add(startNode)
        
        var previousNode = startNode
        
        // 为每个操作创建对应的节点
        operations.forEachIndexed { index, operation ->
            val node = createNodeFromOperation(operation, index)
            nodes.add(node)
            
            // 创建连接
            val connection = WorkflowConnection(
                sourceNodeId = previousNode.id,
                sourceOutputId = "output",
                targetNodeId = node.id,
                targetInputId = "input"
            )
            connections.add(connection)
            
            previousNode = node
        }
        
        // 添加结束节点
        val endNode = createEndNode(operations.size)
        nodes.add(endNode)
        
        // 连接最后一个操作节点到结束节点
        if (operations.isNotEmpty()) {
            val finalConnection = WorkflowConnection(
                sourceNodeId = previousNode.id,
                sourceOutputId = "output",
                targetNodeId = endNode.id,
                targetInputId = "input"
            )
            connections.add(finalConnection)
        }
        
        return Workflow(
            name = workflowName,
            description = "通过录制生成的工作流，包含${operations.size}个操作",
            nodes = nodes,
            connections = connections
        )
    }
    
    /**
     * 生成JSON配置字符串
     */
    fun generateJson(operations: List<RecordedOperation>, workflowName: String = "录制的工作流"): String {
        val workflow = generateWorkflow(operations, workflowName)
        return gson.toJson(workflow)
    }
    
    private fun createStartNode(): WorkflowNode {
        return WorkflowNode(
            type = NodeType.START,
            title = "开始",
            x = 100f,
            y = 100f,
            outputs = listOf(NodeOutput("output", "输出", "any"))
        )
    }
    
    private fun createEndNode(operationCount: Int): WorkflowNode {
        return WorkflowNode(
            type = NodeType.END,
            title = "结束",
            x = 100f,
            y = 200f + operationCount * 150f,
            inputs = listOf(NodeInput("input", "输入", "any"))
        )
    }
    
    private fun createNodeFromOperation(operation: RecordedOperation, index: Int): WorkflowNode {
        return when (operation.type) {
            OperationType.CLICK -> createClickNode(operation, index)
            OperationType.LONG_CLICK -> createLongClickNode(operation, index)
            OperationType.INPUT -> createInputNode(operation, index)
            OperationType.SCROLL -> createScrollNode(operation, index)
            OperationType.SWIPE -> createSwipeNode(operation, index)
        }
    }
    
    private fun createClickNode(operation: RecordedOperation, index: Int): WorkflowNode {
        val selectorString = operation.element?.let { element ->
            when (element.selector) {
                is ElementSelector.ById -> "id=${element.selector.resourceId}"
                is ElementSelector.ByText -> "text=${element.selector.text}"
                is ElementSelector.ByDescription -> "desc=${element.selector.description}"
                is ElementSelector.ByClassName -> "class=${element.selector.className}"
                is ElementSelector.ByCoordinate -> "${element.selector.x},${element.selector.y}"
            }
        } ?: operation.coordinates?.let { "${it.first},${it.second}" } ?: ""
        
        return WorkflowNode(
            type = NodeType.UI_CLICK,
            title = "点击操作 ${index + 1}",
            x = 100f,
            y = 250f + index * 150f,
            inputs = listOf(NodeInput("input", "输入", "any")),
            outputs = listOf(NodeOutput("output", "输出", "any")),
            config = mapOf(
                "selector" to selectorString,
                "clickType" to "SINGLE"
            )
        )
    }
    
    private fun createLongClickNode(operation: RecordedOperation, index: Int): WorkflowNode {
        val selectorString = operation.element?.let { element ->
            when (element.selector) {
                is ElementSelector.ById -> "id=${element.selector.resourceId}"
                is ElementSelector.ByText -> "text=${element.selector.text}"
                is ElementSelector.ByDescription -> "desc=${element.selector.description}"
                is ElementSelector.ByClassName -> "class=${element.selector.className}"
                is ElementSelector.ByCoordinate -> "${element.selector.x},${element.selector.y}"
            }
        } ?: ""
        
        return WorkflowNode(
            type = NodeType.UI_LONG_CLICK,
            title = "长按操作 ${index + 1}",
            x = 100f,
            y = 250f + index * 150f,
            inputs = listOf(NodeInput("input", "输入", "any")),
            outputs = listOf(NodeOutput("output", "输出", "any")),
            config = mapOf("selector" to selectorString)
        )
    }
    
    private fun createInputNode(operation: RecordedOperation, index: Int): WorkflowNode {
        val selectorString = operation.element?.let { element ->
            when (element.selector) {
                is ElementSelector.ById -> "id=${element.selector.resourceId}"
                is ElementSelector.ByText -> "text=${element.selector.text}"
                is ElementSelector.ByDescription -> "desc=${element.selector.description}"
                is ElementSelector.ByClassName -> "class=${element.selector.className}"
                is ElementSelector.ByCoordinate -> "${element.selector.x},${element.selector.y}"
            }
        } ?: ""
        
        return WorkflowNode(
            type = NodeType.UI_INPUT,
            title = "输入操作 ${index + 1}",
            x = 100f,
            y = 250f + index * 150f,
            inputs = listOf(NodeInput("input", "输入", "any")),
            outputs = listOf(NodeOutput("output", "输出", "any")),
            config = mapOf(
                "selector" to selectorString,
                "text" to (operation.text ?: ""),
                "clearFirst" to true
            )
        )
    }
    
    private fun createScrollNode(operation: RecordedOperation, index: Int): WorkflowNode {
        return WorkflowNode(
            type = NodeType.UI_SCROLL,
            title = "滚动操作 ${index + 1}",
            x = 100f,
            y = 250f + index * 150f,
            inputs = listOf(NodeInput("input", "输入", "any")),
            outputs = listOf(NodeOutput("output", "输出", "any")),
            config = mapOf(
                "direction" to (operation.scrollDirection ?: "down"),
                "distance" to 1
            )
        )
    }
    
    private fun createSwipeNode(operation: RecordedOperation, index: Int): WorkflowNode {
        val coordinates = operation.coordinates ?: Pair(0f, 0f)
        
        return WorkflowNode(
            type = NodeType.UI_SWIPE,
            title = "滑动操作 ${index + 1}",
            x = 100f,
            y = 250f + index * 150f,
            inputs = listOf(NodeInput("input", "输入", "any")),
            outputs = listOf(NodeOutput("output", "输出", "any")),
            config = mapOf(
                "startX" to coordinates.first,
                "startY" to coordinates.second,
                "endX" to coordinates.first + 100f,
                "endY" to coordinates.second,
                "duration" to 500L
            )
        )
    }
    
    /**
     * 优化操作序列（去重、合并等）
     */
    fun optimizeOperations(operations: List<RecordedOperation>): List<RecordedOperation> {
        val optimized = mutableListOf<RecordedOperation>()
        
        operations.forEach { operation ->
            // 简单的去重逻辑：如果与上一个操作相同且时间间隔很短，则跳过
            val lastOperation = optimized.lastOrNull()
            if (lastOperation == null || 
                !isSimilarOperation(lastOperation, operation) ||
                operation.timestamp - lastOperation.timestamp > 1000) {
                optimized.add(operation)
            }
        }
        
        return optimized
    }
    
    private fun isSimilarOperation(op1: RecordedOperation, op2: RecordedOperation): Boolean {
        return op1.type == op2.type && 
               op1.element?.selector == op2.element?.selector &&
               op1.text == op2.text
    }
}
