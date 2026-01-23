package com.carlos.autoflow.workflow.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carlos.autoflow.workflow.models.*
import com.carlos.autoflow.workflow.ui.ExecutionStatusManager
import com.carlos.autoflow.workflow.ui.NodeExecutionState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*
import com.google.gson.Gson
import okhttp3.*
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.text.SimpleDateFormat

class WorkflowViewModel : ViewModel() {
    private val _workflow = MutableStateFlow(createEmptyWorkflow())
    val workflow: StateFlow<Workflow> = _workflow
    
    // 执行状态管理
    private val executionStatusManager = ExecutionStatusManager()
    val executingNodes: StateFlow<Map<String, NodeExecutionState>> = 
        MutableStateFlow(executionStatusManager.executingNodes).apply {
            // 监听状态变化
        }

    private val _selectedNodeId = MutableStateFlow<String?>(null)
    val selectedNodeId: StateFlow<String?> = _selectedNodeId
    
    // 第二阶段新增：JSON和HTTP支持
    private val gson = Gson()
    private val httpClient = OkHttpClient()

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
    
    fun updateNodeConfig(nodeId: String, config: Map<String, Any>) {
        val currentWorkflow = _workflow.value
        val updatedNodes = currentWorkflow.nodes.map { node ->
            if (node.id == nodeId) {
                node.copy(config = config)
            } else {
                node
            }
        }
        
        _workflow.value = currentWorkflow.copy(
            nodes = updatedNodes,
            updatedAt = System.currentTimeMillis()
        )
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
        // UI交互节点
        NodeType.UI_CLICK -> listOf(
            NodeInput("selector", "元素选择器", "string", true),
            NodeInput("clickType", "点击类型", "string")
        )
        NodeType.UI_INPUT -> listOf(
            NodeInput("selector", "元素选择器", "string", true),
            NodeInput("text", "输入文本", "string", true),
            NodeInput("clearFirst", "先清空", "boolean")
        )
        NodeType.UI_SCROLL -> listOf(
            NodeInput("direction", "滚动方向", "string", true),
            NodeInput("distance", "滚动距离", "number")
        )
        // UI检测节点
        NodeType.UI_FIND -> listOf(
            NodeInput("selector", "查找条件", "string", true),
            NodeInput("multiple", "查找多个", "boolean")
        )
        NodeType.UI_WAIT -> listOf(
            NodeInput("selector", "等待元素", "string", true),
            NodeInput("timeout", "超时时间(ms)", "number", true)
        )
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
        // UI交互节点
        NodeType.UI_CLICK -> listOf(
            NodeOutput("success", "执行成功", "boolean"),
            NodeOutput("element", "目标元素", "object")
        )
        NodeType.UI_INPUT -> listOf(
            NodeOutput("success", "输入成功", "boolean"),
            NodeOutput("inputText", "输入内容", "string")
        )
        NodeType.UI_SCROLL -> listOf(
            NodeOutput("success", "滚动成功", "boolean"),
            NodeOutput("scrolled", "滚动距离", "number")
        )
        // UI检测节点
        NodeType.UI_FIND -> listOf(
            NodeOutput("elements", "找到的元素", "array"),
            NodeOutput("count", "元素数量", "number")
        )
        NodeType.UI_WAIT -> listOf(
            NodeOutput("found", "找到元素", "boolean"),
            NodeOutput("element", "目标元素", "object")
        )
    }
    
    // 第二阶段新增功能：配置管理
    fun exportToJson(): String = gson.toJson(_workflow.value)

    fun importFromJson(json: String): Boolean {
        return try {
            val workflow = gson.fromJson(json, Workflow::class.java)
            _workflow.value = workflow.copy(updatedAt = System.currentTimeMillis())
            true
        } catch (e: Exception) {
            false
        }
    }

