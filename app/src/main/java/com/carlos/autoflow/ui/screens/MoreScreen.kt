package com.carlos.autoflow.ui.screens

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.carlos.autoflow.compliance.ComplianceConfig
import com.carlos.autoflow.foundation.network.WebRoutes
import com.carlos.autoflow.foundation.ui.WebViewActivity
import com.carlos.autoflow.ui.screens.AboutActivity
import com.carlos.autoflow.ui.screens.HistoryActivity
import com.carlos.autoflow.ui.screens.LicenseActivity
import com.carlos.autoflow.ui.screens.NodeMonitorActivity
import com.carlos.autoflow.ui.screens.SettingsActivity

@Composable
fun MoreScreen(
    contentPadding: PaddingValues,
    onLaunchDemo: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val menuItems = buildList<MoreMenuItemData> {
        add(
            MoreMenuItemData("历史记录", Icons.Default.History) {
                context.startActivity(Intent(context, HistoryActivity::class.java))
            }
        )
        if (!ComplianceConfig.isComplianceMode) {
            add(
                MoreMenuItemData("节点监控器", Icons.Default.Visibility) {
                    context.startActivity(Intent(context, NodeMonitorActivity::class.java))
                }
            )
        }
        add(
            MoreMenuItemData("示例应用", Icons.Default.Apps) {
                onLaunchDemo()
            }
        )
        add(
            MoreMenuItemData("设置", Icons.Default.Settings) {
                context.startActivity(Intent(context, SettingsActivity::class.java))
            }
        )
        add(
            MoreMenuItemData("关于", Icons.Default.Info) {
                context.startActivity(Intent(context, AboutActivity::class.java))
            }
        )
        add(
            MoreMenuItemData("帮助", Icons.Default.Visibility) {
                context.startActivity(
                    WebViewActivity.createIntent(
                        context,
                        WebRoutes.HELP,
                        "帮助"
                    )
                )
            }
        )
        add(
            MoreMenuItemData("许可证管理", Icons.Default.Stars) {
                context.startActivity(Intent(context, LicenseActivity::class.java))
            }
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(contentPadding)
    ) {
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
            items(menuItems) { item ->
                MoreMenuItem(title = item.title, icon = item.icon, onClick = item.action)
            }
        }
    }
}

private data class MoreMenuItemData(
    val title: String,
    val icon: ImageVector,
    val action: () -> Unit
)

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
        androidx.compose.foundation.layout.Row(
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
