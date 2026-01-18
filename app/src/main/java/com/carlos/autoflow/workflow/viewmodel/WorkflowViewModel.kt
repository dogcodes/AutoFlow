package com.carlos.autoflow.workflow.viewmodel

import androidx.lifecycle.ViewModel
import com.carlos.autoflow.workflow.models.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.*

class WorkflowViewModel : ViewModel() {
    private val _workflow = MutableStateFlow(createEmptyWorkflow())
    val workflow: StateFlow<Workflow> = _workflow

    private val _selectedNodeId = MutableStateFlow<String?>(null)
    val selectedNodeId: StateFlow<String?> = _selectedNodeId

    fun addNode(type: NodeType, x: Float, y: Float) {
        val newNode = WorkflowNode(
            id = UUID.randomUUID().toString(),
            type = type,
            title = type.displayName,
            x = x,
            y = y,
            inputs = getDefaultInputs(type),
            outputs = getDefaultOutputs(type)
        )
        
        _workflow.value = _workflow.value.copy(
            nodes = _workflow.value.nodes + newNode,
            updatedAt = System.currentTimeMillis()
        )
    }

    fun moveNode(nodeId: String, deltaX: Float, deltaY: Float) {
        val currentWorkflow = _workflow.value
        val updatedNodes = currentWorkflow.nodes.map { node ->
            if (node.id == nodeId) {
                node.copy(
                    x = node.x + deltaX,
                    y = node.y + deltaY
                )
            } else {
                node
            }
        }
        
        val updatedWorkflow = currentWorkflow.copy(nodes = updatedNodes)
        _workflow.value = updatedWorkflow
    }

    fun deleteNode(nodeId: String) {
        _workflow.value = _workflow.value.copy(
            nodes = _workflow.value.nodes.filter { it.id != nodeId },
            connections = _workflow.value.connections.filter { 
                it.sourceNodeId != nodeId && it.targetNodeId != nodeId 
            },
            updatedAt = System.currentTimeMillis()
        )
    }

    fun selectNode(nodeId: String?) {
        _selectedNodeId.value = nodeId
    }
    
    fun removeConnection(connectionId: String) {
        _workflow.value = _workflow.value.copy(
            connections = _workflow.value.connections.filter { it.id != connectionId },
            updatedAt = System.currentTimeMillis()
        )
    }
    
    private fun addConnection(sourceNodeId: String, targetNodeId: String) {
        // 避免重复连接
        val existingConnection = _workflow.value.connections.find { 
            it.sourceNodeId == sourceNodeId && it.targetNodeId == targetNodeId 
        }
        if (existingConnection != null) return
        
        val connection = WorkflowConnection(
            sourceNodeId = sourceNodeId,
            sourceOutputId = "output",
            targetNodeId = targetNodeId,
            targetInputId = "input"
        )
        
        _workflow.value = _workflow.value.copy(
            connections = _workflow.value.connections + connection,
            updatedAt = System.currentTimeMillis()
        )
    }
    
    private val _connectingNodeId = MutableStateFlow<String?>(null)
    val connectingNodeId: StateFlow<String?> = _connectingNodeId
    
    fun startConnection(nodeId: String) {
        val node = _workflow.value.nodes.find { it.id == nodeId }
        // 结束节点不能作为连接源（没有输出）
        if (node?.type == NodeType.END) return
        
        _connectingNodeId.value = nodeId
    }
    
    fun finishConnection(targetNodeId: String) {
        val sourceNodeId = _connectingNodeId.value
        if (sourceNodeId != null && sourceNodeId != targetNodeId) {
            val targetNode = _workflow.value.nodes.find { it.id == targetNodeId }
            // 开始节点不能作为连接目标（没有输入）
            if (targetNode?.type != NodeType.START) {
                addConnection(sourceNodeId, targetNodeId)
            }
        }
        _connectingNodeId.value = null
    }
    
    fun cancelConnection() {
        _connectingNodeId.value = null
    }

    private fun createEmptyWorkflow() = Workflow(
        id = UUID.randomUUID().toString(),
        name = "新建工作流",
        nodes = emptyList(),
        connections = emptyList()
    )

    private fun getDefaultInputs(type: NodeType): List<NodeInput> = when (type) {
        NodeType.START -> emptyList()
        NodeType.END -> listOf(NodeInput("input", "输入", "any"))
        NodeType.HTTP_REQUEST -> listOf(
            NodeInput("url", "URL", "string", true),
            NodeInput("method", "方法", "string")
        )
        NodeType.DATA_TRANSFORM -> listOf(NodeInput("data", "数据", "any", true))
        NodeType.CONDITION -> listOf(NodeInput("condition", "条件", "boolean", true))
        NodeType.LOOP -> listOf(NodeInput("items", "循环项", "array", true))
        NodeType.DELAY -> listOf(NodeInput("duration", "延时(ms)", "number", true))
        NodeType.SCRIPT -> listOf(NodeInput("code", "代码", "string", true))
    }

    private fun getDefaultOutputs(type: NodeType): List<NodeOutput> = when (type) {
        NodeType.START -> listOf(NodeOutput("output", "输出", "any"))
        NodeType.END -> emptyList()
        NodeType.HTTP_REQUEST -> listOf(
            NodeOutput("response", "响应", "object"),
            NodeOutput("status", "状态码", "number")
        )
        NodeType.DATA_TRANSFORM -> listOf(NodeOutput("result", "结果", "any"))
        NodeType.CONDITION -> listOf(
            NodeOutput("true", "真", "any"),
            NodeOutput("false", "假", "any")
        )
        NodeType.LOOP -> listOf(NodeOutput("output", "输出", "any"))
        NodeType.DELAY -> listOf(NodeOutput("output", "输出", "any"))
        NodeType.SCRIPT -> listOf(NodeOutput("result", "结果", "any"))
    }
}
