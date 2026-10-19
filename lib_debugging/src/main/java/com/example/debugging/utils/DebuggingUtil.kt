package com.example.debugging.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.Build
import com.example.common.utils.builder.writeCrashReport
import com.example.framework.utils.function.value.currentTimeStamp
import com.example.framework.utils.logE
import com.zxy.recovery.callback.RecoveryCallback
import com.zxy.recovery.core.Recovery

/**
 * 调试库
 */
@SuppressLint("StaticFieldLeak")
object DebuggingUtil {

    /**
     * 调用前做好 isDebug 的判断
     * clazz -> MainActivity::class.java
     */
    @JvmStatic
    fun init(context: Context, clazz: Class<out Activity>) {
        // 闪退抓捕 -> 不能和LeakCanary共存
        Recovery.getInstance()
            // 是否开启debug模式
            .debug(true)
            // 当应用在后台时发生Crash，是否需要进行恢复
            .recoverInBackground(false)
            // 是否恢复整个Activity Stack，否则将恢复栈顶Activity
            .recoverStack(true)
            // 回退的界面
            .mainPage(clazz)
            // 发布版本不跳转
            .recoverEnabled(true)
            // 发生Crash时的回调
            .callback(object : RecoveryCallback {
                private val infoList = mutableListOf<String>()
                override fun stackTrace(stackTrace: String?) {
                    infoList.add("StackTrace:\n$stackTrace\n\n")
                }

                override fun cause(cause: String?) {
                    infoList.add("Cause:\n$cause\n\n")
                }

                override fun exception(throwExceptionType: String?, throwClassName: String?, throwMethodName: String?, throwLineNumber: Int) {
                    infoList.add("\nException:\nExceptionData{" +
                            "className='" + throwClassName + '\'' +
                            ", type='" + throwExceptionType + '\'' +
                            ", methodName='" + throwMethodName + '\'' +
                            ", lineNumber=" + throwLineNumber +
                            '}'
                    )
                }

                override fun throwable(throwable: Throwable?) {
                    val report = StringBuilder()
                    infoList.reverse()
                    infoList.forEach {
                        report.append(it)
                    }
                    infoList.clear()
                    ("————————————————————————应用崩溃————————————————————————" +
                            "${report}\n" +
                            " ").logE("LoggingInterceptor")
//                    // 构建日志内容（包含设备信息和异常信息）
//                    val (threadName, threadId) = Thread.currentThread().let { it.name to (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA) it.threadId() else it.id) }
//                    // 捕获异常并生成日志
//                    val crashLog = buildString {
//                        append("===== 崩溃时间: $currentTimeStamp =====\n")
//                        append("设备型号: ${Build.MODEL}\n")
//                        append("系统版本: Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})\n")
//                        append("崩溃线程: ${threadName} (id: ${threadId})\n")
//                        append("===== 异常信息 =====\n")
//                        append(report)
//                        append("\n===== 日志结束 =====\n\n")
//                    }
//                    // 保存日志到本地文件
//                    writeCrashReport(crashLog)
                }
            })
            // 是否使用静默恢复，如果设置为true的情况下，那么在发生Crash时将不显示RecoveryActivity界面来进行恢复，而是自动的恢复Activity的堆栈和数据，也就是无界面恢复
            .silent(false, Recovery.SilentMode.RECOVER_ACTIVITY_STACK)
//            .skip(TestActivity.class)
            .init(context)
//        // LeakCanary 会增加应用的内存和性能开销
//        // 创建 LeakCanary 配置
//        val config = LeakCanary.Config(
//            dumpHeap = true, // 是否在检测到内存泄漏时转储堆
//            retainedVisibleThreshold = 5 // 保留对象的可见阈值
//        )
//        // 应用配置
//        LeakCanary.config = config
//        // 启动 LeakCanary 显示 LeakCanary 图标
//        LeakCanary.showLeakDisplayActivityLauncherIcon(true)
    }

}