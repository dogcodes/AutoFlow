package com.carlos.autoflow.foundation.upgrade

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Environment
import androidx.core.content.FileProvider
import com.carlos.autoflow.foundation.network.FoundationNetworkClient
import com.carlos.autoflow.foundation.network.NetworkResult
import com.google.gson.Gson
import java.io.File

class UpgradeManager(
    private val networkClient: FoundationNetworkClient,
    private val gson: Gson = Gson()
) {
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

    fun downloadAndInstall(context: Context, downloadUrl: String) {
        val fileName = "autoflow_update.apk"
        val request = DownloadManager.Request(Uri.parse(downloadUrl)).apply {
            setTitle("AutoFlow 更新")
            setDescription("正在下载新版本...")
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, fileName)
            setMimeType("application/vnd.android.package-archive")
        }

        val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val downloadId = dm.enqueue(request)

        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context, intent: Intent) {
                if (intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1) == downloadId) {
                    ctx.unregisterReceiver(this)
                    val apkFile = File(
                        context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
                        fileName
                    )
                    installApk(context, apkFile)
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

    private fun installApk(context: Context, apkFile: File) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            apkFile
        )
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
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
