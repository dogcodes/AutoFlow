package com.carlos.autoflow.platform.ad

data class AdPlatformConfig(
    val platformName: String,
    val manager: AdManager,
    val enabledTypes: Set<AdType>,
    val priority: Int = 0
)
