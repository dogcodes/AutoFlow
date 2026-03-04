package com.carlos.autoflow.workflow.examples

import com.carlos.autoflow.workflow.examples.json.EventListenerWorkflowExample
import com.carlos.autoflow.workflow.examples.json.WechatRedEnvelopeWorkflowExample
import com.carlos.autoflow.workflow.examples.json.WechatPreciseRedEnvelopeWorkflowExample
import com.carlos.autoflow.workflow.examples.json.WechatRedEnvelopeV8069WorkflowExample
import com.carlos.autoflow.workflow.examples.json.WechatRedEnvelopeV8069FinalExample
import com.carlos.autoflow.workflow.examples.json.WechatRedEnvelopeV8069AccessibilityNewExample

object JsonWorkflowExamples {
    fun getAllExamples(): List<JsonWorkflowExample> {
        return listOf(
            JsonWorkflowExample(
                name = "事件监听器",
                getJson = { EventListenerWorkflowExample.EVENT_LISTENER_WORKFLOW_JSON }
            ),
            JsonWorkflowExample(
                name = "微信红包自动抢 (轮询版)",
                getJson = { WechatRedEnvelopeWorkflowExample.WECHAT_RED_ENVELOPE_JSON }
            ),
            JsonWorkflowExample(
                name = "微信红包精准识别 (子节点约束版)",
                getJson = { WechatPreciseRedEnvelopeWorkflowExample.WECHAT_PRECISE_RED_ENVELOPE_JSON }
            ),
            JsonWorkflowExample(
                name = "微信红包自动抢 (v8.0.69 终极还原版)",
                getJson = { WechatRedEnvelopeV8069FinalExample.WECHAT_V8069_FINAL_JSON }
            ),
            JsonWorkflowExample(
                name = "微信红包 8.0.69 无障碍还原示例 (新版)",
                getJson = { WechatRedEnvelopeV8069AccessibilityNewExample.WECHAT_V8069_ACCESSIBILITY_NEW_JSON }
            )
        )
    }
}

// 定义一个数据类来表示一个JSON工作流示例
data class JsonWorkflowExample(
    val name: String,
    val getJson: () -> String // 一个函数，用于获取JSON字符串
)
