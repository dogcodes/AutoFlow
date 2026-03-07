package com.carlos.autoflow.workflow.examples.json

object WechatRedEnvelopeV8069FinalExample {
    const val WECHAT_V8069_FINAL_JSON = """
{
  "id": "workflow-wechat-v8069-final-logic",
  "name": "微信红包助手 (8.0.69 终极还原版)",
  "version": "1.5",
  "targetPackage": "com.tencent.mm",
  "description": "基于 GrabRedEnvelope 源码 1:1 还原。包含：开始节点、状态机路由、双重气泡校验、弹窗决策分支及详情页回退。",
  "nodes": [
    {
      "id": "node_start",
      "type": "START",
      "title": "开始",
      "x": 50, "y": 50,
      "outputs": [{ "id": "out", "name": "输出", "type": "any" }],
      "config": {}
    },
    {
      "id": "node_trigger",
      "type": "EVENT_TRIGGER",
      "title": "系统监控入口",
      "x": 50, "y": 200,
      "inputs": [{ "id": "in", "name": "输入", "type": "any" }],
      "outputs": [{ "id": "out", "name": "事件", "type": "any" }],
      "config": {
        "conditions": [
          {
            "type": "PACKAGE_NAME",
            "value": "com.tencent.mm"
          }
        ]
      }
    },
    {
      "id": "node_router",
      "type": "CONDITION",
      "title": "页面路由分流",
      "x": 250, "y": 200,
      "inputs": [{ "id": "in", "name": "输入", "type": "any" }],
      "outputs": [
        { "id": "launcher", "name": "主界面", "type": "any" },
        { "id": "chatting", "name": "对话页", "type": "any" },
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
      "id": "node_list_click",
      "type": "UI_CLICK",
      "title": "阶段1: 列表进入",
      "x": 500, "y": 50,
      "inputs": [{ "id": "in", "name": "输入", "type": "any" }],
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
      "id": "node_packet_click",
      "type": "UI_CLICK",
      "title": "阶段2: 气泡点击",
      "x": 500, "y": 150,
      "inputs": [{ "id": "in", "name": "输入", "type": "any" }],
      "config": {
        "selector": "id=com.tencent.mm:id/bkg",
        "childConditions": [
          { "selector": "id=com.tencent.mm:id/a3y" },
          { "selector": "id=com.tencent.mm:id/a3m", "exclude": true }
        ],
        "delay": 500,
        "stateGuard": "WAIT_NEW|HAS_RECEIVED",
        "stateTransition": "HAS_CLICKED",
        "stateTransitionReason": "聊天页点击红包气泡"
      }
    },
    {
      "id": "node_popup_check",
      "type": "CONDITION",
      "title": "阶段3: 弹窗决策",
      "x": 500, "y": 250,
      "inputs": [{ "id": "in", "name": "输入", "type": "any" }],
      "outputs": [
        { "id": "has_open", "name": "有开按钮", "type": "any" },
        { "id": "no_open", "name": "无开按钮", "type": "any" }
      ],
      "config": {
        "checkType": "element_exists",
        "selector": "id=com.tencent.mm:id/j6g"
      }
    },
    {
      "id": "node_open_click",
      "type": "UI_CLICK",
      "title": "点击“开”拆红包",
      "x": 750, "y": 200,
      "inputs": [{ "id": "in", "name": "输入", "type": "any" }],
      "config": {
        "selector": "id=com.tencent.mm:id/j6g",
        "delay": 500,
        "stateGuard": "HAS_CLICKED",
        "stateTransition": "HAS_OPENED",
        "stateTransitionReason": "点击开按钮"
      }
    },
    {
      "id": "node_close_click",
      "type": "UI_CLICK",
      "title": "点击关闭退出",
      "x": 750, "y": 300,
      "inputs": [{ "id": "in", "name": "输入", "type": "any" }],
      "config": {
        "selector": "id=com.tencent.mm:id/j6f",
        "stateGuard": "HAS_CLICKED",
        "stateTransition": "HAS_RECEIVED",
        "stateTransitionReason": "弹窗关闭后回到会话继续检测红包"
      }
    },
    {
      "id": "node_detail_back",
      "type": "SYSTEM_GLOBAL_ACTION",
      "title": "阶段4: 自动返回",
      "x": 500, "y": 400,
      "inputs": [{ "id": "in", "name": "输入", "type": "any" }],
      "config": {
        "eventType": "GLOBAL_ACTION_BACK",
        "delay": 1500,
        "stateGuard": "HAS_OPENED",
        "stateTransition": "WAIT_NEW",
        "stateTransitionReason": "红包流程返回聊天页"
      }
    }
  ],
  "connections": [
    { "id": "c0", "sourceNodeId": "node_start", "sourceOutputId": "out", "targetNodeId": "node_trigger", "targetInputId": "in" },
    { "id": "c1", "sourceNodeId": "node_trigger", "sourceOutputId": "out", "targetNodeId": "node_router", "targetInputId": "in" },
    { "id": "c2", "sourceNodeId": "node_router", "sourceOutputId": "launcher", "targetNodeId": "node_list_click", "targetInputId": "in" },
    { "id": "c3", "sourceNodeId": "node_router", "sourceOutputId": "chatting", "targetNodeId": "node_packet_click", "targetInputId": "in" },
    { "id": "c4", "sourceNodeId": "node_router", "sourceOutputId": "popup", "targetNodeId": "node_popup_check", "targetInputId": "in" },
    { "id": "c5", "sourceNodeId": "node_router", "sourceOutputId": "detail", "targetNodeId": "node_detail_back", "targetInputId": "in" },
    { "id": "c6", "sourceNodeId": "node_popup_check", "sourceOutputId": "has_open", "targetNodeId": "node_open_click", "targetInputId": "in" },
    { "id": "c7", "sourceNodeId": "node_popup_check", "sourceOutputId": "no_open", "targetNodeId": "node_close_click", "targetInputId": "in" }
  ]
}
"""
}
