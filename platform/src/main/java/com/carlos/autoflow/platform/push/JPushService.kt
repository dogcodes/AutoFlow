package com.carlos.autoflow.platform.push

import android.app.Application
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import cn.jpush.android.api.JPushInterface
import com.carlos.autoflow.platform.BuildConfig

class JPushService : PushService {
    override fun initialize(application: Application) {
        JPushInterface.setDebugMode(BuildConfig.ENABLE_PLATFORM_LOGGING)
        JPushInterface.init(application)
        if (BuildConfig.ENABLE_PLATFORM_LOGGING) {
            logNotificationState(application)
        }
    }

    override fun setAlias(alias: String) {
        if (alias.isBlank()) return
        JPushInterface.setAliasAndTags(null, alias, null, null)
    }

    private fun logNotificationState(application: Application) {
        Log.d(
            TAG,
            "notificationsEnabled=${NotificationManagerCompat.from(application).areNotificationsEnabled()}"
        )
    }

    private companion object {
        private const val TAG = "JPushService"
    }
}
