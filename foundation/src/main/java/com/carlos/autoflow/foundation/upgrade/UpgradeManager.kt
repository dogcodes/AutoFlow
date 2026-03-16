package com.carlos.autoflow.foundation.upgrade

import com.carlos.autoflow.foundation.network.FoundationNetworkClient
import com.carlos.autoflow.foundation.network.NetworkResult
import com.google.gson.Gson
import java.lang.Exception

class UpgradeManager(
    private val networkClient: FoundationNetworkClient,
    private val gson: Gson = Gson()
) {
    fun checkForUpdate(
        currentVersionCode: Int,
        infoUrl: String,
        listener: (UpgradeResult) -> Unit
    ) {
        networkClient.get(infoUrl) { result ->
            when (result) {
                is NetworkResult.Success -> parsePayload(result.body, currentVersionCode, listener)
                is NetworkResult.Error -> listener(UpgradeResult.Error(result.message))
            }
        }
    }

    private fun parsePayload(
        payload: String,
        currentVersionCode: Int,
        listener: (UpgradeResult) -> Unit
    ) {
        try {
            val info = gson.fromJson(payload, UpgradeInfo::class.java)
            if (info.versionCode > currentVersionCode) {
                listener(
                    UpgradeResult.Available(
                        info,
                        info.downloadUrl
                    )
                )
            } else {
                listener(UpgradeResult.UpToDate)
            }
        } catch (e: Exception) {
            listener(UpgradeResult.Error(e.message))
        }
    }
}

data class UpgradeInfo(
    val versionCode: Int,
    val downloadUrl: String,
    val releaseNotes: String? = null
)

sealed class UpgradeResult {
    object UpToDate : UpgradeResult()
    data class Available(val info: UpgradeInfo, val downloadUrl: String) : UpgradeResult()
    data class Error(val message: String? = null) : UpgradeResult()
}
