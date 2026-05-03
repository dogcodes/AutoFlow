package com.carlos.autoflow.foundation.upgrade

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.Looper
import androidx.core.app.NotificationCompat
import androidx.core.content.FileProvider
import com.carlos.autoflow.foundation.network.FoundationNetworkClient
import com.carlos.autoflow.foundation.network.NetworkResult
import com.google.gson.Gson
import java.io.File

class UpgradeManager(
    private val networkClient: FoundationNetworkClient,
    private val gson: Gson = Gson()
) {
    private var currentDownloadId: Long? = null
    private var progressListener: ((DownloadProgress) -> Unit)? = null
    private var downloadCompleteListener: ((File) -> Unit)? = null
    private val handler = Handler(Looper.getMainLooper())
    private var forcedUpgradeStore: ForcedUpgradeStore? = null
    private var notificationManager: NotificationManager? = null
    private val NOTIFICATION_CHANNEL_ID = "upgrade_channel"
    private val NOTIFICATION_ID = 1001

    fun setForcedUpgradeStore(store: ForcedUpgradeStore) {
        forcedUpgradeStore = store
    }

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "应用更新",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "显示应用更新进度"
            }
            notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager?.createNotificationChannel(channel)
        }
    }

    fun showDownloadNotification(context: Context, progress: Int, isComplete: Boolean = false) {
        if (notificationManager == null) {
            createNotificationChannel(context)
        }

        val builder = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setContentTitle("AutoFlow 更新")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(!isComplete)

        if (isComplete) {
            builder.setContentText("下载完成，点击安装")
                .setSmallIcon(android.R.drawable.stat_sys_download_done)
                .setAutoCancel(true)
        } else {
            builder.setContentText("下载进度: $progress%")
                .setProgress(100, progress, false)
        }

        notificationManager?.notify(NOTIFICATION_ID, builder.build())
    }

    fun cancelNotification() {
        notificationManager?.cancel(NOTIFICATION_ID)
    }

    fun checkForUpdate(
        currentVersionCode: Int,
        infoUrl: String,
        listener: (UpgradeResult) -> Unit
    ) {
        networkClient.get(infoUrl) { result ->
            when (result) {
                is NetworkResult.Success -> listener(parsePayload(result.body, currentVersionCode))
                is NetworkResult.Error -> listener(UpgradeResult.Error(result.message))
            }
        }
    }

    fun startDownload(context: Context, downloadUrl: String, onProgress: (DownloadProgress) -> Unit, onComplete: (File) -> Unit) {
        val fileName = "autoflow_update.apk"
        val request = DownloadManager.Request(Uri.parse(downloadUrl)).apply {
            setTitle("AutoFlow 更新")
            setDescription("正在下载新版本...")
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, fileName)
            setMimeType("application/vnd.android.package-archive")
            setAllowedOverRoaming(false)
            setAllowedOverMetered(true)
        }

        val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        currentDownloadId = dm.enqueue(request)

        // Save download state
        forcedUpgradeStore?.saveDownloadState(DownloadState(downloadUrl, currentDownloadId!!))

        progressListener = onProgress
        downloadCompleteListener = onComplete

        // Start progress monitoring
        monitorDownloadProgress(context, currentDownloadId!!)

        setupDownloadCompletionReceiver(context, currentDownloadId!!, fileName)
    }

    private fun monitorDownloadProgress(context: Context, downloadId: Long) {
        val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val runnable = object : Runnable {
            override fun run() {
                val query = DownloadManager.Query().setFilterById(downloadId)
                val cursor: Cursor = dm.query(query)
                if (cursor.moveToFirst()) {
                    val status = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))
                    val downloaded = cursor.getLong(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                    val total = cursor.getLong(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))

                    when (status) {
                        DownloadManager.STATUS_SUCCESSFUL -> {
                            val progress = DownloadProgress.Progress(100, total, total, true)
                            progressListener?.invoke(progress)
                            showDownloadNotification(context, 100, true)
                            cursor.close()
                            return
                        }
                        DownloadManager.STATUS_FAILED -> {
                            val reason = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_REASON))
                            val errorMessage = getDownloadErrorMessage(reason)
                            progressListener?.invoke(DownloadProgress.Error(errorMessage))
                            cancelNotification()
                            cursor.close()
                            return
                        }
                        DownloadManager.STATUS_RUNNING -> {
                            val progressValue = if (total > 0) ((downloaded * 100) / total).toInt() else 0
                            val progress = DownloadProgress.Progress(progressValue, downloaded, total, false)
                            progressListener?.invoke(progress)
                            showDownloadNotification(context, progressValue)
                        }
                        else -> {
                            // Handle other statuses if needed
                        }
                    }
                }
                cursor.close()
                handler.postDelayed(this, 1000) // Update every second for notification
            }
        }
        handler.post(runnable)
    }

    private fun getDownloadErrorMessage(reason: Int): String {
        return when (reason) {
            DownloadManager.ERROR_CANNOT_RESUME -> "下载无法恢复"
            DownloadManager.ERROR_DEVICE_NOT_FOUND -> "存储设备未找到"
            DownloadManager.ERROR_FILE_ALREADY_EXISTS -> "文件已存在"
            DownloadManager.ERROR_FILE_ERROR -> "文件错误"
            DownloadManager.ERROR_HTTP_DATA_ERROR -> "网络数据错误"
            DownloadManager.ERROR_INSUFFICIENT_SPACE -> "存储空间不足"
            DownloadManager.ERROR_TOO_MANY_REDIRECTS -> "重定向次数过多"
            DownloadManager.ERROR_UNHANDLED_HTTP_CODE -> "HTTP错误"
            DownloadManager.ERROR_UNKNOWN -> "未知错误"
            else -> "下载失败"
        }
    }

    fun cancelDownload(context: Context) {
        currentDownloadId?.let { id ->
            val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            dm.remove(id)
            currentDownloadId = null
            progressListener = null
            downloadCompleteListener = null
            forcedUpgradeStore?.clearDownloadState()
            cancelNotification()
        }
    }

    fun installApk(context: Context, apkFile: File) {
        try {
            // 检查文件是否存在
            if (!apkFile.exists() || !apkFile.canRead()) {
                throw IllegalArgumentException("APK文件不存在或无法读取: ${apkFile.absolutePath}")
            }

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                apkFile
            )

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)

                // 对于Android 8.0+，需要添加安装来源
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true)
                }
            }

            // 检查是否有应用可以处理这个Intent
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
            } else {
                throw IllegalStateException("没有找到可以安装APK的应用")
            }
        } catch (e: Exception) {
            // 记录错误或显示用户友好的错误信息
            android.util.Log.e("UpgradeManager", "安装APK失败", e)
            // 可以考虑显示Toast或Snackbar提示用户
            android.widget.Toast.makeText(
                context,
                "安装失败: ${e.localizedMessage ?: "未知错误"}",
                android.widget.Toast.LENGTH_LONG
            ).show()
        }
    }

    fun parsePayload(payload: String, currentVersionCode: Int): UpgradeResult {
        try {
            val info = gson.fromJson(payload, UpgradeInfo::class.java)
            return if (info.versionCode > currentVersionCode) {
                UpgradeResult.Available(info, info.downloadUrl)
            } else {
                UpgradeResult.UpToDate
            }
        } catch (e: Exception) {
            return UpgradeResult.Error(e.message)
        }
    }

    private fun setupDownloadCompletionReceiver(context: Context, downloadId: Long, fileName: String) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context, intent: Intent) {
                if (intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1) == downloadId) {
                    ctx.unregisterReceiver(this)
                    val apkFile = File(
                        context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
                        fileName
                    )
                    // Send final progress update
                    progressListener?.invoke(DownloadProgress.Progress(100, apkFile.length(), apkFile.length(), true))
                    downloadCompleteListener?.invoke(apkFile)
                    currentDownloadId = null
                    progressListener = null
                    downloadCompleteListener = null
                    forcedUpgradeStore?.clearDownloadState()
                    cancelNotification()
                }
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(
                receiver,
                IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
                Context.RECEIVER_NOT_EXPORTED
            )
        } else {
            @Suppress("UnspecifiedRegisterReceiverFlag")
            context.registerReceiver(
                receiver,
                IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
            )
        }
    }
}

data class UpgradeInfo(
    val versionCode: Int,
    val downloadUrl: String,
    val releaseNotes: String? = null,
    val forceUpdate: Boolean = false
)

sealed class UpgradeResult {
    object UpToDate : UpgradeResult()
    data class Available(val info: UpgradeInfo, val downloadUrl: String) : UpgradeResult()
    data class Error(val message: String? = null) : UpgradeResult()
}

sealed class DownloadProgress {
    data class Progress(
        val progress: Int,
        val downloadedBytes: Long,
        val totalBytes: Long,
        val isComplete: Boolean
    ) : DownloadProgress()

    data class Error(val message: String) : DownloadProgress()
}
