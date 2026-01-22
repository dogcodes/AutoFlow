package com.carlos.autoflow.workflow.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.carlos.autoflow.workflow.models.NodeType
import com.carlos.autoflow.workflow.models.Workflow
import com.carlos.autoflow.workflow.viewmodel.WorkflowViewModel

@Composable
fun WorkflowNodes(
    workflow: Workflow,
    selectedNodeId: String?,
    connectingNodeId: String?,
    workflowViewModel: WorkflowViewModel
) {
    workflow.nodes.forEach { node ->
        key(node.id) {
            Box(
                modifier = Modifier.offset(x = node.x.dp, y = node.y.dp)
            ) {
                WorkflowNodeView(
                    node = node,
                    isSelected = selectedNodeId == node.id,
                    isConnecting = connectingNodeId == node.id,
                    onMove = { deltaX, deltaY ->
                        workflowViewModel.moveNode(
                            node.id,
                            deltaX,
                            deltaY
                        )
                    },
                    onSelect = {
                        if (connectingNodeId != null) {
                            workflowViewModel.finishConnection(node.id)
                        } else {
                            workflowViewModel.selectNode(node.id)
                        }
                    },
                    onDelete = { workflowViewModel.deleteNode(node.id) },
                    onDoubleClick = {
                        if (connectingNodeId == null) {
                            if (node.type == NodeType.END) {
                                workflowViewModel.selectNode("end_tip_${node.id}")
                            } else {
                                workflowViewModel.startConnection(node.id)
                            }
                        } else {
                            workflowViewModel.cancelConnection()
                        }
                    }
                )
            }
        }
    }
}