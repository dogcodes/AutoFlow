package com.carlos.autoflow.workflow.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.carlos.autoflow.workflow.models.Workflow
import com.carlos.autoflow.workflow.examples.AccessibilityWorkflowExamples

@Composable
fun AccessibilityExamplesDialog(
    onDismiss: () -> Unit,
    onExampleSelected: (Workflow) -> Unit
) {
    val examples = remember { AccessibilityWorkflowExamples.getAllAccessibilityExamples() }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.8f),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // 标题栏
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "无障碍工作流示例",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "关闭")
                    }
                }
                
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                
                Text(
                    text = "选择一个示例工作流来快速开始",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                // 示例列表
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(examples) { example ->
                        ExampleCard(
                            workflow = example,
                            onClick = { onExampleSelected(example) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ExampleCard(
    workflow: Workflow,
    onClick: () -> Unit
) {
    val (icon, color) = getExampleIcon(workflow.id)
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 图标
            Card(
                modifier = Modifier.size(48.dp),
                colors = CardDefaults.cardColors(
                    containerColor = color.copy(alpha = 0.1f)
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            
            // 内容
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = workflow.name,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = workflow.description,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                // 节点统计
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    StatChip(
                        icon = Icons.Default.AccountBox,
                        text = "${workflow.nodes.size} 节点"
                    )
                    StatChip(
                        icon = Icons.Default.ArrowForward,
                        text = "${workflow.connections.size} 连接"
                    )
                }
            }
            
            // 箭头
            Icon(
                Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun StatChip(
    icon: ImageVector,
    text: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(12.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = text,
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun getExampleIcon(workflowId: String): Pair<ImageVector, androidx.compose.ui.graphics.Color> {
    return when (workflowId) {
        "demo_app_login" -> Icons.Default.Person to androidx.compose.ui.graphics.Color(0xFF1976D2)
        "demo_app_form" -> Icons.Default.Edit to androidx.compose.ui.graphics.Color(0xFF2196F3)
        "app_auto_checkin" -> Icons.Default.CheckCircle to androidx.compose.ui.graphics.Color(0xFF4CAF50)
        "demo_app_search" -> Icons.Default.Search to androidx.compose.ui.graphics.Color(0xFF673AB7) // Search icon with purple color
        else -> Icons.Default.Android to androidx.compose.ui.graphics.Color.Gray
    }
}
