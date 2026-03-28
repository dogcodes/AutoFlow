package com.carlos.autoflow.license

class LicenseDebugTool(
    private val licenseManager: LicenseManager
) {

    data class GenerateResult(
        val deviceId: String,
        val days: Int,
        val validityDays: Int,
        val type: Int,
        val activationKey: String
    )

    data class VerifyResult(
        val deviceId: String,
        val activationKey: String,
        val isValid: Boolean
    )

    fun generateKey(
        deviceId: String,
        days: Int,
        validityDays: Int = 1,
        type: Int = 1,
        seed: String = System.currentTimeMillis().toString()
    ): GenerateResult {
        val normalizedDeviceId = deviceId.trim()
        val normalizedDays = days.coerceIn(1, 365)
        val normalizedValidity = validityDays.coerceIn(1, 30)
        val normalizedType = type.coerceIn(0, 3)
        return GenerateResult(
            deviceId = normalizedDeviceId,
            days = normalizedDays,
            validityDays = normalizedValidity,
            type = normalizedType,
            activationKey = licenseManager.generateLicenseKey(
                days = normalizedDays,
                validityDays = normalizedValidity,
                type = normalizedType,
                seed = seed,
                deviceId = normalizedDeviceId
            )
        )
    }

    fun verifyKey(
        deviceId: String,
        activationKey: String
    ): VerifyResult {
        val normalizedDeviceId = deviceId.trim()
        val normalizedKey = activationKey.trim()
        return VerifyResult(
            deviceId = normalizedDeviceId,
            activationKey = normalizedKey,
            isValid = licenseManager.verifyLicenseKey(
                key = normalizedKey,
                deviceId = normalizedDeviceId
            )
        )
    }
}
