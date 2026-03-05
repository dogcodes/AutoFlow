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
) {
    fun viewportCenterX(): Float = viewportWidth / 2f

    fun viewportCenterY(): Float = viewportHeight / 2f

    fun worldToScreenX(worldX: Float): Float = worldX * scale + offsetX

    fun worldToScreenY(worldY: Float): Float = worldY * scale + offsetY

    fun screenToWorldX(screenX: Float): Float = (screenX - offsetX) / scale

    fun screenToWorldY(screenY: Float): Float = (screenY - offsetY) / scale

    fun applyTransform(anchorX: Float, anchorY: Float, panX: Float, panY: Float, zoomFactor: Float): CanvasState {
        val newScale = (scale * zoomFactor).coerceIn(minScale, maxScale)
        val effectiveZoom = newScale / scale

        return copy(
            scale = newScale,
            offsetX = offsetX * effectiveZoom + (1 - effectiveZoom) * anchorX + panX,
            offsetY = offsetY * effectiveZoom + (1 - effectiveZoom) * anchorY + panY
        )
    }
}

class CanvasViewModel : ViewModel() {
    private val _canvasState = MutableStateFlow(CanvasState())
    val canvasState: StateFlow<CanvasState> = _canvasState

    fun updateViewportSize(width: Float, height: Float) {
        if (width <= 0f || height <= 0f) return
        _canvasState.value = _canvasState.value.copy(
            viewportWidth = width,
            viewportHeight = height
        )
    }

    fun applyTransform(anchorX: Float, anchorY: Float, panX: Float, panY: Float, zoomFactor: Float) {
        _canvasState.value = _canvasState.value.applyTransform(
            anchorX = anchorX,
            anchorY = anchorY,
            panX = panX,
            panY = panY,
            zoomFactor = zoomFactor
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
        applyTransform(
            anchorX = _canvasState.value.viewportCenterX(),
            anchorY = _canvasState.value.viewportCenterY(),
            panX = 0f,
            panY = 0f,
            zoomFactor = 1.2f
        )
    }

    fun zoomOut() {
        applyTransform(
            anchorX = _canvasState.value.viewportCenterX(),
            anchorY = _canvasState.value.viewportCenterY(),
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
