package com.carlos.autoflow.workflow.viewmodel

import androidx.compose.ui.geometry.Size
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class CanvasState(
    val scale: Float = 1f,
    val offsetX: Float = 0f,
    val offsetY: Float = 0f,
    val minScale: Float = 0.3f,
    val maxScale: Float = 3f,
    val isAdaptiveGridEnabled: Boolean = false, // 是否启用自适应网格密度（用于控制网格在缩放时是否自动调整密度）
    val showFlowAnimation: Boolean = true // 是否显示连接线流动动画
)

class CanvasViewModel : ViewModel() {
    private val _canvasState = MutableStateFlow(CanvasState())
    val canvasState: StateFlow<CanvasState> = _canvasState
    
    fun updateScale(scale: Float) {
        _canvasState.value = _canvasState.value.copy(
            scale = scale.coerceIn(
                _canvasState.value.minScale,
                _canvasState.value.maxScale
            )
        )
    }
    
    fun updateOffset(offsetX: Float, offsetY: Float) {
        _canvasState.value = _canvasState.value.copy(
            offsetX = offsetX,
            offsetY = offsetY
        )
    }
    
    fun resetZoom() {
        _canvasState.value = CanvasState()
    }
    
    fun zoomIn() {
        updateScale(_canvasState.value.scale * 1.2f)
    }
    
    fun zoomOut() {
        updateScale(_canvasState.value.scale / 1.2f)
    }

    // Function to toggle adaptive grid density
    fun toggleAdaptiveGrid() {
        _canvasState.value = _canvasState.value.copy(
            isAdaptiveGridEnabled = !_canvasState.value.isAdaptiveGridEnabled
        )
    }
    
    // Function to toggle flow animation
    fun toggleFlowAnimation() {
        _canvasState.value = _canvasState.value.copy(
            showFlowAnimation = !_canvasState.value.showFlowAnimation
        )
    }
}
