package com.carlos.autoflow.compliance

import com.carlos.autoflow.BuildConfig

object ComplianceConfig {
    val isComplianceMode: Boolean
        get() = BuildConfig.COMPLIANCE_MODE
}
