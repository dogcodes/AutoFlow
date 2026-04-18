package com.carlos.autoflow.utils

import android.content.Context
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.SystemClock
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import kotlin.math.abs

/**
 * 可信时间提供者。
 * 通过网络时间 + elapsedRealtime 锚定，防止用户修改系统时间绕过授权。
 *
 * 使用前调用 [configure] 注入时间源 URL（第一个优先，其余 fallback）。
 * 调用方负责传入 SharedPreferences，本类不持有任何业务依赖。
 */
object TrustedTimeProvider {

    private const val KEY_TRUSTED_NETWORK_TIME = "trusted_network_time"
    private const val KEY_TRUSTED_ELAPSED_TIME = "trusted_elapsed_time"

    /** 可信时间缓存有效期，窗口内复用缓存避免频繁请求 */
    private const val TRUSTED_TIME_CACHE_MILLIS = 23L * 60 * 60 * 1000

    /** 系统时间与可信时间允许的最大偏差，超过则视为时间被篡改 */
    private const val TIME_INVALID_THRESHOLD_MILLIS = 10L * 60 * 1000

    private var timeUrls: List<String> = emptyList()

    /**
     * 初始化时间源 URL 列表，第一个为主源，其余为 fallback。
     * 建议第一个传自己的 backend（如 Cloudflare Worker /time 接口），
     * 后续传公共 URL 作为兜底。
     */
    fun configure(urls: List<String>) {
        timeUrls = urls
    }

    /** 预取并缓存可信时间，23小时内有缓存则跳过，返回是否有有效可信时间 */
    fun prefetch(prefs: SharedPreferences): Boolean {
        val cached = getTrustedTimeSnapshot(prefs)
        if (cached != null) {
            val cachedElapsed = prefs.getLong(KEY_TRUSTED_ELAPSED_TIME, 0L)
            val age = SystemClock.elapsedRealtime() - cachedElapsed
            if (age <= TRUSTED_TIME_CACHE_MILLIS) return true  // 缓存有效，跳过请求
        }
        return sync(prefs) != null
    }

    /**
     * 重启后如有网络则重新同步，返回是否执行了同步。
     * 建议在 AccessibilityService.onCreate() 调用。
     */
    fun prefetchAfterRebootIfNeeded(context: Context, prefs: SharedPreferences): Boolean {
        if (!hasRebootedSinceLastSync(prefs)) return false
        if (!isNetworkAvailable(context)) return false
        return sync(prefs) != null
    }

    /**
     * 激活时获取可信时间：优先复用缓存，缓存过期则重新同步。
     * 返回 null 表示无网络且无有效缓存。
     */
    fun getTrustedTimeForActivation(prefs: SharedPreferences): Long? {
        val cached = getTrustedTimeSnapshot(prefs)
        if (cached != null) {
            val cachedElapsed = prefs.getLong(KEY_TRUSTED_ELAPSED_TIME, 0L)
            val age = SystemClock.elapsedRealtime() - cachedElapsed
            if (age <= TRUSTED_TIME_CACHE_MILLIS) return cached
        }
        return sync(prefs)
    }

    /**
     * 根据缓存的网络时间 + elapsedRealtime 推算当前可信时间。
     * 重启后 elapsedRealtime 重置，此时返回 null。
     */
    fun getTrustedTimeSnapshot(prefs: SharedPreferences): Long? {
        val networkTime = prefs.getLong(KEY_TRUSTED_NETWORK_TIME, 0L)
        val elapsedAtSync = prefs.getLong(KEY_TRUSTED_ELAPSED_TIME, 0L)
        val currentElapsed = SystemClock.elapsedRealtime()
        if (networkTime <= 0L || elapsedAtSync <= 0L || currentElapsed < elapsedAtSync) return null
        return networkTime + (currentElapsed - elapsedAtSync)
    }

    /** 系统时间与可信时间偏差是否超过阈值 */
    fun isSystemTimeInvalid(trustedTimeMillis: Long): Boolean =
        abs(System.currentTimeMillis() - trustedTimeMillis) > TIME_INVALID_THRESHOLD_MILLIS

    /** 当前时间是否比上次记录时间早（回拨检测） */
    fun isTimeRolledBack(previousWallTimeMillis: Long, currentWallTimeMillis: Long): Boolean =
        currentWallTimeMillis + TIME_INVALID_THRESHOLD_MILLIS < previousWallTimeMillis

    /** elapsedRealtime 比上次同步时记录的小，说明发生了重启 */
    fun hasRebootedSinceLastSync(prefs: SharedPreferences): Boolean {
        val elapsedAtSync = prefs.getLong(KEY_TRUSTED_ELAPSED_TIME, 0L)
        if (elapsedAtSync <= 0L) return false
        return SystemClock.elapsedRealtime() < elapsedAtSync
    }

    fun isNetworkAvailable(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return false
        val caps = cm.getNetworkCapabilities(cm.activeNetwork) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    fun forceSync(prefs: SharedPreferences): Boolean = sync(prefs) != null

    // ---- private ----

    private fun sync(prefs: SharedPreferences): Long? {
        val networkTime = fetchNetworkTime() ?: return null
        val elapsed = SystemClock.elapsedRealtime()
        prefs.edit()
            .putLong(KEY_TRUSTED_NETWORK_TIME, networkTime)
            .putLong(KEY_TRUSTED_ELAPSED_TIME, elapsed)
            .apply()
        return networkTime
    }

    private fun fetchNetworkTime(): Long? {
        for (url in timeUrls) {
            try {
                val t = fetchFromUrl(url)
                if (t != null && t > 0L) return t
            } catch (_: Exception) {}
        }
        return null
    }

    private fun fetchFromUrl(urlStr: String): Long? {
        val conn = (URL(urlStr).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 5000
            readTimeout = 5000
            instanceFollowRedirects = true
            useCaches = false
        }
        return try {
            conn.connect()
            // 优先尝试解析 JSON body（自有 backend /time 接口返回 {"t": timestamp}）
            val contentType = conn.contentType ?: ""
            if (contentType.contains("application/json")) {
                val body = conn.inputStream.bufferedReader().readText()
                JSONObject(body).optLong("t", 0L).takeIf { it > 0L }
            } else {
                // fallback：从 HTTP Date header 读取
                conn.date.takeIf { it > 0L }
                    ?: conn.getHeaderFieldDate("Date", 0L).takeIf { it > 0L }
            }
        } finally {
            conn.disconnect()
        }
    }
}
