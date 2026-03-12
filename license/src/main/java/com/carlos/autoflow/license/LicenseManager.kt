package com.carlos.autoflow.license

import android.content.Context
import android.content.SharedPreferences
import java.security.MessageDigest
import java.util.UUID

/**
 * 许可证管理器 - 基础离线验证
 */
class LicenseManager(
    context: Context,
    private val forcePremium: Boolean = false
) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("license", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_LICENSE_CODE = "license_code"
        private const val KEY_LICENSE_STATUS = "license_status"
        private const val KEY_ACTIVATION_TIME = "activation_time"
        private const val KEY_DEVICE_ID = "device_id"

        const val STATUS_FREE = 0
        const val STATUS_PREMIUM = 1
        const val STATUS_EXPIRED = 2
    }

    fun activateLicense(activationCode: String): Boolean {
        val deviceId = getDeviceId()
        val expectedCode = generateLicenseCode(deviceId)

        return if (activationCode == expectedCode) {
            prefs.edit()
                .putString(KEY_LICENSE_CODE, activationCode)
                .putInt(KEY_LICENSE_STATUS, STATUS_PREMIUM)
                .putLong(KEY_ACTIVATION_TIME, System.currentTimeMillis())
                .apply()
            true
        } else {
            false
        }
    }

    fun getLicenseStatus(): Int {
        if (forcePremium) {
            return STATUS_PREMIUM
        }

        val status = prefs.getInt(KEY_LICENSE_STATUS, STATUS_FREE)
        val activationTime = prefs.getLong(KEY_ACTIVATION_TIME, 0)

        if (status == STATUS_PREMIUM) {
            val oneYear = 365L * 24 * 60 * 60 * 1000
            if (System.currentTimeMillis() - activationTime > oneYear) {
                prefs.edit().putInt(KEY_LICENSE_STATUS, STATUS_EXPIRED).apply()
                return STATUS_EXPIRED
            }
        }

        return status
    }

    fun isPremium(): Boolean = getLicenseStatus() == STATUS_PREMIUM

    fun isFree(): Boolean = getLicenseStatus() == STATUS_FREE

    fun getRemainingDays(): Int {
        if (!isPremium()) return 0

        val activationTime = prefs.getLong(KEY_ACTIVATION_TIME, 0)
        val oneYear = 365L * 24 * 60 * 60 * 1000
        val remaining = oneYear - (System.currentTimeMillis() - activationTime)

        return if (remaining > 0) (remaining / (24 * 60 * 60 * 1000)).toInt() else 0
    }

    fun resetLicense() {
        prefs.edit()
            .remove(KEY_LICENSE_CODE)
            .putInt(KEY_LICENSE_STATUS, STATUS_FREE)
            .remove(KEY_ACTIVATION_TIME)
            .apply()
    }

    fun getDeviceActivationCode(): String {
        return generateLicenseCode(getDeviceId())
    }

    private fun getDeviceId(): String {
        var deviceId = prefs.getString(KEY_DEVICE_ID, null)
        if (deviceId == null) {
            deviceId = UUID.randomUUID().toString()
            prefs.edit().putString(KEY_DEVICE_ID, deviceId).apply()
        }
        return deviceId
    }

    private fun generateLicenseCode(deviceId: String): String {
        val input = "AutoFlow_Premium_$deviceId"
        val md = MessageDigest.getInstance("MD5")
        val digest = md.digest(input.toByteArray())

        val sb = StringBuilder()
        for (i in 0..7) {
            val b = digest[i].toInt() and 0xff
            sb.append(String.format("%02X", b))
        }

        val code = sb.toString()
        return "${code.substring(0, 4)}-${code.substring(4, 8)}"
    }
}
