package com.carlos.autoflow.recorder.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.carlos.autoflow.accessibility.AutoFlowAccessibilityService
import com.carlos.autoflow.recorder.ConfigurationGenerator
import com.carlos.autoflow.recorder.RecordedOperation

@Composable
fun RecordingControlPanel(
    onWorkflowGenerated: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var isRecording by remember { mutableStateOf(false) }
    var recordedCount by remember { mutableStateOf(0) }
    var showSaveDialog by remember { mutableStateOf(false) }
    var recordedOperations by remember { mutableStateOf<List<RecordedOperation>>(emptyList()) }
    
    // 定期更新录制状态
    LaunchedEffect(isRecording) {
        while (isRecording) {
            kotlinx.coroutines.delay(500)
            recordedCount = AutoFlowAccessibilityService.getInstance()?.getRecordedCount() ?: 0
        }
    }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "操作录制",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1976D2)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 录制状态指示器
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(
                            color = if (isRecording) Color.Red else Color.Gray,
                            shape = RoundedCornerShape(6.dp)
                        )
                )
                
                Text(
                    text = if (isRecording) "录制中..." else "未录制",
                    fontSize = 14.sp,
                    color = if (isRecording) Color.Red else Color.Gray
                )
                
                if (isRecording) {
                    Text(
                        text = "($recordedCount 个操作)",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 控制按钮
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (!isRecording) {
                    Button(
                        onClick = {
                            val service = AutoFlowAccessibilityService.getInstance()
                            if (service != null) {
                                service.startRecording()
                                isRecording = true
                                recordedCount = 0
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50)
                        )
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("开始录制")
                    }
                } else {
                    Button(
                        onClick = {
                            val service = AutoFlowAccessibilityService.getInstance()
                            if (service != null) {
                                recordedOperations = service.stopRecording()
                                isRecording = false
                                if (recordedOperations.isNotEmpty()) {
                                    showSaveDialog = true
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFF44336)
                        )
                    ) {
                        Icon(Icons.Default.Stop, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("停止录制")
                    }
                }
                
                OutlinedButton(
                    onClick = {
                        // 清除录制
                        recordedOperations = emptyList()
                        recordedCount = 0
                    },
                    enabled = !isRecording && recordedOperations.isNotEmpty()
                ) {
                    Icon(Icons.Default.Clear, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("清除")
                }
            }
            
            // 录制提示
            if (isRecording) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "请在其他应用中执行需要录制的操作",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }
    }
    
    // 保存对话框
    if (showSaveDialog) {
        SaveRecordingDialog(
            operations = recordedOperations,
            onSave = { workflowName ->
                val generator = ConfigurationGenerator()
                val optimizedOps = generator.optimizeOperations(recordedOperations)
                val json = generator.generateJson(optimizedOps, workflowName)
                onWorkflowGenerated(json)
                showSaveDialog = false
                recordedOperations = emptyList()
                recordedCount = 0
            },
            onDismiss = { showSaveDialog = false }
        )
    }
}

@Composable
private fun SaveRecordingDialog(
    operations: List<RecordedOperation>,
    onSave: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var workflowName by remember { mutableStateOf("录制的工作流") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("保存录制") },
        text = {
            Column {
                Text("录制了 ${operations.size} 个操作，是否保存为工作流？")
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = workflowName,
                    onValueChange = { workflowName = it },
                    label = { Text("工作流名称") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(workflowName) },
                enabled = workflowName.isNotBlank()
            ) {
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
