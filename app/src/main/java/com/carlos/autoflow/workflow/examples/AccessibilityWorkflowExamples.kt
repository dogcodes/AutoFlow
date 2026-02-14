package com.carlos.autoflow.workflow.examples

import com.carlos.autoflow.utils.AutoFlowLogger
import com.carlos.autoflow.workflow.models.*

object AccessibilityWorkflowExamples {

    private val TAG = AccessibilityWorkflowExamples::class.java.simpleName

    // 示例3: 应用自动签到
    fun createAppAutoCheckInWorkflow(): Workflow {
        AutoFlowLogger.d(TAG, "正在创建应用自动签到工作流")
        val nodes = listOf(
            WorkflowNode(
                id = "start_3",
                type = NodeType.START,
                title = "开始",
                x = 100f,
                y = 100f
            ),
            WorkflowNode(
                id = "launch_demo_app_3",
                type = NodeType.LAUNCH_ACTIVITY,
                title = "启动示例应用",
                x = 100f,
                y = 200f,
                config = mutableMapOf(
                    "packageName" to "com.carlos.autoflow",
                    "className" to "com.carlos.autoflow.demo.DemoAppActivity"
                )
            ),
            WorkflowNode(
                id = "wait_demo_app_3",
                type = NodeType.UI_WAIT,
                title = "等待示例应用加载",
                x = 100f,
                y = 300f,
                config = mutableMapOf(
                    "selector" to "text=AutoFlow 示例应用",
                    "timeout" to 5000L
                )
            ),
            WorkflowNode(
                id = "click_daily_checkin_card",
                type = NodeType.UI_CLICK,
                title = "点击每日签到卡片",
                x = 100f,
                y = 400f,
                config = mutableMapOf(
                    "selector" to "text=每日签到",
                    "clickType" to "SINGLE",
                    "clickStrategy" to ClickStrategy.FIND_CLICKABLE_PARENT.name
                )
            ),
            WorkflowNode(
                id = "wait_checkin_btn",
                type = NodeType.UI_WAIT,
                title = "等待签到按钮",
                x = 100f,
                y = 500f,
                config = mutableMapOf(
                    "selector" to "text_exact=签到",
                    "timeout" to 3000L
                )
            ),
            WorkflowNode(
                id = "click_checkin",
                type = NodeType.UI_CLICK,
                title = "点击签到按钮",
                x = 100f,
                y = 600f,
                config = mutableMapOf(
                    "selector" to "text_exact=签到",
                    "clickType" to "SINGLE",
                    "clickStrategy" to ClickStrategy.FIND_CLICKABLE_PARENT.name
                )
            ),
            WorkflowNode(
                id = "end_3",
                type = NodeType.END,
                title = "结束",
                x = 100f,
                y = 700f
            )
        )

        val connections = listOf(
            WorkflowConnection(sourceNodeId = "start_3", sourceOutputId = "output", targetNodeId = "launch_demo_app_3", targetInputId = "input"),
            WorkflowConnection(sourceNodeId = "launch_demo_app_3", sourceOutputId = "output", targetNodeId = "wait_demo_app_3", targetInputId = "input"),
            WorkflowConnection(sourceNodeId = "wait_demo_app_3", sourceOutputId = "output", targetNodeId = "click_daily_checkin_card", targetInputId = "input"),
            WorkflowConnection(sourceNodeId = "click_daily_checkin_card", sourceOutputId = "output", targetNodeId = "wait_checkin_btn", targetInputId = "input"),
            WorkflowConnection(sourceNodeId = "wait_checkin_btn", sourceOutputId = "output", targetNodeId = "click_checkin", targetInputId = "input"),
            WorkflowConnection(sourceNodeId = "click_checkin", sourceOutputId = "output", targetNodeId = "end_3", targetInputId = "input")
        )
        
        return Workflow(
            id = "app_auto_checkin",
            name = "示例应用自动签到",
            description = "在AutoFlow示例应用中自动执行签到操作",
            nodes = nodes,
            connections = connections
        )
    }

