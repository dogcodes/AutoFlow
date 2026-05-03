package com.carlos.autoflow.workflow.examples.json

object WechatRedEnvelopeV8069AccessibilityNewExample {
    const val WECHAT_V8069_ACCESSIBILITY_NEW_JSON = """
{
  "id": "workflow-wechat-v8069-accessibility-new",
  "name": "微信红包 8.0.69 无障碍",
  "version": "1.0",
  "targetPackage": "com.tencent.mm",
  "description": "包含主页入口监控、对话气泡双重过滤、弹窗开/关决策、金额读取与详情页返回。",
  "createdAt": 1772553600000,
  "updatedAt": 1772553600000,
  "nodes": [
    {
      "id": "node-start",
      "type": "START",
      "title": "开始",
      "x": 80,
      "y": 80,
      "outputs": [
        { "id": "out", "name": "输出", "type": "any" }
      ],
      "config": {}
    },
    {
      "id": "node-event-trigger",
      "type": "EVENT_TRIGGER",
      "title": "微信窗口事件监控",
      "x": 80,
      "y": 220,
      "inputs": [
        { "id": "in", "name": "输入", "type": "any" }
      ],
      "outputs": [
        { "id": "out", "name": "输出", "type": "any" }
      ],
      "config": {
        "conditions": [
          { "type": "PACKAGE_NAME", "value": "com.tencent.mm" },
          {
            "type": "OR",
            "children": [
              { "type": "EVENT_TYPE", "value": "TYPE_WINDOW_STATE_CHANGED" },
              { "type": "EVENT_TYPE", "value": "TYPE_WINDOW_CONTENT_CHANGED" }
            ]
          }
        ],
        "actions": [
          { "type": "LOG", "message": "[红包8.0.69] 捕获到窗口切换事件" }
        ]
      }
    },
    {
      "id": "node-activity-router",
      "type": "CONDITION",
      "title": "Activity 路由分流",
      "x": 320,
      "y": 220,
      "inputs": [
        { "id": "in", "name": "输入", "type": "any" }
      ],
      "outputs": [
        { "id": "launcher", "name": "主页", "type": "any" },
        { "id": "chatting", "name": "聊天页", "type": "any" },
        { "id": "popup", "name": "弹窗页", "type": "any" },
        { "id": "detail", "name": "详情页", "type": "any" }
      ],
      "config": {
        "checkType": "activity_name_matches",
        "cases": [
          { "value": "com.tencent.mm.ui.LauncherUI", "outputId": "launcher" },
          { "value": "com.tencent.mm.ui.chatting.ChattingUI", "outputId": "chatting" },
          { "value": "com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyNewReceiveUI", "outputId": "popup" },
          { "value": "com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyBeforeDetailUI", "outputId": "detail" }
        ]
      }
    },
    {
      "id": "node-list-enter-chat",
      "type": "UI_CLICK",
      "title": "主页命中红包会话并进入",
      "x": 620,
      "y": 80,
      "inputs": [
        { "id": "in", "name": "输入", "type": "any" }
      ],
      "outputs": [
        { "id": "out", "name": "输出", "type": "any" }
      ],
      "config": {
        "selector": "id=com.tencent.mm:id/cj1",
        "childConditions": [
          { "selector": "id=com.tencent.mm:id/ht5" },
          { "selector": "text=[微信红包]" }
        ],
        "clickStrategy": "FIND_CLICKABLE_PARENT",
        "stateTransition": "HAS_RECEIVED",
        "stateTransitionReason": "列表命中红包会话并进入"
      }
    },
    {
      "id": "node-click-chat-envelope",
      "type": "UI_CLICK",
      "title": "聊天页点击未领取红包气泡",
      "x": 620,
      "y": 180,
      "inputs": [
        { "id": "in", "name": "输入", "type": "any" }
      ],
      "outputs": [
        { "id": "out", "name": "输出", "type": "any" }
      ],
      "config": {
        "selector": "id=com.tencent.mm:id/bkg",
        "childConditions": [
          { "selector": "id=com.tencent.mm:id/a3y" },
          { "selector": "id=com.tencent.mm:id/a3m", "exclude": true }
        ],
        "clickStrategy": "DEFAULT",
        "stateGuard": "WAIT_NEW|HAS_RECEIVED",
        "stateTransition": "HAS_CLICKED",
        "stateTransitionReason": "聊天页点击红包气泡"
      }
    },
    {
      "id": "node-chat-packet-check",
      "type": "CONDITION",
      "title": "会话中是否存在红包气泡",
      "x": 420,
      "y": 180,
      "inputs": [
        { "id": "in", "name": "输入", "type": "any" }
      ],
      "outputs": [
        { "id": "has_open", "name": "有红包", "type": "any" },
        { "id": "no_open", "name": "无红包", "type": "any" }
      ],
      "config": {
        "checkType": "element_exists",
        "selector": "id=com.tencent.mm:id/a3y"
      }
    },
    {
      "id": "node-popup-check-open",
      "type": "CONDITION",
      "title": "弹窗是否存在开按钮",
      "x": 620,
      "y": 300,
      "inputs": [
        { "id": "in", "name": "输入", "type": "any" }
      ],
      "outputs": [
        { "id": "has_open", "name": "可拆", "type": "any" },
        { "id": "no_open", "name": "不可拆", "type": "any" }
      ],
      "config": {
        "checkType": "element_exists",
        "selector": "id=com.tencent.mm:id/j6g"
      }
    },
    {
      "id": "node-click-open",
      "type": "UI_CLICK",
      "title": "点击开按钮",
      "x": 880,
      "y": 260,
      "inputs": [
        { "id": "in", "name": "输入", "type": "any" }
      ],
      "outputs": [
        { "id": "out", "name": "输出", "type": "any" }
      ],
      "config": {
        "selector": "id=com.tencent.mm:id/j6g",
        "clickStrategy": "DEFAULT",
        "stateGuard": "HAS_CLICKED",
        "stateTransition": "HAS_OPENED",
        "stateTransitionReason": "点击开按钮"
      }
    },
    {
      "id": "node-click-close",
      "type": "UI_CLICK",
      "title": "点击关闭按钮",
      "x": 880,
      "y": 360,
      "inputs": [
        { "id": "in", "name": "输入", "type": "any" }
      ],
      "outputs": [
        { "id": "out", "name": "输出", "type": "any" }
      ],
      "config": {
        "selector": "id=com.tencent.mm:id/j6f",
        "clickStrategy": "DEFAULT",
        "stateGuard": "HAS_CLICKED",
        "stateTransition": "HAS_RECEIVED",
        "stateTransitionReason": "弹窗关闭后回到会话继续检测红包"
      }
    },
    {
      "id": "node-read-amount",
      "type": "UI_GET_TEXT",
      "title": "读取金额文本",
      "x": 620,
      "y": 420,
      "inputs": [
        { "id": "in", "name": "输入", "type": "any" }
      ],
      "outputs": [
        { "id": "out", "name": "输出", "type": "any" }
      ],
      "config": {
        "selector": "id=com.tencent.mm:id/iyw"
      }
    },
    {
      "id": "node-delay-before-back",
      "type": "DELAY",
      "title": "详情页停留 1500ms",
      "x": 880,
      "y": 470,
      "inputs": [
        { "id": "in", "name": "输入", "type": "any" }
      ],
      "outputs": [
        { "id": "out", "name": "输出", "type": "any" }
      ],
      "config": {
        "delay": 1500
      }
    },
    {
      "id": "node-global-back",
      "type": "SYSTEM_GLOBAL_ACTION",
      "title": "全局返回聊天页",
      "x": 1120,
      "y": 470,
      "inputs": [
        { "id": "in", "name": "输入", "type": "any" }
      ],
      "config": {
        "eventType": "GLOBAL_ACTION_BACK",
        "stateGuard": "HAS_OPENED",
        "stateTransition": "WAIT_NEW",
        "stateTransitionReason": "红包流程返回聊天页"
      }
    }
  ],
  "connections": [
    { "id": "c1", "sourceNodeId": "node-start", "sourceOutputId": "out", "targetNodeId": "node-event-trigger", "targetInputId": "in" },
    { "id": "c2", "sourceNodeId": "node-event-trigger", "sourceOutputId": "out", "targetNodeId": "node-activity-router", "targetInputId": "in" },
    { "id": "c2b", "sourceNodeId": "node-event-trigger", "sourceOutputId": "out", "targetNodeId": "node-chat-packet-check", "targetInputId": "in" },

    { "id": "c3", "sourceNodeId": "node-activity-router", "sourceOutputId": "launcher", "targetNodeId": "node-list-enter-chat", "targetInputId": "in" },
    { "id": "c4", "sourceNodeId": "node-activity-router", "sourceOutputId": "chatting", "targetNodeId": "node-click-chat-envelope", "targetInputId": "in" },
    { "id": "c5", "sourceNodeId": "node-activity-router", "sourceOutputId": "popup", "targetNodeId": "node-popup-check-open", "targetInputId": "in" },
    { "id": "c6", "sourceNodeId": "node-activity-router", "sourceOutputId": "detail", "targetNodeId": "node-read-amount", "targetInputId": "in" },

    { "id": "c4b", "sourceNodeId": "node-chat-packet-check", "sourceOutputId": "has_open", "targetNodeId": "node-click-chat-envelope", "targetInputId": "in" },

    { "id": "c7", "sourceNodeId": "node-popup-check-open", "sourceOutputId": "has_open", "targetNodeId": "node-click-open", "targetInputId": "in" },
    { "id": "c8", "sourceNodeId": "node-popup-check-open", "sourceOutputId": "no_open", "targetNodeId": "node-click-close", "targetInputId": "in" },

    { "id": "c9", "sourceNodeId": "node-read-amount", "sourceOutputId": "out", "targetNodeId": "node-delay-before-back", "targetInputId": "in" },
    { "id": "c10", "sourceNodeId": "node-delay-before-back", "sourceOutputId": "out", "targetNodeId": "node-global-back", "targetInputId": "in" }
  ]
}
"""
}
