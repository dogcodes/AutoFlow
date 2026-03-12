package com.carlos.autoflow.license

class LicenseDebugTool(
    private val licenseManager: LicenseManager
) {

    data class GenerateResult(
        val deviceId: String,
        val days: Int,
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
        seed: String = System.currentTimeMillis().toString()
    ): GenerateResult {
        val normalizedDeviceId = deviceId.trim()
        val normalizedDays = days.coerceIn(1, 365)
        return GenerateResult(
            deviceId = normalizedDeviceId,
            days = normalizedDays,
            activationKey = licenseManager.generateLicenseKey(
                days = normalizedDays,
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
