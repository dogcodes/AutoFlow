package com.carlos.autoflow

import android.app.Application
import com.carlos.autoflow.platform.PlatformInitializer

class AutoFlowApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        PlatformInitializer.init(this)
    }
}
