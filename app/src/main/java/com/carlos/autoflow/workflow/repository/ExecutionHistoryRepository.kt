package com.carlos.autoflow.workflow.repository

import android.content.Context
import android.content.SharedPreferences
import com.carlos.autoflow.workflow.models.WorkflowExecution
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ExecutionHistoryRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("execution_history", Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private val sharedExecutions = MutableStateFlow<List<WorkflowExecution>>(emptyList())
        private var isLoaded = false
    }

    val executions: StateFlow<List<WorkflowExecution>> = sharedExecutions

    init {
        if (!isLoaded) {
            loadExecutions()
            isLoaded = true
        }
    }

    fun addExecution(execution: WorkflowExecution) {
        val current = sharedExecutions.value.toMutableList()
        current.add(0, execution) // 最新的在前面

        // 只保留最近50条记录
        if (current.size > 50) {
            current.removeAt(current.size - 1)
        }

        sharedExecutions.value = current
        saveExecutions()
    }

    fun updateExecution(execution: WorkflowExecution) {
        val current = sharedExecutions.value.toMutableList()
        val index = current.indexOfFirst { it.id == execution.id }
        if (index != -1) {
            current[index] = execution
            sharedExecutions.value = current
            saveExecutions()
        }
    }

    private fun loadExecutions() {
        val json = prefs.getString("executions", "[]") ?: "[]"
        val type = object : TypeToken<List<WorkflowExecution>>() {}.type
        val executions = gson.fromJson<List<WorkflowExecution>>(json, type) ?: emptyList()
        sharedExecutions.value = executions
    }

    fun deleteExecution(executionId: String) {
        val current = sharedExecutions.value.toMutableList()
        current.removeAll { it.id == executionId }
        sharedExecutions.value = current
        saveExecutions()
    }

    fun clearAllExecutions() {
        sharedExecutions.value = emptyList()
        saveExecutions()
    }

    private fun saveExecutions() {
        val json = gson.toJson(sharedExecutions.value)
        prefs.edit().putString("executions", json).apply()
    }
}
