package com.carlos.autoflow.license

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme

class LicenseDebugActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val initialDeviceId = intent.getStringExtra(EXTRA_DEVICE_ID).orEmpty()
        val licenseDebugTool = LicenseDebugTool()

        setContent {
            MaterialTheme {
                LicenseDebugDialog(
                    licenseDebugTool = licenseDebugTool,
                    initialDeviceId = initialDeviceId,
                    onDismiss = { finish() }
                )
            }
        }
    }

    companion object {
        const val EXTRA_DEVICE_ID = "device_id"
    }
}
