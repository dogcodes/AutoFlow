package com.carlos.autoflow.workflow.examples

import com.carlos.autoflow.workflow.examples.json.EventListenerWorkflowExample
import com.carlos.autoflow.workflow.examples.json.WechatRedEnvelopeWorkflowExample
import com.carlos.autoflow.workflow.examples.json.WechatRedEnvelopeEventWorkflowExample
import com.carlos.autoflow.workflow.examples.json.WechatPreciseRedEnvelopeWorkflowExample

object JsonWorkflowExamples {
    fun getAllExamples(): List<JsonWorkflowExample> {
        return listOf(
            JsonWorkflowExample(
                name = "事件监听器",
                getJson = { EventListenerWorkflowExample.EVENT_LISTENER_WORKFLOW_JSON } // 调用新的独立文件
            ),
            JsonWorkflowExample(
                name = "微信红包自动抢 (轮询版)",
                getJson = { WechatRedEnvelopeWorkflowExample.WECHAT_RED_ENVELOPE_JSON }
            ),
            JsonWorkflowExample(
                name = "微信红包秒抢 (事件驱动版)",
                getJson = { WechatRedEnvelopeEventWorkflowExample.WECHAT_EVENT_DRIVEN_JSON }
            ),
            JsonWorkflowExample(
                name = "微信红包精准识别 (子节点约束版)",
                getJson = { WechatPreciseRedEnvelopeWorkflowExample.WECHAT_PRECISE_RED_ENVELOPE_JSON }
            )
        )
    }
}


// 定义一个数据类来表示一个JSON工作流示例
data class JsonWorkflowExample(
    val name: String,
    val getJson: () -> String // 一个函数，用于获取JSON字符串
)
