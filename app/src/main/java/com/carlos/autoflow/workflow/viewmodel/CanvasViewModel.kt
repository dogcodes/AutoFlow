package com.carlos.autoflow.workflow.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class CanvasState(
    val scale: Float = 1f,
    val offsetX: Float = 0f,
    val offsetY: Float = 0f,
    val minScale: Float = 0.3f,
    val maxScale: Float = 3f,
    val viewportWidth: Float = 0f,
    val viewportHeight: Float = 0f,
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

    fun updateViewportSize(width: Float, height: Float) {
        if (width <= 0f || height <= 0f) return
        _canvasState.value = _canvasState.value.copy(
            viewportWidth = width,
            viewportHeight = height
        )
    }

    fun applyTransform(anchorX: Float, anchorY: Float, panX: Float, panY: Float, zoomFactor: Float) {
        val state = _canvasState.value
        val oldScale = state.scale
        val newScale = (oldScale * zoomFactor).coerceIn(state.minScale, state.maxScale)
        val effectiveZoom = newScale / oldScale

        // Top-left origin camera model:
        // screen = world * scale + offset
        // Incremental gesture transform: x' = zoom * (x - anchor) + anchor + pan
        val newOffsetX = state.offsetX * effectiveZoom + (1 - effectiveZoom) * anchorX + panX
        val newOffsetY = state.offsetY * effectiveZoom + (1 - effectiveZoom) * anchorY + panY

        _canvasState.value = state.copy(
            scale = newScale,
            offsetX = newOffsetX,
            offsetY = newOffsetY
        )
    }

    fun resetZoom() {
        _canvasState.value = _canvasState.value.copy(
            scale = 1f,
            offsetX = 0f,
            offsetY = 0f
        )
    }

    fun zoomIn() {
        val state = _canvasState.value
        applyTransform(
            anchorX = state.viewportWidth / 2f,
            anchorY = state.viewportHeight / 2f,
            panX = 0f,
            panY = 0f,
            zoomFactor = 1.2f
        )
    }

    fun zoomOut() {
        val state = _canvasState.value
        applyTransform(
            anchorX = state.viewportWidth / 2f,
            anchorY = state.viewportHeight / 2f,
            panX = 0f,
            panY = 0f,
            zoomFactor = 1f / 1.2f
        )
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
