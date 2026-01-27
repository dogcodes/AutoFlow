package com.carlos.autoflow.workflow.models

import java.text.SimpleDateFormat
import java.util.*

data class WorkflowExecution(
    val id: String = UUID.randomUUID().toString(),
    val workflowId: String,
    val workflowName: String,
    val startTime: Long = System.currentTimeMillis(),
    val endTime: Long? = null,
    val status: ExecutionStatus,
    val result: String,
    val nodeResults: List<NodeExecutionResult> = emptyList()
) {
    val duration: Long
        get() = (endTime ?: System.currentTimeMillis()) - startTime
        
    val formattedStartTime: String
        get() = SimpleDateFormat("MM-dd HH:mm:ss", Locale.getDefault()).format(Date(startTime))
}

data class NodeExecutionResult(
    val nodeId: String,
    val nodeName: String,
    val status: ExecutionStatus,
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
)

enum class ExecutionStatus {
    RUNNING,
    SUCCESS,
    FAILED,
    STOPPED
}
