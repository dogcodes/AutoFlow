package com.carlos.autoflow.foundation.upgrade.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.carlos.autoflow.foundation.upgrade.UpgradeManager
import com.carlos.autoflow.foundation.upgrade.UpgradeResult

@Composable
fun UpgradeDialog(
    result: UpgradeResult.Available,
    forceUpdate: Boolean,
    upgradeManager: UpgradeManager,
    onDismiss: (() -> Unit)? = null
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var isDownloading by remember { mutableStateOf(false) }
    var downloadProgress by remember { mutableStateOf<com.carlos.autoflow.foundation.upgrade.DownloadProgress?>(null) }
    var downloadComplete by remember { mutableStateOf(false) }
    var apkFile by remember { mutableStateOf<java.io.File?>(null) }

    if (downloadComplete && apkFile != null) {
        // Show install confirmation dialog
        InstallConfirmationDialog(
            onConfirm = {
                upgradeManager.installApk(context, apkFile!!)
                downloadComplete = false
            },
            onCancel = {
                downloadComplete = false
            }
        )
    }

    AlertDialog(
        onDismissRequest = {
            if (!forceUpdate && !isDownloading) {
                onDismiss?.invoke()
            }
        },
        title = {
            Text(
                text = if (forceUpdate) "发现强制更新" else "发现新版本",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = result.info.releaseNotes
                        ?: "检测到新版本，请及时更新以获得更好的体验。"
                )

                if (isDownloading) {
                    downloadProgress?.let { progress ->
                        when (progress) {
                            is com.carlos.autoflow.foundation.upgrade.DownloadProgress.Error -> {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("下载失败: ${progress.message}")
                                    TextButton(
                                        onClick = {
                                            // Retry download
                                            downloadProgress = null
                                            upgradeManager.startDownload(
                                                context = context,
                                                downloadUrl = result.downloadUrl,
                                                onProgress = { progress ->
                                                    downloadProgress = progress
                                                    if (progress is com.carlos.autoflow.foundation.upgrade.DownloadProgress.Progress && progress.isComplete) {
                                                        apkFile = java.io.File(context.getExternalFilesDir(android.os.Environment.DIRECTORY_DOWNLOADS), "autoflow_update.apk")
                                                        downloadComplete = true
                                                        isDownloading = false
                                                    }
                                                },
                                                onComplete = { file ->
                                                    apkFile = file
                                                    downloadComplete = true
                                                    isDownloading = false
                                                }
                                            )
                                        }
                                    ) {
                                        Text("重试")
                                    }
                                }
                            }
                            is com.carlos.autoflow.foundation.upgrade.DownloadProgress.Progress -> {
                                Text("下载进度: ${progress.progress}%")
                                LinearProgressIndicator(
                                    progress = { progress.progress / 100f },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Text("已下载: ${formatBytes(progress.downloadedBytes)}${if (progress.totalBytes > 0) " / ${formatBytes(progress.totalBytes)}" else ""}")
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (!isDownloading) {
                TextButton(
                    onClick = {
                        isDownloading = true
                        upgradeManager.startDownload(
                            context = context,
                            downloadUrl = result.downloadUrl,
                            onProgress = { progress ->
                                downloadProgress = progress
                                if (progress is com.carlos.autoflow.foundation.upgrade.DownloadProgress.Progress && progress.isComplete) {
                                    apkFile = java.io.File(context.getExternalFilesDir(android.os.Environment.DIRECTORY_DOWNLOADS), "autoflow_update.apk")
                                    downloadComplete = true
                                    isDownloading = false
                                }
                            },
                            onComplete = { file ->
                                apkFile = file
                                downloadComplete = true
                                isDownloading = false
                            }
                        )
                    }
                ) {
                    Text("立即更新")
                }
            } else if (isDownloading) {
                TextButton(
                    onClick = {
                        upgradeManager.cancelDownload(context)
                        isDownloading = false
                        downloadProgress = null
                    }
                ) {
                    Text("取消下载")
                }
            }
        },
        dismissButton = if (forceUpdate || isDownloading) null else {
            {
                TextButton(onClick = { onDismiss?.invoke() }) {
                    Text("暂不更新")
                }
            }
        }
    )
}

@Composable
private fun InstallConfirmationDialog(
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancel,
        title = {
            Text("安装确认")
        },
        text = {
            Text("下载完成，是否立即安装新版本？")
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("安装")
            }
        },
        dismissButton = {
            TextButton(onClick = onCancel) {
                Text("稍后")
            }
        }
    )
}

private fun formatBytes(bytes: Long): String {
    if (bytes == 0L) return "0 B"
    val units = arrayOf("B", "KB", "MB", "GB")
    var value = bytes.toDouble()
    var unitIndex = 0
    while (value >= 1024 && unitIndex < units.size - 1) {
        value /= 1024
        unitIndex++
    }
    return "%.1f %s".format(value, units[unitIndex])
}
