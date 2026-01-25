package com.carlos.autoflow.billing

import android.content.Context
import android.content.SharedPreferences
import java.security.MessageDigest
import java.util.*

/**
 * 许可证管理器 - 基础离线验证
 */
class LicenseManager(private val context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences("license", Context.MODE_PRIVATE)
    
    companion object {
        private const val KEY_LICENSE_CODE = "license_code"
        private const val KEY_LICENSE_STATUS = "license_status"
        private const val KEY_ACTIVATION_TIME = "activation_time"
        private const val KEY_DEVICE_ID = "device_id"
        
        // 许可证状态
        const val STATUS_FREE = 0
        const val STATUS_PREMIUM = 1
        const val STATUS_EXPIRED = 2
    }
    
    /**
     * 验证激活码
     */
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
    
    /**
     * 检查许可证状态
     */
    fun getLicenseStatus(): Int {
        val status = prefs.getInt(KEY_LICENSE_STATUS, STATUS_FREE)
        val activationTime = prefs.getLong(KEY_ACTIVATION_TIME, 0)
        
        // 检查是否过期 (1年有效期)
        if (status == STATUS_PREMIUM) {
            val oneYear = 365L * 24 * 60 * 60 * 1000
            if (System.currentTimeMillis() - activationTime > oneYear) {
                prefs.edit().putInt(KEY_LICENSE_STATUS, STATUS_EXPIRED).apply()
                return STATUS_EXPIRED
            }
        }
        
        return status
    }
    
    /**
     * 是否为专业版
     */
    fun isPremium(): Boolean = getLicenseStatus() == STATUS_PREMIUM
    
    /**
     * 是否为免费版
     */
    fun isFree(): Boolean = getLicenseStatus() == STATUS_FREE
    
    /**
     * 获取剩余天数
     */
    fun getRemainingDays(): Int {
        if (!isPremium()) return 0
        
        val activationTime = prefs.getLong(KEY_ACTIVATION_TIME, 0)
        val oneYear = 365L * 24 * 60 * 60 * 1000
        val remaining = oneYear - (System.currentTimeMillis() - activationTime)
        
        return if (remaining > 0) (remaining / (24 * 60 * 60 * 1000)).toInt() else 0
    }
    
    /**
     * 重置许可证
     */
    fun resetLicense() {
        prefs.edit()
            .remove(KEY_LICENSE_CODE)
            .putInt(KEY_LICENSE_STATUS, STATUS_FREE)
            .remove(KEY_ACTIVATION_TIME)
            .apply()
    }
    
    /**
     * 获取设备ID
     */
    private fun getDeviceId(): String {
        var deviceId = prefs.getString(KEY_DEVICE_ID, null)
        if (deviceId == null) {
            deviceId = UUID.randomUUID().toString()
            prefs.edit().putString(KEY_DEVICE_ID, deviceId).apply()
        }
        return deviceId
    }
    
    /**
     * 生成许可证代码 (简单算法)
     */
    private fun generateLicenseCode(deviceId: String): String {
        val input = "AutoFlow_Premium_$deviceId"
        val md = MessageDigest.getInstance("MD5")
        val digest = md.digest(input.toByteArray())
        
        // 取前8位转换为大写字母数字组合
        val sb = StringBuilder()
        for (i in 0..7) {
            val b = digest[i].toInt() and 0xff
            sb.append(String.format("%02X", b))
        }
        
        // 格式化为 XXXX-XXXX
        val code = sb.toString()
        return "${code.substring(0, 4)}-${code.substring(4, 8)}"
    }
    
    /**
     * 获取当前设备的激活码 (用于生成许可证)
     */
    fun getDeviceActivationCode(): String {
        return generateLicenseCode(getDeviceId())
    }
}
