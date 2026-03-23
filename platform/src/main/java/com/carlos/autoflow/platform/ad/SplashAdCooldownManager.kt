package com.carlos.autoflow.platform.ad

import android.content.Context
import com.carlos.autoflow.utils.AutoFlowLogger
import java.util.Calendar

/** 负责开屏广告的频率控制，包括冷启动/热启动间隔和每天展示上限。 */
class SplashAdCooldownManager(
    private val preferenceStore: AdPreferenceStore,
    context: Context
) {
    companion object {
        private const val TAG = "SplashAdCooldown"
        private const val KEY_SPLASH_LAST_SHOWN = "key_splash_last_shown"
        private const val KEY_SPLASH_LAST_HOT_SHOWN = "key_splash_last_hot_shown"
        private const val KEY_SPLASH_DAILY_COUNT = "key_splash_daily_count"
        private const val KEY_SPLASH_DAILY_DATE = "key_splash_daily_date"
        private const val HOT_COOLDOWN_MS = 10 * 60 * 1000L // 热启动 10 分钟内只展示一次
        private const val DAILY_LIMIT = 10 // 每天最多展示次数
    }

    private val prefs =
        context.applicationContext.getSharedPreferences("ad_cooldown_meta", Context.MODE_PRIVATE)

    /**
     * 判断当前场景是否可以展示开屏广告。
     * @param isColdStart 是否为冷启动（无需热启动计时）。
     */
    fun shouldShowSplash(isColdStart: Boolean): Boolean {
        if (!hasRemainingDailyQuota()) {
            AutoFlowLogger.d(TAG, "展示次数已达上限")
            return false
        }

        return if (isColdStart) {
            val cooldownMs = preferenceStore.getCooldown(AdType.SPLASH)
            val lastShown = prefs.getLong(KEY_SPLASH_LAST_SHOWN, 0L)
            val elapsed = System.currentTimeMillis() - lastShown
            AutoFlowLogger.d(TAG, "冷启动检查：上次展示=$lastShown，间隔=${elapsed}ms，冷却=${cooldownMs}ms")
            System.currentTimeMillis() - lastShown >= cooldownMs
        } else {
            val lastHot = prefs.getLong(KEY_SPLASH_LAST_HOT_SHOWN, 0L)
            val elapsed = System.currentTimeMillis() - lastHot
            AutoFlowLogger.d(TAG, "热启动检查：上次热展示=$lastHot，间隔=${elapsed}ms，热冷却=${HOT_COOLDOWN_MS}ms")
            System.currentTimeMillis() - lastHot >= HOT_COOLDOWN_MS
        }
    }

    /**
     * 标记一次开屏展示，更新冷启动/热启动时间和每日计数。
     */
    fun markSplashShown(isColdStart: Boolean) {
        val editor = prefs.edit()
        val now = System.currentTimeMillis()
        if (isColdStart) {
            editor.putLong(KEY_SPLASH_LAST_SHOWN, now)
        } else {
            editor.putLong(KEY_SPLASH_LAST_HOT_SHOWN, now)
        }
        editor.apply()
        AutoFlowLogger.d(TAG, "记录展示（冷启动=$isColdStart），时间=$now")
        updateDailyCount()
    }

    /**
     * 检查今天还有没有剩余展示次数，超过 10 次后阻止再展示。
     */
    private fun hasRemainingDailyQuota(): Boolean {
        val todayKey = currentDateKey()
        val recordedDate = prefs.getString(KEY_SPLASH_DAILY_DATE, todayKey) ?: todayKey
        if (recordedDate != todayKey) {
            prefs.edit().putString(KEY_SPLASH_DAILY_DATE, todayKey).putInt(KEY_SPLASH_DAILY_COUNT, 0).apply()
            AutoFlowLogger.d(TAG, "新的一天，重置计数，日期=$todayKey")
            return true
        }
        val count = prefs.getInt(KEY_SPLASH_DAILY_COUNT, 0)
        AutoFlowLogger.d(TAG, "今日展示=$count/$DAILY_LIMIT")
        return count < DAILY_LIMIT
    }

    private fun updateDailyCount() {
        val todayKey = currentDateKey()
        val recordedDate = prefs.getString(KEY_SPLASH_DAILY_DATE, todayKey) ?: todayKey
        val editor = prefs.edit()
        if (recordedDate != todayKey) {
            editor.putString(KEY_SPLASH_DAILY_DATE, todayKey).putInt(KEY_SPLASH_DAILY_COUNT, 1)
            AutoFlowLogger.d(TAG, "今日首次展示，计数=1")
        } else {
            val current = prefs.getInt(KEY_SPLASH_DAILY_COUNT, 0)
            editor.putInt(KEY_SPLASH_DAILY_COUNT, current + 1)
            AutoFlowLogger.d(TAG, "今日展示计数+1=${current + 1}")
        }
        editor.apply()
    }

    private fun currentDateKey(): String {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        return "%04d-%02d-%02d".format(year, month, day)
    }
}
