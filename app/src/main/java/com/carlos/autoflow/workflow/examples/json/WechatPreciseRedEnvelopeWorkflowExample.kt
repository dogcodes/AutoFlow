package com.carlos.autoflow.workflow.examples.json

object WechatPreciseRedEnvelopeWorkflowExample {
    const val WECHAT_PRECISE_RED_ENVELOPE_JSON = """
{
  "id": "workflow-wechat-precise-red-packet-v1",
  "name": "微信红包精准识别 (子节点约束版)",
  "version": "1.0",
  "targetPackage": "com.tencent.mm",
  "description": "监控聊天界面内容变化。仅当找到ID为cj1且其子节点包含特定ID(ht5)和文本([微信红包])的容器时才触发点击。极高命中率，防误触。",
  "nodes": [
    {
      "id": "node-manual-start",
      "type": "START",
      "title": "手动启动入口",
      "x": 100,
      "y": 0,
      "config": {}
    },
    {
      "id": "node-event-trigger",
      "type": "EVENT_TRIGGER",
      "title": "内容变更监控",
      "x": 100,
      "y": 150,
      "config": {
        "conditions": [
          {
            "type": "EVENT_TYPE",
            "value": "TYPE_WINDOW_CONTENT_CHANGED"
          }
        ],
        "actions": [
          {
            "type": "LOG",
            "message": "监听到聊天内容变动"
          }
        ]
      }
    },
    {
      "id": "node-click-target",
      "type": "UI_CLICK",
      "title": "精准点击红包",
      "x": 100,
      "y": 300,
      "config": {
        "selector": "id=com.tencent.mm:id/cj1",
        "childConditions": [
          { "selector": "id=com.tencent.mm:id/ht5" },
          { "selector": "text=[微信红包]" }
        ],
        "clickStrategy": "DEFAULT"
      }
    }
  ],
  "connections": [
    {
      "id": "c0",
      "sourceNodeId": "node-manual-start",
      "sourceOutputId": "output",
      "targetNodeId": "node-event-trigger",
      "targetInputId": "input"
    },
    {
      "id": "c1",
      "sourceNodeId": "node-event-trigger",
      "sourceOutputId": "output",
      "targetNodeId": "node-click-target",
      "targetInputId": "input"
    }
  ]
}
"""
}
