package com.carlos.autoflow.license

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LicenseKeyGenerationTest {

    @Test
    fun generateAndVerifyLicenseKey() {
        val fakeDeviceId = "test-device-id-123"
        val licenseManager = LicenseManagerForTest()
        val key = licenseManager.generateLicenseKey(days = 30, seed = "seed1234567890", deviceId = fakeDeviceId)

        assertTrue(licenseManager.verifyLicenseKey(key, fakeDeviceId))
        assertFalse(licenseManager.verifyLicenseKey(key, "other-device-id"))
    }

    /**
     * 测试里不需要 Context，只复用纯算法能力。
     */
    private class LicenseManagerForTest : LicenseManagerAlgorithm()
}
