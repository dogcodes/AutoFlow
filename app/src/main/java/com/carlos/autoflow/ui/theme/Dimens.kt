package com.carlos.autoflow.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

object Dimens {
    object WorkflowEditor {
        val GridSize = 20.dp
        val NodeWidth = 160.dp
        val NodeHeight = 100.dp
        val ConnectionControlOffset = 60.dp
        val ConnectionStrokeWidth = 3.dp
        val ConnectionCircleRadiusOuter = 5.dp
        val ConnectionCircleRadiusInner = 2.dp
        val ConnectionDeleteButtonSize = 24.dp
        val ConnectionDeleteButtonRadius = ConnectionDeleteButtonSize / 2
        val DefaultPadding = 16.dp
        val SmallPadding = 8.dp
        val CardPadding = 12.dp
    }

    object WorkflowEditorColors {
        val ConnectionLine = Color(0xFF1976D2)
        val ConnectionCircleOutline = Color(0xFF2196F3)
        val ConnectionCircleTarget = Color(0xFF42A5F5)
        val ConnectionDeleteButtonBackground = Color(0xFFE53E3E).copy(alpha = 0.8f)
        val ConnectingModeCardBackground = Color(0xFF2196F3)
        val EndNodeTipCardBackground = Color(0xFFE53E3E)
        val GridLine = Color.Gray.copy(alpha = 0.2f)
    }
}
