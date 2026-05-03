package com.carlos.autoflow.platform.analytics

import android.app.Application
import com.carlos.autoflow.platform.BuildConfig
import com.carlos.autoflow.utils.AutoFlowLogger
import com.umeng.analytics.MobclickAgent
import com.umeng.commonsdk.UMConfigure

class UmengAnalyticsTracker : AnalyticsTracker {
    private var context: Application? = null

    override fun initialize(application: Application) {
        if (BuildConfig.UMENG_APP_KEY.isBlank()) return

        this.context = application
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
        AutoFlowLogger.d("abcc", "conext" + context)
        val ctx = context ?: return
        MobclickAgent.onEventObject(ctx, name, properties)
    }
}
