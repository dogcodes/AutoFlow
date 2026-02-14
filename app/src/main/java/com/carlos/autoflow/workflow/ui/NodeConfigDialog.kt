@file:OptIn(ExperimentalMaterial3Api::class)
package com.carlos.autoflow.workflow.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.carlos.autoflow.workflow.models.NodeType
import com.carlos.autoflow.workflow.models.WorkflowNode
import com.carlos.autoflow.accessibility.ElementPickerDialog
import com.carlos.autoflow.workflow.models.ClickStrategy

@Composable
fun NodeConfigDialog(
    node: WorkflowNode,
    onDismiss: () -> Unit,
    onSave: (Map<String, Any>) -> Unit
) {
    var config by remember { mutableStateOf(node.config.toMutableMap()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Text("配置 ${node.title}", fontSize = 18.sp) 
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                when (node.type) {
                    NodeType.HTTP_REQUEST -> HttpRequestConfig(config) { config = it }
                    NodeType.DATA_TRANSFORM -> DataTransformConfig(config) { config = it }
                    NodeType.CONDITION -> ConditionConfig(config) { config = it }
                    NodeType.DELAY -> DelayConfig(config) { config = it }
                    NodeType.SCRIPT -> ScriptConfig(config) { config = it }
                    NodeType.LOOP -> LoopConfig(config) { config = it }
                    // UI交互节点配置
                    NodeType.UI_CLICK -> UIClickConfig(config) { config = it }
                    NodeType.UI_INPUT -> UIInputConfig(config) { config = it }
                    NodeType.UI_SCROLL -> UIScrollConfig(config) { config = it }
                    NodeType.UI_LONG_CLICK -> UILongClickConfig(config) { config = it }
                    NodeType.UI_SWIPE -> UISwipeConfig(config) { config = it }
                    // UI检测节点配置
                    NodeType.UI_FIND -> UIFindConfig(config) { config = it }
                    NodeType.UI_WAIT -> UIWaitConfig(config) { config = it }
                    NodeType.UI_GET_TEXT -> UIGetTextConfig(config) { config = it }
                    NodeType.UI_CHECK -> UICheckConfig(config) { config = it }
                    NodeType.LAUNCH_ACTIVITY -> LaunchActivityConfig(config) { config = it }
                    else -> {
                        Text("此节点暂无配置项", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(config) }) {
                Text("保存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

@Composable
private fun HttpRequestConfig(
    config: MutableMap<String, Any>,
    onUpdate: (MutableMap<String, Any>) -> Unit
) {
    var url by remember { mutableStateOf(config["url"] as? String ?: "") }
    var method by remember { mutableStateOf(config["method"] as? String ?: "GET") }
    
    OutlinedTextField(
        value = url,
        onValueChange = { 
            url = it
            config["url"] = it
            onUpdate(config)
        },
        label = { Text("请求URL") },
        placeholder = { Text("https://api.example.com/data") },
        modifier = Modifier.fillMaxWidth()
    )
    
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = method,
            onValueChange = {},
            readOnly = true,
            label = { Text("请求方法") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            listOf("GET", "POST", "PUT", "DELETE").forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        method = option
                        config["method"] = option
                        onUpdate(config)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun DataTransformConfig(
    config: MutableMap<String, Any>,
    onUpdate: (MutableMap<String, Any>) -> Unit
) {
    var transformRule by remember { mutableStateOf(config["rule"] as? String ?: "") }
    
    OutlinedTextField(
        value = transformRule,
        onValueChange = { 
            transformRule = it
            config["rule"] = it
            onUpdate(config)
        },
        label = { Text("转换规则") },
        placeholder = { Text("例: data.result.items") },
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun ConditionConfig(
    config: MutableMap<String, Any>,
    onUpdate: (MutableMap<String, Any>) -> Unit
) {
    var condition by remember { mutableStateOf(config["condition"] as? String ?: "") }
    
    OutlinedTextField(
        value = condition,
        onValueChange = { 
            condition = it
            config["condition"] = it
            onUpdate(config)
        },
        label = { Text("判断条件") },
        placeholder = { Text("例: data.status == 'success'") },
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun DelayConfig(
    config: MutableMap<String, Any>,
    onUpdate: (MutableMap<String, Any>) -> Unit
) {
    var delay by remember { mutableStateOf(config["delay"] as? String ?: "1000") }
    
    OutlinedTextField(
        value = delay,
        onValueChange = { 
            delay = it
            config["delay"] = it
            onUpdate(config)
        },
        label = { Text("延时时间 (毫秒)") },
        placeholder = { Text("1000") },
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun ScriptConfig(
    config: MutableMap<String, Any>,
    onUpdate: (MutableMap<String, Any>) -> Unit
) {
    var script by remember { mutableStateOf(config["script"] as? String ?: "") }
    
    OutlinedTextField(
        value = script,
        onValueChange = { 
            script = it
            config["script"] = it
            onUpdate(config)
        },
        label = { Text("脚本代码") },
        placeholder = { Text("// 在此输入JavaScript代码") },
        modifier = Modifier.fillMaxWidth().height(120.dp),
        maxLines = 6
    )
}

@Composable
private fun LoopConfig(
    config: MutableMap<String, Any>,
    onUpdate: (MutableMap<String, Any>) -> Unit
) {
    var count by remember { mutableStateOf(config["count"] as? String ?: "1") }
    
    OutlinedTextField(
        value = count,
        onValueChange = { 
            count = it
            config["count"] = it
            onUpdate(config)
        },
        label = { Text("循环次数") },
        placeholder = { Text("1") },
        modifier = Modifier.fillMaxWidth()
    )
}

// UI交互节点配置
@Composable
private fun UIClickConfig(
    config: MutableMap<String, Any>,
    onUpdate: (MutableMap<String, Any>) -> Unit
) {
    var selector by remember { mutableStateOf(config["selector"] as? String ?: "") }
    var clickType by remember { mutableStateOf(config["clickType"] as? String ?: "SINGLE") }
    var clickStrategy by remember { mutableStateOf(config["clickStrategy"] as? String ?: ClickStrategy.DEFAULT.name) }
    var showElementPicker by remember { mutableStateOf(false) }
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = selector,
            onValueChange = { 
                selector = it
                config["selector"] = it
                onUpdate(config)
            },
            label = { Text("元素选择器") },
            placeholder = { Text("id=button1 或 text=确定") },
            modifier = Modifier.weight(1f),
            supportingText = {
                Text(
                    "支持: id=资源ID, text=包含文本, text_exact=精确文本, text_starts=开头文本, text_ends=结尾文本, desc=描述, class=类名",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        )
        
        OutlinedButton(
            onClick = { showElementPicker = true },
            modifier = Modifier.align(Alignment.CenterVertically)
        ) {
            Text("拾取")
        }
    }
    
    if (showElementPicker) {
        ElementPickerDialog(
            onDismiss = { showElementPicker = false },
            onElementSelected = { selectedSelector ->
                selector = selectedSelector
                config["selector"] = selectedSelector
                onUpdate(config)
                showElementPicker = false
            }
        )
    }
    
    Spacer(modifier = Modifier.height(8.dp))
    
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = when(clickType) {
                "SINGLE" -> "单击"
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
            listOf("SINGLE" to "单击", "LONG" to "长按").forEach { (value, label) ->
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

    Spacer(modifier = Modifier.height(8.dp))

    var strategyExpanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = strategyExpanded,
        onExpandedChange = { strategyExpanded = !strategyExpanded }
    ) {
        OutlinedTextField(
            value = ClickStrategy.valueOf(clickStrategy).displayName,
            onValueChange = {},
            readOnly = true,
            label = { Text("点击策略") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = strategyExpanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = strategyExpanded,
            onDismissRequest = { strategyExpanded = false }
        ) {
            ClickStrategy.values().forEach { strategy ->
                DropdownMenuItem(
                    text = { Text(strategy.displayName) },
                    onClick = {
                        clickStrategy = strategy.name
                        config["clickStrategy"] = strategy.name
                        onUpdate(config)
                        strategyExpanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun UIInputConfig(
    config: MutableMap<String, Any>,
    onUpdate: (MutableMap<String, Any>) -> Unit
) {
    var selector by remember { mutableStateOf(config["selector"] as? String ?: "") }
    var text by remember { mutableStateOf(config["text"] as? String ?: "") }
    var clearFirst by remember { mutableStateOf(config["clearFirst"] as? Boolean ?: true) }
    
    OutlinedTextField(
        value = selector,
        onValueChange = { 
            selector = it
            config["selector"] = it
            onUpdate(config)
        },
        label = { Text("元素选择器") },
        placeholder = { Text("id=editText1 或 text=输入框") },
        modifier = Modifier.fillMaxWidth()
    )
    
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
        horizontalArrangement = Arrangement.spacedBy(8.dp)
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

@Composable
private fun UIScrollConfig(
    config: MutableMap<String, Any>,
    onUpdate: (MutableMap<String, Any>) -> Unit
) {
    var direction by remember { mutableStateOf(config["direction"] as? String ?: "down") }
    var distance by remember { mutableStateOf(config["distance"] as? String ?: "1") }
    
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = when(direction) {
                "up" -> "向上"
                "down" -> "向下"
                else -> "向下"
            },
            onValueChange = {},
            readOnly = true,
            label = { Text("滚动方向") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            listOf("up" to "向上", "down" to "向下").forEach { (value, label) ->
                DropdownMenuItem(
                    text = { Text(label) },
                    onClick = {
                        direction = value
                        config["direction"] = value
                        onUpdate(config)
                        expanded = false
                    }
                )
            }
        }
    }
    
    Spacer(modifier = Modifier.height(8.dp))
    
    OutlinedTextField(
        value = distance,
        onValueChange = { 
            distance = it
            config["distance"] = it.toIntOrNull() ?: 1
            onUpdate(config)
        },
        label = { Text("滚动距离") },
        placeholder = { Text("1") },
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun UIFindConfig(
    config: MutableMap<String, Any>,
    onUpdate: (MutableMap<String, Any>) -> Unit
) {
    var selector by remember { mutableStateOf(config["selector"] as? String ?: "") }
    var multiple by remember { mutableStateOf(config["multiple"] as? Boolean ?: false) }
    
    OutlinedTextField(
        value = selector,
        onValueChange = { 
            selector = it
            config["selector"] = it
            onUpdate(config)
        },
        label = { Text("查找条件") },
        placeholder = { Text("id=button 或 text=按钮") },
        modifier = Modifier.fillMaxWidth()
    )
    
    Spacer(modifier = Modifier.height(8.dp))
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Checkbox(
            checked = multiple,
            onCheckedChange = { 
                multiple = it
                config["multiple"] = it
                onUpdate(config)
            }
        )
        Text("查找多个元素")
    }
}

@Composable
private fun UIWaitConfig(
    config: MutableMap<String, Any>,
    onUpdate: (MutableMap<String, Any>) -> Unit
) {
    var selector by remember { mutableStateOf(config["selector"] as? String ?: "") }
    var timeout by remember { mutableStateOf(config["timeout"] as? String ?: "10000") }
    
    OutlinedTextField(
        value = selector,
        onValueChange = { 
            selector = it
            config["selector"] = it
            onUpdate(config)
        },
        label = { Text("等待元素") },
        placeholder = { Text("id=loading 或 text=加载完成") },
        modifier = Modifier.fillMaxWidth()
    )
    
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

// 新增的UI交互节点配置
@Composable
private fun UILongClickConfig(
    config: MutableMap<String, Any>,
    onUpdate: (MutableMap<String, Any>) -> Unit
) {
    var selector by remember { mutableStateOf(config["selector"] as? String ?: "") }
    
    OutlinedTextField(
        value = selector,
        onValueChange = { 
            selector = it
            config["selector"] = it
            onUpdate(config)
        },
        label = { Text("元素选择器") },
        placeholder = { Text("id=button1 或 text=确定") },
        modifier = Modifier.fillMaxWidth(),
        supportingText = {
            Text(
                "支持: id=资源ID, text=包含文本, text_exact=精确文本, text_starts=开头文本, text_ends=结尾文本, desc=描述, class=类名",
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    )
}

@Composable
private fun UISwipeConfig(
    config: MutableMap<String, Any>,
    onUpdate: (MutableMap<String, Any>) -> Unit
) {
    var startX by remember { mutableStateOf(config["startX"] as? String ?: "0") }
    var startY by remember { mutableStateOf(config["startY"] as? String ?: "0") }
    var endX by remember { mutableStateOf(config["endX"] as? String ?: "0") }
    var endY by remember { mutableStateOf(config["endY"] as? String ?: "0") }
    var duration by remember { mutableStateOf(config["duration"] as? String ?: "500") }
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = startX,
            onValueChange = { 
                startX = it
                config["startX"] = it.toFloatOrNull() ?: 0f
                onUpdate(config)
            },
            label = { Text("起始X") },
            modifier = Modifier.weight(1f)
        )
        OutlinedTextField(
            value = startY,
            onValueChange = { 
                startY = it
                config["startY"] = it.toFloatOrNull() ?: 0f
                onUpdate(config)
            },
            label = { Text("起始Y") },
            modifier = Modifier.weight(1f)
        )
    }
    
    Spacer(modifier = Modifier.height(8.dp))
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = endX,
            onValueChange = { 
                endX = it
                config["endX"] = it.toFloatOrNull() ?: 0f
                onUpdate(config)
            },
            label = { Text("结束X") },
            modifier = Modifier.weight(1f)
        )
        OutlinedTextField(
            value = endY,
            onValueChange = { 
                endY = it
                config["endY"] = it.toFloatOrNull() ?: 0f
                onUpdate(config)
            },
            label = { Text("结束Y") },
            modifier = Modifier.weight(1f)
        )
    }
    
    Spacer(modifier = Modifier.height(8.dp))
    
    OutlinedTextField(
        value = duration,
        onValueChange = { 
            duration = it
            config["duration"] = it.toLongOrNull() ?: 500L
            onUpdate(config)
        },
        label = { Text("滑动时长 (毫秒)") },
        placeholder = { Text("500") },
        modifier = Modifier.fillMaxWidth()
    )
}

// 新增的UI检测节点配置
@Composable
private fun UIGetTextConfig(
    config: MutableMap<String, Any>,
    onUpdate: (MutableMap<String, Any>) -> Unit
) {
    var selector by remember { mutableStateOf(config["selector"] as? String ?: "") }
    
    OutlinedTextField(
        value = selector,
        onValueChange = { 
            selector = it
            config["selector"] = it
            onUpdate(config)
        },
        label = { Text("元素选择器") },
        placeholder = { Text("id=textView1 或 text=标题") },
        modifier = Modifier.fillMaxWidth(),
        supportingText = {
            Text(
                "获取指定元素的文本内容",
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    )
}

@Composable
private fun UICheckConfig(
    config: MutableMap<String, Any>,
    onUpdate: (MutableMap<String, Any>) -> Unit
) {
    var selector by remember { mutableStateOf(config["selector"] as? String ?: "") }
    var expectedState by remember { mutableStateOf(config["expectedState"] as? String ?: "visible") }
    
    OutlinedTextField(
        value = selector,
        onValueChange = { 
            selector = it
            config["selector"] = it
            onUpdate(config)
        },
        label = { Text("元素选择器") },
        placeholder = { Text("id=checkbox1 或 text=选项") },
        modifier = Modifier.fillMaxWidth()
    )
    
    Spacer(modifier = Modifier.height(8.dp))
    
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = when(expectedState) {
                "checked" -> "已选中"
                "enabled" -> "已启用"
                "visible" -> "可见"
                else -> "可见"
            },
            onValueChange = {},
            readOnly = true,
            label = { Text("检查状态") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            listOf(
                "visible" to "可见",
                "enabled" to "已启用", 
                "checked" to "已选中"
            ).forEach { (value, label) ->
                DropdownMenuItem(
                    text = { Text(label) },
                    onClick = {
                        expectedState = value
                        config["expectedState"] = value
                        onUpdate(config)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun LaunchActivityConfig(
    config: MutableMap<String, Any>,
    onUpdate: (MutableMap<String, Any>) -> Unit
) {
    var packageName by remember { mutableStateOf(config["packageName"] as? String ?: "") }
    var className by remember { mutableStateOf(config["className"] as? String ?: "") }
    var action by remember { mutableStateOf(config["action"] as? String ?: "") }
    var data by remember { mutableStateOf(config["data"] as? String ?: "") }
    // For extras, starting with a simple string input for JSON or key-value pairs
    var extrasJson by remember { mutableStateOf(config["extras"] as? String ?: "") }

    OutlinedTextField(
        value = packageName,
        onValueChange = {
            packageName = it
            config["packageName"] = it
            onUpdate(config)
        },
        label = { Text("包名 (packageName)") },
        placeholder = { Text("com.example.app") },
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(8.dp))

    OutlinedTextField(
        value = className,
        onValueChange = {
            className = it
            config["className"] = it
            onUpdate(config)
        },
        label = { Text("类名 (className)") },
        placeholder = { Text("com.example.app.MainActivity") },
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(8.dp))

    OutlinedTextField(
        value = action,
        onValueChange = {
            action = it
            config["action"] = it
            onUpdate(config)
        },
        label = { Text("Action (可选)") },
        placeholder = { Text("android.intent.action.VIEW") },
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(8.dp))

    OutlinedTextField(
        value = data,
        onValueChange = {
            data = it
            config["data"] = it
            onUpdate(config)
        },
        label = { Text("Data URI (可选)") },
        placeholder = { Text("http://example.com/data") },
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(8.dp))
    
    OutlinedTextField(
        value = extrasJson,
        onValueChange = {
            extrasJson = it
            config["extras"] = it // Store as JSON string for now
            onUpdate(config)
        },
        label = { Text("额外参数 (JSON格式, 可选)") },
        placeholder = { Text("{ \"key1\": \"value1\", \"key2\": \"value2\" }") },
        modifier = Modifier.fillMaxWidth()
    )
}
