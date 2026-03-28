package com.carlos.autoflow.license

import org.junit.Assert.assertTrue
import org.junit.Test

class LicenseKeyGenerationTest {

    @Test
    fun generateAndVerifyLicenseKey() {
        val fakeDeviceId = "843a448704a0ff0a"
        val days = 30
        val seed = "seed1234567890"
        val licenseManager = LicenseManagerForTest()
        val validityDays = 3
        val key = licenseManager.generateLicenseKey(
            days = days,
            validityDays = validityDays,
            seed = seed,
            deviceId = fakeDeviceId
        )
        val isValidForDevice = licenseManager.verifyLicenseKey(key, fakeDeviceId)


        assertTrue(isValidForDevice)
    }

    /**
     * 测试里不需要 Context，只复用纯算法能力。
     */
    private class LicenseManagerForTest : LicenseManagerAlgorithm()
}
