package com.carlos.autoflow.platform.ad.config

data class RemoteAdConfiguration(
    val version: Int,
    val timestamp: Long,
    val global: RemoteAdGlobalConfiguration = RemoteAdGlobalConfiguration(),
    val rewardedPolicy: RemoteRewardedAdPolicy = RemoteRewardedAdPolicy(),
    val placements: Map<String, RemotePlacementConfiguration> = emptyMap(),
    val platforms: List<RemotePlatformConfiguration> = emptyList()
)

data class RemoteAdGlobalConfiguration(
    val enabled: Boolean = true,
    val requireOaid: Boolean = false,
    val hotStartupCooldownMs: Long? = null
)

data class RemoteRewardedAdPolicy(
    val rewardMinutes: Int = 30,
    val dailyLimit: Int = 5,
    val cooldownSeconds: Int = 60
)

data class RemotePlacementConfiguration(
    val enabled: Boolean = true,
    val slotId: String? = null,
    val cooldownMs: Long? = null,
    val dailyLimit: Int? = null
)

data class RemotePlatformConfiguration(
    val name: String,
    val enabled: Boolean = true,
    val priority: Int = 0,
    val enabledTypes: List<String> = emptyList()
)
