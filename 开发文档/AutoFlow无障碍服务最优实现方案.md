# AutoFlow 无障碍服务最优实现方案

## 📋 概述

本文档整合了第三阶段详细步骤、无障碍参考文档和当前实现的优势，提供一个完整的无障碍服务实现方案。

## 🏗️ 系统架构

### 三层架构设计

```
┌─────────────────────────────────────┐
│        用户界面层 (UI Layer)          │
│  - 可视化工作流编辑器                 │
│  - 节点配置面板                      │
│  - 元素拾取器                        │
│  - 执行状态监控                      │
└─────────────────────────────────────┘
                    ↕
┌─────────────────────────────────────┐
│      工作流引擎层 (Engine Layer)      │
│  - 工作流解析与执行                   │
│  - 节点调度管理                      │
│  - 数据流处理                        │
│  - 错误处理与重试                    │
└─────────────────────────────────────┘
                    ↕
┌─────────────────────────────────────┐
│    无障碍服务层 (Service Layer)       │
│  - AccessibilityService             │
│  - UI元素操作                        │
│  - 事件监听与处理                    │
│  - 系统权限管理                      │
└─────────────────────────────────────┘
```

## 🎯 节点库设计

### 节点分类系统

```kotlin
enum class NodeCategory(val displayName: String, val color: String) {
    CONTROL_FLOW("控制流", "#2196F3"),      // 蓝色
    UI_INTERACTION("UI交互", "#4CAF50"),    // 绿色
    UI_DETECTION("UI检测", "#FF9800"),      // 橙色
    SYSTEM_EVENT("系统事件", "#9C27B0"),    // 紫色
    NETWORK("网络", "#F44336")              // 红色
}

enum class NodeType(
    val displayName: String, 
    val category: NodeCategory,
    val description: String
) {
    // 控制流节点
    START("开始", CONTROL_FLOW, "工作流起点"),
    END("结束", CONTROL_FLOW, "工作流终点"),
    CONDITION("条件判断", CONTROL_FLOW, "分支逻辑控制"),
    LOOP("循环", CONTROL_FLOW, "重复执行操作"),
    DELAY("延时", CONTROL_FLOW, "等待指定时间"),
    SCRIPT("脚本", CONTROL_FLOW, "自定义代码执行"),
    
    // UI交互节点
    UI_CLICK("点击操作", UI_INTERACTION, "模拟点击UI元素"),
    UI_INPUT("文本输入", UI_INTERACTION, "输入文本内容"),
    UI_SCROLL("滚动操作", UI_INTERACTION, "滚动页面或列表"),
    UI_LONG_CLICK("长按操作", UI_INTERACTION, "长按UI元素"),
    UI_SWIPE("滑动操作", UI_INTERACTION, "滑动手势操作"),
    
    // UI检测节点
    UI_FIND("查找元素", UI_DETECTION, "查找页面元素"),
    UI_WAIT("等待元素", UI_DETECTION, "等待元素出现"),
    UI_GET_TEXT("获取文本", UI_DETECTION, "获取元素文本"),
    UI_CHECK("检查状态", UI_DETECTION, "检查元素状态"),
    
    // 系统事件节点
    APP_LAUNCH("应用启动", SYSTEM_EVENT, "检测应用启动"),
    NOTIFICATION("通知处理", SYSTEM_EVENT, "处理系统通知"),
    SCREEN_STATE("屏幕状态", SYSTEM_EVENT, "检测屏幕开关"),
    
    // 网络节点
    HTTP_REQUEST("HTTP请求", NETWORK, "发送网络请求"),
    DATA_TRANSFORM("数据转换", NETWORK, "处理响应数据")
}
```

### 元素定位系统

