package com.carlos.autoflow.license

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LicenseDebugDialog(
    licenseDebugTool: LicenseDebugTool,
    initialDeviceId: String,
    onDismiss: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    var debugDeviceId by remember(initialDeviceId) { mutableStateOf(initialDeviceId) }
    var debugDays by remember { mutableStateOf("30") }
    var debugValidityDays by remember { mutableStateOf("3") }
    var debugType by remember { mutableStateOf("1") }
    var generatedDebugKey by remember { mutableStateOf("") }
    var verifyActivationKey by remember { mutableStateOf("") }
    var verifyMessage by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("调试生成激活码") },
        text = {
            Column {
                OutlinedTextField(
                    value = debugDeviceId,
                    onValueChange = {
                        debugDeviceId = it.trim()
                        generatedDebugKey = ""
                        verifyMessage = ""
                    },
                    label = { Text("设备ID") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = debugDays,
                    onValueChange = { debugDays = it.filter(Char::isDigit) },
                    label = { Text("使用时长（天）") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = debugValidityDays,
                    onValueChange = { debugValidityDays = it.filter(Char::isDigit) },
                    label = { Text("激活码有效期（天）") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = debugType,
                    onValueChange = { debugType = it.filter(Char::isDigit) },
                    label = { Text("激活码类型（0-3）") },
                    modifier = Modifier.fillMaxWidth()
                )
                if (generatedDebugKey.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = generatedDebugKey,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                            OutlinedTextField(
                                value = verifyActivationKey,
                                onValueChange = {
                                    verifyActivationKey = it
                                    verifyMessage = ""
                                },
                                label = { Text("待校验激活码") },
                                placeholder = { Text("请输入24位激活码") },
                                modifier = Modifier.fillMaxWidth()
                            )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(
                        onClick = {
                            val result = licenseDebugTool.verifyKey(
                                deviceId = debugDeviceId,
                                activationKey = verifyActivationKey
                            )
                            verifyMessage = if (result.isValid) "激活码有效" else "激活码无效"
                        },
                                enabled = debugDeviceId.isNotBlank() && verifyActivationKey.length == 24
                            ) {
                                Text("校验")
                            }
                    Button(
                        onClick = {
                                val days = debugDays.toIntOrNull() ?: 30
                                val validity = debugValidityDays.toIntOrNull() ?: 1
                                val typeValue = debugType.toIntOrNull() ?: 1
                                val result = licenseDebugTool.generateKey(
                                    deviceId = debugDeviceId,
                                    days = days,
                                    validityDays = validity,
                                    type = typeValue
                                )
                            generatedDebugKey = result.activationKey
                            clipboardManager.setText(AnnotatedString(result.activationKey))
                            verifyMessage = "已复制激活码"
                        },
                        enabled = debugDeviceId.isNotBlank()
                    ) {
                        Text("生成")
                    }
                }
                if (verifyMessage.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = verifyMessage,
                        fontSize = 12.sp,
                        color = if (verifyMessage.contains("有效")) {
                            Color(0xFF4CAF50)
                        } else {
                            MaterialTheme.colorScheme.error
                        }
                    )
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("关闭")
            }
        }
    )
}