    // 示例4: 基于示例应用的登录测试
    fun createDemoAppLoginWorkflow(): Workflow {
        AutoFlowLogger.d(TAG, "正在创建演示应用登录工作流")
        val nodes = listOf(
            WorkflowNode(
                id = "start_4",
                type = NodeType.START,
                title = "开始",
                x = 100f,
                y = 100f
            ),
            WorkflowNode(
                id = "launch_demo_app_4",
                type = NodeType.LAUNCH_ACTIVITY,
                title = "启动示例应用",
                x = 100f,
                y = 200f,
                config = mutableMapOf(
                    "packageName" to "com.carlos.autoflow",
                    "className" to "com.carlos.autoflow.demo.DemoAppActivity"
                )
            ),
            WorkflowNode(
                id = "wait_demo_app",
                type = NodeType.UI_WAIT,
                title = "等待示例应用",
                x = 100f,
                y = 300f,
                config = mutableMapOf(
                    "selector" to "text=AutoFlow 示例应用",
                    "timeout" to 5000L
                )
            ),
            WorkflowNode(
                id = "click_login_card",
                type = NodeType.UI_CLICK,
                title = "点击登录卡片",
                x = 100f,
                y = 400f,
                config = mutableMapOf(
                    "selector" to "text=用户登录",
                    "clickType" to "SINGLE",
                    "clickStrategy" to ClickStrategy.FIND_CLICKABLE_PARENT.name
                )
            ),
            WorkflowNode(
                id = "wait_login_page",
                type = NodeType.UI_WAIT,
                title = "等待登录页面",
                x = 100f,
                y = 500f,
                config = mutableMapOf(
                    "selector" to "text=登录账户",
                    "timeout" to 3000L
                )
            ),
            WorkflowNode(
                id = "input_username",
                type = NodeType.UI_INPUT,
                title = "输入用户名",
                x = 100f,
                y = 600f,
                config = mutableMapOf(
                    "selector" to "text=用户名",
                    "text" to "testuser",
                    "clearFirst" to true,
                    "inputStrategy" to InputStrategy.FIND_INPUTTABLE_PARENT.name
                )
            ),
            WorkflowNode(
                id = "input_password",
                type = NodeType.UI_INPUT,
                title = "输入密码",
                x = 100f,
                y = 700f,
                config = mutableMapOf(
                    "selector" to "text=密码",
                    "text" to "123456",
                    "clearFirst" to true,
                    "inputStrategy" to InputStrategy.FIND_INPUTTABLE_PARENT.name
                )
            ),
            WorkflowNode(
                id = "click_login_btn",
                type = NodeType.UI_CLICK,
                title = "点击登录按钮",
                x = 100f,
                y = 800f,
                config = mutableMapOf(
                    "selector" to "text_exact=登录",
                    "clickType" to "SINGLE",
                    "clickStrategy" to ClickStrategy.FIND_CLICKABLE_PARENT.name
                )
            ),
            WorkflowNode(
                id = "wait_success",
                type = NodeType.UI_WAIT,
                title = "等待登录成功",
                x = 100f,
                y = 900f,
                config = mutableMapOf(
                    "selector" to "text=登录成功！",
                    "timeout" to 3000L
                )
            ),
            WorkflowNode(
                id = "end_4",
                type = NodeType.END,
                title = "结束",
                x = 100f,
                y = 1000f
            )
        )
        
        val connections = listOf(
            WorkflowConnection(sourceNodeId = "start_4", sourceOutputId = "output", targetNodeId = "launch_demo_app_4", targetInputId = "input"),
            WorkflowConnection(sourceNodeId = "launch_demo_app_4", sourceOutputId = "output", targetNodeId = "wait_demo_app", targetInputId = "input"),
            WorkflowConnection(sourceNodeId = "wait_demo_app", sourceOutputId = "output", targetNodeId = "click_login_card", targetInputId = "input"),
            WorkflowConnection(sourceNodeId = "click_login_card", sourceOutputId = "output", targetNodeId = "wait_login_page", targetInputId = "input"),
            WorkflowConnection(sourceNodeId = "wait_login_page", sourceOutputId = "output", targetNodeId = "input_username", targetInputId = "input"),
            WorkflowConnection(sourceNodeId = "input_username", sourceOutputId = "output", targetNodeId = "input_password", targetInputId = "input"),
            WorkflowConnection(sourceNodeId = "input_password", sourceOutputId = "output", targetNodeId = "click_login_btn", targetInputId = "input"),
            WorkflowConnection(sourceNodeId = "click_login_btn", sourceOutputId = "output", targetNodeId = "wait_success", targetInputId = "input"),
            WorkflowConnection(sourceNodeId = "wait_success", sourceOutputId = "output", targetNodeId = "end_4", targetInputId = "input")
        )
        
        return Workflow(
            id = "demo_app_login",
            name = "示例应用登录测试",
            description = "在AutoFlow示例应用中测试登录功能",
            nodes = nodes,
            connections = connections
        )
    }

