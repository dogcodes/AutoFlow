package com.carlos.autoflow.platform.task.config

data class DailyCheckInConfig(
    val enabled: Boolean = true,
    val showBanner: Boolean = true,
    val title: String = "每日签到",
    val description: String = "签到即可获得时长奖励",
    val buttonText: String = "立即签到",
    val rewardMinutes: Int = 10,
    val cooldownMinutes: Int = 1440
)
