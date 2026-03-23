package com.carlos.autoflow.platform.ad.config

data class RemoteAdConfiguration(
    val version: Int,
    val timestamp: Long,
    val globalEnabled: Boolean = true,
    val requireOaid: Boolean = false,
    val hotStartupCooldownMs: Long? = null,
    val slots: Map<String, RemoteSlotConfiguration> = emptyMap(),
    val platforms: List<RemotePlatformConfiguration> = emptyList()
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
