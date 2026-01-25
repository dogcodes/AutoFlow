package com.carlos.autoflow.billing

import android.content.Context
import android.content.SharedPreferences
import java.text.SimpleDateFormat
import java.util.*

/**
 * 功能限制管理器
 */
class FeatureManager(private val context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences("features", Context.MODE_PRIVATE)
    private val licenseManager = LicenseManager(context)
    
    companion object {
        private const val KEY_DAILY_RECORDINGS = "daily_recordings"
        private const val KEY_LAST_RESET_DATE = "last_reset_date"
        private const val MAX_FREE_RECORDINGS = 3
    }
    
    /**
     * 检查是否可以开始录制
     */
    fun canStartRecording(): Boolean {
        if (licenseManager.isPremium()) return true
        
        resetDailyCountIfNeeded()
        val todayCount = prefs.getInt(KEY_DAILY_RECORDINGS, 0)
        return todayCount < MAX_FREE_RECORDINGS
    }
    
    /**
     * 记录一次录制使用
     */
    fun recordRecordingUsage() {
        if (licenseManager.isPremium()) return
        
        resetDailyCountIfNeeded()
        val todayCount = prefs.getInt(KEY_DAILY_RECORDINGS, 0)
        prefs.edit().putInt(KEY_DAILY_RECORDINGS, todayCount + 1).apply()
    }
    
    /**
     * 获取今日剩余录制次数
     */
    fun getRemainingRecordings(): Int {
        if (licenseManager.isPremium()) return Int.MAX_VALUE
        
        resetDailyCountIfNeeded()
        val todayCount = prefs.getInt(KEY_DAILY_RECORDINGS, 0)
        return (MAX_FREE_RECORDINGS - todayCount).coerceAtLeast(0)
    }
    
    /**
     * 是否显示广告
     */
    fun shouldShowAds(): Boolean {
        return !licenseManager.isPremium()
    }
    
    /**
     * 是否可以使用高级节点
     */
    fun canUseAdvancedNodes(): Boolean {
        return licenseManager.isPremium()
    }
    
    /**
     * 获取限制的节点类型
     */
    fun getRestrictedNodeTypes(): Set<String> {
        return if (licenseManager.isPremium()) {
            emptySet()
        } else {
            setOf(
                "UI_SWIPE",
                "UI_GET_TEXT", 
                "UI_CHECK",
                "APP_LAUNCH",
                "NOTIFICATION",
                "SCREEN_STATE"
            )
        }
    }
    
    /**
     * 检查节点是否可用
     */
    fun isNodeTypeAvailable(nodeType: String): Boolean {
        return !getRestrictedNodeTypes().contains(nodeType)
    }
    
    /**
     * 重置每日计数（如果需要）
     */
    private fun resetDailyCountIfNeeded() {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val lastResetDate = prefs.getString(KEY_LAST_RESET_DATE, "")
        
        if (today != lastResetDate) {
            prefs.edit()
                .putInt(KEY_DAILY_RECORDINGS, 0)
                .putString(KEY_LAST_RESET_DATE, today)
                .apply()
        }
    }
    
    /**
     * 获取升级提示消息
     */
    fun getUpgradeMessage(): String {
        return when {
            !canStartRecording() -> "今日录制次数已用完，升级专业版享受无限录制"
            shouldShowAds() -> "升级专业版，移除广告并解锁全部功能"
            else -> "升级专业版，解锁更多高级功能"
        }
    }
}
