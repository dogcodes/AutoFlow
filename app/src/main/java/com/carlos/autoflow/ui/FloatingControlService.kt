package com.carlos.autoflow.ui

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import android.view.MotionEvent
import kotlin.math.abs

class FloatingControlService : Service() {
    
    private var windowManager: WindowManager? = null
    private var floatingView: View? = null
    
    companion object {
        private var instance: FloatingControlService? = null
        private var startCallback: (() -> Unit)? = null
        private var stopCallback: (() -> Unit)? = null
        private var isExecuting = false
        
        fun start(context: Context, onStart: () -> Unit, onStop: () -> Unit) {
            if (!canDrawOverlays(context)) {
                requestOverlayPermission(context)
                return
            }
            
            startCallback = onStart
            stopCallback = onStop
            isExecuting = false
            context.startService(Intent(context, FloatingControlService::class.java))
        }
        
        fun updateExecutionState(executing: Boolean) {
            isExecuting = executing
            instance?.updateButtonState()
        }
        
        fun stop(context: Context) {
            context.stopService(Intent(context, FloatingControlService::class.java))
        }
        
        private fun canDrawOverlays(context: Context): Boolean {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Settings.canDrawOverlays(context)
            } else {
                true
            }
        }
        
        private fun requestOverlayPermission(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Toast.makeText(context, "请授予悬浮窗权限以使用浮动控制按钮", Toast.LENGTH_LONG).show()
                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${context.packageName}"))
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        instance = this
        if (canDrawOverlays(this)) {
            showFloatingButtons()
        } else {
            stopSelf()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
        hideFloatingButtons()
    }

    private fun canDrawOverlays(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(context)
        } else {
            true
        }
    }

    private fun showFloatingButtons() {
        try {
            windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
            
            val button = ImageView(this).apply {
                updateButtonState()
                setBackgroundResource(android.R.drawable.btn_default)
                setPadding(20, 20, 20, 20)

                var initialX: Int = 0
                var initialY: Int = 0
                var initialTouchX: Float = 0f
                var initialTouchY: Float = 0f
                var isDragging: Boolean = false

                setOnTouchListener { v, event ->
                    val layoutParams = v.layoutParams as WindowManager.LayoutParams
                    when (event.action) {
                        MotionEvent.ACTION_DOWN -> {
                            initialX = layoutParams.x
                            initialY = layoutParams.y
                            initialTouchX = event.rawX
                            initialTouchY = event.rawY
                            isDragging = false
                            true
                        }
                        MotionEvent.ACTION_MOVE -> {
                            val dx = event.rawX - initialTouchX
                            val dy = event.rawY - initialTouchY
                            
                            if (abs(dx) > 5 || abs(dy) > 5) {
                                isDragging = true
                                layoutParams.x = (initialX + dx).toInt()
                                layoutParams.y = (initialY + dy).toInt()
                                windowManager?.updateViewLayout(v, layoutParams)
                            }
                            true
                        }
                        MotionEvent.ACTION_UP -> {
                            if (!isDragging) {
                                // Only trigger click if it was not a drag
                                if (isExecuting) {
                                    stopCallback?.invoke()
                                } else {
                                    startCallback?.invoke()
                                }
                            }
                            isDragging = false
                            true
                        }
                        else -> false
                    }
                }
            }
            
            val layoutFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE
            }
            
            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                layoutFlag,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.TOP or Gravity.START
                x = 50
                y = 200
            }
            
            floatingView = button
            windowManager?.addView(floatingView, params)
        } catch (e: Exception) {
            e.printStackTrace()
            stopSelf()
        }
    }
    
    private fun ImageView.updateButtonState() {
        setImageResource(
            if (isExecuting) android.R.drawable.ic_media_pause
            else android.R.drawable.ic_media_play
        )
    }
    
    fun updateButtonState() {
        (floatingView as? ImageView)?.updateButtonState()
    }
    
    private fun hideFloatingButtons() {
        try {
            floatingView?.let {
                windowManager?.removeView(it)
                floatingView = null
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
