package com.carlos.autoflow.workflow.repository

import android.content.Context
import android.content.SharedPreferences
import com.carlos.autoflow.workflow.models.Workflow
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class WorkflowRepository(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("workflows", Context.MODE_PRIVATE)
    private val gson = Gson()

    private val _workflows = MutableStateFlow<List<Workflow>>(emptyList())
    val workflows: StateFlow<List<Workflow>> = _workflows

    init {
        loadWorkflows()
    }

    fun upsertWorkflow(workflow: Workflow) {
        val current = _workflows.value.toMutableList()
        val index = current.indexOfFirst { it.id == workflow.id }
        val normalized = workflow.copy(updatedAt = System.currentTimeMillis())

        if (index >= 0) {
            current[index] = normalized
        } else {
            current.add(0, normalized)
        }

        _workflows.value = current.sortedByDescending { it.updatedAt }
        saveWorkflows()
    }

    fun deleteWorkflow(workflowId: String) {
        _workflows.value = _workflows.value.filterNot { it.id == workflowId }
        saveWorkflows()
    }

    fun getWorkflow(workflowId: String): Workflow? {
        return _workflows.value.firstOrNull { it.id == workflowId }
    }

    private fun loadWorkflows() {
        val json = prefs.getString("items", "[]") ?: "[]"
        val type = object : TypeToken<List<Workflow>>() {}.type
        _workflows.value = gson.fromJson<List<Workflow>>(json, type) ?: emptyList()
    }

    private fun saveWorkflows() {
        prefs.edit().putString("items", gson.toJson(_workflows.value)).apply()
    }
}
