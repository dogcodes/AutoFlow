package com.carlos.autoflow.workflow.engine

import android.view.accessibility.AccessibilityEvent
import com.carlos.autoflow.accessibility.AutoFlowAccessibilityService
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.flow.collect
import com.carlos.autoflow.utils.AutoFlowLogger

/**
 * 专门负责无障碍事件监听与过滤的管理器
 */
class AccessibilityEventManager(
    private val service: AutoFlowAccessibilityService,
    private val isRunning: () -> Boolean
) {
    private val TAG = "AccessibilityEventManager"

    /**
     * 监听通知变化
     */
    suspend fun monitorNotification(onMatched: suspend (AccessibilityEvent) -> Unit) {
        service.accessibilityEvents
            .takeWhile { isRunning() }
            .filter { it.eventType == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED }
            .collect { event ->
                AutoFlowLogger.d(TAG, "收到通知事件: $event")
                onMatched(event)
            }
    }

    /**
     * 监听窗口变化
     */
    suspend fun monitorWindow(onMatched: suspend (AccessibilityEvent) -> Unit) {
        service.accessibilityEvents
            .takeWhile { isRunning() }
            .filter { it.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED }
            .collect { event ->
                AutoFlowLogger.d(TAG, "收到窗口变化事件: $event")
                onMatched(event)
            }
    }

    /**
     * 监听内容变化
     */
    suspend fun monitorContent(onMatched: suspend (AccessibilityEvent) -> Unit) {
        service.accessibilityEvents
            .takeWhile { isRunning() }
            .filter { it.eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED }
            .collect { event ->
                AutoFlowLogger.d(TAG, "收到内容变化事件: $event")
                onMatched(event)
            }
    }
}
