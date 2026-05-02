package com.carlos.autoflow.task

import android.content.Context
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlin.math.max

private const val PREFS_NAME = "reward_ad"
private const val KEY_LAST_REWARDED_AT = "last_rewarded_at"
private const val KEY_DAILY_COUNT = "daily_count"
private const val KEY_DAILY_DATE = "daily_date"

data class RewardAdEligibility(
    val canClaim: Boolean,
    val remainingDailyCount: Int,
    val cooldownRemainingSeconds: Int
)

class RewardAdPrefs(context: Context) {
    companion object {
        const val DEFAULT_REWARD_MINUTES = 30
        const val DEFAULT_DAILY_LIMIT = 5
        const val DEFAULT_COOLDOWN_SECONDS = 60
    }

    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getEligibility(
        dailyLimit: Int = DEFAULT_DAILY_LIMIT,
        cooldownSeconds: Int = DEFAULT_COOLDOWN_SECONDS,
        now: Long = System.currentTimeMillis()
    ): RewardAdEligibility {
        val normalizedDailyLimit = dailyLimit.coerceAtLeast(0)
        val normalizedCooldownSeconds = cooldownSeconds.coerceAtLeast(0)
        val cooldownMillis = normalizedCooldownSeconds * 1000L
        val today = toLocalDate(now).toString()
        val recordedDate = prefs.getString(KEY_DAILY_DATE, today) ?: today
        val recordedCount = if (recordedDate == today) {
            prefs.getInt(KEY_DAILY_COUNT, 0)
        } else {
            0
        }
        val remainingDailyCount = max(0, normalizedDailyLimit - recordedCount)
        val lastRewardedAt = prefs.getLong(KEY_LAST_REWARDED_AT, 0L)
        val cooldownRemainingMillis = max(0L, cooldownMillis - (now - lastRewardedAt))
        val cooldownRemainingSeconds = ((cooldownRemainingMillis + 999L) / 1000L).toInt()
        val canClaim = remainingDailyCount > 0 && cooldownRemainingSeconds <= 0
        return RewardAdEligibility(
            canClaim = canClaim,
            remainingDailyCount = remainingDailyCount,
            cooldownRemainingSeconds = cooldownRemainingSeconds
        )
    }

    fun markRewarded(now: Long = System.currentTimeMillis()) {
        val today = toLocalDate(now).toString()
        val recordedDate = prefs.getString(KEY_DAILY_DATE, today) ?: today
        val nextCount = if (recordedDate == today) {
            prefs.getInt(KEY_DAILY_COUNT, 0) + 1
        } else {
            1
        }
        prefs.edit()
            .putString(KEY_DAILY_DATE, today)
            .putInt(KEY_DAILY_COUNT, nextCount)
            .putLong(KEY_LAST_REWARDED_AT, now)
            .apply()
    }

    private fun toLocalDate(timestamp: Long): LocalDate {
        return Instant.ofEpochMilli(timestamp)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
    }
}
