package com.carlos.autoflow.license

import java.security.MessageDigest
import java.time.LocalDate
import java.time.format.DateTimeFormatter

internal class LicenseKeyGenerator {

    companion object {
        private const val DATE_PATTERN = "yyMMdd"
        private const val VALIDITY_DAY_MAX = 30
        private const val TYPE_MIN = 0
        private const val TYPE_MAX = 3
        private val DATE_FORMATTER: DateTimeFormatter =
            DateTimeFormatter.ofPattern(DATE_PATTERN)
    }

    fun generateKey(
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

    fun verifyKey(key: String, deviceId: String): Boolean {
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
        if (validityDays !in 1..VALIDITY_DAY_MAX) {
            return false
        }
        if (typeValue !in TYPE_MIN..TYPE_MAX) {
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
