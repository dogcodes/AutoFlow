package com.carlos.autoflow.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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

data class DrawerItem(
    val title: String,
    val icon: ImageVector,
    val key: String
)

@Composable
fun SideDrawer(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    onItemSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val drawerItems = remember {
        listOf(
            DrawerItem("工作流编辑器", Icons.Default.AccountTree, "workflow"),
            DrawerItem("节点监控器", Icons.Default.Visibility, "monitor"),
            DrawerItem("示例应用", Icons.Default.Apps, "demo"),
            DrawerItem("操作录制", Icons.Default.FiberManualRecord, "recording"),
            DrawerItem("历史记录", Icons.Default.History, "history"),
            DrawerItem("设置", Icons.Default.Settings, "settings"),
            DrawerItem("关于", Icons.Default.Info, "about")
        )
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = slideInHorizontally { -it },
        exit = slideOutHorizontally { -it },
        modifier = modifier
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            // 侧边栏
            Card(
                modifier = Modifier
                    .width(280.dp)
                    .fillMaxHeight(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // 标题栏
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "AutoFlow",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, "关闭")
                        }
                    }
                    
                    // 菜单项
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 16.dp)
                    ) {
                        items(drawerItems) { item ->
                            DrawerMenuItem(
                                item = item,
                                onClick = { 
                                    onItemSelected(item.key)
                                }
                            )
                        }
                    }
                }
            }
            
            // 背景遮罩
            Spacer(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(Color.Black.copy(alpha = 0.3f))
                    .clickable { onDismiss() }
            )
        }
    }
}

@Composable
private fun DrawerMenuItem(
    item: DrawerItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                item.icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = item.title,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal
            )
        }
    }
}
