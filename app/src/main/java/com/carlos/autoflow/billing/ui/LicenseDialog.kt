package com.carlos.autoflow.billing.ui

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.carlos.autoflow.BuildConfig
import com.carlos.autoflow.billing.PaymentDialog
import com.carlos.autoflow.license.ActivationResult
import com.carlos.autoflow.license.FailureReason
import com.carlos.autoflow.license.LicenseManager
import com.carlos.autoflow.utils.TrustedTimeProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun LicenseDialog(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val licenseManager = remember { LicenseManager(context, BuildConfig.FORCE_PREMIUM) }

    var licenseStatus by remember { mutableStateOf(licenseManager.getLicenseStatus()) }
    var activationCode by remember { mutableStateOf("") }
    var showActivation by remember { mutableStateOf(false) }
    var showPayment by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }
    var isTimeAbnormal by remember { mutableStateOf(false) }

    fun refreshState() {
        licenseStatus = licenseManager.getLicenseStatus()
        if (licenseManager.isSystemTimeAbnormal()) {
            isTimeAbnormal = true
            val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            message = "⚠️ 系统时间异常（$date），请在系统设置中校准后重试"
        } else {
            isTimeAbnormal = false
            if (message.startsWith("⚠️ 系统时间异常")) message = ""
        }
    }

    // 进入页面时预取网络时间 + 检测时间异常
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            TrustedTimeProvider.prefetch(licenseManager.getPrefs())
        }
        refreshState()
    }

    // 从后台切回来时刷新状态
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) refreshState()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "许可证管理",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 当前状态显示
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = when {
                            isTimeAbnormal -> Color(0xFFF44336)
                            licenseStatus == LicenseManager.STATUS_PREMIUM -> Color(0xFF4CAF50)
                            licenseStatus == LicenseManager.STATUS_EXPIRED -> Color(0xFFFF9800)
                            else -> Color(0xFF9E9E9E)
                        }
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = when {
                                isTimeAbnormal -> Icons.Default.Warning
                                licenseStatus == LicenseManager.STATUS_PREMIUM -> Icons.Default.CheckCircle
                                licenseStatus == LicenseManager.STATUS_EXPIRED -> Icons.Default.Warning
                                else -> Icons.Default.Info
                            },
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = when {
                                isTimeAbnormal -> "时间异常"
                                licenseStatus == LicenseManager.STATUS_PREMIUM -> "已激活"
                                licenseStatus == LicenseManager.STATUS_EXPIRED -> "已过期"
                                else -> "未激活"
                            },
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )

                        // 有激活记录时始终显示截止日期，时间异常时也显示方便用户核对
                        val expiryTimestamp = licenseManager.getExpiryTimestamp()
                        expiryTimestamp?.let {
                            val formatter = SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss", Locale.getDefault())
                            Text(
                                text = "截至 ${formatter.format(Date(it))}",
                                color = if (isTimeAbnormal) Color(0xFFFFCDD2) else Color.White,
                                fontSize = 12.sp
                            )
                        }                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 功能对比
//                Column {
//                    Text(
//                        text = "版本对比",
//                        fontSize = 14.sp,
//                        fontWeight = FontWeight.Bold
//                    )
//
//                    Spacer(modifier = Modifier.height(8.dp))
//
//                    FeatureRow("每日录制次数", "3次", "无限制")
//                    FeatureRow("广告显示", "有", "无")
//                    FeatureRow("高级节点", "部分", "全部")
//                    FeatureRow("技术支持", "社区", "优先")
//                }

                Spacer(modifier = Modifier.height(16.dp))

                // 激活区域
                if (!showActivation) {
                    Column {
                        Button(
                            onClick = { showPayment = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF1976D2)
                            )
                        ) {
                            Text("购买使用时长")
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedButton(
                            onClick = { showActivation = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (licenseStatus == LicenseManager.STATUS_PREMIUM) "延长使用时长" else "已有激活码")
                        }
                    }
                } else {
                    Column {
                        OutlinedTextField(
                            value = activationCode,
                            onValueChange = { activationCode = it },
                            label = { Text("激活码") },
                            placeholder = { Text("请输入24位激活码") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { showActivation = false },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("取消")
                            }

                                Button(
                                    onClick = {
                                        if (activationCode.length == 24) {
                                            when (val result = licenseManager.activateLicense(activationCode)) {
                                                ActivationResult.Success -> {
                                                    licenseStatus = LicenseManager.STATUS_PREMIUM
                                                    message = "激活成功！"
                                                    showActivation = false
                                                }
                                                is ActivationResult.Failure -> {
                                                    message = result.reason.toMessage()
                                                }
                                            }
                                        } else {
                                            message = "激活码格式错误"
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    enabled = activationCode.length == 24
                                ) {
                                    Text("激活")
                                }
                        }
                    }
                }

                // 消息显示
                if (message.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = message,
                        color = if (message.contains("成功")) Color(0xFF4CAF50) else Color(0xFFF44336),
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }

                // 设备信息 (调试用)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "设备ID: ${licenseManager.getDeviceId()}",
                    fontSize = 10.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = if (BuildConfig.DEBUG) {
                        Modifier.clickable {
                            val deviceId = licenseManager.getDeviceId()
                            clipboardManager.setText(AnnotatedString(deviceId))
                            message = "设备ID已复制"
                            runCatching {
                                context.startActivity(
                                    Intent()
                                        .setClassName(
                                            context,
                                            "com.carlos.autoflow.license.LicenseDebugActivity"
                                        )
                                        .putExtra("device_id", deviceId)
                                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                )
                            }.onFailure {
                                message = "设备ID已复制，调试页不可用"
                            }
                        }
                    } else {
                        Modifier
                    }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("关闭")
            }
        }
    )
    
    if (showPayment) {
        PaymentDialog(
            onDismiss = { showPayment = false },
            onPaymentSuccess = {
                licenseStatus = LicenseManager.STATUS_PREMIUM
                message = "购买成功，使用时长已生效！"
                showPayment = false
            }
        )
    }
}

@Composable
private fun FeatureRow(
    feature: String,
    freeValue: String,
    premiumValue: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = feature,
            fontSize = 12.sp,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = freeValue,
            fontSize = 12.sp,
            color = Color.Gray,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center
        )
        Text(
            text = premiumValue,
            fontSize = 12.sp,
            color = Color(0xFF4CAF50),
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.End
        )
    }
}

private fun FailureReason.toMessage(): String {
    return when (this) {
        FailureReason.FORMAT_ERROR -> "激活码格式错误"
        FailureReason.EXPIRED -> "激活码已过期"
        FailureReason.TYPE_MISMATCH -> "此激活码不适用于当前应用"
        FailureReason.ALREADY_USED -> "激活码已被使用"
        FailureReason.DEVICE_MISMATCH -> "设备与激活码不匹配"
        FailureReason.SYSTEM_TIME_INVALID -> "系统日期异常，请在系统设置中校准后重试"
        FailureReason.TIME_SYNC_FAILED -> "无法获取网络时间，请检查网络连接并确认系统时间正确后重试"
        FailureReason.UNKNOWN -> "激活码无效"
    }
}
