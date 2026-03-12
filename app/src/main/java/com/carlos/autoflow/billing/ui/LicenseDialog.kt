package com.carlos.autoflow.billing.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.carlos.autoflow.BuildConfig
import com.carlos.autoflow.billing.PaymentDialog
import com.carlos.autoflow.license.LicenseManager

@Composable
fun LicenseDialog(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val licenseManager = remember { LicenseManager(context, BuildConfig.FORCE_PREMIUM) }
    
    var licenseStatus by remember { mutableStateOf(licenseManager.getLicenseStatus()) }
    var activationCode by remember { mutableStateOf("") }
    var showActivation by remember { mutableStateOf(false) }
    var showPayment by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }
    
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
                        containerColor = when (licenseStatus) {
                            LicenseManager.STATUS_PREMIUM -> Color(0xFF4CAF50)
                            LicenseManager.STATUS_EXPIRED -> Color(0xFFFF9800)
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
                            imageVector = when (licenseStatus) {
                                LicenseManager.STATUS_PREMIUM -> Icons.Default.CheckCircle
                                LicenseManager.STATUS_EXPIRED -> Icons.Default.Warning
                                else -> Icons.Default.Info
                            },
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = when (licenseStatus) {
                                LicenseManager.STATUS_PREMIUM -> "专业版"
                                LicenseManager.STATUS_EXPIRED -> "已过期"
                                else -> "免费版"
                            },
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        
                        if (licenseStatus == LicenseManager.STATUS_PREMIUM) {
                            Text(
                                text = "剩余 ${licenseManager.getRemainingDays()} 天",
                                color = Color.White,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 功能对比
                Column {
                    Text(
                        text = "版本对比",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    FeatureRow("每日录制次数", "3次", "无限制")
                    FeatureRow("广告显示", "有", "无")
                    FeatureRow("高级节点", "部分", "全部")
                    FeatureRow("技术支持", "社区", "优先")
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 激活区域
                if (licenseStatus != LicenseManager.STATUS_PREMIUM) {
                    if (!showActivation) {
                        Column {
                            Button(
                                onClick = { showPayment = true },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF1976D2)
                                )
                            ) {
                                Text("立即购买专业版")
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            OutlinedButton(
                                onClick = { showActivation = true },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("已有激活码")
                            }
                        }
                    } else {
                        Column {
                            OutlinedTextField(
                                value = activationCode,
                                onValueChange = { activationCode = it.uppercase() },
                                label = { Text("激活码") },
                                placeholder = { Text("XXXX-XXXX") },
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
                                        if (licenseManager.activateLicense(activationCode)) {
                                            licenseStatus = LicenseManager.STATUS_PREMIUM
                                            message = "激活成功！"
                                            showActivation = false
                                        } else {
                                            message = "激活码无效"
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    enabled = activationCode.length >= 8
                                ) {
                                    Text("激活")
                                }
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
                    text = "设备激活码: ${licenseManager.getDeviceActivationCode()}",
                    fontSize = 10.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("关闭")
            }
        }
    )
    
    // 支付对话框
    if (showPayment) {
        PaymentDialog(
            onDismiss = { showPayment = false },
            onPaymentSuccess = {
                licenseStatus = LicenseManager.STATUS_PREMIUM
                message = "购买成功，专业版已激活！"
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
