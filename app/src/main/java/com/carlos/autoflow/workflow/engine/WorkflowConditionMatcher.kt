package com.carlos.autoflow.workflow.engine

import android.view.accessibility.AccessibilityEvent
import com.carlos.autoflow.utils.AutoFlowLogger

object WorkflowConditionMatcher {
    private const val TAG = "WorkflowConditionMatcher"

    /**
     * 检查无障碍事件是否匹配给定的条件列表
     */
    fun isMatch(event: AccessibilityEvent, conditions: List<Map<String, Any>>?): Boolean {
        if (conditions == null || conditions.isEmpty()) return true
        
        // 多个顶层条件通常是 AND 关系
        return conditions.all { checkCondition(event, it) }
    }

    private fun checkCondition(event: AccessibilityEvent, condition: Map<String, Any>): Boolean {
        val type = condition["type"] as? String ?: return true
        
        return when (type) {
            "OR" -> {
                val children = condition["children"] as? List<Map<String, Any>> ?: return true
                children.any { checkCondition(event, it) }
            }
            "AND" -> {
                val children = condition["children"] as? List<Map<String, Any>> ?: return true
                children.all { checkCondition(event, it) }
            }
            "EVENT_TYPE" -> {
                val expectedValue = condition["value"] as? String
                val actualValue = AccessibilityEvent.eventTypeToString(event.eventType)
                AutoFlowLogger.d(TAG, "EVENT_TYPE 匹配: 期望 $expectedValue, 实际 $actualValue")
                expectedValue == actualValue
            }
            "PACKAGE_NAME" -> {
                val expectedValue = condition["value"] as? String
                val actualValue = event.packageName?.toString()
                expectedValue == actualValue
            }
            "CLASS_NAME" -> {
                val expectedValue = condition["value"] as? String
                val actualValue = event.className?.toString()
                AutoFlowLogger.d(TAG, "CLASS_NAME 匹配: 期望 $expectedValue, 实际 $actualValue")
                expectedValue == actualValue
            }
            "TEXT_CONTAINS" -> {
                val expectedValue = condition["value"] as? String ?: return false
                val allText = event.text.joinToString(" ")
                allText.contains(expectedValue, ignoreCase = true)
            }
            else -> {
                AutoFlowLogger.d(TAG, "未知的条件类型: $type")
                true
            }
        }
    }
}
