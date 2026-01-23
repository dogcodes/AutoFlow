package com.carlos.autoflow.workflow.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.carlos.autoflow.workflow.models.NodeType

data class NodeTemplate(
    val type: NodeType,
    val title: String,
    val description: String,
    val icon: ImageVector
)

@Composable
fun NodeSelectionPanel(
    onNodeSelected: (NodeType) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val nodeTemplates = listOf(
        NodeTemplate(NodeType.START, "开始", "工作流起点", Icons.Default.PlayArrow),
        NodeTemplate(NodeType.END, "结束", "工作流终点", Icons.Default.Stop),
        NodeTemplate(NodeType.HTTP_REQUEST, "HTTP请求", "发送网络请求", Icons.Default.Send),
        NodeTemplate(NodeType.DATA_TRANSFORM, "数据转换", "处理数据", Icons.Default.Build),
        NodeTemplate(NodeType.CONDITION, "条件判断", "分支逻辑", Icons.Default.Info),
        NodeTemplate(NodeType.LOOP, "循环", "重复执行", Icons.Default.Refresh),
        NodeTemplate(NodeType.DELAY, "延时", "等待指定时间", Icons.Default.DateRange),
        NodeTemplate(NodeType.SCRIPT, "脚本", "自定义代码", Icons.Default.Edit),
        // UI交互节点
        NodeTemplate(NodeType.UI_CLICK, "点击操作", "点击屏幕元素", Icons.Default.TouchApp),
        NodeTemplate(NodeType.UI_INPUT, "文本输入", "输入文本内容", Icons.Default.Keyboard),
        NodeTemplate(NodeType.UI_SCROLL, "滚动操作", "滚动页面", Icons.Default.SwipeUp),
        // UI检测节点
        NodeTemplate(NodeType.UI_FIND, "查找元素", "查找页面元素", Icons.Default.Search),
        NodeTemplate(NodeType.UI_WAIT, "等待元素", "等待元素出现", Icons.Default.HourglassEmpty)
    )

    Card(
        modifier = modifier
            .width(200.dp)
            .fillMaxHeight(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "节点库",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1976D2)
                )
                IconButton(onClick = onClose) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "关闭面板",
                        tint = Color(0xFF1976D2)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(nodeTemplates) { template ->
                    NodeTemplateItem(
                        template = template,
                        onClick = { onNodeSelected(template.type) }
                    )
                }
            }
        }
    }
}

@Composable
private fun NodeTemplateItem(
    template: NodeTemplate,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = template.icon,
                contentDescription = null,
                tint = Color(0xFF1976D2),
                modifier = Modifier.size(20.dp)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column {
                Text(
                    text = template.title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = template.description,
                    fontSize = 10.sp,
                    color = Color.Gray
                )
            }
        }
    }
}
