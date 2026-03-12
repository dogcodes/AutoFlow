package com.carlos.autoflow.license

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.provider.Settings
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.security.MessageDigest
import java.util.Calendar

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

    fun activateLicense(activationCode: String): Boolean {
        if (!validateLicenseKey(activationCode)) {
            return false
        }
        val currentActivatedKeys = getActivatedKeys().toMutableSet()
        if (currentActivatedKeys.contains(activationCode)) {
            return false
        }

        val daysToAdd = parseDaysFromKey(activationCode) ?: return false
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
        return true
    }

    fun grantDays(days: Int, seed: String = System.currentTimeMillis().toString()): Boolean {
        return activateLicense(generateLicenseKey(days, seed, getDeviceId()))
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

    fun generateLicenseKey(days: Int, seed: String, deviceId: String): String {
        val daysStr = days.toString().padStart(3, '0')
        val seedData = seed.take(13).padEnd(13, '0')
        val data = daysStr + seedData
        val checksum = generateChecksum(data, deviceId)
        return data + checksum
    }

    fun verifyLicenseKey(key: String, deviceId: String): Boolean {
        if (key.length != 20 || deviceId.isEmpty()) return false

        val data = key.substring(0, 16)
        val checksum = key.substring(16)
        return generateChecksum(data, deviceId) == checksum
    }

    private fun getActivatedKeys(): Set<String> {
        return prefs.getStringSet(KEY_ACTIVATED_KEYS, emptySet()) ?: emptySet()
    }

    private fun validateLicenseKey(key: String): Boolean {
        if (key.length != 20) return false

        val data = key.substring(0, 16)
        val checksum = key.substring(16)
        val deviceId = getDeviceId()
        if (deviceId.isEmpty()) return false

        return verifyLicenseKey(key, deviceId)
    }

    private fun parseDaysFromKey(key: String): Int? {
        if (key.length < 3) return null
        return key.substring(0, 3).toIntOrNull()
    }

    private fun generateChecksum(data: String, deviceId: String): String {
        val md = MessageDigest.getInstance("MD5")
        val hash = md.digest((data + deviceId).toByteArray())
        return hash.take(2).joinToString("") { "%02x".format(it) }
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
