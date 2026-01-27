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
    
    private val _executions = MutableStateFlow<List<WorkflowExecution>>(emptyList())
    val executions: StateFlow<List<WorkflowExecution>> = _executions
    
    init {
        loadExecutions()
    }
    
    fun addExecution(execution: WorkflowExecution) {
        val current = _executions.value.toMutableList()
        current.add(0, execution) // 最新的在前面
        
        // 只保留最近50条记录
        if (current.size > 50) {
            current.removeAt(current.size - 1)
        }
        
        _executions.value = current
        saveExecutions()
    }
    
    fun updateExecution(execution: WorkflowExecution) {
        val current = _executions.value.toMutableList()
        val index = current.indexOfFirst { it.id == execution.id }
        if (index != -1) {
            current[index] = execution
            _executions.value = current
            saveExecutions()
        }
    }
    
    private fun loadExecutions() {
        val json = prefs.getString("executions", "[]") ?: "[]"
        val type = object : TypeToken<List<WorkflowExecution>>() {}.type
        val executions = gson.fromJson<List<WorkflowExecution>>(json, type) ?: emptyList()
        _executions.value = executions
    }
    
    fun deleteExecution(executionId: String) {
        val current = _executions.value.toMutableList()
        current.removeAll { it.id == executionId }
        _executions.value = current
        saveExecutions()
    }
    
    fun clearAllExecutions() {
        _executions.value = emptyList()
        saveExecutions()
    }
    
    private fun saveExecutions() {
        val json = gson.toJson(_executions.value)
        prefs.edit().putString("executions", json).apply()
    }
}