```kotlin
sealed class ElementSelector {
    data class ById(val resourceId: String) : ElementSelector()
    data class ByText(val text: String, val exact: Boolean = false) : ElementSelector()
    data class ByDescription(val description: String) : ElementSelector()
    data class ByClassName(val className: String) : ElementSelector()
    data class ByXPath(val xpath: String) : ElementSelector()
    data class ByCoordinate(val x: Float, val y: Float) : ElementSelector()
    
    companion object {
        fun parse(selector: String): ElementSelector {
            return when {
                selector.startsWith("id=") -> ById(selector.substring(3))
                selector.startsWith("text=") -> ByText(selector.substring(5))
                selector.startsWith("desc=") -> ByDescription(selector.substring(5))
                selector.startsWith("class=") -> ByClassName(selector.substring(6))
                selector.startsWith("xpath=") -> ByXPath(selector.substring(6))
                selector.contains(",") -> {
                    val coords = selector.split(",")
                    ByCoordinate(coords[0].toFloat(), coords[1].toFloat())
                }
                else -> ByText(selector) // 默认按文本查找
            }
        }
    }
}
```

## 🔧 核心组件实现

### 1. 增强的无障碍服务

```kotlin
class AutoFlowAccessibilityService : AccessibilityService() {
    
    companion object {
        private var instance: AutoFlowAccessibilityService? = null
        fun getInstance(): AutoFlowAccessibilityService? = instance
        fun isServiceEnabled(): Boolean = instance != null
    }

    private val operationQueue = mutableListOf<AccessibilityOperation>()
    private var isExecuting = false

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        Log.d(TAG, "无障碍服务已连接")
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
        Log.d(TAG, "无障碍服务已断开")
    }

    // 队列化操作执行
    suspend fun executeOperation(operation: AccessibilityOperation): OperationResult {
        return withContext(Dispatchers.IO) {
            try {
                when (operation) {
                    is ClickOperation -> performClick(operation)
                    is InputOperation -> performInput(operation)
                    is ScrollOperation -> performScroll(operation)
                    is FindOperation -> performFind(operation)
                    is WaitOperation -> performWait(operation)
                }
            } catch (e: Exception) {
                OperationResult.Error(e.message ?: "操作失败")
            }
        }
    }

    private fun performClick(operation: ClickOperation): OperationResult {
        val node = findElement(operation.selector) ?: return OperationResult.Error("元素未找到")
        
        val success = when (operation.clickType) {
            ClickType.SINGLE -> node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            ClickType.DOUBLE -> {
                node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                Thread.sleep(100)
                node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            }
            ClickType.LONG -> node.performAction(AccessibilityNodeInfo.ACTION_LONG_CLICK)
        }
        
        return if (success) OperationResult.Success(mapOf("clicked" to true))
        else OperationResult.Error("点击操作失败")
    }

    private fun findElement(selector: ElementSelector): AccessibilityNodeInfo? {
        val root = rootInActiveWindow ?: return null
        
        return when (selector) {
            is ElementSelector.ById -> findNodeById(root, selector.resourceId)
            is ElementSelector.ByText -> findNodeByText(root, selector.text, selector.exact)
            is ElementSelector.ByDescription -> findNodeByDescription(root, selector.description)
            is ElementSelector.ByClassName -> findNodeByClassName(root, selector.className)
            is ElementSelector.ByCoordinate -> findNodeByCoordinate(selector.x, selector.y)
            else -> null
        }
    }

    // 重试机制
    private suspend fun <T> retryOperation(
        maxRetries: Int = 3,
        delay: Long = 1000,
        operation: suspend () -> T
    ): T {
        repeat(maxRetries - 1) {
            try {
                return operation()
            } catch (e: Exception) {
                delay(delay)
            }
        }
        return operation() // 最后一次尝试，不捕获异常
    }
}
```

### 2. 操作定义

```kotlin
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

data class WaitOperation(
    val selector: ElementSelector,
    val maxWaitTime: Long = 10000,
    val checkInterval: Long = 500,
    override val timeout: Long = 10000,
    override val retryCount: Int = 1
) : AccessibilityOperation()

enum class ClickType { SINGLE, DOUBLE, LONG }

sealed class OperationResult {
    data class Success(val data: Map<String, Any>) : OperationResult()
    data class Error(val message: String) : OperationResult()
}
```

