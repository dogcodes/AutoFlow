package com.carlos.autoflow.foundation.privacy

import android.content.Context

class PrivacyConsentManager(context: Context) {
    private val store = PrivacyConsentStore(context.applicationContext)

    fun hasConsent(): Boolean = store.isConsented()

    fun grantConsent() {
        store.setConsented(true)
    }

    fun revokeConsent() {
        store.setConsented(false)
    }
}
