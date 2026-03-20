package com.carlos.autoflow.foundation.privacy

import android.content.Context

internal class PrivacyConsentStore(context: Context) {
    private val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    fun isConsented(): Boolean = prefs.getBoolean(KEY_CONSENTED, false)

    fun setConsented(consented: Boolean) {
        prefs.edit().putBoolean(KEY_CONSENTED, consented).apply()
    }

    fun reset() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val PREF_NAME = "privacy_consent"
        private const val KEY_CONSENTED = "key_privacy_consented"
    }
}
