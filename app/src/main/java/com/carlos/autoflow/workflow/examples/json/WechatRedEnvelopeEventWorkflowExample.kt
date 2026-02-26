package com.carlos.autoflow.workflow.examples.json

object WechatRedEnvelopeEventWorkflowExample {
    const val WECHAT_EVENT_DRIVEN_JSON = """
{
  "id": "workflow-wechat-red-packet-event-driven-v2",
  "name": "微信红包秒抢 (事件驱动版)",
  "version": "2.0",
  "targetPackage": "com.tencent.mm",
  "description": "高性能事件响应版。支持监听通知直达、弹窗秒点、详情秒退。无轮询，极致响应性能。",
  "nodes": [
    {
      "id": "node-start",
      "type": "START",
      "title": "事件监控中心",
      "x": 100,
      "y": 100,
      "config": {
        "conditions": [
          {
            "type": "OR",
            "children": [
              { "type": "EVENT_TYPE", "value": "TYPE_NOTIFICATION_STATE_CHANGED" },
              { "type": "EVENT_TYPE", "value": "TYPE_WINDOW_STATE_CHANGED" },
              { "type": "EVENT_TYPE", "value": "TYPE_WINDOW_CONTENT_CHANGED" }
            ]
          }
        ],
        "actions": [
          {
            "type": "LOG",
            "message": "捕捉到微信动态"
          }
        ]
      }
    },
    {
      "id": "node-dispatch-center",
      "type": "CONDITION",
      "title": "场景识别路由",
      "x": 350,
      "y": 100,
      "config": {
        "logic": "IF_ELSE_FLOW"
      }
    },
    {
      "id": "node-click-open",
      "type": "UI_CLICK",
      "title": "秒点“开”",
      "x": 600,
      "y": 50,
      "config": {
        "selector": "id=com.tencent.mm:id/giq OR text=开",
        "clickStrategy": "FIND_CLICKABLE_CHILD"
      }
    },
    {
      "id": "node-click-packet",
      "type": "UI_CLICK",
      "title": "点击红包消息",
      "x": 600,
      "y": 150,
      "config": {
        "selector": "text=微信红包 OR id=com.tencent.mm:id/e4h",
        "clickStrategy": "DEFAULT",
        "searchStrategy": "LATEST_OR_VISIBLE"
      }
    },
    {
      "id": "node-back-from-detail",
      "type": "SYSTEM_GLOBAL_ACTION",
      "title": "自动退出详情页",
      "x": 600,
      "y": 250,
      "config": {
        "eventType": "GLOBAL_ACTION_BACK"
      }
    },
    {
      "id": "node-notification-handler",
      "type": "UI_CLICK",
      "title": "点击通知进入",
      "x": 600,
      "y": 350,
      "config": {
        "selector": "text_contains=[微信红包]",
        "clickStrategy": "DEFAULT"
      }
    }
  ],
  "connections": [
    {
      "id": "c1",
      "sourceNodeId": "node-start",
      "sourceOutputId": "output",
      "targetNodeId": "node-dispatch-center",
      "targetInputId": "input"
    },
    {
      "id": "c2",
      "sourceNodeId": "node-dispatch-center",
      "sourceOutputId": "true",
      "targetNodeId": "node-click-open",
      "targetInputId": "input"
    },
    {
      "id": "c3",
      "sourceNodeId": "node-dispatch-center",
      "sourceOutputId": "false",
      "targetNodeId": "node-click-packet",
      "targetInputId": "input"
    }
  ]
}
"""
}
