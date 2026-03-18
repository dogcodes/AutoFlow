package com.carlos.autoflow.foundation.upgrade.ui

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import com.carlos.autoflow.foundation.upgrade.UpgradeResult

@Composable
fun UpgradeDialog(
    result: UpgradeResult.Available,
    forceUpdate: Boolean,
    onConfirm: () -> Unit,
    onDismiss: (() -> Unit)? = null
) {
    AlertDialog(
        onDismissRequest = {
            if (!forceUpdate) {
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
            Text(
                text = result.info.releaseNotes
                    ?: "检测到新版本，请及时更新以获得更好的体验。"
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm()
                    if (!forceUpdate) {
                        onDismiss?.invoke()
                    }
                }
            ) {
                Text("立即更新")
            }
        },
        dismissButton = if (forceUpdate) null else {
            {
                TextButton(onClick = { onDismiss?.invoke() }) {
                    Text("暂不更新")
                }
            }
        }
    )
}
