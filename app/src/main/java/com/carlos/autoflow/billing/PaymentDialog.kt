package com.carlos.autoflow.billing

import android.app.Activity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.carlos.autoflow.BuildConfig
import com.carlos.autoflow.license.PaymentManager

@Composable
fun PaymentDialog(
    onDismiss: () -> Unit,
    onPaymentSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val paymentManager = remember { PaymentManager(context) }
    val products = remember { paymentManager.getProducts() }
    val paymentAvailable = remember { paymentManager.isPaymentAvailable() }

    var selectedProduct by remember { mutableStateOf<PaymentManager.Product?>(null) }
    var isProcessing by remember { mutableStateOf(false) }
    var paymentMessage by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "购买使用时长",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "选择时长套餐，激活全部功能",
                    fontSize = 14.sp,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(16.dp))

                products.forEach { product ->
                    ProductItem(
                        product = product,
                        isSelected = selectedProduct?.id == product.id,
                        onClick = { selectedProduct = product }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (selectedProduct != null) {
                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (!paymentAvailable) {
                                paymentMessage = "支付暂未开放，请联系微信 xbdcc1 购买激活码\n并激活使用时长"
                                return@Button
                            }
                            if (context is Activity) {
                                isProcessing = true
                                paymentManager.startPayment(
                                    activity = context,
                                    productId = selectedProduct!!.id,
                                    onSuccess = { orderId ->
                                        if (paymentManager.verifyPayment(orderId)) {
                                            if (paymentManager.applyPurchase(
                                                    selectedProduct!!.id,
                                                    BuildConfig.FORCE_PREMIUM
                                                )
                                            ) {
                                                onPaymentSuccess()
                                            } else {
                                                paymentMessage = "支付成功，但权益发放失败"
                                            }
                                        }
                                        isProcessing = false
                                    },
                                    onError = { error ->
                                        paymentMessage = "支付失败: $error"
                                        isProcessing = false
                                    },
                                    onCancel = {
                                        paymentMessage = "支付已取消"
                                        isProcessing = false
                                    }
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = paymentAvailable && !isProcessing,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1976D2)
                        )
                    ) {
                        if (isProcessing) {
                            CircularProgressIndicator(
                                modifier = Modifier.width(16.dp).height(16.dp),
                                strokeWidth = 2.dp,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("处理中...")
                        } else {
                            Text("立即支付 ${selectedProduct!!.price}")
                        }
                    }
                }

                if (!paymentAvailable) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "支付暂未开放，请联系微信 xbdcc1 购买激活码\n并激活使用时长",
                        fontSize = 12.sp,
                        color = Color(0xFFF44336),
                        textAlign = TextAlign.Center
                    )
                }

                if (paymentMessage.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = paymentMessage,
                        fontSize = 12.sp,
                        color = if (paymentMessage.contains("成功")) {
                            Color(0xFF4CAF50)
                        } else {
                            Color(0xFFF44336)
                        },
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "• 支持支付宝、微信支付\n• 购买后立即生效\n• 时长按设备绑定",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("关闭")
            }
        }
    )
}

@Composable
private fun ProductItem(
    product: PaymentManager.Product,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFFE3F2FD) else Color(0xFFF5F5F5)
        ),
        border = if (isSelected) BorderStroke(2.dp, Color(0xFF1976D2)) else null,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = product.name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )

                    if (product.id == PaymentManager.PRODUCT_PREMIUM_YEAR) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFFF9800)
                            ),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                "推荐",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                fontSize = 10.sp,
                                color = Color.White
                            )
                        }
                    }
                }

                Text(
                    text = product.description,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = product.price,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1976D2)
                )
                product.originalPrice?.let {
                    Text(
                        text = it,
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF1976D2)
                    )
                }
            }
        }
    }
}
