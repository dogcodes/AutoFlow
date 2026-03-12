package com.carlos.autoflow.license

import android.content.Context
import android.content.SharedPreferences
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FeatureManager(
    context: Context,
    forcePremium: Boolean = false
) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("features", Context.MODE_PRIVATE)
    private val licenseManager = LicenseManager(context, forcePremium)

    companion object {
        private const val KEY_DAILY_RECORDINGS = "daily_recordings"
        private const val KEY_LAST_RESET_DATE = "last_reset_date"
        private const val MAX_FREE_RECORDINGS = 3
    }

    fun canStartRecording(): Boolean {
        if (licenseManager.isPremium()) return true

        resetDailyCountIfNeeded()
        val todayCount = prefs.getInt(KEY_DAILY_RECORDINGS, 0)
        return todayCount < MAX_FREE_RECORDINGS
    }

    fun recordRecordingUsage() {
        if (licenseManager.isPremium()) return

        resetDailyCountIfNeeded()
        val todayCount = prefs.getInt(KEY_DAILY_RECORDINGS, 0)
        prefs.edit().putInt(KEY_DAILY_RECORDINGS, todayCount + 1).apply()
    }

    fun getRemainingRecordings(): Int {
        if (licenseManager.isPremium()) return Int.MAX_VALUE

        resetDailyCountIfNeeded()
        val todayCount = prefs.getInt(KEY_DAILY_RECORDINGS, 0)
        return (MAX_FREE_RECORDINGS - todayCount).coerceAtLeast(0)
    }

    fun shouldShowAds(): Boolean {
        return !licenseManager.isPremium()
    }

    fun canUseAdvancedNodes(): Boolean {
        return licenseManager.isPremium()
    }

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

    fun isNodeTypeAvailable(nodeType: String): Boolean {
        return !getRestrictedNodeTypes().contains(nodeType)
    }

    fun earnExtraRecording() {
        resetDailyCountIfNeeded()
        val todayCount = prefs.getInt(KEY_DAILY_RECORDINGS, 0)
        if (todayCount > 0) {
            prefs.edit().putInt(KEY_DAILY_RECORDINGS, todayCount - 1).apply()
        }
    }

    fun getUpgradeMessage(): String {
        return when {
            !canStartRecording() -> "今日录制次数已用完，购买使用时长后可继续录制"
            shouldShowAds() -> "激活使用时长后可移除广告并解锁全部功能"
            else -> "激活使用时长后可解锁更多高级功能"
        }
    }

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
}
