package com.carlos.autoflow.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import com.carlos.autoflow.workflow.models.ExecutionStatus
import com.carlos.autoflow.workflow.models.Workflow
import com.carlos.autoflow.workflow.models.WorkflowExecution
import com.carlos.autoflow.workflow.models.WorkflowScheduleConfig
import com.carlos.autoflow.workflow.repository.ExecutionHistoryRepository
import com.carlos.autoflow.workflow.repository.WorkflowRepository
import com.carlos.autoflow.workflow.viewmodel.WorkflowViewModel

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(
    workflowViewModel: WorkflowViewModel,
    workflowRepository: WorkflowRepository,
    historyRepository: ExecutionHistoryRepository,
    onEditTask: (Workflow) -> Unit,
    onCreateTask: () -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val workflows by workflowRepository.workflows.collectAsState()
    val histories by historyRepository.executions.collectAsState()
    val isExecuting by workflowViewModel.isExecuting.collectAsState()
    val executingWorkflowId by workflowViewModel.executingWorkflowId.collectAsState()
    var deleteTarget by remember { mutableStateOf<Workflow?>(null) }
    var scheduleTarget by remember { mutableStateOf<Workflow?>(null) }

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
                        onSchedule = { scheduleTarget = workflow },
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

    scheduleTarget?.let { workflow ->
        ScheduleConfigSheet(
            workflow = workflow,
            onDismiss = { scheduleTarget = null },
            onClear = {
                workflowRepository.upsertWorkflow(workflow.copy(scheduleConfig = null))
                scheduleTarget = null
            },
            onSave = { scheduleConfig ->
                workflowRepository.upsertWorkflow(workflow.copy(scheduleConfig = scheduleConfig))
                scheduleTarget = null
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
                    workflow.scheduleConfig?.let { scheduleConfig ->
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "定时: ${scheduleConfig.toSummary()}",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (scheduleConfig.enabled) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
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

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun ScheduleConfigSheet(
    workflow: Workflow,
    onDismiss: () -> Unit,
    onClear: () -> Unit,
    onSave: (WorkflowScheduleConfig) -> Unit
) {
    val currentConfig = workflow.scheduleConfig
    var enabled by remember(workflow.id, currentConfig) {
        mutableStateOf(currentConfig?.enabled ?: true)
    }
    var hour by remember(workflow.id, currentConfig) {
        mutableStateOf(currentConfig?.hour ?: 9)
    }
    var minute by remember(workflow.id, currentConfig) {
        mutableStateOf(currentConfig?.minute ?: 0)
    }
    var daysOfWeek by remember(workflow.id, currentConfig) {
        mutableStateOf(currentConfig?.daysOfWeek ?: setOf(1, 2, 3, 4, 5))
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = "任务定时",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = workflow.name,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("启用定时", style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = "关闭后保留配置，但任务不会按计划执行。",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = enabled,
                    onCheckedChange = { enabled = it }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text("执行时间", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TimeStepper(
                    label = "小时",
                    valueText = "%02d".format(hour),
                    onDecrease = { hour = if (hour == 0) 23 else hour - 1 },
                    onIncrease = { hour = if (hour == 23) 0 else hour + 1 }
                )
                Text(
                    text = ":",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                TimeStepper(
                    label = "分钟",
                    valueText = "%02d".format(minute),
                    onDecrease = { minute = if (minute == 0) 59 else minute - 1 },
                    onIncrease = { minute = if (minute == 59) 0 else minute + 1 }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text("重复日期", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(12.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                weekdayOptions.forEach { option ->
                    FilterChip(
                        selected = daysOfWeek.contains(option.value),
                        onClick = {
                            daysOfWeek = if (daysOfWeek.contains(option.value)) {
                                daysOfWeek - option.value
                            } else {
                                daysOfWeek + option.value
                            }
                        },
                        label = {
                            Text(
                                text = option.label,
                                textAlign = TextAlign.Center
                            )
                        },
                        modifier = Modifier
                            .defaultMinSize(minWidth = 40.dp)
                            .heightIn(min = 32.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "预览: ${
                    WorkflowScheduleConfig(
                        enabled = enabled,
                        hour = hour,
                        minute = minute,
                        daysOfWeek = daysOfWeek
                    ).toSummary()
                }",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onClear,
                ) {
                    Text(
                        text = "清除",
                        color = MaterialTheme.colorScheme.error
                    )
                }
                TextButton(
                    onClick = onDismiss,
                ) {
                    Text("取消")
                }
                Spacer(modifier = Modifier.weight(1f))
                Button(
                    onClick = {
                        onSave(
                            WorkflowScheduleConfig(
                                enabled = enabled,
                                hour = hour,
                                minute = minute,
                                daysOfWeek = daysOfWeek
                            )
                        )
                    },
                    modifier = Modifier.defaultMinSize(minWidth = 96.dp)
                ) {
                    Text("保存")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun TimeStepper(
    label: String,
    valueText: String,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit
) {
    var repeatAction by remember { mutableStateOf<StepperAction?>(null) }

    LaunchedEffect(repeatAction) {
        val action = repeatAction ?: return@LaunchedEffect
        delay(250)
        while (repeatAction == action) {
            when (action) {
                StepperAction.DECREASE -> onDecrease()
                StepperAction.INCREASE -> onIncrease()
            }
            delay(80)
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            StepperButton(
                onClick = onDecrease,
                onPressStateChange = { isPressed ->
                    repeatAction = if (isPressed) StepperAction.DECREASE else null
                }
            ) {
                Icon(Icons.Default.Remove, contentDescription = "减少$label")
            }
            Text(
                text = valueText,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(36.dp),
                textAlign = TextAlign.Center
            )
            StepperButton(
                onClick = onIncrease,
                onPressStateChange = { isPressed ->
                    repeatAction = if (isPressed) StepperAction.INCREASE else null
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = "增加$label")
            }
        }
    }
}

@Composable
private fun StepperButton(
    onClick: () -> Unit,
    onPressStateChange: (Boolean) -> Unit,
    content: @Composable () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .defaultMinSize(minWidth = 36.dp, minHeight = 36.dp)
            .pointerInput(Unit) {
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false)
                    onPressStateChange(true)
                    waitForUpOrCancellation()
                    onPressStateChange(false)
                }
            }
    ) {
        content()
    }
}

private enum class StepperAction {
    DECREASE,
    INCREASE
}

private data class WeekdayOption(val value: Int, val label: String)

private val weekdayOptions = listOf(
    WeekdayOption(1, "一"),
    WeekdayOption(2, "二"),
    WeekdayOption(3, "三"),
    WeekdayOption(4, "四"),
    WeekdayOption(5, "五"),
    WeekdayOption(6, "六"),
    WeekdayOption(7, "日")
)

private fun WorkflowScheduleConfig.toSummary(): String {
    if (!enabled) {
        return "已关闭"
    }
    val timeLabel = "%02d:%02d".format(hour, minute)
    if (daysOfWeek.isEmpty()) {
        return "每天 $timeLabel"
    }
    val daysLabel = weekdayOptions
        .filter { daysOfWeek.contains(it.value) }
        .joinToString("") { "周${it.label}" }
    return "$daysLabel $timeLabel"
}
