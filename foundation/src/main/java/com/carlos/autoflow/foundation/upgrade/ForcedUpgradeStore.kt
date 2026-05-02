package com.carlos.autoflow.foundation.upgrade

import android.content.Context
import com.google.gson.Gson

class ForcedUpgradeStore(
    context: Context,
    private val gson: Gson = Gson()
) {
    companion object {
        private const val PREFS_NAME = "forced_upgrade"
        private const val KEY_UPGRADE_INFO_JSON = "upgrade_info_json"
    }

    private val prefs =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun load(currentVersionCode: Int): UpgradeResult.Available? {
        val json = prefs.getString(KEY_UPGRADE_INFO_JSON, null) ?: return null
        val info = try {
            gson.fromJson(json, UpgradeInfo::class.java)
        } catch (_: Throwable) {
            clear()
            return null
        }

        if (!info.forceUpdate || info.versionCode <= currentVersionCode) {
            clear()
            return null
        }
        return UpgradeResult.Available(info, info.downloadUrl)
    }

    fun save(info: UpgradeInfo) {
        if (!info.forceUpdate) {
            clear()
            return
        }
        prefs.edit()
            .putString(KEY_UPGRADE_INFO_JSON, gson.toJson(info))
            .apply()
    }

    fun clear() {
        prefs.edit().remove(KEY_UPGRADE_INFO_JSON).apply()
    }
}