    // 示例5: 示例应用表单填写
    fun createDemoAppFormWorkflow(): Workflow {
        AutoFlowLogger.d(TAG, "正在创建演示应用表单工作流")
        val nodes = listOf(
            WorkflowNode(
                id = "start_5",
                type = NodeType.START,
                title = "开始",
                x = 100f,
                y = 100f
            ),
            WorkflowNode(
                id = "launch_demo_app_5",
                type = NodeType.LAUNCH_ACTIVITY,
                title = "启动示例应用",
                x = 100f,
                y = 200f,
                config = mutableMapOf(
                    "packageName" to "com.carlos.autoflow",
                    "className" to "com.carlos.autoflow.demo.DemoAppActivity"
                )
            ),
            WorkflowNode(
                id = "wait_demo_app_5",
                type = NodeType.UI_WAIT,
                title = "等待示例应用加载",
                x = 100f,
                y = 300f,
                config = mutableMapOf(
                    "selector" to "text=AutoFlow 示例应用",
                    "timeout" to 5000L
                )
            ),
            WorkflowNode(
                id = "click_form_card",
                type = NodeType.UI_CLICK,
                title = "点击表单卡片",
                x = 100f,
                y = 400f,
                config = mutableMapOf(
                    "selector" to "text=信息表单",
                    "clickType" to "SINGLE",
                    "clickStrategy" to ClickStrategy.FIND_CLICKABLE_PARENT.name
                )
            ),
            WorkflowNode(
                id = "wait_form_page",
                type = NodeType.UI_WAIT,
                title = "等待表单页面",
                x = 100f,
                y = 500f,
                config = mutableMapOf(
                    "selector" to "text=个人信息",
                    "timeout" to 3000L
                )
            ),
            WorkflowNode(
                id = "fill_name",
                type = NodeType.UI_INPUT,
                title = "填写姓名",
                x = 100f,
                y = 600f,
                config = mutableMapOf(
                    "selector" to "text=姓名",
                    "text" to "张三",
                    "clearFirst" to true,
                    "inputStrategy" to InputStrategy.FIND_INPUTTABLE_PARENT.name
                )
            ),
            WorkflowNode(
                id = "fill_phone",
                type = NodeType.UI_INPUT,
                title = "填写手机号",
                x = 100f,
                y = 700f,
                config = mutableMapOf(
                    "selector" to "text=手机号",
                    "text" to "13800138000",
                    "clearFirst" to true,
                    "inputStrategy" to InputStrategy.FIND_INPUTTABLE_PARENT.name
                )
            ),
            WorkflowNode(
                id = "fill_email",
                type = NodeType.UI_INPUT,
                title = "填写邮箱",
                x = 100f,
                y = 800f,
                config = mutableMapOf(
                    "selector" to "text=邮箱",
                    "text" to "zhangsan@example.com",
                    "clearFirst" to true,
                    "inputStrategy" to InputStrategy.FIND_INPUTTABLE_PARENT.name
                )
            ),
            WorkflowNode(
                id = "fill_address",
                type = NodeType.UI_INPUT,
                title = "填写地址",
                x = 100f,
                y = 900f,
                config = mutableMapOf(
                    "selector" to "text=地址",
                    "text" to "北京市朝阳区xxx街道xxx号",
                    "clearFirst" to true,
                    "inputStrategy" to InputStrategy.FIND_INPUTTABLE_PARENT.name
                )
            ),
            WorkflowNode(
                id = "submit_form",
                type = NodeType.UI_CLICK,
                title = "提交表单",
                x = 100f,
                y = 1000f,
                config = mutableMapOf(
                    "selector" to "text=提交",
                    "clickType" to "SINGLE",
                    "clickStrategy" to ClickStrategy.FIND_CLICKABLE_PARENT.name
                )
            ),
            WorkflowNode(
                id = "wait_form_success",
                type = NodeType.UI_WAIT,
                title = "等待提交成功",
                x = 100f,
                y = 1100f,
                config = mutableMapOf(
                    "selector" to "text=提交成功！",
                    "timeout" to 3000L
                )
            ),
            WorkflowNode(
                id = "end_5",
                type = NodeType.END,
                title = "结束",
                x = 100f,
                y = 1200f
            )
        )
        
        val connections = listOf(
            WorkflowConnection(sourceNodeId = "start_5", sourceOutputId = "output", targetNodeId = "launch_demo_app_5", targetInputId = "input"),
            WorkflowConnection(sourceNodeId = "launch_demo_app_5", sourceOutputId = "output", targetNodeId = "wait_demo_app_5", targetInputId = "input"),
            WorkflowConnection(sourceNodeId = "wait_demo_app_5", sourceOutputId = "output", targetNodeId = "click_form_card", targetInputId = "input"),
            WorkflowConnection(sourceNodeId = "click_form_card", sourceOutputId = "output", targetNodeId = "wait_form_page", targetInputId = "input"),
            WorkflowConnection(sourceNodeId = "wait_form_page", sourceOutputId = "output", targetNodeId = "fill_name", targetInputId = "input"),
            WorkflowConnection(sourceNodeId = "fill_name", sourceOutputId = "output", targetNodeId = "fill_phone", targetInputId = "input"),
            WorkflowConnection(sourceNodeId = "fill_phone", sourceOutputId = "output", targetNodeId = "fill_email", targetInputId = "input"),
            WorkflowConnection(sourceNodeId = "fill_email", sourceOutputId = "output", targetNodeId = "fill_address", targetInputId = "input"),
            WorkflowConnection(sourceNodeId = "fill_address", sourceOutputId = "output", targetNodeId = "submit_form", targetInputId = "input"),
            WorkflowConnection(sourceNodeId = "submit_form", sourceOutputId = "output", targetNodeId = "wait_form_success", targetInputId = "input"),
            WorkflowConnection(sourceNodeId = "wait_form_success", sourceOutputId = "output", targetNodeId = "end_5", targetInputId = "input")
        )
        
        return Workflow(
            id = "demo_app_form",
            name = "示例应用表单填写",
            description = "在AutoFlow示例应用中测试表单填写功能",
            nodes = nodes,
            connections = connections
        )
    }

