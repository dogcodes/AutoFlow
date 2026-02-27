package com.carlos.autoflow.workflow.examples.json

object WechatRedEnvelopeV8069WorkflowExample {
    const val WECHAT_V8069_JSON = """
{
  "id": "workflow-wechat-v8069-red-packet",
  "name": "微信红包自动抢 (v8.0.69+ 适配版)",
  "version": "1.0",
  "targetPackage": "com.tencent.mm",
  "description": "适配微信 8.0.69+。支持 Activity 精准识别及“已领取”过滤。包含：通知监听、对话页秒点、弹窗自动拆解及详情页自动返回。",
  "nodes": [
    {
      "id": "start-node",
      "type": "START",
      "title": "启动红包助手",
      "x": 50,
      "y": 50,
      "inputs": [],
      "outputs": [
        { "id": "out-1", "name": "输出", "type": "any" }
      ],
      "config": {}
    },
    {
      "id": "monitor-node",
      "type": "EVENT_TRIGGER",
      "title": "全场景监控",
      "x": 50,
      "y": 180,
      "inputs": [
        { "id": "in-1", "name": "输入", "type": "any" }
      ],
      "outputs": [
        { "id": "out-1", "name": "输出", "type": "any" }
      ],
      "config": {
        "conditions": [
          {
            "type": "PACKAGE_NAME",
            "value": "com.tencent.mm"
          }
        ],
        "actions": [
          {
            "type": "LOG",
            "message": "正在监控微信动态..."
          }
        ]
      }
    },
    {
      "id": "click-chat-node",
      "type": "UI_CLICK",
      "title": "从列表进入聊天",
      "x": 300,
      "y": 50,
      "inputs": [
        { "id": "in-1", "name": "输入", "type": "any" }
      ],
      "outputs": [
        { "id": "out-1", "name": "输出", "type": "any" }
      ],
      "config": {
        "selector": "id=com.tencent.mm:id/cj1",
        "childConditions": [
          { "selector": "text=[微信红包]" },
          { "selector": "id=com.tencent.mm:id/ht5" }
        ],
        "clickStrategy": "DEFAULT"
      }
    },
    {
      "id": "click-envelope-node",
      "type": "UI_CLICK",
      "title": "抢未领取的红包",
      "x": 300,
      "y": 180,
      "inputs": [
        { "id": "in-1", "name": "输入", "type": "any" }
      ],
      "outputs": [
        { "id": "out-1", "name": "输出", "type": "any" }
      ],
      "config": {
        "selector": "id=com.tencent.mm:id/bkg",
        "childConditions": [
          { "selector": "id=com.tencent.mm:id/a3y" },
          { "selector": "id=com.tencent.mm:id/a3m", "exclude": true }
        ],
        "clickStrategy": "DEFAULT"
      }
    },
    {
      "id": "wait-receive-ui",
      "type": "UI_WAIT",
      "title": "等待拆红包弹窗",
      "x": 300,
      "y": 310,
      "inputs": [
        { "id": "in-1", "name": "输入", "type": "any" }
      ],
      "outputs": [
        { "id": "out-1", "name": "找到", "type": "boolean" }
      ],
      "config": {
        "selector": "id=com.tencent.mm:id/j6g",
        "timeout": 3000
      }
    },
    {
      "id": "click-open-btn",
      "type": "UI_CLICK",
      "title": "点击“开”字拆解",
      "x": 550,
      "y": 180,
      "inputs": [
        { "id": "in-1", "name": "输入", "type": "any" }
      ],
      "outputs": [
        { "id": "out-1", "name": "成功", "type": "boolean" }
      ],
      "config": {
        "selector": "id=com.tencent.mm:id/j6g",
        "clickStrategy": "FIND_CLICKABLE_CHILD"
      }
    },
    {
      "id": "auto-back-node",
      "type": "SYSTEM_GLOBAL_ACTION",
      "title": "自动返回聊天页",
      "x": 550,
      "y": 310,
      "inputs": [
        { "id": "in-1", "name": "输入", "type": "any" }
      ],
      "outputs": [],
      "config": {
        "eventType": "GLOBAL_ACTION_BACK"
      }
    }
  ],
  "connections": [
    {
      "id": "c1",
      "sourceNodeId": "start-node",
      "sourceOutputId": "output",
      "targetNodeId": "monitor-node",
      "targetInputId": "input"
    },
    {
      "id": "c2",
      "sourceNodeId": "monitor-node",
      "sourceOutputId": "output",
      "targetNodeId": "click-chat-node",
      "targetInputId": "input"
    },
    {
      "id": "c3",
      "sourceNodeId": "monitor-node",
      "sourceOutputId": "output",
      "targetNodeId": "click-envelope-node",
      "targetInputId": "input"
    },
    {
      "id": "c4",
      "sourceNodeId": "click-envelope-node",
      "sourceOutputId": "success",
      "targetNodeId": "wait-receive-ui",
      "targetInputId": "input"
    },
    {
      "id": "c5",
      "sourceNodeId": "wait-receive-ui",
      "sourceOutputId": "found",
      "targetNodeId": "click-open-btn",
      "targetInputId": "input"
    },
    {
      "id": "c6",
      "sourceNodeId": "click-open-btn",
      "sourceOutputId": "success",
      "targetNodeId": "auto-back-node",
      "targetInputId": "input"
    }
  ]
}
"""
}
