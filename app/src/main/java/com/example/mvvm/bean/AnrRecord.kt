package com.example.mvvm.bean

/**
 * 统一的 ANR 记录模型
 */
data class AnrRecord(
    // 发生时间戳 (毫秒)
    val timestamp: Long,
    // 进程名 (如 "com.example.app" 或 "com.example.app:push")
    val processName: String,
    /**
     * 是否为系统确认的真实 ANR。
     * - true: 高版本 ApplicationExitInfo 确认的系统级 ANR
     * - false: 低版本 Watchdog 检测到的"疑似"主线程阻塞
     */
    val isConfirmed: Boolean,
    // 数据来源标识，用于埋点区分 (如 "SystemExitInfo", "LegacyWatchdog")
    val source: String,
    // ANR 描述信息。高版本取系统 description，低版本可填 "Main thread blocked > 5s"
    val description: String? = null,
    /**
     * 主线程阻塞时长 (毫秒)。
     * - 低版本 Watchdog 直接提供
     * - 高版本通常拿不到精确值（除非自己去解析 trace），默认 null
     */
    val blockedDurationMs: Long? = null,
    /**
     * 发生 ANR 时的顶层 Activity 名称。
     * - 低版本 Watchdog 容易拿到
     * - 高版本系统不提供此字段，需从 trace 解析或留空
     */
    val currentActivity: String? = null
)