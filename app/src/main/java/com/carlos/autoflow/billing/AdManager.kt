package com.carlos.autoflow.billing

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.ViewGroup
import androidx.compose.runtime.*
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 广告管理器 - 国内广告平台适配
 */
class AdManager(private val context: Context) {
    
    companion object {
        private const val TAG = "AdManager"
        private var isInitialized = false
    }
    
    /**
     * 初始化广告SDK
     */
    fun initialize() {
        if (isInitialized) return
        
        try {
            // 这里应该初始化真实的广告SDK
            // 例如：穿山甲、优量汇等
            Log.d(TAG, "广告SDK初始化成功")
            isInitialized = true
        } catch (e: Exception) {
            Log.e(TAG, "广告SDK初始化失败", e)
        }
    }
    
    /**
     * 加载横幅广告
     */
    fun loadBannerAd(
        adUnitId: String,
        onAdLoaded: () -> Unit = {},
        onAdFailed: (String) -> Unit = {}
    ) {
        try {
            // 模拟广告加载
            Log.d(TAG, "加载横幅广告: $adUnitId")
            onAdLoaded()
        } catch (e: Exception) {
            Log.e(TAG, "横幅广告加载失败", e)
            onAdFailed(e.message ?: "未知错误")
        }
    }
    
    /**
     * 加载插屏广告
     */
    fun loadInterstitialAd(
        adUnitId: String,
        onAdLoaded: () -> Unit = {},
        onAdFailed: (String) -> Unit = {}
    ) {
        try {
            Log.d(TAG, "加载插屏广告: $adUnitId")
            onAdLoaded()
        } catch (e: Exception) {
            Log.e(TAG, "插屏广告加载失败", e)
            onAdFailed(e.message ?: "未知错误")
        }
    }
    
    /**
     * 显示插屏广告
     */
    fun showInterstitialAd(
        activity: Activity,
        onAdShown: () -> Unit = {},
        onAdClosed: () -> Unit = {}
    ) {
        try {
            Log.d(TAG, "显示插屏广告")
            onAdShown()
            // 模拟广告关闭
            onAdClosed()
        } catch (e: Exception) {
            Log.e(TAG, "插屏广告显示失败", e)
        }
    }
    
    /**
     * 加载激励视频广告
     */
    fun loadRewardedAd(
        adUnitId: String,
        onAdLoaded: () -> Unit = {},
        onAdFailed: (String) -> Unit = {}
    ) {
        try {
            Log.d(TAG, "加载激励视频广告: $adUnitId")
            onAdLoaded()
        } catch (e: Exception) {
            Log.e(TAG, "激励视频广告加载失败", e)
            onAdFailed(e.message ?: "未知错误")
        }
    }
    
    /**
     * 显示激励视频广告
     */
    fun showRewardedAd(
        activity: Activity,
        onRewarded: () -> Unit = {},
        onAdClosed: () -> Unit = {}
    ) {
        try {
            Log.d(TAG, "显示激励视频广告")
            // 模拟观看完成
            onRewarded()
            onAdClosed()
        } catch (e: Exception) {
            Log.e(TAG, "激励视频广告显示失败", e)
        }
    }
    
    /**
     * 检查广告是否可用
     */
    fun isAdAvailable(): Boolean {
        return isInitialized
    }
}

/**
 * 横幅广告组件
 */
@Composable
fun BannerAdView(
    modifier: Modifier = Modifier,
    adUnitId: String = "demo_banner_ad"
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val adManager = remember { AdManager(context) }
    var isAdLoaded by remember { mutableStateOf(false) }
    var adError by remember { mutableStateOf<String?>(null) }
    
    LaunchedEffect(Unit) {
        adManager.initialize()
        adManager.loadBannerAd(
            adUnitId = adUnitId,
            onAdLoaded = { isAdLoaded = true },
            onAdFailed = { error -> adError = error }
        )
    }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(60.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            when {
                isAdLoaded -> {
                    // 这里应该显示真实的广告视图
                    // AndroidView(factory = { context -> 真实广告视图 })
                    
                    // 模拟广告内容
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color(0xFF2196F3)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("AD", color = Color.White, fontSize = 12.sp)
                        }
                        Column {
                            Text(
                                "AutoFlow Pro - 专业版",
                                fontSize = 12.sp,
                                color = Color.Black
                            )
                            Text(
                                "无限录制，无广告干扰",
                                fontSize = 10.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
                adError != null -> {
                    Text(
                        "广告加载失败",
                        fontSize = 10.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
                else -> {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                }
            }
        }
    }
}

/**
 * 激励视频广告按钮
 */
@Composable
fun RewardedAdButton(
    onRewardEarned: () -> Unit,
    modifier: Modifier = Modifier,
    text: String = "观看广告获得奖励"
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val adManager = remember { AdManager(context) }
    var isLoading by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        adManager.initialize()
    }
    
    Button(
        onClick = {
            isLoading = true
            adManager.loadRewardedAd(
                adUnitId = "demo_rewarded_ad",
                onAdLoaded = {
                    if (context is Activity) {
                        adManager.showRewardedAd(
                            activity = context,
                            onRewarded = {
                                onRewardEarned()
                                isLoading = false
                            },
                            onAdClosed = {
                                isLoading = false
                            }
                        )
                    }
                },
                onAdFailed = {
                    isLoading = false
                }
            )
        },
        modifier = modifier,
        enabled = !isLoading && adManager.isAdAvailable(),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFFFF9800)
        )
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                strokeWidth = 2.dp,
                color = Color.White
            )
        } else {
            Text(text)
        }
    }
}
