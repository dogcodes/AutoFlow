package com.carlos.autoflow.platform.analytics

import android.app.Application

interface AnalyticsTracker {
    fun initialize(application: Application)
    fun trackScreen(screenName: String)
    fun trackEvent(name: String, properties: Map<String, String> = emptyMap())
}
