package com.carlos.autoflow.workflow.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.SettingsApplications
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
import com.carlos.autoflow.workflow.models.NodeCategory

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
    val nodeTemplates = mapOf(
        NodeCategory.CONTROL_FLOW to listOf(
            NodeTemplate(NodeType.START, "开始", "工作流起点", Icons.Default.PlayArrow),
            NodeTemplate(NodeType.EVENT_TRIGGER, "事件触发", "监听触发执行", Icons.Default.Notifications),
            NodeTemplate(NodeType.END, "结束", "工作流终点", Icons.Default.Stop),
            NodeTemplate(NodeType.CONDITION, "条件判断", "分支逻辑", Icons.Default.Info),
            NodeTemplate(NodeType.LOOP, "循环", "重复执行", Icons.Default.Refresh),
            NodeTemplate(NodeType.DELAY, "延时", "等待指定时间", Icons.Default.DateRange),
            NodeTemplate(NodeType.SCRIPT, "脚本", "自定义代码", Icons.Default.Edit)
        ),
        NodeCategory.UI_INTERACTION to listOf(
            NodeTemplate(NodeType.UI_CLICK, "点击操作", "点击屏幕元素", Icons.Default.TouchApp),
            NodeTemplate(NodeType.UI_INPUT, "文本输入", "输入文本内容", Icons.Default.Keyboard),
            NodeTemplate(NodeType.UI_SCROLL, "滚动操作", "滚动页面", Icons.Default.SwipeUp),
            NodeTemplate(NodeType.UI_LONG_CLICK, "长按操作", "长按UI元素", Icons.Default.TouchApp),
            NodeTemplate(NodeType.UI_SWIPE, "滑动操作", "滑动手势", Icons.Default.SwipeLeft)
        ),
        NodeCategory.UI_DETECTION to listOf(
            NodeTemplate(NodeType.UI_FIND, "查找元素", "查找页面元素", Icons.Default.Search),
            NodeTemplate(NodeType.UI_WAIT, "等待元素", "等待元素出现", Icons.Default.HourglassEmpty),
            NodeTemplate(NodeType.UI_GET_TEXT, "获取文本", "获取元素文本", Icons.Default.TextFields),
            NodeTemplate(NodeType.UI_CHECK, "检查状态", "检查元素状态", Icons.Default.CheckCircle)
        ),
        NodeCategory.SYSTEM_EVENT to listOf(
            NodeTemplate(NodeType.APP_LAUNCH, "应用启动", "检测应用启动", Icons.Default.Launch),
            NodeTemplate(NodeType.LAUNCH_ACTIVITY, "启动Activity", "启动指定Android Activity", Icons.Default.OpenInNew),
            NodeTemplate(NodeType.NOTIFICATION, "通知处理", "处理系统通知", Icons.Default.Notifications),
            NodeTemplate(NodeType.SCREEN_STATE, "屏幕状态", "检测屏幕开关", Icons.Default.ScreenLockPortrait),
            NodeTemplate(NodeType.SYSTEM_GLOBAL_ACTION, "全局系统动作", "执行全局系统动作", Icons.Default.SettingsApplications)
        ),
        NodeCategory.NETWORK to listOf(
            NodeTemplate(NodeType.HTTP_REQUEST, "HTTP请求", "发送网络请求", Icons.Default.Send),
            NodeTemplate(NodeType.DATA_TRANSFORM, "数据转换", "处理数据", Icons.Default.Build)
        )
    )

    Card(
        modifier = modifier
            .width(220.dp)
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
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                nodeTemplates.forEach { (category, templates) ->
                    item {
                        CategoryHeader(category = category)
                    }
                    items(templates) { template ->
                        NodeTemplateItem(
                            template = template,
                            onClick = { onNodeSelected(template.type) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryHeader(category: NodeCategory) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = Color(android.graphics.Color.parseColor(category.color)).copy(alpha = 0.1f),
                shape = RoundedCornerShape(4.dp)
            )
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(
                    color = Color(android.graphics.Color.parseColor(category.color)),
                    shape = RoundedCornerShape(4.dp)
                )
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = category.displayName,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = Color(android.graphics.Color.parseColor(category.color))
        )
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
