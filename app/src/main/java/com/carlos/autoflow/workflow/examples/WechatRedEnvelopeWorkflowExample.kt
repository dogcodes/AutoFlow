package com.carlos.autoflow.workflow.examples

object WechatRedEnvelopeWorkflowExample {
    const val WECHAT_RED_ENVELOPE_JSON = """
{
  "id": "workflow-wechat-red-envelope-grabber-v1",
  "name": "微信红包自动抢 (持续监控)",
  "version": "1.0",
  "description": "基于WechatService逻辑，自动在微信中查找、领取和关闭红包。该工作流设计为持续循环监控模式。",
  "nodes": [
    {
      "id": "node-start",
      "type": "START",
      "title": "开始",
      "x": 100,
      "y": 50,
      "config": {}
    },
    {
      "id": "node-loop-main",
      "type": "LOOP",
      "title": "持续监控微信",
      "x": 100,
      "y": 150,
      "config": {
        "loopIntervalMs": 500
      }
    },
    {
      "id": "node-check-activity-lucky-money-receive",
      "type": "CONDITION",
      "title": "当前在红包领取页?",
      "x": 300,
      "y": 150,
      "inputs": [],
      "outputs": [
        {
          "id": "out-cond-true",
          "name": "是",
          "type": "Boolean"
        },
        {
          "id": "out-cond-false",
          "name": "否",
          "type": "Boolean"
        }
      ],
      "config": {
        "checkType": "activity_name_matches",
        "value": "com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyReceiveUI"
      }
    },
    {
      "id": "node-click-open-button",
      "type": "UI_CLICK",
      "title": "点击“开”",
      "x": 500,
      "y": 50,
      "config": {
        "selector": "id=com.tencent.mm:id/e4_ OR text=开",
        "clickStrategy": "DEFAULT"
      }
    },
    {
      "id": "node-delay-after-open",
      "type": "DELAY",
      "title": "领取后延时",
      "x": 700,
      "y": 50,
      "config": {
        "durationMs": 1500
      }
    },
    {
      "id": "node-check-activity-lucky-money-detail",
      "type": "CONDITION",
      "title": "当前在红包详情页?",
      "x": 300,
      "y": 250,
      "inputs": [],
      "outputs": [
        {
          "id": "out-cond-true",
          "name": "是",
          "type": "Boolean"
        },
        {
          "id": "out-cond-false",
          "name": "否",
          "type": "Boolean"
        }
      ],
      "config": {
        "checkType": "activity_name_matches",
        "value": "com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyDetailUI"
      }
    },
    {
      "id": "node-check-red-envelope-status",
      "type": "CONDITION",
      "title": "红包已领完/过期?",
      "x": 500,
      "y": 250,
      "inputs": [],
      "outputs": [
        {
          "id": "out-cond-true",
          "name": "是",
          "type": "Boolean"
        },
        {
          "id": "out-cond-false",
          "name": "否",
          "type": "Boolean"
        }
      ],
      "config": {
        "checkType": "element_exists",
        "selector": "text=手慢了，红包派完了 OR text=该红包已超过24小时 OR text=该红包已被领取完"
      }
    },
    {
      "id": "node-close-lucky-money-detail",
      "type": "UI_CLICK",
      "title": "关闭红包详情",
      "x": 700,
      "y": 250,
      "config": {
        "selector": "id=com.tencent.mm:id/e4o OR text=完成 OR desc=返回",
        "clickStrategy": "DEFAULT"
      }
    },
    {
      "id": "node-delay-after-close",
      "type": "DELAY",
      "title": "关闭后延时",
      "x": 900,
      "y": 250,
      "config": {
        "durationMs": 1000
      }
    },
    {
      "id": "node-global-back-if-no-close-button",
      "type": "SYSTEM_GLOBAL_ACTION",
      "title": "强制返回",
      "x": 700,
      "y": 350,
      "config": {
        "eventType": "GLOBAL_ACTION_BACK"
      }
    },
    {
      "id": "node-check-activity-chat-or-home",
      "type": "CONDITION",
      "title": "当前在聊天/主页?",
      "x": 300,
      "y": 450,
      "inputs": [],
      "outputs": [
        {
          "id": "out-cond-true",
          "name": "是",
          "type": "Boolean"
        },
        {
          "id": "out-cond-false",
          "name": "否",
          "type": "Boolean"
        }
      ],
      "config": {
        "checkType": "activity_name_matches_any",
        "values": [
          "com.tencent.mm.ui.LauncherUI",
          "com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyReceiveUI"
        ]
      }
    },
    {
      "id": "node-find-red-envelope",
      "type": "UI_FIND",
      "title": "查找红包消息",
      "x": 500,
      "y": 450,
      "inputs": [],
      "outputs": [
        {
          "id": "out-element-found",
          "name": "找到元素",
          "type": "Boolean"
        },
        {
          "id": "out-element-not-found",
          "name": "未找到元素",
          "type": "Boolean"
        }
      ],
      "config": {
        "selector": "text=微信红包 OR id=com.tencent.mm:id/e4h",
        "searchStrategy": "LATEST_OR_VISIBLE",
        "timeoutMs": 1000
      }
    },
    {
      "id": "node-check-if-taken-or-expired",
      "type": "CONDITION",
      "title": "红包是否已领/过期?",
      "x": 700,
      "y": 450,
      "inputs": [
        {
          "id": "in-element",
          "name": "元素",
          "type": "AccessibilityNodeInfo"
        }
      ],
      "outputs": [
        {
          "id": "out-cond-true",
          "name": "是",
          "type": "Boolean"
        },
        {
          "id": "out-cond-false",
          "name": "否",
          "type": "Boolean"
        }
      ],
      "config": {
        "checkType": "child_element_exists",
        "parentSelector": "text=微信红包 OR id=com.tencent.mm:id/e4h",
        "childSelector": "text=已领取 OR text=已过期",
        "searchStrategy": "LATEST_OR_VISIBLE"
      }
    },
    {
      "id": "node-click-red-envelope-message",
      "type": "UI_CLICK",
      "title": "点击红包消息",
      "x": 900,
      "y": 450,
      "config": {
        "selector": "text=微信红包 OR id=com.tencent.mm:id/e4h",
        "clickStrategy": "DEFAULT",
        "searchStrategy": "LATEST_OR_VISIBLE"
      }
    },
    {
      "id": "node-delay-after-click-red-envelope",
      "type": "DELAY",
      "title": "点击红包后延时",
      "x": 1100,
      "y": 450,
      "config": {
        "durationMs": 500
      }
    },
    {
      "id": "node-end",
      "type": "END",
      "title": "结束",
      "x": 100,
      "y": 700,
      "config": {}
    },
    {
      "id": "node-delay-no-red-envelope",
      "type": "DELAY",
      "title": "无红包时延时",
      "x": 500,
      "y": 600,
      "config": {
        "durationMs": 2000
      }
    }
  ],
  "connections": [
    {
      "id": "conn-start-to-loop",
      "sourceNodeId": "node-start",
      "sourceOutputId": "output",
      "targetNodeId": "node-loop-main",
      "targetInputId": "input"
    },
    {
      "id": "conn-loop-to-check-receive-ui",
      "sourceNodeId": "node-loop-main",
      "sourceOutputId": "loop_body",
      "targetNodeId": "node-check-activity-lucky-money-receive",
      "targetInputId": "input"
    },
    {
      "id": "conn-receive-ui-true-to-click-open",
      "sourceNodeId": "node-check-activity-lucky-money-receive",
      "sourceOutputId": "out-cond-true",
      "targetNodeId": "node-click-open-button",
      "targetInputId": "input"
    },
    {
      "id": "conn-receive-ui-false-to-check-detail-ui",
      "sourceNodeId": "node-check-activity-lucky-money-receive",
      "sourceOutputId": "out-cond-false",
      "targetNodeId": "node-check-activity-lucky-money-detail",
      "targetInputId": "input"
    },
    {
      "id": "conn-click-open-to-delay-after-open",
      "sourceNodeId": "node-click-open-button",
      "sourceOutputId": "output",
      "targetNodeId": "node-delay-after-open",
      "targetInputId": "input"
    },
    {
      "id": "conn-delay-after-open-to-loop-end",
      "sourceNodeId": "node-delay-after-open",
      "sourceOutputId": "output",
      "targetNodeId": "node-loop-main",
      "targetInputId": "loop_end"
    },
    {
      "id": "conn-detail-ui-true-to-check-status",
      "sourceNodeId": "node-check-activity-lucky-money-detail",
      "sourceOutputId": "out-cond-true",
      "targetNodeId": "node-check-red-envelope-status",
      "targetInputId": "input"
    },
    {
      "id": "conn-detail-ui-false-to-check-chat-or-home",
      "sourceNodeId": "node-check-activity-lucky-money-detail",
      "sourceOutputId": "out-cond-false",
      "targetNodeId": "node-check-activity-chat-or-home",
      "targetInputId": "input"
    },
    {
      "id": "conn-check-status-true-to-close-detail",
      "sourceNodeId": "node-check-red-envelope-status",
      "sourceOutputId": "out-cond-true",
      "targetNodeId": "node-close-lucky-money-detail",
      "targetInputId": "input"
    },
    {
      "id": "conn-check-status-false-to-close-detail",
      "sourceNodeId": "node-check-red-envelope-status",
      "sourceOutputId": "out-cond-false",
      "targetNodeId": "node-close-lucky-money-detail",
      "targetInputId": "input"
    },
    {
      "id": "conn-close-detail-to-delay-after-close",
      "sourceNodeId": "node-close-lucky-money-detail",
      "sourceOutputId": "output",
      "targetNodeId": "node-delay-after-close",
      "targetInputId": "input"
    },
    {
      "id": "conn-delay-after-close-to-loop-end",
      "sourceNodeId": "node-delay-after-close",
      "sourceOutputId": "output",
      "targetNodeId": "node-loop-main",
      "targetInputId": "loop_end"
    },
    {
      "id": "conn-chat-home-true-to-find-red-envelope",
      "sourceNodeId": "node-check-activity-chat-or-home",
      "sourceOutputId": "out-cond-true",
      "targetNodeId": "node-find-red-envelope",
      "targetInputId": "input"
    },
    {
      "id": "conn-chat-home-false-to-delay-no-red-envelope",
      "sourceNodeId": "node-check-activity-chat-or-home",
      "sourceOutputId": "out-cond-false",
      "targetNodeId": "node-delay-no-red-envelope",
      "targetInputId": "input"
    },
    {
      "id": "conn-find-red-envelope-found-to-check-taken-expired",
      "sourceNodeId": "node-find-red-envelope",
      "sourceOutputId": "out-element-found",
      "targetNodeId": "node-check-if-taken-or-expired",
      "targetInputId": "input"
    },
    {
      "id": "conn-find-red-envelope-not-found-to-loop-end",
      "sourceNodeId": "node-find-red-envelope",
      "sourceOutputId": "out-element-not-found",
      "targetNodeId": "node-loop-main",
      "targetInputId": "loop_end"
    },
    {
      "id": "conn-check-taken-expired-false-to-click-red-envelope",
      "sourceNodeId": "node-check-if-taken-or-expired",
      "sourceOutputId": "out-cond-false",
      "targetNodeId": "node-click-red-envelope-message",
      "targetInputId": "input"
    },
    {
      "id": "conn-check-taken-expired-true-to-loop-end",
      "sourceNodeId": "node-check-if-taken-or-expired",
      "sourceOutputId": "out-cond-true",
      "targetNodeId": "node-loop-main",
      "targetInputId": "loop_end"
    },
    {
      "id": "conn-click-red-envelope-to-delay",
      "sourceNodeId": "node-click-red-envelope-message",
      "sourceOutputId": "output",
      "targetNodeId": "node-delay-after-click-red-envelope",
      "targetInputId": "input"
    },
    {
      "id": "conn-delay-after-click-red-envelope-to-loop-end",
      "sourceNodeId": "node-delay-after-click-red-envelope",
      "sourceOutputId": "output",
      "targetNodeId": "node-loop-main",
      "targetInputId": "loop_end"
    },
    {
      "id": "conn-delay-no-red-envelope-to-loop-end",
      "sourceNodeId": "node-delay-no-red-envelope",
      "sourceOutputId": "output",
      "targetNodeId": "node-loop-main",
      "targetInputId": "loop_end"
    }
  ]
}"""
}
