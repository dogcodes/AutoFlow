package com.carlos.autoflow.platform.push

import android.app.Application

interface PushService {
    fun initialize(application: Application)
    fun setAlias(alias: String)
}