    // 示例6: 搜索功能测试
    fun createDemoAppSearchWorkflow(): Workflow {
        AutoFlowLogger.d(TAG, "正在创建演示应用搜索工作流")
        val nodes = listOf(
            WorkflowNode(
                id = "start_6",
                type = NodeType.START,
                title = "开始",
                x = 100f,
                y = 100f
            ),
            WorkflowNode(
                id = "launch_demo_app_6",
                type = NodeType.LAUNCH_ACTIVITY,
                title = "启动示例应用",
                x = 100f,
                y = 200f,
                config = mutableMapOf(
                    "packageName" to "com.carlos.autoflow",
                    "className" to "com.carlos.autoflow.demo.DemoAppActivity"
                )
            ),
            WorkflowNode(
                id = "wait_demo_app_6",
                type = NodeType.UI_WAIT,
                title = "等待示例应用加载",
                x = 100f,
                y = 300f,
                config = mutableMapOf(
                    "selector" to "text=AutoFlow 示例应用",
                    "timeout" to 5000L
                )
            ),
            WorkflowNode(
                id = "click_search_card",
                type = NodeType.UI_CLICK,
                title = "点击搜索卡片",
                x = 100f,
                y = 400f,
                config = mutableMapOf(
                    "selector" to "text=搜索功能",
                    "clickType" to "SINGLE",
                    "clickStrategy" to ClickStrategy.FIND_CLICKABLE_PARENT.name
                )
            ),
            WorkflowNode(
                id = "wait_search_page",
                type = NodeType.UI_WAIT,
                title = "等待搜索页面",
                x = 100f,
                y = 500f,
                config = mutableMapOf(
                    "selector" to "text=搜索商品",
                    "timeout" to 3000L
                )
            ),
            WorkflowNode(
                id = "input_search_term",
                type = NodeType.UI_INPUT,
                title = "输入搜索词",
                x = 100f,
                y = 600f,
                config = mutableMapOf(
                    "selector" to "text=搜索商品",
                    "text" to "iPhone 14",
                    "clearFirst" to true,
                    "inputStrategy" to InputStrategy.FIND_INPUTTABLE_PARENT.name
                )
            ),
            WorkflowNode(
                id = "wait_search_btn",
                type = NodeType.UI_WAIT,
                title = "等待搜索按钮出现",
                x = 100f,
                y = 700f,
                config = mutableMapOf(
                    "selector" to "text_exact=搜索",
                    "timeout" to 2000L
                )
            ),
            WorkflowNode(
                id = "click_search_btn",
                type = NodeType.UI_CLICK,
                title = "点击搜索按钮",
                x = 100f,
                y = 800f,
                config = mutableMapOf(
                    "selector" to "text_exact=搜索",
                    "clickType" to "SINGLE",
                    "clickStrategy" to ClickStrategy.FIND_CLICKABLE_PARENT.name
                )
            ),
            WorkflowNode(
                id = "end_6",
                type = NodeType.END,
                title = "结束",
                x = 100f,
                y = 900f
            )
        )
        
        val connections = listOf(
            WorkflowConnection(sourceNodeId = "start_6", sourceOutputId = "output", targetNodeId = "launch_demo_app_6", targetInputId = "input"),
            WorkflowConnection(sourceNodeId = "launch_demo_app_6", sourceOutputId = "output", targetNodeId = "wait_demo_app_6", targetInputId = "input"),
            WorkflowConnection(sourceNodeId = "wait_demo_app_6", sourceOutputId = "output", targetNodeId = "click_search_card", targetInputId = "input"),
            WorkflowConnection(sourceNodeId = "click_search_card", sourceOutputId = "output", targetNodeId = "wait_search_page", targetInputId = "input"),
            WorkflowConnection(sourceNodeId = "wait_search_page", sourceOutputId = "output", targetNodeId = "input_search_term", targetInputId = "input"),
            WorkflowConnection(sourceNodeId = "input_search_term", sourceOutputId = "output", targetNodeId = "wait_search_btn", targetInputId = "input"),
            WorkflowConnection(sourceNodeId = "wait_search_btn", sourceOutputId = "output", targetNodeId = "click_search_btn", targetInputId = "input"),
            WorkflowConnection(sourceNodeId = "click_search_btn", sourceOutputId = "output", targetNodeId = "end_6", targetInputId = "input")
        )
        
        return Workflow(
            id = "demo_app_search",
            name = "示例应用搜索测试",
            description = "在AutoFlow示例应用中测试搜索输入功能",
            nodes = nodes,
            connections = connections
        )
    }

