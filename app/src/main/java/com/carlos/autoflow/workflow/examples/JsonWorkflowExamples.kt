package com.carlos.autoflow.workflow.examples

object JsonWorkflowExamples {
    fun getAllExamples(): List<JsonWorkflowExample> {
        return listOf(
            JsonWorkflowExample(
                name = "事件监听器",
                getJson = { EventListenerWorkflowExample.EVENT_LISTENER_WORKFLOW_JSON } // 调用新的独立文件
            ),
            JsonWorkflowExample(
                name = "微信红包自动抢",
                getJson = { WechatRedEnvelopeWorkflowExample.WECHAT_RED_ENVELOPE_JSON }
            )
        )
    }
}


// 定义一个数据类来表示一个JSON工作流示例
data class JsonWorkflowExample(
    val name: String,
    val getJson: () -> String // 一个函数，用于获取JSON字符串
)
