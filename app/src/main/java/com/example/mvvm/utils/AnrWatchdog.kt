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
import com.example.common.utils.StorageUtil.getStoragePath
import com.example.common.utils.function.getAllFilePathsRecursively
import com.example.common.utils.function.safeDelete
import com.example.common.utils.helper.ConfigHelper.getPackageName
import com.example.common.utils.manager.AppManager
import com.example.framework.utils.function.value.DateFormat.CN_YMDHMS
import com.example.framework.utils.function.value.convert
import com.example.framework.utils.function.value.currentTimeStamp
import com.example.framework.utils.function.value.toNewList
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileWriter
import java.io.IOException
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

    fun writeANRReport(logContent: String) {
        try {
            // 获取存储路径（优先使用应用内部存储，避免权限问题）
            val logDir = File(getStoragePath("超时日志", false))
            if (!logDir.exists()) {
                logDir.mkdirs()
            }
            // 日志文件名（以时间命名）
            val fileName = "anr_${CN_YMDHMS.convert(currentTimeStamp)}.txt"
            val logFile = File(logDir, fileName)
            // 写入日志
            FileWriter(logFile, true).use { writer ->
                writer.write(logContent)
                writer.flush()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * @param pid 查所有进程。传具体 PID 则只查该进程；传 0 表示不限进程，返回该包名下所有历史进程的退出记录
     * @param maxNum 最多返回条数。系统按时间倒序返回最近 N 条，传 10 就是拿最近 10 次退出记录
     */
    fun fetchCrashFiles(logDirPath: String? = getStoragePath("超时日志", false), rsp: ((data: List<File>) -> Unit)) {
        logDirPath ?: return rsp.invoke(emptyList())
        val logDir = File(logDirPath)
        if (!logDir.exists()) {
            logDir.mkdirs()
            return rsp.invoke(emptyList())
        }
        if (!logDir.isDirectory) {
            return rsp.invoke(emptyList())
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            ProcessLifecycleOwner.get().lifecycleScope.launch(Main.immediate) {
                withContext(IO) {
                    // 获取系统记录的最新 10 条 ANR 数据
                    val anrEntries = try {
                        (BaseApplication.instance.applicationContext.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager)?.getHistoricalProcessExitReasons(getPackageName(), 0, 10).orEmpty()
                    } catch (e: Exception) {
                        e.printStackTrace()
                        emptyList()
                    }.filter { it.reason == ApplicationExitInfo.REASON_ANR }.mapNotNull { onSuspectedAnr(it) }
                    // 取得本地存放 ANR 文件目录下的所有 txt 文件路径
                    val anrCaches = logDir.getAllFilePathsRecursively()
                    // 提取本地已有文件的 uniqueKey 集合（用于 O(1) 查重）
                    val localKeySet = anrCaches.mapNotNull { path -> extractUniqueKey(File(path)) }.toHashSet()
                    // 差集比对：仅对本地不存在的记录调用 writeANRReport
                    anrEntries.forEach { record ->
                        if (!localKeySet.contains(record.uniqueKey)) {
                            // 仅在确认需要写入时，才调用 buildANRContent 生成字符串
                            val content = buildANRContent(record)
                            writeANRReport(content)
                            localKeySet.add(record.uniqueKey)
                        }
                    }
                    // 再次获取并返回
                    val anrFiles = logDir.getAllFilePathsRecursively().toNewList { File(it) }
                    withContext(Main) {
                        rsp.invoke(anrFiles)
                    }
                }
            }
        } else {
            rsp.invoke(logDir.listFiles { file ->
                file.isFile && file.name.endsWith(".txt", ignoreCase = true) && isNonEmptyFile(file)
            }?.sortedBy { it.lastModified() } ?: emptyList())
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
        val (threadName, threadId) = Thread.currentThread().let { it.name to (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA) it.threadId() else it.id) }
        return AnrRecord(
            timestamp = info.timestamp,
            source = "SystemExitInfo",
            processName = info.processName,
            threadName = threadName,
            threadId = threadId,
            blockedDurationMs = getBlockedDurationMs(info),
            currentActivity = AppManager.currentActivityName,
            description = info.description
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
        val (threadName, threadId) = Thread.currentThread().let { it.name to (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA) it.threadId() else it.id) }
        return AnrRecord(
            timestamp = System.currentTimeMillis(),
            source = "LegacyWatchdog",
            processName = getProcessName(),
            threadName = threadName,
            threadId = threadId,
            blockedDurationMs = blockedMs,
            currentActivity = AppManager.currentActivityName,
            description = "Main thread blocked for ${blockedMs}ms"
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

    /**
     * 构建返回文本
     */
    private fun buildANRContent(data: AnrRecord): String {
        return buildString {
            append("===== ANR 时间: ${data.timestamp} =====\n")
            append("设备型号: ${Build.MODEL}\n")
            append("系统版本: Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})\n")
            append("数据来源: ${data.source}\n")
            append("崩溃进程: ${data.processName}\n")
            append("崩溃线程: ${data.threadName} (id: ${data.threadId})\n")
            append("主线程阻塞时长: ${data.blockedDurationMs}\n")
            append("顶层页面名称: ${data.currentActivity}\n")
            append("===== 描述信息 =====\n")
            append("${data.description}")
            append("\n===== 日志结束 =====\n\n")
        }
    }

    /**
     * 从本地 ANR txt 文件中提取 uniqueKey
     * 返回 null 表示文件格式异常或关键字段缺失
     */
    private fun extractUniqueKey(file: File): String? {
        var timestamp: String? = null
        var processName: String? = null
        file.useLines { lines ->
            for (line in lines) {
                when {
                    line.startsWith("===== ANR 时间:") -> {
                        // 截取 ": " 之后、" =====" 之前的内容
                        timestamp = line.substringAfter(": ", "").substringBefore(" =====").trim()
                    }
                    line.startsWith("崩溃进程:") -> {
                        processName = line.substringAfter(": ", "").trim()
                    }
                }
                // 两个字段都拿到了就提前退出，不用读完整个文件
                if (timestamp != null && processName != null) break
            }
        }
        return if (!timestamp.isNullOrEmpty() && !processName.isNullOrEmpty()) {
            "${timestamp}_${processName}_ANR"
        } else {
            null
        }
    }

    private fun isNonEmptyFile(file: File): Boolean {
        if (file.length() == 0L) {
            file.safeDelete()
            return false
        }
        return try {
            file.bufferedReader().use { reader ->
                val hasValidContent = reader.lineSequence()
                    .take(10)
                    .any { line -> line.isNotBlank() }
                if (!hasValidContent) {
                    file.safeDelete()
                }
                hasValidContent
            }
        } catch (e: IOException) {
            e.printStackTrace()
            file.safeDelete()
            false
        } catch (e: SecurityException) {
            e.printStackTrace()
            false
        }
    }

}

/**
 * 统一的 ANR 记录模型
 */
data class AnrRecord(
    // 发生时间戳 (毫秒)
    val timestamp: Long? = null,
    // 数据来源标识，用于埋点区分 (如 "SystemExitInfo", "LegacyWatchdog")
    val source: String? = null,
    // 进程名 (如 "com.example.app" 或 "com.example.app:push")
    val processName: String? = null,
    // 崩溃线程
    val threadName: String? = null,
    // 崩溃线程 id
    val threadId: Long? = null,
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
    val currentActivity: String? = null,
    // ANR 描述信息。高版本取系统 description，低版本可填 "Main thread blocked > 5s"
    val description: String? = null
) {
    /**
     * 跨数据源的统一去重键
     * 低版本写入 txt 时也以此作为文件名/首行标识
     * 高版本读取系统记录后，用此 key 与已上传集合比对
     */
    val uniqueKey: String
        get() = "${timestamp}_${processName}_ANR"
}