    // 示例7: 智能聊天自动回复（持续监听）
    fun createDemoAppChatWorkflow(): Workflow {
        AutoFlowLogger.d(TAG, "正在创建演示应用聊天工作流")
        val nodes = listOf(
            WorkflowNode(
                id = "start_7",
                type = NodeType.START,
                title = "开始",
                x = 100f,
                y = 100f
            ),
            WorkflowNode(
                id = "launch_demo_app_7",
                type = NodeType.LAUNCH_ACTIVITY,
                title = "启动示例应用",
                x = 100f,
                y = 200f,
                config = mutableMapOf(
                    "packageName" to "com.carlos.autoflow",
                    "className" to "com.carlos.autoflow.demo.DemoAppActivity"
                )
            ),
            WorkflowNode(
                id = "wait_demo_app_7",
                type = NodeType.UI_WAIT,
                title = "等待示例应用加载",
                x = 100f,
                y = 300f,
                config = mutableMapOf(
                    "selector" to "text=AutoFlow 示例应用",
                    "timeout" to 5000L
                )
            ),
            WorkflowNode(
                id = "click_chat_card",
                type = NodeType.UI_CLICK,
                title = "点击智能聊天卡片",
                x = 100f,
                y = 400f,
                config = mutableMapOf(
                    "selector" to "text=智能聊天",
                    "clickType" to "SINGLE",
                    "clickStrategy" to ClickStrategy.FIND_CLICKABLE_PARENT.name
                )
            ),
            WorkflowNode(
                id = "wait_chat_page",
                type = NodeType.UI_WAIT,
                title = "等待聊天页面",
                x = 100f,
                y = 500f,
                config = mutableMapOf(
                    "selector" to "text=输入消息",
                    "timeout" to 3000L
                )
            ),
            WorkflowNode(
                id = "loop_start",
                type = NodeType.LOOP,
                title = "循环50次",
                x = 100f,
                y = 600f,
                config = mutableMapOf(
                    "loopType" to "count",
                    "count" to 50
                )
            ),
            WorkflowNode(
                id = "delay_check",
                type = NodeType.DELAY,
                title = "等待机器人回复",
                x = 100f,
                y = 700f,
                config = mutableMapOf(
                    "duration" to 1500L
                )
            ),
            WorkflowNode(
                id = "input_reply",
                type = NodeType.UI_INPUT,
                title = "输入收到数字",
                x = 100f,
                y = 800f,
                config = mutableMapOf(
                    "selector" to "text=输入消息",
                    "text" to "收到",
                    "clearFirst" to true,
                    "inputStrategy" to InputStrategy.FIND_INPUTTABLE_PARENT.name
                )
            ),
            WorkflowNode(
                id = "click_send",
                type = NodeType.UI_CLICK,
                title = "点击发送按钮",
                x = 100f,
                y = 900f,
                config = mutableMapOf(
                    "selector" to "text=发送",
                    "clickType" to "SINGLE",
                    "clickStrategy" to ClickStrategy.FIND_CLICKABLE_PARENT.name
                )
            ),
            WorkflowNode(
                id = "end_7",
                type = NodeType.END,
                title = "结束",
                x = 100f,
                y = 1000f
            )
        )
        
        val connections = listOf(
            WorkflowConnection(sourceNodeId = "start_7", sourceOutputId = "output", targetNodeId = "launch_demo_app_7", targetInputId = "input"),
            WorkflowConnection(sourceNodeId = "launch_demo_app_7", sourceOutputId = "output", targetNodeId = "wait_demo_app_7", targetInputId = "input"),
            WorkflowConnection(sourceNodeId = "wait_demo_app_7", sourceOutputId = "output", targetNodeId = "click_chat_card", targetInputId = "input"),
            WorkflowConnection(sourceNodeId = "click_chat_card", sourceOutputId = "output", targetNodeId = "wait_chat_page", targetInputId = "input"),
            WorkflowConnection(sourceNodeId = "wait_chat_page", sourceOutputId = "output", targetNodeId = "loop_start", targetInputId = "input"),
            WorkflowConnection(sourceNodeId = "loop_start", sourceOutputId = "output", targetNodeId = "delay_check", targetInputId = "input"),
            WorkflowConnection(sourceNodeId = "delay_check", sourceOutputId = "output", targetNodeId = "input_reply", targetInputId = "input"),
            WorkflowConnection(sourceNodeId = "input_reply", sourceOutputId = "output", targetNodeId = "click_send", targetInputId = "input"),
            WorkflowConnection(sourceNodeId = "click_send", sourceOutputId = "output", targetNodeId = "loop_start", targetInputId = "input"),
            WorkflowConnection(sourceNodeId = "loop_start", sourceOutputId = "complete", targetNodeId = "end_7", targetInputId = "input")
        )
        
        return Workflow(
            id = "demo_app_chat",
            name = "示例应用智能聊天",
            description = "持续监听并自动回复消息（手动停止）",
            nodes = nodes,
            connections = connections
        )
    }

    // 获取所有无障碍示例
    fun getAllAccessibilityExamples(): List<Workflow> {
        AutoFlowLogger.d(TAG, "正在获取所有无障碍示例")
        return listOf(
            createDemoAppLoginWorkflow(),
            createDemoAppFormWorkflow(),
            createAppAutoCheckInWorkflow(),
            createDemoAppSearchWorkflow(),
            createDemoAppChatWorkflow()
        )
    }
}
