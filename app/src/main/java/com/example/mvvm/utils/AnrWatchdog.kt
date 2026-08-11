package com.example.mvvm.utils

import android.app.ActivityManager
import android.app.ApplicationExitInfo
import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.MessageQueue
import androidx.annotation.RequiresApi
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.example.common.BaseApplication
import com.example.common.utils.helper.ConfigHelper.getPackageName
import com.example.common.utils.manager.AppManager
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean

/**
 * ANR Watchdog (仅用于 API < 30 兜底)
 * 低功耗特性：
 * 1) 仅在前台生效，后台自动停止，不消耗任何电量
 * 2) 基于 IdleHandler，不与主线程抢时间片
 * 3) 无独立线程，无 Timer/TimerTask，无协程轮询
 * 4) 检测到疑似 ANR 后仅记录轻量快照，不做堆栈抓取
 */
object AnrWatchdog : DefaultLifecycleObserver {
    private var lastActiveTimeMs = 0L
    private val isChecking = AtomicBoolean(false)
    private val mainHandler = Handler(Looper.getMainLooper())
    private val anrMap = ConcurrentHashMap<Long, AnrRecord>() // 用 timestamp 作为 key，避免相同阻塞时长覆盖
    private const val ANR_THRESHOLD_MS = 5000L // ANR 判定阈值
    private const val CHECK_INTERVAL_MS = 2000L // 心跳间隔（比阈值小，避免漏检）
    // 当主线程空闲时才会被调用，不会阻塞 UI
    private val idleHandler = MessageQueue.IdleHandler {
        if (!isChecking.get()) return@IdleHandler false
        lastActiveTimeMs = System.currentTimeMillis()
        // 返回 true 保持注册，持续监听
        true
    }
    // 心跳检查任务
    private val heartbeatRunnable = object : Runnable {
        override fun run() {
            if (!isChecking.get()) return
            val elapsed = System.currentTimeMillis() - lastActiveTimeMs
            if (elapsed > ANR_THRESHOLD_MS) {
                anrMap[System.currentTimeMillis()] = onSuspectedAnr(elapsed)
            }
            // 仅在前台时才继续调度下一次心跳
            if (isChecking.get()) {
                mainHandler.postDelayed(this, CHECK_INTERVAL_MS)
            }
        }
    }

    /**
     * 在 Application.onCreate() 中调用一次即可，无需手动管理生命周期，自动跟随进程前后台状态
     */
    fun install() {
        ProcessLifecycleOwner.get().let { owner ->
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
                owner.lifecycle.addObserver(this)
            } else {
                onStop(owner)
            }
        }
    }

    /**
     * 上报日志
     * @param pid 查所有进程。传具体 PID 则只查该进程；传 0 表示不限进程，返回该包名下所有历史进程的退出记录
     * @param maxNum 最多返回条数。系统按时间倒序返回最近 N 条，传 10 就是拿最近 10 次退出记录
     */
    fun submit(pid: Int = 0, maxNum: Int = 10, rsp: ((data: List<AnrRecord>) -> Unit)) {
        ProcessLifecycleOwner.get().lifecycleScope.launch(Main.immediate) {
            var data = ArrayList<AnrRecord>()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                withContext(IO) {
                    val exitInfos = (BaseApplication.instance.applicationContext.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager)?.getHistoricalProcessExitReasons(getPackageName(), pid, maxNum).orEmpty()
                    data = exitInfos.filter { it.reason == ApplicationExitInfo.REASON_ANR }.mapNotNull { onSuspectedAnr(it) }.toCollection(ArrayList())
                }
            } else {
                data = ArrayList(anrMap.values)
                anrMap.clear()
            }
            rsp.invoke(data)
        }
    }

    /**
     * App 进入前台：启动检测
     */
    override fun onStart(owner: LifecycleOwner) {
        isChecking.set(true)
        lastActiveTimeMs = System.currentTimeMillis()
        Looper.myQueue().addIdleHandler(idleHandler)
        mainHandler.postDelayed(heartbeatRunnable, CHECK_INTERVAL_MS)
    }

    /**
     * App 进入后台：彻底停止，释放所有资源
     */
    override fun onStop(owner: LifecycleOwner) {
        isChecking.set(false)
        Looper.myQueue().removeIdleHandler(idleHandler)
        mainHandler.removeCallbacks(heartbeatRunnable)
    }

    /**
     * ANR 回调
     */
    @RequiresApi(Build.VERSION_CODES.R)
    private fun onSuspectedAnr(info: ApplicationExitInfo): AnrRecord {
        return AnrRecord(
            timestamp = info.timestamp,
            processName = info.processName,
            isConfirmed = true,
            source = "SystemExitInfo",
            description = info.description,
            blockedDurationMs = getBlockedDurationMs(info),
            currentActivity = AppManager.currentActivityName
        )
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun getBlockedDurationMs(info: ApplicationExitInfo): Long {
        // 高版本权威值
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.CINNAMON_BUN) {
            info.anrInfo?.timeoutMillis?.let {
                return it
            }
        }
        // API 30-36 从 description 推断
        if (info.reason == ApplicationExitInfo.REASON_ANR) {
            val desc = info.description ?: ""
            when {
                desc.contains("Input dispatching", ignoreCase = true) -> return 5000L
                desc.contains("Broadcast", ignoreCase = true)       -> return 10000L
                desc.contains("Service", ignoreCase = true)         -> return 20000L
                desc.contains("ContentProvider", ignoreCase = true) -> return 10000L
            }
        }
        // 真拿不到
        return 0L
    }

    private fun onSuspectedAnr(blockedMs: Long): AnrRecord {
        return AnrRecord(
            timestamp = System.currentTimeMillis(),
            processName = getProcessName(),
            isConfirmed = false,
            source = "LegacyWatchdog",
            description = "Main thread blocked for ${blockedMs}ms",
            blockedDurationMs = blockedMs,
            currentActivity = AppManager.currentActivityName
        )
    }

    /**
     * 获取当前进程名
     */
    private fun getProcessName(): String {
        return try {
            File("/proc/self/cmdline").readText().trimEnd('\u0000')
        } catch (e: Exception) {
            e.printStackTrace()
            // 极端兜底：取包名作为默认值
            "unknown"
        }
    }

}

/**
 * 统一的 ANR 记录模型
 */
data class AnrRecord(
    // 发生时间戳 (毫秒)
    val timestamp: Long? = null,
    // 进程名 (如 "com.example.app" 或 "com.example.app:push")
    val processName: String? = null,
    /**
     * 是否为系统确认的真实 ANR。
     * - true: 高版本 ApplicationExitInfo 确认的系统级 ANR
     * - false: 低版本 Watchdog 检测到的"疑似"主线程阻塞
     */
    val isConfirmed: Boolean? = null,
    // 数据来源标识，用于埋点区分 (如 "SystemExitInfo", "LegacyWatchdog")
    val source: String? = null,
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