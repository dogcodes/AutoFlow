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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.carlos.autoflow.compliance.ComplianceConfig
import com.carlos.autoflow.demo.DemoAppActivity
import com.carlos.autoflow.BuildConfig
import com.carlos.autoflow.license.LicenseManager
import com.carlos.autoflow.recorder.ui.RecordingControlPanel
import com.carlos.autoflow.workflow.models.Workflow
import com.carlos.autoflow.workflow.repository.ExecutionHistoryRepository
import com.carlos.autoflow.workflow.repository.WorkflowRepository
import com.carlos.autoflow.workflow.ui.WorkflowEditor
import com.carlos.autoflow.workflow.viewmodel.WorkflowViewModel
import com.carlos.autoflow.platform.ad.AdCallback
import com.carlos.autoflow.platform.ad.AdService
import com.carlos.autoflow.platform.ad.AdSlots
import com.carlos.autoflow.platform.ad.config.AdConfigStore
import com.carlos.autoflow.platform.ad.config.AdConfigurationManager
import com.carlos.autoflow.task.RewardAdPrefs

private enum class HomeTab(val title: String) {
    TASKS("任务"),
    ARRANGE("编排"),
    // RECORD("录制"),  // 暂时隐藏，录制功能未实现
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
    val licenseManager = remember { LicenseManager(context, BuildConfig.FORCE_PREMIUM) }
    val rewardAdPrefs = remember { RewardAdPrefs(context) }
    val adConfigStore = remember { AdConfigStore(context) }
    var currentTab by rememberSaveable { mutableStateOf(HomeTab.TASKS) }
    var showSaveNameDialog by remember { mutableStateOf(false) }
    var saveNameInput by remember(currentWorkflow.id) { mutableStateOf(currentWorkflow.name) }
    var rewardAdRefreshTick by remember { mutableStateOf(0) }
    val rewardedPolicy = remember(rewardAdRefreshTick) {
        AdConfigurationManager.DEFAULT_REWARDED_POLICY.let { default ->
            adConfigStore.loadConfig()?.rewardedPolicy ?: default
        }
    }
    val rewardAdEligibility = remember(rewardAdRefreshTick, rewardedPolicy) {
        rewardAdPrefs.getEligibility(
            dailyLimit = rewardedPolicy.dailyLimit,
            cooldownSeconds = rewardedPolicy.cooldownSeconds
        )
    }
    val rewardSlotId = AdSlots.REWARD
    val rewardAdRequest: () -> Unit = rewardAdRequest@{
        val activity = context as? Activity ?: return@rewardAdRequest
        val eligibility = rewardAdPrefs.getEligibility(
            dailyLimit = rewardedPolicy.dailyLimit,
            cooldownSeconds = rewardedPolicy.cooldownSeconds
        )
        if (!eligibility.canClaim) {
            coroutineScope.launch {
                val message = when {
                    eligibility.remainingDailyCount <= 0 -> "今日奖励次数已用完，请明天再来"
                    eligibility.cooldownRemainingSeconds > 0 -> "请 ${eligibility.cooldownRemainingSeconds} 秒后再试"
                    else -> "当前不可领取奖励"
                }
                snackbarHostState.showSnackbar(message)
            }
            rewardAdRefreshTick++
            return@rewardAdRequest
        }
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
                    if (licenseManager.isSystemTimeAbnormal()) {
                        val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                            .format(Date())
                        snackbarHostState.showSnackbar("系统日期异常（$date），请校准后重试")
                        return@launch
                    }

                    val success = licenseManager.extendMinutes(rewardedPolicy.rewardMinutes)
                    if (success) {
                        rewardAdPrefs.markRewarded()
                        rewardAdRefreshTick++
                        snackbarHostState.showSnackbar("已延长 ${rewardedPolicy.rewardMinutes} 分钟体验时间")
                    } else {
                        snackbarHostState.showSnackbar("奖励发放失败，请稍后再试")
                    }
                }
            }
        })
    }

    LaunchedEffect(Unit) {
        if (workflowRepository.workflows.value.isEmpty()) {
            workflowRepository.upsertWorkflow(currentWorkflow)
        }
    }

    LaunchedEffect(rewardAdEligibility.cooldownRemainingSeconds) {
        if (rewardAdEligibility.cooldownRemainingSeconds > 0) {
            delay(1000)
            rewardAdRefreshTick++
        }
    }

    val availableTabs = remember {
        HomeTab.values().filter {
            // 录制功能已隐藏
            true
        }
    }

    fun requiresNamingBeforeSave(name: String): Boolean {
        val trimmed = name.trim()
        return trimmed.isBlank() || trimmed == "新建任务" || trimmed == "新建工作流"
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
                                    // HomeTab.RECORD -> Icons.Default.FiberManualRecord
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
                            rewardedPolicy = rewardedPolicy,
                            rewardAdRemainingDailyCount = rewardAdEligibility.remainingDailyCount,
                            rewardAdCooldownRemainingSeconds = rewardAdEligibility.cooldownRemainingSeconds,
                            rewardAdEnabled = rewardAdEligibility.canClaim,
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
                            val workflow = workflowViewModel.workflow.value
                            if (requiresNamingBeforeSave(workflow.name)) {
                                saveNameInput = workflow.name.takeIf { it.isNotBlank() } ?: "新建任务"
                                showSaveNameDialog = true
                            } else {
                                workflowRepository.upsertWorkflow(workflow)
                            }
                        }
                    )
                }
            }

            // HomeTab.RECORD -> {
            //     if (ComplianceConfig.isComplianceMode) return@Scaffold
            //     Box(
            //         modifier = Modifier
            //             .fillMaxSize()
            //             .padding(innerPadding)
            //     ) {
            //         RecordingControlPanel(
            //             onWorkflowGenerated = { json ->
            //                 workflowViewModel.importFromJson(json)
            //                 currentTab = HomeTab.ARRANGE
            //             },
            //             modifier = Modifier.fillMaxSize()
            //         )
            //     }
            // }

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

    if (showSaveNameDialog) {
        AlertDialog(
            onDismissRequest = { showSaveNameDialog = false },
            title = { Text("保存当前任务") },
            text = {
                OutlinedTextField(
                    value = saveNameInput,
                    onValueChange = { saveNameInput = it },
                    label = { Text("任务名称") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        workflowRepository.upsertWorkflow(
                            workflowViewModel.workflow.value.copy(
                                name = saveNameInput.trim().ifBlank { "新建任务" }
                            )
                        )
                        showSaveNameDialog = false
                    }
                ) {
                    Text("保存")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveNameDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
}