    fun loadSampleWorkflow() {
        val sampleWorkflow = Workflow(
            id = "sample-workflow",
            name = "示例工作流",
            description = "获取GitHub用户信息的示例工作流",
            nodes = listOf(
                WorkflowNode(
                    id = "start",
                    type = NodeType.START,
                    title = "开始",
                    x = 100f, y = 200f,
                    outputs = listOf(NodeOutput("out", "输出", "any"))
                ),
                WorkflowNode(
                    id = "http",
                    type = NodeType.HTTP_REQUEST,
                    title = "获取GitHub用户",
                    x = 350f, y = 200f,
                    inputs = listOf(NodeInput("in", "输入", "any", true)),
                    outputs = listOf(NodeOutput("response", "响应", "object")),
                    config = mapOf(
                        "url" to "https://api.github.com/users/xbdcc",
                        "method" to "GET"
                    )
                ),
                WorkflowNode(
                    id = "end",
                    type = NodeType.END,
                    title = "结束",
                    x = 600f, y = 200f,
                    inputs = listOf(NodeInput("in", "输入", "any"))
                )
            ),
            connections = listOf(
                WorkflowConnection("c1", "start", "out", "http", "in"),
                WorkflowConnection("c2", "http", "response", "end", "in")
            )
        )
        
        _workflow.value = sampleWorkflow
    }
    
