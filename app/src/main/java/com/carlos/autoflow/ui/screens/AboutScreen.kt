package com.carlos.autoflow.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.carlos.autoflow.BuildConfig
import com.carlos.autoflow.foundation.network.FoundationNetworkClient
import com.carlos.autoflow.foundation.upgrade.UpgradeManager
import com.carlos.autoflow.foundation.upgrade.UpgradeResult

private const val UPDATE_INFO_URL = "http://xbdcc.cn/autoflow/version.json"

@Composable
fun AboutScreen() {
    val context = LocalContext.current
    var checkState by remember { mutableStateOf<CheckState>(CheckState.Idle) }

    if (checkState is CheckState.Available) {
        val result = (checkState as CheckState.Available).result
        AlertDialog(
            onDismissRequest = { checkState = CheckState.Idle },
            title = { Text("发现新版本") },
            text = { Text(result.info.releaseNotes ?: "有新版本可用，是否立即更新？") },
            confirmButton = {
                TextButton(onClick = {
                    checkState = CheckState.Idle
                    val upgradeManager = UpgradeManager(FoundationNetworkClient())
                    upgradeManager.downloadAndInstall(context, result.downloadUrl)
                }) { Text("立即更新") }
            },
            dismissButton = {
                TextButton(onClick = { checkState = CheckState.Idle }) { Text("取消") }
            }
        )
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Info,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text("AutoFlow", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text("版本 ${BuildConfig.VERSION_NAME}", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(8.dp))
        Text("自动化工作流编辑器", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(8.dp))
        Text("简化您的移动端自动化任务", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = {
                checkState = CheckState.Checking
                val upgradeManager = UpgradeManager(FoundationNetworkClient())
                upgradeManager.checkForUpdate(BuildConfig.VERSION_CODE, UPDATE_INFO_URL) { result ->
                    checkState = when (result) {
                        is UpgradeResult.Available -> CheckState.Available(result)
                        is UpgradeResult.UpToDate -> CheckState.UpToDate
                        is UpgradeResult.Error -> CheckState.Error(result.message)
                    }
                }
            },
            enabled = checkState !is CheckState.Checking
        ) {
            if (checkState is CheckState.Checking) {
                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text("检查更新")
        }
        when (checkState) {
            is CheckState.UpToDate -> Text("已是最新版本", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
            is CheckState.Error -> Text("检查失败：${(checkState as CheckState.Error).msg}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
            else -> {}
        }
    }
}

private sealed class CheckState {
    object Idle : CheckState()
    object Checking : CheckState()
    object UpToDate : CheckState()
    data class Available(val result: com.carlos.autoflow.foundation.upgrade.UpgradeResult.Available) : CheckState()
    data class Error(val msg: String?) : CheckState()
}
