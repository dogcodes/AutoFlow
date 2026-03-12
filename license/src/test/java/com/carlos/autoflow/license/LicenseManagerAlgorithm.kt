package com.carlos.autoflow.license

import java.security.MessageDigest

/**
 * 仅供测试复用的纯算法类，避免单元测试依赖 Android Context。
 */
open class LicenseManagerAlgorithm {
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

    private fun generateChecksum(data: String, deviceId: String): String {
        val md = MessageDigest.getInstance("MD5")
        val hash = md.digest((data + deviceId).toByteArray())
        return hash.take(2).joinToString("") { "%02x".format(it) }
    }
}
