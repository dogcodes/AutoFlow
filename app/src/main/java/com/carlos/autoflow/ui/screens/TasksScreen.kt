package com.carlos.autoflow.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.carlos.autoflow.workflow.models.ExecutionStatus
import com.carlos.autoflow.workflow.models.Workflow
import com.carlos.autoflow.workflow.models.WorkflowExecution
import com.carlos.autoflow.workflow.repository.ExecutionHistoryRepository
import com.carlos.autoflow.workflow.repository.WorkflowRepository
import com.carlos.autoflow.workflow.viewmodel.WorkflowViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(
    workflowViewModel: WorkflowViewModel,
    workflowRepository: WorkflowRepository,
    historyRepository: ExecutionHistoryRepository,
    onEditTask: (Workflow) -> Unit,
    onCreateTask: () -> Unit,
    onShowSchedule: (Workflow) -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val workflows by workflowRepository.workflows.collectAsState()
    val histories by historyRepository.executions.collectAsState()
    val isExecuting by workflowViewModel.isExecuting.collectAsState()
    val executingWorkflowId by workflowViewModel.executingWorkflowId.collectAsState()
    var deleteTarget by remember { mutableStateOf<Workflow?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("任务", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onCreateTask) {
                Icon(Icons.Default.Add, contentDescription = "新建任务")
            }
        }
    ) { innerPadding ->
        val mergedPadding = PaddingValues(
            start = 16.dp,
            top = innerPadding.calculateTopPadding() + contentPadding.calculateTopPadding(),
            end = 16.dp,
            bottom = innerPadding.calculateBottomPadding() + contentPadding.calculateBottomPadding() + 16.dp
        )

        if (workflows.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(mergedPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("还没有已保存任务", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "先进入编排或录制生成任务，再保存到列表中。",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = mergedPadding,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(workflows, key = { it.id }) { workflow ->
                    TaskCard(
                        workflow = workflow,
                        latestExecution = histories.firstOrNull { it.workflowId == workflow.id },
                        isExecuting = isExecuting && executingWorkflowId == workflow.id,
                        onExecute = {
                            workflowViewModel.loadWorkflow(workflow)
                            workflowViewModel.executeWorkflow(context) { }
                        },
                        onStop = {
                            workflowViewModel.stopWorkflowExecution()
                        },
                        onEdit = { onEditTask(workflow) },
                        onSchedule = { onShowSchedule(workflow) },
                        onDelete = { deleteTarget = workflow }
                    )
                }
            }
        }
    }

    deleteTarget?.let { workflow ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text("删除任务") },
            text = { Text("确定删除任务“${workflow.name}”吗？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        workflowRepository.deleteWorkflow(workflow.id)
                        deleteTarget = null
                    }
                ) {
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) {
                    Text("取消")
                }
            }
        )
    }
}

@Composable
private fun TaskCard(
    workflow: Workflow,
    latestExecution: WorkflowExecution?,
    isExecuting: Boolean,
    onExecute: () -> Unit,
    onStop: () -> Unit,
    onEdit: () -> Unit,
    onSchedule: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = workflow.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (workflow.description.isBlank()) "未填写任务描述" else workflow.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "更多操作")
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("定时") },
                        leadingIcon = { Icon(Icons.Default.Schedule, contentDescription = null) },
                        onClick = {
                            showMenu = false
                            onSchedule()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("删除") },
                        leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) },
                        onClick = {
                            showMenu = false
                            onDelete()
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = buildString {
                    append("最近执行: ")
                    if (isExecuting) {
                        append(latestExecution?.formattedStartTime ?: "进行中")
                        append(" · 运行中")
                    } else {
                        append(latestExecution?.formattedStartTime ?: "暂无")
                    }
                    latestExecution?.status?.takeIf {
                        !isExecuting && it != ExecutionStatus.RUNNING
                    }?.let {
                        append(" · ")
                        append(it.toLabel())
                    }
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = if (isExecuting) onStop else onExecute,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = if (isExecuting) Icons.Default.Stop else Icons.Default.PlayArrow,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isExecuting) "停止" else "执行")
                }

                OutlinedButton(
                    onClick = onEdit,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("编辑")
                }
            }
        }
    }
}

private fun ExecutionStatus.toLabel(): String = when (this) {
    ExecutionStatus.RUNNING -> "运行中"
    ExecutionStatus.SUCCESS -> "成功"
    ExecutionStatus.FAILED -> "失败"
    ExecutionStatus.STOPPED -> "已停止"
}
