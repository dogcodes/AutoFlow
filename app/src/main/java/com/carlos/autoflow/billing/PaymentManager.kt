package com.carlos.autoflow.billing

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 支付管理器 - 国内支付平台
 */
class PaymentManager(private val context: Context) {
    
    companion object {
        private const val TAG = "PaymentManager"
        
        // 商品信息
        const val PRODUCT_PREMIUM_YEAR = "autoflow_premium_year"
        const val PRODUCT_PREMIUM_MONTH = "autoflow_premium_month"
        const val PRODUCT_EXTRA_RECORDINGS = "autoflow_extra_recordings"
    }
    
    data class Product(
        val id: String,
        val name: String,
        val description: String,
        val price: String,
        val originalPrice: String? = null
    )
    
    /**
     * 获取商品列表
     */
    fun getProducts(): List<Product> {
        return listOf(
            Product(
                id = PRODUCT_PREMIUM_YEAR,
                name = "专业版年费",
                description = "无限录制 + 无广告 + 全功能",
                price = "¥88",
                originalPrice = "¥128"
            ),
            Product(
                id = PRODUCT_PREMIUM_MONTH,
                name = "专业版月费",
                description = "无限录制 + 无广告 + 全功能",
                price = "¥12"
            ),
            Product(
                id = PRODUCT_EXTRA_RECORDINGS,
                name = "额外录制次数",
                description = "10次额外录制机会",
                price = "¥6"
            )
        )
    }
    
    /**
     * 发起支付
     */
    fun startPayment(
        activity: Activity,
        productId: String,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit,
        onCancel: () -> Unit
    ) {
        try {
            Log.d(TAG, "发起支付: $productId")
            
            // 这里应该调用真实的支付SDK
            // 例如：支付宝SDK、微信支付SDK等
            
            // 模拟支付流程
            when (productId) {
                PRODUCT_PREMIUM_YEAR -> {
                    // 模拟支付成功
                    val orderId = "ORDER_${System.currentTimeMillis()}"
                    onSuccess(orderId)
                }
                PRODUCT_PREMIUM_MONTH -> {
                    val orderId = "ORDER_${System.currentTimeMillis()}"
                    onSuccess(orderId)
                }
                PRODUCT_EXTRA_RECORDINGS -> {
                    val orderId = "ORDER_${System.currentTimeMillis()}"
                    onSuccess(orderId)
                }
                else -> {
                    onError("未知商品")
                }
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "支付失败", e)
            onError(e.message ?: "支付异常")
        }
    }
    
    /**
     * 验证支付结果
     */
    fun verifyPayment(orderId: String): Boolean {
        // 这里应该向服务器验证支付结果
        // 简化实现，直接返回true
        Log.d(TAG, "验证支付: $orderId")
        return true
    }
}

/**
 * 支付对话框
 */
@Composable
fun PaymentDialog(
    onDismiss: () -> Unit,
    onPaymentSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val paymentManager = remember { PaymentManager(context) }
    val products = remember { paymentManager.getProducts() }
    
    var selectedProduct by remember { mutableStateOf<PaymentManager.Product?>(null) }
    var isProcessing by remember { mutableStateOf(false) }
    var paymentMessage by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Text(
                "升级专业版", 
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            ) 
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "选择套餐，解锁全部功能",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 商品列表
                products.forEach { product ->
                    ProductItem(
                        product = product,
                        isSelected = selectedProduct?.id == product.id,
                        onClick = { selectedProduct = product }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                // 支付按钮
                if (selectedProduct != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Button(
                        onClick = {
                            if (context is Activity) {
                                isProcessing = true
                                paymentManager.startPayment(
                                    activity = context,
                                    productId = selectedProduct!!.id,
                                    onSuccess = { orderId ->
                                        if (paymentManager.verifyPayment(orderId)) {
                                            // 处理支付成功
                                            when (selectedProduct!!.id) {
                                                PaymentManager.PRODUCT_PREMIUM_YEAR,
                                                PaymentManager.PRODUCT_PREMIUM_MONTH -> {
                                                    // 激活专业版
                                                    val licenseManager = LicenseManager(context)
                                                    val deviceCode = licenseManager.getDeviceActivationCode()
                                                    licenseManager.activateLicense(deviceCode)
                                                }
                                                PaymentManager.PRODUCT_EXTRA_RECORDINGS -> {
                                                    // 增加录制次数
                                                    val featureManager = FeatureManager(context)
                                                    repeat(10) { featureManager.earnExtraRecording() }
                                                }
                                            }
                                            onPaymentSuccess()
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
                        enabled = !isProcessing,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1976D2)
                        )
                    ) {
                        if (isProcessing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
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
                
                // 消息显示
                if (paymentMessage.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = paymentMessage,
                        fontSize = 12.sp,
                        color = if (paymentMessage.contains("成功")) Color(0xFF4CAF50) else Color(0xFFF44336),
                        textAlign = TextAlign.Center
                    )
                }
                
                // 支付说明
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "• 支持支付宝、微信支付\n• 购买后立即生效\n• 7天无理由退款",
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
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (isSelected) Modifier else Modifier
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFFE3F2FD) else Color(0xFFF5F5F5)
        ),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(
            2.dp, Color(0xFF1976D2)
        ) else null,
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
                    
                    // 推荐标签
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
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1976D2)
                )
                
                product.originalPrice?.let { originalPrice ->
                    Text(
                        text = originalPrice,
                        fontSize = 12.sp,
                        color = Color.Gray,
                        textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                    )
                }
            }
        }
    }
}
