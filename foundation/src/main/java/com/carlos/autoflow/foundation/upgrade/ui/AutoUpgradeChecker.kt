package com.carlos.autoflow.foundation.upgrade.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.carlos.autoflow.foundation.upgrade.UpgradeConfig
import com.carlos.autoflow.foundation.upgrade.ForcedUpgradeStore
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
    val forcedUpgradeStore = remember { ForcedUpgradeStore(context) }
    val checkInbox = remember { mutableStateOf<UpgradeResult.Available?>(null) }

    LaunchedEffect(Unit) {
        upgradeManager.setForcedUpgradeStore(forcedUpgradeStore)
    }

    LaunchedEffect(infoUrl, versionCode) {
        checkInbox.value = forcedUpgradeStore.load(versionCode)

        upgradeManager.checkForUpdate(versionCode, infoUrl) { result ->
            when (result) {
                is UpgradeResult.Available -> {
                    if (result.info.forceUpdate) {
                        forcedUpgradeStore.save(result.info)
                    } else {
                        forcedUpgradeStore.clear()
                    }
                    checkInbox.value = result
                    onCheckFinished?.invoke(result)
                }
                is UpgradeResult.UpToDate -> {
                    forcedUpgradeStore.clear()
                    checkInbox.value = null
                    onCheckFinished?.invoke(result)
                }
                is UpgradeResult.Error -> onCheckFinished?.invoke(result)
            }
        }
    }

    checkInbox.value?.let { result ->
        UpgradeDialog(
            result = result,
            forceUpdate = result.info.forceUpdate,
            upgradeManager = upgradeManager,
            onDismiss = {
                checkInbox.value = null
            }
        )
    }
}
