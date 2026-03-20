package com.carlos.autoflow.platform.ad

import android.content.Context

class AdPreferenceStore(context: Context) {
    companion object {
        private const val PREFS_NAME = "ad_preferences" // 存储每种广告位的配置
        private const val KEY_PREFIX_ENABLED = "enabled_"
        private const val KEY_PREFIX_COOLDOWN = "cooldown_"
        private const val DEFAULT_SPLASH_COOLDOWN = 24 * 60 * 60 * 1000L
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

    fun reset() {
        prefs.edit().clear().apply()
    }
}
