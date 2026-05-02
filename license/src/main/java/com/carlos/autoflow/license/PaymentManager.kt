package com.carlos.autoflow.license

import android.app.Activity
import android.content.Context
import android.util.Log

class PaymentManager(private val context: Context) {

    companion object {
        private const val TAG = "PaymentManager"

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

    fun getProducts(): List<Product> {
        return listOf(
            Product(
                id = PRODUCT_PREMIUM_YEAR,
                name = "365天使用时长",
                description = "365天内可使用全部功能",
                price = "¥66.6",
//                originalPrice = "¥128"
            ),
            Product(
                id = PRODUCT_PREMIUM_MONTH,
                name = "90天使用时长",
                description = "90天内可使用全部功能",
//                description = "无限录制 + 无广告 + 全功能",
                price = "¥18.88",
//                originalPrice = "¥33"
            ),
            Product(
                id = PRODUCT_PREMIUM_MONTH,
                name = "30天使用时长",
                description = "30天内可使用全部功能",
//                description = "无限录制 + 无广告 + 全功能",
                price = "¥6.66",
//                originalPrice = "¥12"

            ),
//            Product(
//                id = PRODUCT_EXTRA_RECORDINGS,
//                name = "额外录制次数",
//                description = "10次额外录制机会",
//                price = "¥6"
//            )
        )
    }

    fun startPayment(
        activity: Activity,
        productId: String,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit,
        onCancel: () -> Unit
    ) {
        try {
            Log.d(TAG, "发起支付: $productId")

            when (productId) {
                PRODUCT_PREMIUM_YEAR,
                PRODUCT_PREMIUM_MONTH,
                PRODUCT_EXTRA_RECORDINGS -> {
                    val orderId = "ORDER_${System.currentTimeMillis()}"
                    onSuccess(orderId)
                }
                else -> onError("未知商品")
            }
        } catch (e: Exception) {
            Log.e(TAG, "支付失败", e)
            onError(e.message ?: "支付异常")
        }
    }

    fun isPaymentAvailable(): Boolean = false

    fun verifyPayment(orderId: String): Boolean {
        Log.d(TAG, "验证支付: $orderId")
        return true
    }

    fun applyPurchase(productId: String, forcePremium: Boolean = false): Boolean {
        return when (productId) {
            PRODUCT_PREMIUM_YEAR,
            PRODUCT_PREMIUM_MONTH -> {
                val licenseManager = LicenseManager(context, forcePremium)
                if (licenseManager.isSystemTimeAbnormal()) return false
                val days = if (productId == PRODUCT_PREMIUM_YEAR) 365 else 30
                licenseManager.grantDays(days)
            }

            PRODUCT_EXTRA_RECORDINGS -> {
                val featureManager = FeatureManager(context, forcePremium)
                repeat(10) { featureManager.earnExtraRecording() }
                true
            }

            else -> false
        }
    }
}
