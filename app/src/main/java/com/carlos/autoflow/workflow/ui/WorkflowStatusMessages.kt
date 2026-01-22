package com.carlos.autoflow.workflow.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.carlos.autoflow.ui.theme.Dimens
import com.carlos.autoflow.workflow.models.NodeType
import com.carlos.autoflow.workflow.models.Workflow
import com.carlos.autoflow.workflow.viewmodel.WorkflowViewModel
import kotlinx.coroutines.delay

@Composable
fun WorkflowStatusMessages(
    workflow: Workflow,
    connectingNodeId: String?,
    selectedNodeId: String?,
    workflowViewModel: WorkflowViewModel
) {
    androidx.compose.foundation.layout.Box(modifier = Modifier.fillMaxSize()) {
        if (connectingNodeId != null) {
            val connectingNode = workflow.nodes.find { it.id == connectingNodeId }
            Card(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(Dimens.WorkflowEditor.DefaultPadding),
                colors = CardDefaults.cardColors(containerColor = Dimens.WorkflowEditorColors.ConnectingModeCardBackground)
            ) {
                Text(
                    text = when (connectingNode?.type) {
                        NodeType.START -> "🔗 从【开始】连接：点击其他节点完成连接"
                        else -> "🔗 连接模式：点击目标节点完成连接"
                    },
                    modifier = Modifier.padding(Dimens.WorkflowEditor.CardPadding),
                    color = Color.White
                )
            }
        }

        if (selectedNodeId?.startsWith("end_tip_") == true) {
            Card(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(Dimens.WorkflowEditor.DefaultPadding),
                colors = CardDefaults.cardColors(containerColor = Dimens.WorkflowEditorColors.EndNodeTipCardBackground)
            ) {
                Text(
                    text = "❌ 结束节点无法向外连接",
                    modifier = Modifier.padding(Dimens.WorkflowEditor.CardPadding),
                    color = Color.White
                )
            }

            LaunchedEffect(selectedNodeId) {
                delay(2000)
                workflowViewModel.selectNode(null)
            }
        }
    }
}