package com.carlos.autoflow.workflow.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.carlos.autoflow.workflow.examples.JsonWorkflowExamples
import com.carlos.autoflow.workflow.viewmodel.WorkflowViewModel

@Composable
fun JsonExamplesDialog(
    workflowViewModel: WorkflowViewModel,
    onDismiss: () -> Unit
) {
    val examples = remember { JsonWorkflowExamples.getAllExamples() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("无障碍JSON示例") },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth()
            ) {
                items(examples) { example ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        onClick = {
                            workflowViewModel.importFromJson(example.getJson())
                            onDismiss()
                        },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Text(
                            text = example.name,
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("关闭")
            }
        }
    )
}

