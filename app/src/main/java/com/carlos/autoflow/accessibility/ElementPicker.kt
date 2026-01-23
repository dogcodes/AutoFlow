package com.carlos.autoflow.accessibility

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

data class ElementInfo(
    val text: String?,
    val resourceId: String?,
    val className: String?,
    val contentDescription: String?,
    val isClickable: Boolean,
    val isEditable: Boolean,
    val bounds: String
)

@Composable
fun ElementPickerDialog(
    onDismiss: () -> Unit,
    onElementSelected: (String) -> Unit
) {
    var elements by remember { mutableStateOf<List<ElementInfo>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var selectedElement by remember { mutableStateOf<ElementInfo?>(null) }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.8f),
            shape = RoundedCornerShape(16.dp)
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
                        text = "元素拾取器",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(
                            onClick = {
                                isLoading = true
                                elements = inspectCurrentScreen()
                                isLoading = false
                            }
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "刷新")
                        }
                        
                        TextButton(onClick = onDismiss) {
                            Text("关闭")
                        }
                    }
                }
                
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                
                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else if (elements.isEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "点击刷新按钮获取当前屏幕元素",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    // 元素列表
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(elements) { element ->
                            ElementCard(
                                element = element,
                                isSelected = selectedElement == element,
                                onClick = { selectedElement = element }
                            )
                        }
                    }
                    
                    // 底部操作栏
                    if (selectedElement != null) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Divider()
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Column {
                            Text(
                                "选择器预览:",
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            val selectors = generateSelectors(selectedElement!!)
                            selectors.forEach { selector ->
                                SelectorChip(
                                    selector = selector,
                                    onClick = { onElementSelected(selector) }
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ElementCard(
    element: ElementInfo,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) 
                MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surface
        ),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (element.isClickable) {
                    Chip(text = "可点击", color = Color(0xFF4CAF50))
                }
                if (element.isEditable) {
                    Chip(text = "可输入", color = Color(0xFF2196F3))
                }
            }
            
            if (!element.text.isNullOrBlank()) {
                Text(
                    text = "文本: ${element.text}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            
            if (!element.resourceId.isNullOrBlank()) {
                Text(
                    text = "ID: ${element.resourceId}",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (!element.className.isNullOrBlank()) {
                Text(
                    text = "类名: ${element.className}",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun Chip(text: String, color: Color) {
    Box(
        modifier = Modifier
            .background(color.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            fontSize = 10.sp,
            color = color,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun SelectorChip(
    selector: String,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = selector,
            fontSize = 12.sp,
            modifier = Modifier.weight(1f)
        )
    }
}

private fun inspectCurrentScreen(): List<ElementInfo> {
    val service = AutoFlowAccessibilityService.getInstance()
    if (service == null) {
        return emptyList()
    }
    
    val rootNode = service.rootInActiveWindow
    if (rootNode == null) {
        return emptyList()
    }
    
    val elements = mutableListOf<ElementInfo>()
    collectElements(rootNode, elements)
    
    return elements.filter { element ->
        // 过滤有用的元素
        element.isClickable || 
        element.isEditable || 
        !element.text.isNullOrBlank() ||
        !element.resourceId.isNullOrBlank()
    }
}

private fun collectElements(node: android.view.accessibility.AccessibilityNodeInfo, elements: MutableList<ElementInfo>) {
    val element = ElementInfo(
        text = node.text?.toString(),
        resourceId = node.viewIdResourceName,
        className = node.className?.toString(),
        contentDescription = node.contentDescription?.toString(),
        isClickable = node.isClickable,
        isEditable = node.isEditable,
        bounds = "${node.getBoundsInScreen(android.graphics.Rect())}"
    )
    
    elements.add(element)
    
    // 递归收集子节点
    for (i in 0 until node.childCount) {
        node.getChild(i)?.let { child ->
            collectElements(child, elements)
        }
    }
}

private fun generateSelectors(element: ElementInfo): List<String> {
    val selectors = mutableListOf<String>()
    
    // 按优先级生成选择器
    element.resourceId?.let { id ->
        selectors.add("id=${id.substringAfterLast("/")}")
    }
    
    element.text?.let { text ->
        if (text.length <= 20) {
            selectors.add("text=$text")
        }
    }
    
    element.contentDescription?.let { desc ->
        if (desc.length <= 20) {
            selectors.add("desc=$desc")
        }
    }
    
    element.className?.let { className ->
        selectors.add("class=${className.substringAfterLast(".")}")
    }
    
    return selectors
}
