package com.carlos.autoflow.platform.ad

import android.content.Context

class AdPreferenceStore(context: Context) {
    companion object {
        private const val PREFS_NAME = "ad_preferences" // 存储每种广告位的开关/冷却配置
        private const val KEY_PREFIX_ENABLED = "enabled_" // 单个广告类型开关前缀
        private const val KEY_PREFIX_COOLDOWN = "cooldown_" // 单个广告类型冷却时间前缀
        private const val KEY_PREFIX_DAILY_LIMIT = "daily_limit_"
        private const val KEY_HOT_STARTUP_COOLDOWN = "hot_startup_cooldown"
        private const val DEFAULT_SPLASH_COOLDOWN = 10 * 1000L // 开屏默认冷却 10 秒
        private const val DEFAULT_SPLASH_DAILY_LIMIT = 10
        private const val DEFAULT_HOT_STARTUP_COOLDOWN = 10 * 60 * 1000L
    }

    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /** 某个广告类型当前是否允许展示（默认 true）。 */
    fun isEnabled(type: AdType): Boolean =
        prefs.getBoolean(KEY_PREFIX_ENABLED + type.name, true)

    /** 修改广告类型是否开启，可配合运行时开关或远端配置使用。 */
    fun setEnabled(type: AdType, enabled: Boolean) {
        prefs.edit().putBoolean(KEY_PREFIX_ENABLED + type.name, enabled).apply()
    }

    /** 获取广告类型的冷却时长（毫秒级），必要时可覆盖默认配置。 */
    fun getCooldown(type: AdType): Long =
        prefs.getLong(KEY_PREFIX_COOLDOWN + type.name, if (type == AdType.SPLASH) DEFAULT_SPLASH_COOLDOWN else 0L)

    /** 设置某个广告位的冷却时长，例如插屏 5 分钟、开屏 24 小时。 */
    fun setCooldown(type: AdType, millis: Long) {
        prefs.edit().putLong(KEY_PREFIX_COOLDOWN + type.name, millis).apply()
    }

    fun getDailyLimit(type: AdType): Int =
        prefs.getInt(
            KEY_PREFIX_DAILY_LIMIT + type.name,
            if (type == AdType.SPLASH) DEFAULT_SPLASH_DAILY_LIMIT else Int.MAX_VALUE
        )

    fun setDailyLimit(type: AdType, limit: Int) {
        prefs.edit().putInt(KEY_PREFIX_DAILY_LIMIT + type.name, if (limit < 0) 0 else limit).apply()
    }

    fun getHotStartupCooldown(): Long =
        prefs.getLong(KEY_HOT_STARTUP_COOLDOWN, DEFAULT_HOT_STARTUP_COOLDOWN)

    fun setHotStartupCooldown(millis: Long) {
        prefs.edit().putLong(KEY_HOT_STARTUP_COOLDOWN, millis).apply()
    }

    fun reset() {
        prefs.edit().clear().apply()
    }
}