### 3. 工作流执行引擎集成

```kotlin
// 在WorkflowViewModel中扩展执行逻辑
private suspend fun executeNode(
    node: WorkflowNode, 
    result: StringBuilder, 
    executed: MutableSet<String>
) {
    if (node.id in executed) return
    executed.add(node.id)
    
    result.appendLine("📍 执行节点: ${node.title} (${node.type.displayName})")
    
    when (node.type) {
        NodeType.UI_CLICK -> {
            val selector = ElementSelector.parse(node.config["selector"] as? String ?: "")
            val clickType = ClickType.valueOf(node.config["clickType"] as? String ?: "SINGLE")
            
            result.appendLine("   👆 点击操作: ${node.config["selector"]}")
            
            if (!AutoFlowAccessibilityService.isServiceEnabled()) {
                result.appendLine("   ❌ 无障碍服务未启用")
                return
            }
            
            val operation = ClickOperation(selector, clickType)
            val operationResult = AutoFlowAccessibilityService.getInstance()?.executeOperation(operation)
            
            when (operationResult) {
                is OperationResult.Success -> result.appendLine("   ✅ 点击成功")
                is OperationResult.Error -> result.appendLine("   ❌ 点击失败: ${operationResult.message}")
                null -> result.appendLine("   ❌ 服务不可用")
            }
        }
        
        NodeType.UI_INPUT -> {
            val selector = ElementSelector.parse(node.config["selector"] as? String ?: "")
            val text = node.config["text"] as? String ?: ""
            val clearFirst = node.config["clearFirst"] as? Boolean ?: true
            
            result.appendLine("   ⌨️ 输入操作: ${node.config["selector"]} -> $text")
            
            val operation = InputOperation(selector, text, clearFirst)
            val operationResult = AutoFlowAccessibilityService.getInstance()?.executeOperation(operation)
            
            when (operationResult) {
                is OperationResult.Success -> result.appendLine("   ✅ 输入成功")
                is OperationResult.Error -> result.appendLine("   ❌ 输入失败: ${operationResult.message}")
                null -> result.appendLine("   ❌ 服务不可用")
            }
        }
        
        NodeType.UI_WAIT -> {
            val selector = ElementSelector.parse(node.config["selector"] as? String ?: "")
            val timeout = node.config["timeout"] as? Long ?: 10000
            
            result.appendLine("   ⏳ 等待元素: ${node.config["selector"]}")
            
            val operation = WaitOperation(selector, timeout)
            val operationResult = AutoFlowAccessibilityService.getInstance()?.executeOperation(operation)
            
            when (operationResult) {
                is OperationResult.Success -> result.appendLine("   ✅ 元素已出现")
                is OperationResult.Error -> result.appendLine("   ❌ 等待超时: ${operationResult.message}")
                null -> result.appendLine("   ❌ 服务不可用")
            }
        }
        
        // ... 其他节点类型
    }
}
```

### 4. 节点配置界面

