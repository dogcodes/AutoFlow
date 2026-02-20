package com.carlos.autoflow.workflow.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.carlos.autoflow.workflow.examples.json.WechatRedEnvelopeWorkflowExample

@Composable
fun ImportDialog(
    onDismiss: () -> Unit,
    onImport: (String) -> Unit,
    onImportFromUrl: (String) -> Unit,
    errorMessage: String? = null
) {
    var jsonText by remember { mutableStateOf(WechatRedEnvelopeWorkflowExample.WECHAT_RED_ENVELOPE_JSON) }
    var urlText by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf(0) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("导入工作流") },
        text = {
            Column {
                // 标签页选择
                Row {
                    TextButton(
                        onClick = { selectedTab = 0 },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = if (selectedTab == 0) MaterialTheme.colorScheme.primary else Color.Gray
                        )
                    ) {
                        Text("JSON文本")
                    }
                    TextButton(
                        onClick = { selectedTab = 1 },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = if (selectedTab == 1) MaterialTheme.colorScheme.primary else Color.Gray
                        )
                    ) {
                        Text("URL链接")
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                when (selectedTab) {
                    0 -> {
                        OutlinedTextField(
                            value = jsonText,
                            onValueChange = { jsonText = it },
                            label = { Text("JSON配置") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            maxLines = 10
                        )
                    }
                    1 -> {
                        OutlinedTextField(
                            value = urlText,
                            onValueChange = { urlText = it },
                            label = { Text("配置文件URL") },
                            placeholder = { Text("https://example.com/workflow.json") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                }
                
                // 错误信息显示
                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "❌ $errorMessage",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    when (selectedTab) {
                        0 -> onImport(jsonText)
                        1 -> onImportFromUrl(urlText)
                    }
                },
                enabled = when (selectedTab) {
                    0 -> jsonText.isNotBlank()
                    1 -> urlText.isNotBlank()
                    else -> false
                }
            ) {
                Text("导入")
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
fun ExportDialog(
    json: String,
    onDismiss: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    var showCopySuccess by remember { mutableStateOf(false) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("导出工作流") },
        text = {
            Column {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                ) {
                    item {
                        Text(
                            text = json,
                            fontSize = 10.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Color.Gray.copy(alpha = 0.1f),
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(12.dp)
                        )
                    }
                }
                
                if (showCopySuccess) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "✅ 已复制到剪贴板",
                        color = Color(0xFF4CAF50),
                        fontSize = 12.sp
                    )
                }
            }
        },
        confirmButton = {
            Row {
                TextButton(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(json))
                        showCopySuccess = true
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "复制",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("复制")
                }
                
                TextButton(onClick = onDismiss) {
                    Text("关闭")
                }
            }
        }
    )
}

@Composable
fun ExecuteResultDialog(
    result: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("执行结果") },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
            ) {
                item {
                    Text(
                        text = result.ifEmpty { "⏳ 正在执行..." },
                        fontSize = 12.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Color.Gray.copy(alpha = 0.1f),
                                RoundedCornerShape(8.dp)
                            )
                            .padding(12.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("关闭")
            }
        }
    )
}
