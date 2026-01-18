package com.carlos.autoflow.workflow.models

import java.util.*

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

enum class NodeType(val displayName: String) {
    START("开始"),
    END("结束"),
    HTTP_REQUEST("HTTP请求"),
    DATA_TRANSFORM("数据转换"),
    CONDITION("条件判断"),
    LOOP("循环"),
    DELAY("延时"),
    SCRIPT("脚本执行")
}
