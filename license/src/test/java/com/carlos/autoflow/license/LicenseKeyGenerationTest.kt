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
        val key = licenseManager.generateLicenseKey(days = days, seed = seed, deviceId = fakeDeviceId)
        val isValidForDevice = licenseManager.verifyLicenseKey(key, fakeDeviceId)

        println("License test deviceId=$fakeDeviceId")
        println("License test days=$days seed=$seed")
        println("License test generatedKey=$key")
        println("License test validForDevice=$isValidForDevice")

        assertTrue(isValidForDevice)
    }

    /**
     * 测试里不需要 Context，只复用纯算法能力。
     */
    private class LicenseManagerForTest : LicenseManagerAlgorithm()
}
