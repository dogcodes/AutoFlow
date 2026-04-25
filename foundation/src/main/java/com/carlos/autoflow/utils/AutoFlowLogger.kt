package com.carlos.autoflow.utils

import android.util.Log
import com.carlos.autoflow.foundation.BuildConfig

object AutoFlowLogger {
    var isDebugEnabled: Boolean = BuildConfig.DEBUG // Global flag to enable/disable debug logs
    var isNodeExecutionLogEnabled: Boolean = BuildConfig.DEBUG // Dedicated flag for workflow node execution logs

    private const val TAG_PREFIX = "AutoFlow-"

    fun d(tag: String, message: String) {
        if (isDebugEnabled) {
            Log.d("$TAG_PREFIX$tag", message)
        }
    }

    fun i(tag: String, message: String) {
        if (isDebugEnabled) {
            Log.i("$TAG_PREFIX$tag", message)
        }
    }

    fun w(tag: String, message: String, throwable: Throwable? = null) {
        if (isDebugEnabled) {
            Log.w("$TAG_PREFIX$tag", message, throwable)
        }
    }

    fun e(tag: String, message: String, throwable: Throwable? = null) {
        if (isDebugEnabled) {
            Log.e("$TAG_PREFIX$tag", message, throwable)
        }
    }

    fun node(tag: String, message: String) {
        if (isNodeExecutionLogEnabled) {
            Log.d("$TAG_PREFIX$tag", message)
        }
    }
}

// Extension function to get a simple tag from any class
inline fun <reified T> T.logTag(): String = T::class.java.simpleName