    fun loadFromUrl(url: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                println("开始从URL加载: $url")
                val request = Request.Builder()
                    .url(url)
                    .addHeader("User-Agent", "AutoFlow/1.0")
                    .addHeader("Accept", "application/json, text/plain, */*")
                    .build()
                
                val response = httpClient.newCall(request).execute()
                
                println("响应状态码: ${response.code}")
                
                if (response.isSuccessful) {
                    val json = response.body?.string() ?: ""
                    println("响应内容长度: ${json.length}")
                    println("响应内容预览: ${json.take(200)}")
                    
                    if (json.isBlank()) {
                        withContext(Dispatchers.Main) {
                            onResult(false, "响应内容为空")
                        }
                        return@launch
                    }
                    
                    withContext(Dispatchers.Main) {
                        val success = importFromJson(json)
                        onResult(success, if (success) null else "JSON解析失败")
                    }
                } else {
                    val errorBody = response.body?.string() ?: ""
                    println("错误响应: $errorBody")
                    withContext(Dispatchers.Main) {
                        onResult(false, "HTTP ${response.code}: ${response.message}")
                    }
                }
            } catch (e: Exception) {
                println("URL加载异常: ${e.javaClass.simpleName} - ${e.message}")
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    onResult(false, "${e.javaClass.simpleName}: ${e.message}")
                }
            }
        }
    }
    
    // 工作流执行引擎
    fun executeWorkflow(onResult: (String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = StringBuilder()
                result.appendLine("🚀 开始执行工作流: ${_workflow.value.name}")
                result.appendLine("⏰ 执行时间: ${SimpleDateFormat("HH:mm:ss").format(System.currentTimeMillis())}")
                result.appendLine()
                
                val executedNodes = mutableSetOf<String>()
                val startNode = _workflow.value.nodes.find { it.type == NodeType.START }
                
                if (startNode != null) {
                    executeNode(startNode, result, executedNodes)
                }
                
                result.appendLine("✅ 工作流执行完成")
                withContext(Dispatchers.Main) {
                    onResult(result.toString())
                }
                
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onResult("❌ 执行失败: ${e.message}")
                }
            }
        }
    }

    private suspend fun executeNode(
        node: WorkflowNode, 
        result: StringBuilder, 
        executed: MutableSet<String>
    ) {
        if (node.id in executed) return
        executed.add(node.id)
        
        result.appendLine("📍 执行节点: ${node.title} (${node.type.displayName})")
        
        when (node.type) {
            NodeType.START -> {
                result.appendLine("   ▶️ 工作流启动")
            }
            NodeType.HTTP_REQUEST -> {
                val url = node.config["url"] as? String ?: "未配置URL"
                val method = node.config["method"] as? String ?: "GET"
                result.appendLine("   🌐 请求URL: $url")
                result.appendLine("   📋 请求方法: $method")
                
                try {
                    val requestBuilder = Request.Builder().url(url)
                    
                    // 添加User-Agent避免被拒绝
                    requestBuilder.addHeader("User-Agent", "AutoFlow/1.0")
                    
                    when (method.uppercase()) {
                        "GET" -> requestBuilder.get()
                        "POST" -> requestBuilder.post("".toRequestBody())
                        else -> requestBuilder.get()
                    }
                    
                    val request = requestBuilder.build()
                    result.appendLine("   ⏳ 发送请求...")
                    
                    // 直接执行网络请求（已在IO线程中）
                    val response = httpClient.newCall(request).execute()
                    val responseBody = response.body?.string() ?: ""
                    
                    result.appendLine("   📊 状态码: ${response.code}")
                    result.appendLine("   📄 响应长度: ${responseBody.length} 字符")
                    
                    if (response.isSuccessful) {
                        result.appendLine("   ✅ 请求成功")
                        if (responseBody.length <= 200) {
                            result.appendLine("   📝 响应内容: $responseBody")
                        } else {
                            result.appendLine("   📝 响应预览: ${responseBody.take(100)}...")
                        }
                    } else {
                        result.appendLine("   ⚠️ 请求失败: HTTP ${response.code}")
                    }
                    
                } catch (e: Exception) {
                    result.appendLine("   ❌ 请求异常: ${e.javaClass.simpleName}")
                    result.appendLine("   💬 错误信息: ${e.message}")
                    e.printStackTrace()
                }
            }
            NodeType.END -> {
                result.appendLine("   🏁 工作流结束")
            }
            // UI交互节点
            NodeType.UI_CLICK -> {
                val selector = node.config["selector"] as? String ?: ""
                val clickType = node.config["clickType"] as? String ?: "SINGLE"
                
                result.appendLine("   👆 点击操作: $selector")
                result.appendLine("   🔧 点击类型: $clickType")
                
                if (!com.carlos.autoflow.accessibility.AutoFlowAccessibilityService.isServiceEnabled()) {
                    result.appendLine("   ❌ 无障碍服务未启用")
                } else {
                    try {
                        val elementSelector = com.carlos.autoflow.workflow.models.ElementSelector.parse(selector)
                        val operation = com.carlos.autoflow.accessibility.ClickOperation(
                            elementSelector, 
                            com.carlos.autoflow.accessibility.ClickType.valueOf(clickType)
                        )
                        
                        val operationResult = com.carlos.autoflow.accessibility.AutoFlowAccessibilityService
                            .getInstance()?.executeOperation(operation)
                        
                        when (operationResult) {
                            is com.carlos.autoflow.accessibility.OperationResult.Success -> 
                                result.appendLine("   ✅ 点击成功")
                            is com.carlos.autoflow.accessibility.OperationResult.Error -> 
                                result.appendLine("   ❌ 点击失败: ${operationResult.message}")
                            null -> result.appendLine("   ❌ 服务不可用")
                        }
                    } catch (e: Exception) {
                        result.appendLine("   ❌ 操作异常: ${e.message}")
                    }
                }
            }
            NodeType.UI_INPUT -> {
                val selector = node.config["selector"] as? String ?: ""
                val text = node.config["text"] as? String ?: ""
                val clearFirst = node.config["clearFirst"] as? Boolean ?: true
                
                result.appendLine("   ⌨️ 输入操作: $selector")
                result.appendLine("   📝 输入内容: $text")
                
                if (!com.carlos.autoflow.accessibility.AutoFlowAccessibilityService.isServiceEnabled()) {
                    result.appendLine("   ❌ 无障碍服务未启用")
                } else {
                    try {
                        val elementSelector = com.carlos.autoflow.workflow.models.ElementSelector.parse(selector)
                        val operation = com.carlos.autoflow.accessibility.InputOperation(
                            elementSelector, text, clearFirst
                        )
                        
                        val operationResult = com.carlos.autoflow.accessibility.AutoFlowAccessibilityService
                            .getInstance()?.executeOperation(operation)
                        
                        when (operationResult) {
                            is com.carlos.autoflow.accessibility.OperationResult.Success -> 
                                result.appendLine("   ✅ 输入成功")
                            is com.carlos.autoflow.accessibility.OperationResult.Error -> 
                                result.appendLine("   ❌ 输入失败: ${operationResult.message}")
                            null -> result.appendLine("   ❌ 服务不可用")
                        }
                    } catch (e: Exception) {
                        result.appendLine("   ❌ 操作异常: ${e.message}")
                    }
                }
            }
            NodeType.UI_SCROLL -> {
                val direction = node.config["direction"] as? String ?: "down"
                val distance = node.config["distance"] as? Number ?: 1
                
                result.appendLine("   📜 滚动操作: $direction")
                
                if (!com.carlos.autoflow.accessibility.AutoFlowAccessibilityService.isServiceEnabled()) {
                    result.appendLine("   ❌ 无障碍服务未启用")
                } else {
                    try {
                        val operation = com.carlos.autoflow.accessibility.ScrollOperation(
                            direction, distance.toInt()
                        )
                        
                        val operationResult = com.carlos.autoflow.accessibility.AutoFlowAccessibilityService
                            .getInstance()?.executeOperation(operation)
                        
                        when (operationResult) {
                            is com.carlos.autoflow.accessibility.OperationResult.Success -> 
                                result.appendLine("   ✅ 滚动成功")
                            is com.carlos.autoflow.accessibility.OperationResult.Error -> 
                                result.appendLine("   ❌ 滚动失败: ${operationResult.message}")
                            null -> result.appendLine("   ❌ 服务不可用")
                        }
                    } catch (e: Exception) {
                        result.appendLine("   ❌ 操作异常: ${e.message}")
                    }
                }
            }
            NodeType.UI_FIND -> {
                val selector = node.config["selector"] as? String ?: ""
                
                result.appendLine("   🔍 查找元素: $selector")
                
                if (!com.carlos.autoflow.accessibility.AutoFlowAccessibilityService.isServiceEnabled()) {
                    result.appendLine("   ❌ 无障碍服务未启用")
                } else {
                    try {
                        val elementSelector = com.carlos.autoflow.workflow.models.ElementSelector.parse(selector)
                        val operation = com.carlos.autoflow.accessibility.FindOperation(elementSelector)
                        
                        val operationResult = com.carlos.autoflow.accessibility.AutoFlowAccessibilityService
                            .getInstance()?.executeOperation(operation)
                        
                        when (operationResult) {
                            is com.carlos.autoflow.accessibility.OperationResult.Success -> {
                                val count = operationResult.data["count"] as? Int ?: 0
                                result.appendLine("   ✅ 找到 $count 个元素")
                            }
                            is com.carlos.autoflow.accessibility.OperationResult.Error -> 
                                result.appendLine("   ❌ 查找失败: ${operationResult.message}")
                            null -> result.appendLine("   ❌ 服务不可用")
                        }
                    } catch (e: Exception) {
                        result.appendLine("   ❌ 操作异常: ${e.message}")
                    }
                }
            }
            NodeType.UI_WAIT -> {
                val selector = node.config["selector"] as? String ?: ""
                val timeout = node.config["timeout"] as? Number ?: 10000
                
                result.appendLine("   ⏳ 等待元素: $selector")
                
                if (!com.carlos.autoflow.accessibility.AutoFlowAccessibilityService.isServiceEnabled()) {
                    result.appendLine("   ❌ 无障碍服务未启用")
                } else {
                    try {
                        val elementSelector = com.carlos.autoflow.workflow.models.ElementSelector.parse(selector)
                        val operation = com.carlos.autoflow.accessibility.WaitOperation(
                            elementSelector, timeout.toLong()
                        )
                        
                        val operationResult = com.carlos.autoflow.accessibility.AutoFlowAccessibilityService
                            .getInstance()?.executeOperation(operation)
                        
                        when (operationResult) {
                            is com.carlos.autoflow.accessibility.OperationResult.Success -> 
                                result.appendLine("   ✅ 元素已出现")
                            is com.carlos.autoflow.accessibility.OperationResult.Error -> 
                                result.appendLine("   ❌ 等待超时: ${operationResult.message}")
                            null -> result.appendLine("   ❌ 服务不可用")
                        }
                    } catch (e: Exception) {
                        result.appendLine("   ❌ 操作异常: ${e.message}")
                    }
                }
            }
            else -> {
                result.appendLine("   ⚙️ 节点处理完成")
            }
        }
        
        result.appendLine()
        
        // 执行下一个连接的节点
        val connections = _workflow.value.connections.filter { it.sourceNodeId == node.id }
        connections.forEach { connection ->
            val nextNode = _workflow.value.nodes.find { it.id == connection.targetNodeId }
            if (nextNode != null) {
                executeNode(nextNode, result, executed)
            }
        }
    }
}
