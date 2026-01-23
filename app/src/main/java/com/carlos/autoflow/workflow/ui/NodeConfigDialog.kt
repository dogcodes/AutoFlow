@file:OptIn(ExperimentalMaterial3Api::class)
package com.carlos.autoflow.workflow.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.carlos.autoflow.workflow.models.NodeType
import com.carlos.autoflow.workflow.models.WorkflowNode

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
                    // UI检测节点配置
                    NodeType.UI_FIND -> UIFindConfig(config) { config = it }
                    NodeType.UI_WAIT -> UIWaitConfig(config) { config = it }
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
                "支持: id=资源ID, text=文本, desc=描述, class=类名",
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    )
    
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
