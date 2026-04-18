package com.carlos.autoflow.license

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.SystemClock
import android.provider.Settings
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.carlos.autoflow.utils.TrustedTimeProvider
import java.security.MessageDigest
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar

sealed interface ActivationResult {
    object Success : ActivationResult
    data class Failure(val reason: FailureReason) : ActivationResult
}

enum class FailureReason {
    FORMAT_ERROR,
    EXPIRED,
    TYPE_MISMATCH,
    ALREADY_USED,
    DEVICE_MISMATCH,
    SYSTEM_TIME_INVALID,
    TIME_SYNC_FAILED,
    UNKNOWN
}

class LicenseManager(
    private val context: Context,
    private val forcePremium: Boolean = false
) {

    private val prefs: SharedPreferences = createSecurePrefs(context)

    companion object {
        private const val PREF_NAME = "license"
        private const val KEY_ACTIVATED_KEYS = "activated_keys"
        private const val KEY_TRIAL_START = "trial_start"
        private const val KEY_TOTAL_DAYS = "total_days"
        private const val KEY_TOTAL_MINUTES = "total_minutes"
        private const val KEY_LAST_USAGE_WALL_TIME = "last_usage_wall_time"
        private const val KEY_LAST_USAGE_ELAPSED_TIME = "last_usage_elapsed_time"

        private const val ONE_DAY_MILLIS = 1000L * 60 * 60 * 24
        private const val ONE_MINUTE_MILLIS = 60_000L
        private const val MINUTES_PER_DAY = 24 * 60

        private const val VALIDITY_DAY_MAX = 30
        private const val TYPE_MIN = 0
        private const val TYPE_MAX = 3
        private val ALLOWED_TYPES_FOR_APP = setOf(0, 1)
        private val DATE_FORMATTER: DateTimeFormatter =
            DateTimeFormatter.ofPattern("yyMMdd")

        const val STATUS_FREE = 0
        const val STATUS_PREMIUM = 1
        const val STATUS_EXPIRED = 2

        private fun createSecurePrefs(context: Context): SharedPreferences {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            return EncryptedSharedPreferences.create(
                context,
                PREF_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        }
    }

    fun activateLicense(activationCode: String): ActivationResult {
        val deviceId = getDeviceId()
        if (deviceId.isEmpty()) {
            return ActivationResult.Failure(FailureReason.DEVICE_MISMATCH)
        }

        // 优先用网络可信时间校验激活码，防止用户回调系统时间激活过期码
        val trustedNow = TrustedTimeProvider.getTrustedTimeForActivation(prefs)
            ?: return ActivationResult.Failure(FailureReason.TIME_SYNC_FAILED)
        if (TrustedTimeProvider.isSystemTimeInvalid(trustedNow)) {
            return ActivationResult.Failure(FailureReason.SYSTEM_TIME_INVALID)
        }

        when (val parseResult = parseActivationCode(activationCode, deviceId, trustedNow)) {
            is ActivationParseResult.Failure -> return ActivationResult.Failure(parseResult.reason)
            is ActivationParseResult.Success -> {
                val parsed = parseResult.parsed
                if (parsed.type !in ALLOWED_TYPES_FOR_APP) {
                    return ActivationResult.Failure(FailureReason.TYPE_MISMATCH)
                }

                val currentActivatedKeys = getActivatedKeys().toMutableSet()
                if (currentActivatedKeys.contains(activationCode)) {
                    return ActivationResult.Failure(FailureReason.ALREADY_USED)
                }

                val daysToAdd = parsed.days
                val isCurrentlyActive = isPremium()
                var currentTotalDays = prefs.getInt(KEY_TOTAL_DAYS, 0)
                var currentTotalMinutes = prefs.getInt(KEY_TOTAL_MINUTES, 0)

                if (!isCurrentlyActive) {
                    prefs.edit()
                        .putLong(KEY_TRIAL_START, getStartOfDayMillis(System.currentTimeMillis()))
                        .putStringSet(KEY_ACTIVATED_KEYS, emptySet())
                        .putInt(KEY_TOTAL_DAYS, 0)
                        .putInt(KEY_TOTAL_MINUTES, 0)
                        .apply()
                    currentActivatedKeys.clear()
                    currentTotalDays = 0
                    currentTotalMinutes = 0
                }

                currentActivatedKeys.add(activationCode)
                val newTotalDays = currentTotalDays + daysToAdd
                val newTotalMinutes = currentTotalMinutes + daysToAdd * MINUTES_PER_DAY
                prefs.edit()
                    .putStringSet(KEY_ACTIVATED_KEYS, currentActivatedKeys)
                    .putInt(KEY_TOTAL_DAYS, newTotalDays)
                    .putInt(KEY_TOTAL_MINUTES, newTotalMinutes)
                    .apply()
                recordUsageSnapshot(trustedNow)
                return ActivationResult.Success
            }
        }

        return ActivationResult.Failure(FailureReason.UNKNOWN)
    }

    fun grantDays(days: Int): Boolean {
        val minutes = days * MINUTES_PER_DAY
        return extendMinutes(minutes)
    }

    fun extendMinutes(minutes: Int): Boolean {
        if (minutes <= 0) return false
        val now = System.currentTimeMillis()
        var trialStart = ensureTrialStart()
        val currentTotalMinutes = prefs.getInt(KEY_TOTAL_MINUTES, 0)
        val minutesUsed = getMinutesSinceTrialStart(trialStart)
        val shouldReset = minutesUsed >= currentTotalMinutes || currentTotalMinutes == 0
        val finalTrialStart = if (shouldReset) now else trialStart
        val finalTotalMinutes = if (shouldReset) minutes else currentTotalMinutes + minutes
        val finalTotalDays = (finalTotalMinutes + MINUTES_PER_DAY - 1) / MINUTES_PER_DAY
        prefs.edit()
            .putInt(KEY_TOTAL_MINUTES, finalTotalMinutes)
            .putInt(KEY_TOTAL_DAYS, finalTotalDays)
            .putLong(KEY_TRIAL_START, finalTrialStart)
            .apply()
        return true
    }

    fun getLicenseStatus(): Int {
        if (forcePremium) {
            return STATUS_PREMIUM
        }

        val totalMinutes = prefs.getInt(KEY_TOTAL_MINUTES, 0)
        return when {
            isPremium() -> STATUS_PREMIUM
            totalMinutes > 0 -> STATUS_EXPIRED
            else -> STATUS_FREE
        }
    }

    fun isPremium(): Boolean {
        if (forcePremium) return true

        var trialStart = prefs.getLong(KEY_TRIAL_START, 0)
        if (trialStart == 0L) {
            trialStart = getStartOfDayMillis(System.currentTimeMillis())
            prefs.edit().putLong(KEY_TRIAL_START, trialStart).apply()
            return false
        }

        val totalMinutes = prefs.getInt(KEY_TOTAL_MINUTES, 0)
        if (totalMinutes == 0) return false

        // 时间回拨检测：系统时间比上次使用时间早，视为异常
        if (isUsageTimeRolledBack()) return false

        // 可信时间快照检测：有网络时间且系统时间偏差过大，视为异常
        val trustedSnapshot = TrustedTimeProvider.getTrustedTimeSnapshot(prefs)
        if (trustedSnapshot != null && TrustedTimeProvider.isSystemTimeInvalid(trustedSnapshot)) return false

        val minutesUsed = getMinutesSinceTrialStart(trialStart)
        val active = minutesUsed < totalMinutes
        if (active) recordUsageSnapshot(trustedSnapshot)
        return active
    }

    fun isFree(): Boolean = getLicenseStatus() == STATUS_FREE

    /** 暴露 prefs 供外部（如 UI 层预取网络时间）使用 */
    fun getPrefs(): SharedPreferences = prefs

    /** 系统时间是否异常（被篡改）。可用于 UI 层显示提示 */
    fun isSystemTimeAbnormal(): Boolean {
        val trustedSnapshot = TrustedTimeProvider.getTrustedTimeSnapshot(prefs)
        if (trustedSnapshot != null && TrustedTimeProvider.isSystemTimeInvalid(trustedSnapshot)) return true
        return isUsageTimeRolledBack()
    }

    fun getRemainingDays(): Int {
        if (forcePremium) {
            return Int.MAX_VALUE
        }

        val trialStart = prefs.getLong(KEY_TRIAL_START, 0)
        val expiry = getExpiryTimestamp()
        if (expiry == null) return 0
        val remainingMillis = expiry - System.currentTimeMillis()
        if (remainingMillis <= 0) return 0
        return ((remainingMillis + ONE_DAY_MILLIS - 1) / ONE_DAY_MILLIS).toInt()
    }

    fun getExpiryTimestamp(): Long? {
        val trialStart = prefs.getLong(KEY_TRIAL_START, 0)
        val totalMinutes = prefs.getInt(KEY_TOTAL_MINUTES, 0)
        if (trialStart == 0L || totalMinutes == 0) return null
        return trialStart + totalMinutes * ONE_MINUTE_MILLIS
    }

    fun resetLicense() {
        prefs.edit()
            .remove(KEY_ACTIVATED_KEYS)
            .remove(KEY_TRIAL_START)
            .remove(KEY_TOTAL_DAYS)
            .remove(KEY_TOTAL_MINUTES)
            .apply()
    }

    private fun recordUsageSnapshot(trustedNow: Long? = null) {
        prefs.edit()
            .putLong(KEY_LAST_USAGE_WALL_TIME, trustedNow ?: System.currentTimeMillis())
            .putLong(KEY_LAST_USAGE_ELAPSED_TIME, SystemClock.elapsedRealtime())
            .apply()
    }

    private fun isUsageTimeRolledBack(): Boolean {
        val lastWall = prefs.getLong(KEY_LAST_USAGE_WALL_TIME, 0L)
        if (lastWall == 0L) return false
        val lastElapsed = prefs.getLong(KEY_LAST_USAGE_ELAPSED_TIME, 0L)
        val currentElapsed = SystemClock.elapsedRealtime()
        // 未重启：用 elapsedRealtime 判断（不受系统时间影响）
        if (currentElapsed >= lastElapsed) {
            val expectedWall = lastWall + (currentElapsed - lastElapsed)
            return TrustedTimeProvider.isTimeRolledBack(expectedWall, System.currentTimeMillis())
        }
        // 已重启：elapsedRealtime 重置，改用墙钟弱校验
        return TrustedTimeProvider.isTimeRolledBack(lastWall, System.currentTimeMillis())
    }

    @SuppressLint("HardwareIds")
    fun getDeviceId(): String {
        return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID) ?: ""
    }

    private fun getActivatedKeys(): Set<String> {
        return prefs.getStringSet(KEY_ACTIVATED_KEYS, emptySet()) ?: emptySet()
    }

    private fun validateLicenseKey(key: String): Boolean {
        if (key.length != 24) return false

        val data = key.substring(0, 16)
        val checksum = key.substring(16)
        val deviceId = getDeviceId()
        if (deviceId.isEmpty()) return false

        if (generateChecksum(data, deviceId) != checksum) return false
        val datePart = data.substring(3, 9)
        val validityDays = data.substring(9, 11).toIntOrNull() ?: return false
        val typeValue = data.substring(11, 12).toIntOrNull() ?: return false

        val parsedDate = LocalDate.parse(datePart, DATE_FORMATTER)
        return validateDateSegment(datePart) && validityDays in 1..VALIDITY_DAY_MAX &&
            typeValue in TYPE_MIN..TYPE_MAX && !isExpired(parsedDate, validityDays, null)
    }

    private fun generateChecksum(data: String, deviceId: String): String {
        val md = MessageDigest.getInstance("MD5")
        val hash = md.digest((data + deviceId).toByteArray())
        return hash.take(4).joinToString("") { "%02x".format(it) }
    }

    private fun validateDateSegment(segment: String): Boolean {
        return try {
            LocalDate.parse(segment, DATE_FORMATTER)
            true
        } catch (_: Exception) {
            false
        }
    }

    private fun isExpired(date: LocalDate, validityDays: Int, trustedNow: Long?): Boolean {
        val expiry = date.plusDays((validityDays - 1).toLong())
        val checkDate = if (trustedNow != null) {
            Instant.ofEpochMilli(trustedNow).atZone(ZoneId.systemDefault()).toLocalDate()
        } else {
            LocalDate.now()
        }
        return checkDate.isAfter(expiry)
    }

    private fun parseActivationCode(key: String, deviceId: String, trustedNow: Long? = null): ActivationParseResult {
        if (key.length != 24 || deviceId.isEmpty()) {
            return ActivationParseResult.Failure(FailureReason.FORMAT_ERROR)
        }

        val data = key.substring(0, 16)
        val checksum = key.substring(16)
        val datePart = data.substring(3, 9)
        val validityDays = data.substring(9, 11).toIntOrNull() ?: return ActivationParseResult.Failure(FailureReason.FORMAT_ERROR)
        val typeValue = data.substring(11, 12).toIntOrNull() ?: return ActivationParseResult.Failure(FailureReason.FORMAT_ERROR)
        val days = data.substring(0, 3).toIntOrNull() ?: return ActivationParseResult.Failure(FailureReason.FORMAT_ERROR)

        if (!validateDateSegment(datePart)) {
            return ActivationParseResult.Failure(FailureReason.FORMAT_ERROR)
        }

        if (validityDays !in 1..VALIDITY_DAY_MAX) {
            return ActivationParseResult.Failure(FailureReason.FORMAT_ERROR)
        }

        if (typeValue !in TYPE_MIN..TYPE_MAX) {
            return ActivationParseResult.Failure(FailureReason.TYPE_MISMATCH)
        }

        val parsedDate = LocalDate.parse(datePart, DATE_FORMATTER)
        if (isExpired(parsedDate, validityDays, trustedNow)) {
            return ActivationParseResult.Failure(FailureReason.EXPIRED)
        }

        if (generateChecksum(data, deviceId) != checksum) {
            return ActivationParseResult.Failure(FailureReason.DEVICE_MISMATCH)
        }

        return ActivationParseResult.Success(
            ParsedLicense(
                days = days,
                date = parsedDate,
                validityDays = validityDays,
                type = typeValue,
                data = data,
                checksum = checksum
            )
        )
    }

    private data class ParsedLicense(
        val days: Int,
        val date: LocalDate,
        val validityDays: Int,
        val type: Int,
        val data: String,
        val checksum: String
    )

    private sealed interface ActivationParseResult {
        data class Success(val parsed: ParsedLicense) : ActivationParseResult
        data class Failure(val reason: FailureReason) : ActivationParseResult
    }

    private fun ensureTrialStart(): Long {
        var trialStart = prefs.getLong(KEY_TRIAL_START, 0)
        if (trialStart == 0L) {
            trialStart = getStartOfDayMillis(System.currentTimeMillis())
        }
        return trialStart
    }

    private fun getMinutesSinceTrialStart(trialStart: Long): Int {
        val durationMillis = System.currentTimeMillis() - trialStart
        return (durationMillis / ONE_MINUTE_MILLIS).toInt().coerceAtLeast(0)
    }

    private fun getStartOfDayMillis(timestamp: Long): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }
}
