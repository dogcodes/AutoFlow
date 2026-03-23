package com.carlos.autoflow.platform.analytics

import android.app.Application
import com.carlos.autoflow.platform.BuildConfig
import com.umeng.analytics.MobclickAgent
import com.umeng.commonsdk.UMConfigure

class UmengAnalyticsTracker : AnalyticsTracker {
    override fun initialize(application: Application) {
        if (BuildConfig.UMENG_APP_KEY.isBlank()) return

        UMConfigure.setLogEnabled(BuildConfig.ENABLE_PLATFORM_LOGGING)
        val effectiveChannel = "auto"
        UMConfigure.preInit(application, BuildConfig.UMENG_APP_KEY, effectiveChannel)
        UMConfigure.init(
            application,
            BuildConfig.UMENG_APP_KEY,
            effectiveChannel,
            UMConfigure.DEVICE_TYPE_PHONE,
            null
        )
        MobclickAgent.setPageCollectionMode(MobclickAgent.PageMode.AUTO)
    }

    override fun trackScreen(screenName: String) {
        // PageMode.AUTO already handles page tracking for regular Activity screens.
    }

    override fun trackEvent(name: String, properties: Map<String, String>) {
        MobclickAgent.onEventObject(null, name, properties)
    }
}
