package com.carlos.autoflow

import android.app.Application
import com.carlos.autoflow.platform.PlatformInitializer
import com.carlos.autoflow.utils.TrustedTimeProvider

class AutoFlowApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        PlatformInitializer.init(this)
        TrustedTimeProvider.configure(listOf(
            "https://autoflow.xbdcc.cn/time",   // 自有 backend，优先
            "https://www.baidu.com",             // fallback
        ))
    }
}
