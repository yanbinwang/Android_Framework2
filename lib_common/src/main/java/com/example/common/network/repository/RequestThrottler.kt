package com.example.common.network.repository

import com.example.framework.utils.logWTF

/**
 * 控制网络请求频率，确保请求间隔不小于指定时间
 * @param minIntervalMs 最小请求间隔（毫秒），默认5000ms（5秒）
 * @param toleranceMs 时间容忍度（毫秒），默认100ms，应对时间源微小偏差
 */
class RequestThrottler(private val minIntervalMs: Long = 5000L, private val toleranceMs: Long = 100L) {
    // 上次请求的时间戳（纳秒，用于相对时间计算）
    private var lastRequestNano = 0L
    // 上次请求的时间戳（毫秒，用于绝对时间校验）
    private var lastRequestMs = 0L
    // 最小间隔的纳秒值（由毫秒转换而来）
    private val minIntervalNano = minIntervalMs * 1_000_000L

    /**
     * 尝试发起请求
     * @return true：发起请求；false：间隔不足，不发起请求
     */
    fun tryRequest(performRequest: () -> Unit = {}): Boolean {
        val currentNano = System.nanoTime()
        val currentMs = System.currentTimeMillis()
        val nanoDiff = currentNano - lastRequestNano
        val msDiff = currentMs - lastRequestMs
        // 首次请求直接放行
        if (lastRequestNano == 0L) {
            updateLastRequestTime(currentNano, currentMs)
            performRequest()
            return true
        }
        // 处理系统时间回溯（用户修改时间到过去）
        if (msDiff < 0) {
            return handleTimeBackwardCase(currentNano, currentMs, nanoDiff, performRequest)
        }
        // 正常时间下的判断逻辑
        val isNanoReady = nanoDiff >= minIntervalNano
        val isMsTooSoon = msDiff < (minIntervalMs - toleranceMs)
        return if (isNanoReady && !isMsTooSoon) {
            updateLastRequestTime(currentNano, currentMs)
            performRequest()
            true
        } else {
            logBlockedRequest(nanoDiff, msDiff)
            false
        }
    }

    /**
     * 处理系统时间回溯的情况
     */
    private fun handleTimeBackwardCase(currentNano: Long, currentMs: Long, nanoDiff: Long, performRequest: () -> Unit = {}): Boolean {
        return if (nanoDiff >= minIntervalNano) {
            updateLastRequestTime(currentNano, currentMs)
            performRequest()
            true
        } else {
            // -1表示时间回溯
            logBlockedRequest(nanoDiff, msDiff = -1)
            false
        }
    }

    /**
     * 更新上次请求时间
     */
    private fun updateLastRequestTime(nano: Long, ms: Long) {
        lastRequestNano = nano
        lastRequestMs = ms
    }

    /**
     * 记录被拦截的请求日志
     */
    private fun logBlockedRequest(nanoDiff: Long, msDiff: Long) {
        val intervalSec = minIntervalMs / 1000.0
        val nanoSec = nanoDiff / 1_000_000_000.0
        val msSec = if (msDiff >= 0) msDiff / 1000.0 else -1.0
        "请求被拦截（需间隔≥${intervalSec}秒）：" +
                "相对间隔=${nanoSec}秒，" +
                "实际间隔=${if (msSec >= 0) msSec else "时间回溯"}秒".logWTF("RequestThrottle")
    }

    /**
     * 重置计时，允许立即发起下一次请求
     */
    fun reset() {
        lastRequestNano = 0L
        lastRequestMs = 0L
    }

}