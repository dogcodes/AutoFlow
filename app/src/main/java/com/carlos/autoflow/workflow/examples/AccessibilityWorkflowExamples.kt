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
                id = "wait_checkin_page",
                type = NodeType.UI_WAIT,
                title = "等待每日签到页面",
                x = 100f,
                y = 500f,
                config = mutableMapOf(
                    "selector" to "text_exact=每日签到",
                    "timeout" to 5000L
                )
            ),
            WorkflowNode(
                id = "find_checkin_btn",
                type = NodeType.UI_FIND,
                title = "查找签到按钮",
                x = 100f,
                y = 600f,
                config = mutableMapOf(
                    "selector" to "text_exact=签到",
                    "multiple" to true
                )
            ),
            WorkflowNode(
                id = "check_btn_exists",
                type = NodeType.CONDITION,
                title = "检查按钮是否存在",
                x = 100f,
                y = 700f,
                config = mutableMapOf(
                    "condition" to "elements.count > 0"
                )
            ),
            WorkflowNode(
                id = "click_checkin",
                type = NodeType.UI_CLICK,
                title = "点击签到",
                x = 50f,
                y = 800f,
                config = mutableMapOf(
                    "selector" to "text_exact=签到",
                    "clickType" to "SINGLE",
                    "clickStrategy" to ClickStrategy.FIND_CLICKABLE_PARENT.name
                )
            ),
            WorkflowNode(
                id = "wait_success",
                type = NodeType.UI_WAIT,
                title = "等待签到成功",
                x = 50f,
                y = 900f,
                config = mutableMapOf(
                    "selector" to "text=签到成功",
                    "timeout" to 3000L
                )
            ),
            WorkflowNode(
                id = "already_signed",
                type = NodeType.DELAY,
                title = "已签到提示",
                x = 230f,
                y = 800f,
                config = mutableMapOf(
                    "duration" to 1000L
                )
            ),
            WorkflowNode(
                id = "end_3",
                type = NodeType.END,
                title = "结束",
                x = 100f,
                y = 1000f
            )
        )

        val connections = listOf(
            WorkflowConnection(sourceNodeId = "start_3", sourceOutputId = "output", targetNodeId = "launch_demo_app_3", targetInputId = "input"),
            WorkflowConnection(sourceNodeId = "launch_demo_app_3", sourceOutputId = "output", targetNodeId = "wait_demo_app_3", targetInputId = "input"),
            WorkflowConnection(sourceNodeId = "wait_demo_app_3", sourceOutputId = "output", targetNodeId = "click_daily_checkin_card", targetInputId = "input"),
            WorkflowConnection(sourceNodeId = "click_daily_checkin_card", sourceOutputId = "output", targetNodeId = "wait_checkin_page", targetInputId = "input"),
            WorkflowConnection(sourceNodeId = "wait_checkin_page", sourceOutputId = "output", targetNodeId = "find_checkin_btn", targetInputId = "input"),
            WorkflowConnection(sourceNodeId = "find_checkin_btn", sourceOutputId = "output", targetNodeId = "check_btn_exists", targetInputId = "input"),
            WorkflowConnection(sourceNodeId = "check_btn_exists", sourceOutputId = "true", targetNodeId = "click_checkin", targetInputId = "input"),
            WorkflowConnection(sourceNodeId = "check_btn_exists", sourceOutputId = "false", targetNodeId = "already_signed", targetInputId = "input"),
            WorkflowConnection(sourceNodeId = "click_checkin", sourceOutputId = "output", targetNodeId = "wait_success", targetInputId = "input"),
            WorkflowConnection(sourceNodeId = "wait_success", sourceOutputId = "output", targetNodeId = "end_3", targetInputId = "input"),
            WorkflowConnection(sourceNodeId = "already_signed", sourceOutputId = "output", targetNodeId = "end_3", targetInputId = "input")
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
                    "selector" to "text=请输入用户名",
                    "text" to "testuser",
                    "clearFirst" to true
                )
            ),
            WorkflowNode(
                id = "input_password",
                type = NodeType.UI_INPUT,
                title = "输入密码",
                x = 100f,
                y = 700f,
                config = mutableMapOf(
                    "selector" to "text=请输入密码",
                    "text" to "123456",
                    "clearFirst" to true
                )
            ),
            WorkflowNode(
                id = "click_login_btn",
                type = NodeType.UI_CLICK,
                title = "点击登录按钮",
                x = 100f,
                y = 800f,
                config = mutableMapOf(
                    "selector" to "text=登录",
                    "clickType" to "SINGLE"
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
                    "selector" to "text=请输入姓名",
                    "text" to "张三",
                    "clearFirst" to true
                )
            ),
            WorkflowNode(
                id = "fill_phone",
                type = NodeType.UI_INPUT,
                title = "填写手机号",
                x = 100f,
                y = 700f,
                config = mutableMapOf(
                    "selector" to "text=请输入手机号",
                    "text" to "13800138000",
                    "clearFirst" to true
                )
            ),
            WorkflowNode(
                id = "fill_email",
                type = NodeType.UI_INPUT,
                title = "填写邮箱",
                x = 100f,
                y = 800f,
                config = mutableMapOf(
                    "selector" to "text=请输入邮箱地址",
                    "text" to "zhangsan@example.com",
                    "clearFirst" to true
                )
            ),
            WorkflowNode(
                id = "fill_address",
                type = NodeType.UI_INPUT,
                title = "填写地址",
                x = 100f,
                y = 900f,
                config = mutableMapOf(
                    "selector" to "text=请输入详细地址",
                    "text" to "北京市朝阳区xxx街道xxx号",
                    "clearFirst" to true
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
                    "clickType" to "SINGLE"
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

    // 获取所有无障碍示例
    fun getAllAccessibilityExamples(): List<Workflow> {
        AutoFlowLogger.d(TAG, "正在获取所有无障碍示例")
        return listOf(
            createDemoAppLoginWorkflow(),
            createDemoAppFormWorkflow(),
            createAppAutoCheckInWorkflow()
        )
    }
}
