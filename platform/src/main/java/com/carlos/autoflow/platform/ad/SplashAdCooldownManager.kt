package com.carlos.autoflow.platform.ad

import android.content.Context
import java.time.LocalDate

/** 负责开屏广告的频率控制，包括冷启动/热启动间隔和每天展示上限。 */
class SplashAdCooldownManager(
    private val preferenceStore: AdPreferenceStore,
    context: Context
) {
    companion object {
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
        if (!hasRemainingDailyQuota()) return false

        return if (isColdStart) {
            val cooldownMs = preferenceStore.getCooldown(AdType.SPLASH)
            val lastShown = prefs.getLong(KEY_SPLASH_LAST_SHOWN, 0L)
            System.currentTimeMillis() - lastShown >= cooldownMs
        } else {
            val lastHot = prefs.getLong(KEY_SPLASH_LAST_HOT_SHOWN, 0L)
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
            return true
        }
        val count = prefs.getInt(KEY_SPLASH_DAILY_COUNT, 0)
        return count < DAILY_LIMIT
    }

    private fun updateDailyCount() {
        val todayKey = currentDateKey()
        val recordedDate = prefs.getString(KEY_SPLASH_DAILY_DATE, todayKey) ?: todayKey
        val editor = prefs.edit()
        if (recordedDate != todayKey) {
            editor.putString(KEY_SPLASH_DAILY_DATE, todayKey).putInt(KEY_SPLASH_DAILY_COUNT, 1)
        } else {
            val current = prefs.getInt(KEY_SPLASH_DAILY_COUNT, 0)
            editor.putInt(KEY_SPLASH_DAILY_COUNT, current + 1)
        }
        editor.apply()
    }

    private fun currentDateKey() = LocalDate.now().toString()
}
