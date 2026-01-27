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

class FloatingStopService : Service() {
    
    private var windowManager: WindowManager? = null
    private var floatingView: View? = null
    
    companion object {
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
            context.startService(Intent(context, FloatingStopService::class.java))
        }
        
        fun updateExecutionState(executing: Boolean) {
            isExecuting = executing
        }
        
        fun stop(context: Context) {
            context.stopService(Intent(context, FloatingStopService::class.java))
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
        if (canDrawOverlays(this)) {
            showFloatingButtons()
        } else {
            stopSelf()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
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
            
            val layout = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
            }
            
            val startButton = ImageView(this).apply {
                setImageResource(android.R.drawable.ic_media_play)
                setBackgroundResource(android.R.drawable.btn_default)
                setPadding(15, 15, 15, 15)
                setOnClickListener {
                    if (!isExecuting) {
                        startCallback?.invoke()
                        isExecuting = true
                    }
                }
            }
            
            val stopButton = ImageView(this).apply {
                setImageResource(android.R.drawable.ic_media_pause)
                setBackgroundResource(android.R.drawable.btn_default)
                setPadding(15, 15, 15, 15)
                setOnClickListener {
                    if (isExecuting) {
                        stopCallback?.invoke()
                        isExecuting = false
                    }
                }
            }
            
            layout.addView(startButton)
            layout.addView(stopButton)
            
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
                gravity = Gravity.TOP or Gravity.END
                x = 50
                y = 200
            }
            
            floatingView = layout
            windowManager?.addView(floatingView, params)
        } catch (e: Exception) {
            e.printStackTrace()
            stopSelf()
        }
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
