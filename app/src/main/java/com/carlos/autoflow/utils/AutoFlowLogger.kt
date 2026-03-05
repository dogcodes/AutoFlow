package com.carlos.autoflow.utils

import android.util.Log

object AutoFlowLogger {
    var isDebugEnabled: Boolean = true // Global flag to enable/disable debug logs
    var isNodeExecutionLogEnabled: Boolean = true // Dedicated flag for workflow node execution logs

    private const val TAG_PREFIX = "AutoFlow-"

    fun d(tag: String, message: String) {
        if (isDebugEnabled) {
            Log.d("$TAG_PREFIX$tag", message)
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

    // You can add more log levels (i.e., i, w, v) as needed
}

// Extension function to get a simple tag from any class
inline fun <reified T> T.logTag(): String = T::class.java.simpleName
