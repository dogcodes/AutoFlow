package com.carlos.autoflow.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.carlos.autoflow.billing.ui.LicenseDialog
import com.carlos.autoflow.monitor.NodeMonitorDemo

private enum class MoreDestination {
    MENU,
    HISTORY,
    MONITOR,
    SETTINGS,
    ABOUT
}

@Composable
fun MoreScreen(
    contentPadding: PaddingValues,
    onLaunchDemo: () -> Unit,
    modifier: Modifier = Modifier
) {
    var destination by remember { mutableStateOf(MoreDestination.MENU) }
    var showLicenseDialog by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(contentPadding)
    ) {
        when (destination) {
            MoreDestination.MENU -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text(
                            text = "更多",
                            style = MaterialTheme.typography.headlineSmall
                        )
                    }
                    item {
                        MoreMenuItem("历史记录", Icons.Default.History) {
                            destination = MoreDestination.HISTORY
                        }
                    }
                    item {
                        MoreMenuItem("节点监控器", Icons.Default.Visibility) {
                            destination = MoreDestination.MONITOR
                        }
                    }
                    item {
                        MoreMenuItem("示例应用", Icons.Default.Apps, onLaunchDemo)
                    }
                    item {
                        MoreMenuItem("设置", Icons.Default.Settings) {
                            destination = MoreDestination.SETTINGS
                        }
                    }
                    item {
                        MoreMenuItem("关于", Icons.Default.Info) {
                            destination = MoreDestination.ABOUT
                        }
                    }
                    item {
                        MoreMenuItem("许可证管理", Icons.Default.Stars) {
                            showLicenseDialog = true
                        }
                    }
                }
            }

            MoreDestination.HISTORY -> MoreScreenContainer(
                title = "历史记录",
                onBack = { destination = MoreDestination.MENU }
            ) {
                HistoryScreen()
            }

            MoreDestination.MONITOR -> MoreScreenContainer(
                title = "节点监控器",
                onBack = { destination = MoreDestination.MENU }
            ) {
                NodeMonitorDemo()
            }

            MoreDestination.SETTINGS -> MoreScreenContainer(
                title = "设置",
                onBack = { destination = MoreDestination.MENU }
            ) {
                SettingsScreen()
            }

            MoreDestination.ABOUT -> MoreScreenContainer(
                title = "关于",
                onBack = { destination = MoreDestination.MENU }
            ) {
                AboutScreen()
            }
        }

        if (showLicenseDialog) {
            LicenseDialog(
                onDismiss = { showLicenseDialog = false }
            )
        }
    }
}

@Composable
private fun MoreScreenContainer(
    title: String,
    onBack: () -> Unit,
    content: @Composable () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "返回"
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall
            )
        }
        Box(modifier = Modifier.fillMaxSize()) {
            content()
        }
    }
}

@Composable
private fun MoreMenuItem(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null)
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )
            Icon(Icons.Default.ChevronRight, contentDescription = null)
        }
    }
}
