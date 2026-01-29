package com.carlos.autoflow.monitor

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun FloatingMonitorButton(
    isMonitoring: Boolean,
    nodeCount: Int,
    onToggleMonitoring: () -> Unit,
    onShowSidePanel: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 主按钮
        FloatingActionButton(
            onClick = onToggleMonitoring,
            modifier = Modifier.size(56.dp),
            containerColor = if (isMonitoring) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
        ) {
            Icon(
                imageVector = if (isMonitoring) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                contentDescription = if (isMonitoring) "停止监控" else "开始监控",
                tint = if (isMonitoring) Color.White else MaterialTheme.colorScheme.onSurface
            )
        }
        
        // 节点计数和侧滑按钮
        AnimatedVisibility(
            visible = isMonitoring,
            enter = slideInVertically() + fadeIn(),
            exit = slideOutVertically() + fadeOut()
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // 节点计数
                Surface(
                    modifier = Modifier.clip(RoundedCornerShape(12.dp)),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = "$nodeCount",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                
                // 侧滑按钮
                FloatingActionButton(
                    onClick = onShowSidePanel,
                    modifier = Modifier.size(40.dp),
                    containerColor = MaterialTheme.colorScheme.secondary
                ) {
                    Icon(
                        imageVector = Icons.Default.List,
                        contentDescription = "显示节点列表",
                        modifier = Modifier.size(20.dp),
                        tint = Color.White
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NodeSidePanel(
    nodes: List<NodeInfo>,
    selectedNode: NodeInfo?,
    onNodeSelected: (NodeInfo) -> Unit,
    onDismiss: () -> Unit
) {
    var showDetails by remember { mutableStateOf(false) }
    
    Row(
        modifier = Modifier.fillMaxSize()
    ) {
        // 半透明背景
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .background(Color.Black.copy(alpha = 0.3f))
                .clickable { onDismiss() }
        )
        
        // 侧滑面板
        Surface(
            modifier = Modifier
                .width(320.dp)
                .fillMaxHeight(),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 8.dp
        ) {
            Column {
                // 标题栏
                TopAppBar(
                    title = { 
                        Text(
                            text = "节点监控器",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "关闭")
                        }
                    },
                    actions = {
                        Text(
                            text = "${nodes.size} 个节点",
                            modifier = Modifier.padding(end = 16.dp),
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )
                
                if (showDetails && selectedNode != null) {
                    NodeDetailView(
                        node = selectedNode,
                        onBack = { showDetails = false }
                    )
                } else {
                    NodeListView(
                        nodes = nodes,
                        onNodeClick = { node ->
                            onNodeSelected(node)
                            showDetails = true
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun NodeListView(
    nodes: List<NodeInfo>,
    onNodeClick: (NodeInfo) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(nodes) { node ->
            NodeItem(
                node = node,
                onClick = { onNodeClick(node) }
            )
        }
    }
}

@Composable
fun NodeItem(
    node: NodeInfo,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = node.className.substringAfterLast('.'),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (node.isClickable) {
                        Icon(
                            Icons.Default.TouchApp,
                            contentDescription = "可点击",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.secondary
                        )
                    }
                    if (node.isScrollable) {
                        Icon(
                            Icons.Default.SwipeVertical,
                            contentDescription = "可滚动",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }
            }
            
            if (node.text.isNotEmpty()) {
                Text(
                    text = "文本: ${node.text}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            
            if (node.resourceId.isNotEmpty()) {
                Text(
                    text = "ID: ${node.resourceId.substringAfterLast('/')}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            
            // 层级指示器
            Row(
                modifier = Modifier.padding(top = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(node.depth) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(
                                MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                CircleShape
                            )
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                }
                Text(
                    text = "层级 ${node.depth}",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NodeDetailView(
    node: NodeInfo,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // 返回按钮
        TopAppBar(
            title = { Text("节点详情") },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                }
            }
        )
        
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                DetailCard("基本信息") {
                    DetailItem("类名", node.className)
                    DetailItem("文本", node.text.ifEmpty { "无" })
                    DetailItem("描述", node.contentDescription.ifEmpty { "无" })
                    DetailItem("资源ID", node.resourceId.ifEmpty { "无" })
                }
            }
            
            item {
                DetailCard("位置信息") {
                    DetailItem("左上角", "(${node.bounds.left}, ${node.bounds.top})")
                    DetailItem("右下角", "(${node.bounds.right}, ${node.bounds.bottom})")
                    DetailItem("宽度", "${node.bounds.width()}px")
                    DetailItem("高度", "${node.bounds.height()}px")
                }
            }
            
            item {
                DetailCard("属性") {
                    DetailItem("可点击", if (node.isClickable) "是" else "否")
                    DetailItem("可滚动", if (node.isScrollable) "是" else "否")
                    DetailItem("已启用", if (node.isEnabled) "是" else "否")
                    DetailItem("层级深度", "${node.depth}")
                }
            }
        }
    }
}

@Composable
fun DetailCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            content()
        }
    }
}

@Composable
fun DetailItem(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(2f)
        )
    }
}