```kotlin
@Composable
private fun UIInteractionConfig(
    nodeType: NodeType,
    config: MutableMap<String, Any>,
    onUpdate: (MutableMap<String, Any>) -> Unit
) {
    var selector by remember { mutableStateOf(config["selector"] as? String ?: "") }
    
    // 元素选择器输入
    OutlinedTextField(
        value = selector,
        onValueChange = { 
            selector = it
            config["selector"] = it
            onUpdate(config)
        },
        label = { Text("元素选择器") },
        placeholder = { Text("id=button1 或 text=确定 或 desc=提交按钮") },
        modifier = Modifier.fillMaxWidth(),
        supportingText = {
            Text(
                "支持格式: id=资源ID, text=文本内容, desc=描述, class=类名, 坐标x,y",
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    )
    
    when (nodeType) {
        NodeType.UI_CLICK -> {
            var clickType by remember { mutableStateOf(config["clickType"] as? String ?: "SINGLE") }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            var expanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = when(clickType) {
                        "SINGLE" -> "单击"
                        "DOUBLE" -> "双击"
                        "LONG" -> "长按"
                        else -> "单击"
                    },
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("点击类型") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    listOf("SINGLE" to "单击", "DOUBLE" to "双击", "LONG" to "长按").forEach { (value, label) ->
                        DropdownMenuItem(
                            text = { Text(label) },
                            onClick = {
                                clickType = value
                                config["clickType"] = value
                                onUpdate(config)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
        
        NodeType.UI_INPUT -> {
            var text by remember { mutableStateOf(config["text"] as? String ?: "") }
            var clearFirst by remember { mutableStateOf(config["clearFirst"] as? Boolean ?: true) }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = text,
                onValueChange = { 
                    text = it
                    config["text"] = it
                    onUpdate(config)
                },
                label = { Text("输入文本") },
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = clearFirst,
                    onCheckedChange = { 
                        clearFirst = it
                        config["clearFirst"] = it
                        onUpdate(config)
                    }
                )
                Text("输入前清空原有内容")
            }
        }
        
        NodeType.UI_WAIT -> {
            var timeout by remember { mutableStateOf(config["timeout"] as? String ?: "10000") }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = timeout,
                onValueChange = { 
                    timeout = it
                    config["timeout"] = it.toLongOrNull() ?: 10000L
                    onUpdate(config)
                },
                label = { Text("超时时间 (毫秒)") },
                placeholder = { Text("10000") },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
```

## 🎯 权限管理系统

### 权限检查与引导

```kotlin
class AccessibilityPermissionManager(private val context: Context) {
    
    fun isAccessibilityEnabled(): Boolean {
        val accessibilityManager = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        val enabledServices = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        )
        return enabledServices?.contains(context.packageName) == true
    }
    
    fun openAccessibilitySettings() {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }
    
    @Composable
    fun AccessibilityPermissionDialog(
        onDismiss: () -> Unit,
        onOpenSettings: () -> Unit
    ) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("需要无障碍权限") },
            text = {
                Column {
                    Text("AutoFlow需要无障碍服务权限来执行自动化操作：")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("• 模拟点击和输入操作")
                    Text("• 读取屏幕内容")
                    Text("• 检测应用状态变化")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "请在设置中找到AutoFlow并开启无障碍服务。",
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = onOpenSettings) {
                    Text("去设置")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("取消")
                }
            }
        )
    }
}
```

## 📋 实施计划

### Phase 1: 核心重构 (1-2天)
1. ✅ 重命名节点类型 (AUTO_* → UI_*)
2. ✅ 实现节点分类系统
3. ✅ 创建ElementSelector系统
4. ✅ 重构AccessibilityService

### Phase 2: 功能完善 (2-3天)
5. ⏳ 实现操作队列和重试机制
6. ⏳ 集成到工作流执行引擎
7. ⏳ 完善节点配置界面
8. ⏳ 添加权限管理系统

### Phase 3: 高级功能 (3-4天)
9. ⏳ 实现元素拾取器
10. ⏳ 添加执行状态可视化
11. ⏳ 性能优化和错误处理
12. ⏳ 完善文档和测试

## 🎯 成功标准

### 功能指标
- 元素定位成功率 > 95%
- 操作执行成功率 > 90%
- 工作流执行稳定性 > 95%
- 权限引导完成率 > 80%

### 性能指标
- 元素查找时间 < 2秒
- 操作响应时间 < 1秒
- 内存占用增长 < 50MB
- 电池消耗 < 5%/小时

### 用户体验
- 配置界面直观易用
- 错误信息清晰明确
- 执行状态实时反馈
- 权限引导流程顺畅

---

**文档版本**: v1.0  
**创建时间**: 2026-01-24  
**适用阶段**: 第三阶段无障碍服务集成  
**维护者**: AutoFlow开发团队
