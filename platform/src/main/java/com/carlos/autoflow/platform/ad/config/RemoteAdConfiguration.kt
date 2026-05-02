package com.carlos.autoflow.platform.ad.config

data class RemoteAdConfiguration(
    val version: Int,
    val timestamp: Long,
    val globalEnabled: Boolean = true,
    val requireOaid: Boolean = false,
    val hotStartupCooldownMs: Long? = null,
    val rewardedPolicy: RemoteRewardedAdPolicy = RemoteRewardedAdPolicy(),
    val slots: Map<String, RemoteSlotConfiguration> = emptyMap(),
    val platforms: List<RemotePlatformConfiguration> = emptyList()
)

data class RemoteRewardedAdPolicy(
    val rewardMinutes: Int = 30,
    val dailyLimit: Int = 5,
    val cooldownSeconds: Int = 60
)

data class RemoteSlotConfiguration(
    val enabled: Boolean = true,
    val slotId: String? = null,
    val cooldownMs: Long? = null,
    val dailyLimit: Int? = null
)

data class RemotePlatformConfiguration(
    val name: String,
    val priority: Int = 0,
    val enabledTypes: List<String> = emptyList()
)
