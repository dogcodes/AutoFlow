package com.carlos.autoflow.license

import java.security.MessageDigest
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.ResolverStyle

/**
 * 仅供测试复用的纯算法类，避免单元测试依赖 Android Context。
 */
private const val DATE_PATTERN = "yyMMdd"
private val DATE_FORMATTER: DateTimeFormatter =
    DateTimeFormatter.ofPattern(DATE_PATTERN)

open class LicenseManagerAlgorithm {
    fun generateLicenseKey(
        days: Int,
        validityDays: Int,
        type: Int,
        seed: String,
        deviceId: String
    ): String {
        val daysStr = days.toString().padStart(3, '0')
        val dateStr = LocalDate.now().format(DATE_FORMATTER)
        val validityStr = validityDays.toString().padStart(2, '0')
        val cleanSeed = seed.filter { it.isLetterOrDigit() }
        val seedData = cleanSeed.take(4).padEnd(4, '0')
        val data = daysStr + dateStr + validityStr + type.toString() + seedData
        val checksum = generateChecksum(data, deviceId)
        return data + checksum
    }

    fun verifyLicenseKey(key: String, deviceId: String): Boolean {
        val normalizedKey = key.trim().lowercase()
        val normalizedDeviceId = deviceId.trim()
        if (normalizedKey.length != 24 || normalizedDeviceId.isEmpty()) return false
        val data = normalizedKey.substring(0, 16)
        val checksum = normalizedKey.substring(16)
        if (generateChecksum(data, normalizedDeviceId) != checksum) {
            return false
        }
        val datePart = data.substring(3, 9)
        val validityDays = data.substring(9, 11).toIntOrNull() ?: return false
        val typeValue = data.substring(11, 12).toIntOrNull() ?: return false
        if (!validateDateSegment(datePart)) {
            return false
        }
        if (validityDays !in 1..30) {
            return false
        }
        if (typeValue !in 0..3) {
            return false
        }
        if (isExpired(datePart, validityDays)) {
            return false
        }
        return true
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

    private fun isExpired(segment: String, validityDays: Int): Boolean {
        val startDate = LocalDate.parse(segment, DATE_FORMATTER)
        val expiry = startDate.plusDays((validityDays - 1).toLong())
        return LocalDate.now().isAfter(expiry)
    }
}
