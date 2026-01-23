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
