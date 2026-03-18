package com.carlos.autoflow.foundation.upgrade.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.carlos.autoflow.foundation.network.FoundationNetworkClient
import com.carlos.autoflow.foundation.upgrade.UpgradeConfig
import com.carlos.autoflow.foundation.upgrade.UpgradeManager
import com.carlos.autoflow.foundation.upgrade.UpgradeResult

@Composable
fun AutoUpgradeChecker(
    versionCode: Int,
    upgradeManager: UpgradeManager,
    infoUrl: String = UpgradeConfig.DEFAULT_VERSION_INFO_URL,
    onCheckFinished: ((UpgradeResult) -> Unit)? = null
) {
    val context = LocalContext.current
    val checkInbox = remember { mutableStateOf<UpgradeResult.Available?>(null) }

    LaunchedEffect(infoUrl, versionCode) {
        upgradeManager.checkForUpdate(versionCode, infoUrl) { result ->
            when (result) {
                is UpgradeResult.Available -> {
                    checkInbox.value = result
                    onCheckFinished?.invoke(result)
                }
                else -> onCheckFinished?.invoke(result)
            }
        }
    }

    checkInbox.value?.let { result ->
        UpgradeDialog(
            result = result,
            forceUpdate = result.info.forceUpdate,
            onConfirm = {
                upgradeManager.downloadAndInstall(context, result.downloadUrl)
                checkInbox.value = null
            },
            onDismiss = {
                checkInbox.value = null
            }
        )
    }
}
