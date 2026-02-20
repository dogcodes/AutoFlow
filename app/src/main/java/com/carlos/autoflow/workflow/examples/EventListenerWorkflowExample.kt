package com.carlos.autoflow.workflow.examples

object EventListenerWorkflowExample {
    const val EVENT_LISTENER_WORKFLOW_JSON = """
{
  "name": "事件监听器工作流",
  "description": "一个监听来自目标应用程序的特定无障碍事件并记录它们的日志的工作流。",
  "targetPackage": "com.example.app",
  "nodes": [
    {
      "id": "listenerNode",
      "type": "START",
      "title": "事件监听器",
      "x": 100,
      "y": 100,
      "config": {
        "description": "该节点在特定事件上触发并记录一条消息。",
        "conditions": [
          {
            "type": "OR",
            "children": [
              {
                "type": "EVENT_TYPE",
                "value": "TYPE_NOTIFICATION_STATE_CHANGED"
              },
              {
                "type": "EVENT_TYPE",
                "value": "TYPE_WINDOW_STATE_CHANGED"
              },
              {
                "type": "EVENT_TYPE",
                "value": "TYPE_WINDOW_CONTENT_CHANGED"
              },
              {
                "type": "EVENT_TYPE",
                "value": "TYPE_VIEW_CLICKED"
              }
            ]
          }
        ],
        "actions": [
          {
            "type": "LOG",
            "message": "检测到事件: {eventType} 在 {packageName}"
          }
        ]
      }
    }
  ],
  "connections": []
}
"""
}
