package com.carlos.autoflow.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * 包含应用程序中使用的各种尺寸常量的对象.
 */
object Dimens {
    /**
     * 专用于工作流编辑器UI的尺寸常量.
     */
    object WorkflowEditor {
        /** 工作流编辑器背景中每个网格单元的大小. */
        val GridSize = 20.dp
        /** 工作流节点的默认宽度. */
        val NodeWidth = 160.dp
        /** 工作流节点的默认高度. */
        val NodeHeight = 100.dp
        /** 用于连接的立方贝塞尔曲线中控制点的偏移量. */
        val ConnectionControlOffset = 60.dp
        /** 绘制节点之间连接线的描边宽度. */
        val ConnectionStrokeWidth = 3.dp
        /** 连接线起点和终点绘制的圆圈外径. */
        val ConnectionCircleRadiusOuter = 5.dp
        /** 连接线起点和终点绘制的圆圈内径. */
        val ConnectionCircleRadiusInner = 2.dp
        /** 用于删除连接的圆形按钮的大小. */
        val ConnectionDeleteButtonSize = 24.dp
        /** 用于删除连接的圆形按钮的半径. */
        val ConnectionDeleteButtonRadius = ConnectionDeleteButtonSize / 2
        /** 各种UI组件中使用的默认填充值. */
        val DefaultPadding = 16.dp
        /** 各种UI组件中使用的较小填充值. */
        val SmallPadding = 8.dp
        /** 专门用于显示消息或信息的卡片的填充. */
        val CardPadding = 12.dp
    }

    /**
     * 专用于工作流编辑器UI的颜色常量.
     */
    object WorkflowEditorColors {
        /** 节点之间连接线的颜色. */
        val ConnectionLine = Color(0xFF1976D2)
        /** 连接线起点圆圈的轮廓颜色. */
        val ConnectionCircleOutline = Color(0xFF2196F3)
        /** 连接线终点圆圈的目标颜色. */
        val ConnectionCircleTarget = Color(0xFF42A5F5)
        /** 连接删除按钮的背景颜色. */
        val ConnectionDeleteButtonBackground = Color(0xFFE53E3E).copy(alpha = 0.8f)
        /** 处于连接模式时显示的卡片背景颜色. */
        val ConnectingModeCardBackground = Color(0xFF2196F3)
        /** 显示关于结束节点提示的卡片背景颜色. */
        val EndNodeTipCardBackground = Color(0xFFE53E3E)
        /** 工作流编辑器背景中网格线的颜色. */
        val GridLine = Color.Gray.copy(alpha = 0.2f)
    }
}
