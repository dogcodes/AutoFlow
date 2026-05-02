package com.carlos.autoflow.task

import android.content.Context
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

private const val PREFS_NAME = "daily_checkin"
private const val KEY_LAST_CHECKIN_AT = "last_checkin_at"

class CheckInPrefs(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var lastCheckInAt: Long
        get() = prefs.getLong(KEY_LAST_CHECKIN_AT, 0L)
        private set(value) {
            prefs.edit().putLong(KEY_LAST_CHECKIN_AT, value).apply()
        }

    fun markCheckedIn(timestamp: Long = System.currentTimeMillis()) {
        lastCheckInAt = timestamp
    }

    fun hasCheckedInToday(now: Long = System.currentTimeMillis()): Boolean {
        val last = lastCheckInAt
        if (last <= 0L) return false
        return toLocalDate(last) == toLocalDate(now)
    }

    fun clear() {
        prefs.edit().remove(KEY_LAST_CHECKIN_AT).apply()
    }

    private fun toLocalDate(timestamp: Long): LocalDate {
        return Instant.ofEpochMilli(timestamp)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
    }
}
