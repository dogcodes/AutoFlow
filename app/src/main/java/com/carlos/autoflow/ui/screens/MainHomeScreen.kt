package com.carlos.autoflow.ui.screens

import android.app.Activity
import android.content.Intent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import com.carlos.autoflow.compliance.ComplianceConfig
import com.carlos.autoflow.demo.DemoAppActivity
import com.carlos.autoflow.recorder.ui.RecordingControlPanel
import com.carlos.autoflow.workflow.models.Workflow
import com.carlos.autoflow.workflow.repository.ExecutionHistoryRepository
import com.carlos.autoflow.workflow.repository.WorkflowRepository
import com.carlos.autoflow.workflow.ui.WorkflowEditor
import com.carlos.autoflow.workflow.viewmodel.WorkflowViewModel
import com.carlos.autoflow.platform.ad.AdCallback
import com.carlos.autoflow.platform.ad.AdService
import com.carlos.autoflow.platform.ad.AdSlots

private enum class HomeTab(val title: String) {
    TASKS("任务"),
    ARRANGE("编排"),
    RECORD("录制"),
    MORE("更多")
}

@Composable
fun MainHomeScreen(
    workflowViewModel: WorkflowViewModel = viewModel()
) {
    val context = LocalContext.current
    val workflowRepository = remember { WorkflowRepository(context) }
    val historyRepository = remember { ExecutionHistoryRepository(context) }
    val currentWorkflow by workflowViewModel.workflow.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    var currentTab by rememberSaveable { mutableStateOf(HomeTab.TASKS) }
    val rewardSlotId = AdSlots.REWARD
    val rewardAdRequest: () -> Unit = rewardAdRequest@{
        val activity = context as? Activity ?: return@rewardAdRequest
        val adManager = AdService.getAdManager()
        adManager.loadRewardedAd(activity, rewardSlotId, object : AdCallback {
            override fun onAdLoaded() {
                adManager.showRewardedAd(activity)
            }

            override fun onAdFailed(error: String?) {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("激励广告加载失败：${error ?: "未知"}")
                }
            }

            override fun onAdShown() {}
            override fun onAdClicked() {}
            override fun onAdClosed() {}

            override fun onAdRewarded() {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("已延长 30 分钟体验时间")
                }
            }
        })
    }

    LaunchedEffect(Unit) {
        if (workflowRepository.workflows.value.isEmpty()) {
            workflowRepository.upsertWorkflow(currentWorkflow)
        }
    }

    val availableTabs = remember {
        HomeTab.values().filter {
            it != HomeTab.RECORD || !ComplianceConfig.isComplianceMode
        }
    }

    if (ComplianceConfig.isComplianceMode && currentTab == HomeTab.RECORD) {
        currentTab = HomeTab.TASKS
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            NavigationBar {
                availableTabs.forEach { tab ->
                    NavigationBarItem(
                        selected = currentTab == tab,
                        onClick = { currentTab = tab },
                        icon = {
                            Icon(
                                imageVector = when (tab) {
                                    HomeTab.TASKS -> Icons.Default.Apps
                                    HomeTab.ARRANGE -> Icons.Default.AccountTree
                                    HomeTab.RECORD -> Icons.Default.FiberManualRecord
                                    HomeTab.MORE -> Icons.Default.MoreHoriz
                                },
                                contentDescription = tab.title
                            )
                        },
                        label = { Text(tab.title) }
                    )
                }
            }
        }
    ) { innerPadding ->
        when (currentTab) {
            HomeTab.TASKS -> {
                        TasksScreen(
                            modifier = Modifier,
                            workflowViewModel = workflowViewModel,
                            workflowRepository = workflowRepository,
                            historyRepository = historyRepository,
                            onEditTask = { workflow ->
                            workflowViewModel.loadWorkflow(workflow)
                            currentTab = HomeTab.ARRANGE
                        },
                        onCreateTask = {
                            val newWorkflow = Workflow(
                                name = "新建任务",
                                nodes = emptyList(),
                                connections = emptyList()
                            )
                            workflowViewModel.loadWorkflow(newWorkflow)
                            currentTab = HomeTab.ARRANGE
                        },
                            onRewardAdRequest = rewardAdRequest,
                            hideRewardAd = ComplianceConfig.isComplianceMode,
                            contentPadding = innerPadding
                        )
                }

            HomeTab.ARRANGE -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    WorkflowEditor(
                        workflowViewModel = workflowViewModel,
                        showSideDrawerButton = false,
                        onSaveWorkflow = {
                            workflowRepository.upsertWorkflow(workflowViewModel.workflow.value)
                        }
                    )
                }
            }

            HomeTab.RECORD -> {
                if (ComplianceConfig.isComplianceMode) return@Scaffold
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    RecordingControlPanel(
                        onWorkflowGenerated = { json ->
                            workflowViewModel.importFromJson(json)
                            currentTab = HomeTab.ARRANGE
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            HomeTab.MORE -> {
                MoreScreen(
                    contentPadding = innerPadding,
                    onLaunchDemo = {
                        context.startActivity(Intent(context, DemoAppActivity::class.java))
                    }
                )
            }
        }
    }
}
