package com.carlos.autoflow.license

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.provider.Settings
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.security.MessageDigest
import java.time.LocalDate
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

        private const val ONE_DAY_MILLIS = 1000L * 60 * 60 * 24

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

        when (val parseResult = parseActivationCode(activationCode, deviceId)) {
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

                if (!isCurrentlyActive) {
                    prefs.edit()
                        .putLong(KEY_TRIAL_START, getStartOfDayMillis(System.currentTimeMillis()))
                        .putStringSet(KEY_ACTIVATED_KEYS, emptySet())
                        .putInt(KEY_TOTAL_DAYS, 0)
                        .apply()
                    currentActivatedKeys.clear()
                    currentTotalDays = 0
                }

                currentActivatedKeys.add(activationCode)
                val newTotalDays = currentTotalDays + daysToAdd
                prefs.edit()
                    .putStringSet(KEY_ACTIVATED_KEYS, currentActivatedKeys)
                    .putInt(KEY_TOTAL_DAYS, newTotalDays)
                    .apply()
                return ActivationResult.Success
            }
        }

        return ActivationResult.Failure(FailureReason.UNKNOWN)
    }

    fun grantDays(
        days: Int,
        validityDays: Int = 1,
        type: Int = 0,
        seed: String = System.currentTimeMillis().toString()
    ): Boolean {
        return activateLicense(
            generateLicenseKey(
                days = days,
                validityDays = validityDays,
                type = type,
                seed = seed,
                deviceId = getDeviceId()
            )
        ) is ActivationResult.Success
    }

    fun getLicenseStatus(): Int {
        if (forcePremium) {
            return STATUS_PREMIUM
        }

        val totalDays = prefs.getInt(KEY_TOTAL_DAYS, 0)
        return when {
            isPremium() -> STATUS_PREMIUM
            totalDays > 0 -> STATUS_EXPIRED
            else -> STATUS_FREE
        }
    }

    fun isPremium(): Boolean {
        if (forcePremium) {
            return true
        }

        var trialStart = prefs.getLong(KEY_TRIAL_START, 0)
        if (trialStart == 0L) {
            trialStart = getStartOfDayMillis(System.currentTimeMillis())
            prefs.edit().putLong(KEY_TRIAL_START, trialStart).apply()
            return false
        }

        val totalDays = prefs.getInt(KEY_TOTAL_DAYS, 0)
        val daysUsed = calculateDaysUsed(trialStart)
        return daysUsed < totalDays
    }

    fun isFree(): Boolean = getLicenseStatus() == STATUS_FREE

    fun getRemainingDays(): Int {
        if (forcePremium) {
            return Int.MAX_VALUE
        }

        val trialStart = prefs.getLong(KEY_TRIAL_START, 0)
        val totalDays = prefs.getInt(KEY_TOTAL_DAYS, 0)
        if (trialStart == 0L || totalDays == 0) {
            return 0
        }

        val daysUsed = calculateDaysUsed(trialStart)
        return (totalDays - daysUsed).toInt().coerceAtLeast(0)
    }

    fun resetLicense() {
        prefs.edit()
            .remove(KEY_ACTIVATED_KEYS)
            .remove(KEY_TRIAL_START)
            .remove(KEY_TOTAL_DAYS)
            .apply()
    }

    @SuppressLint("HardwareIds")
    fun getDeviceId(): String {
        return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID) ?: ""
    }

    fun generateLicenseKey(
        days: Int,
        validityDays: Int,
        type: Int,
        seed: String,
        deviceId: String
    ): String {
        val normalizedDays = days.coerceIn(1, 999)
        val normalizedValidity = validityDays.coerceIn(1, VALIDITY_DAY_MAX)
        val daysStr = normalizedDays.toString().padStart(3, '0')
        val dateStr = LocalDate.now().format(DATE_FORMATTER)
        val validityStr = normalizedValidity.toString().padStart(2, '0')
        val normalizedType = type.coerceIn(TYPE_MIN, TYPE_MAX)
        val typeStr = normalizedType.toString()
        val cleanSeed = seed.filter { it.isLetterOrDigit() }
        val seedData = cleanSeed.take(4).padEnd(4, '0')
        val data = daysStr + dateStr + validityStr + typeStr + seedData
        val checksum = generateChecksum(data, deviceId)
        return data + checksum
    }

    fun verifyLicenseKey(key: String, deviceId: String): Boolean {
        val parseResult = parseActivationCode(key, deviceId)
        return parseResult is ActivationParseResult.Success
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
            typeValue in TYPE_MIN..TYPE_MAX && !isExpired(parsedDate, validityDays)
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

    private fun isExpired(date: LocalDate, validityDays: Int): Boolean {
        val expiry = date.plusDays((validityDays - 1).toLong())
        return LocalDate.now().isAfter(expiry)
    }

    private fun parseActivationCode(key: String, deviceId: String): ActivationParseResult {
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
        if (isExpired(parsedDate, validityDays)) {
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

    private fun calculateDaysUsed(trialStart: Long): Long {
        val currentDayCount = getDaysSinceEpochAtMidnight(System.currentTimeMillis())
        val startDayCount = getDaysSinceEpochAtMidnight(trialStart)
        return currentDayCount - startDayCount
    }

    private fun getDaysSinceEpochAtMidnight(timestamp: Long): Long {
        return getStartOfDayMillis(timestamp) / ONE_DAY_MILLIS
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
