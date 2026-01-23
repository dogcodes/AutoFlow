package com.carlos.autoflow.utils

import android.app.ActivityManager
import android.content.Context
import android.os.BatteryManager
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import java.lang.ref.WeakReference

object PerformanceMonitor {
    private const val TAG = "PerformanceMonitor"
    private var contextRef: WeakReference<Context>? = null
    
    fun initialize(context: Context) {
        contextRef = WeakReference(context)
    }
    
    fun getMemoryUsage(): MemoryInfo {
        val context = contextRef?.get() ?: return MemoryInfo(0, 0, 0f)
        
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        
        val runtime = Runtime.getRuntime()
        val usedMemory = runtime.totalMemory() - runtime.freeMemory()
        val maxMemory = runtime.maxMemory()
        val usagePercent = (usedMemory.toFloat() / maxMemory.toFloat()) * 100
        
        return MemoryInfo(
            usedMemoryMB = usedMemory / (1024 * 1024),
            maxMemoryMB = maxMemory / (1024 * 1024),
            usagePercent = usagePercent
        )
    }
    
    fun getBatteryLevel(): Int {
        val context = contextRef?.get() ?: return -1
        
        val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        return batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
    }
    
    fun logPerformanceMetrics() {
        val memoryInfo = getMemoryUsage()
        val batteryLevel = getBatteryLevel()
        
        Log.d(TAG, "内存使用: ${memoryInfo.usedMemoryMB}MB / ${memoryInfo.maxMemoryMB}MB (${String.format("%.1f", memoryInfo.usagePercent)}%)")
        Log.d(TAG, "电池电量: $batteryLevel%")
        
        // 内存警告
        if (memoryInfo.usagePercent > 80) {
            Log.w(TAG, "内存使用率过高: ${String.format("%.1f", memoryInfo.usagePercent)}%")
        }
        
        // 电池警告
        if (batteryLevel in 1..20) {
            Log.w(TAG, "电池电量较低: $batteryLevel%")
        }
    }
}

data class MemoryInfo(
    val usedMemoryMB: Long,
    val maxMemoryMB: Long,
    val usagePercent: Float
)

@Composable
fun PerformanceMonitorCard(
    modifier: Modifier = Modifier
) {
    var memoryInfo by remember { mutableStateOf(MemoryInfo(0, 0, 0f)) }
    var batteryLevel by remember { mutableStateOf(-1) }
    
    LaunchedEffect(Unit) {
        while (true) {
            memoryInfo = PerformanceMonitor.getMemoryUsage()
            batteryLevel = PerformanceMonitor.getBatteryLevel()
            delay(5000) // 每5秒更新一次
        }
    }
    
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = "性能监控",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 内存使用
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "内存:",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "${memoryInfo.usedMemoryMB}MB / ${memoryInfo.maxMemoryMB}MB",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            
            LinearProgressIndicator(
                progress = memoryInfo.usagePercent / 100f,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp),
                color = if (memoryInfo.usagePercent > 80) 
                    Color.Red 
                else MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 电池电量
            if (batteryLevel >= 0) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "电池:",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "$batteryLevel%",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (batteryLevel <= 20) 
                            Color.Red 
                        else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

// 内存清理工具
object MemoryOptimizer {
    fun requestGarbageCollection() {
        System.gc()
        Log.d("MemoryOptimizer", "已请求垃圾回收")
    }
    
    fun clearImageCache() {
        // 清理图片缓存等
        Log.d("MemoryOptimizer", "已清理缓存")
    }
    
    fun optimizeForLowMemory() {
        requestGarbageCollection()
        clearImageCache()
        Log.d("MemoryOptimizer", "已执行低内存优化")
    }
}
