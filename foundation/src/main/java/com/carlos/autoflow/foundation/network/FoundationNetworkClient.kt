package com.carlos.autoflow.foundation.network

import com.carlos.autoflow.foundation.BuildConfig
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import java.io.IOException
import java.util.concurrent.TimeUnit

class FoundationNetworkClient(
    private val okHttpClient: OkHttpClient = createDefaultClient()
) {

    companion object {
        private fun createDefaultClient(): OkHttpClient {
            val builder = OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)

            if (BuildConfig.DEBUG) {
                val loggingInterceptor = HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
//                    level = HttpLoggingInterceptor.Level.BASIC
                }
                builder.addInterceptor(loggingInterceptor)
            }

            return builder.build()
        }
    }

    fun get(url: String, headers: Map<String, String> = emptyMap(), callback: (NetworkResult) -> Unit) {
        execute(Request.Builder().url(url), headers, null, callback)
    }

    fun postJson(
        url: String,
        jsonBody: String,
        headers: Map<String, String> = emptyMap(),
        callback: (NetworkResult) -> Unit
    ) {
        val body = jsonBody.toRequestBody("application/json; charset=utf-8".toMediaType())
        execute(Request.Builder().url(url).post(body), headers, body, callback)
    }

    private fun execute(
        builder: Request.Builder,
        headers: Map<String, String>,
        body: RequestBody?,
        callback: (NetworkResult) -> Unit
    ) {
        headers.forEach { (key, value) ->
            builder.addHeader(key, value)
        }
        val request = builder.build()
        okHttpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(NetworkResult.Error(e.message.orEmpty()))
            }

            override fun onResponse(call: Call, response: Response) {
                val bodyString = response.body?.string()
                if (response.isSuccessful && bodyString != null) {
                    callback(NetworkResult.Success(bodyString, response.code))
                } else {
                    callback(NetworkResult.Error("HTTP ${response.code}", response.code, bodyString))
                }
                response.close()
            }
        })
    }
}

sealed class NetworkResult {
    data class Success(val body: String, val code: Int) : NetworkResult()
    data class Error(val message: String, val code: Int? = null, val payload: String? = null) : NetworkResult()
}
