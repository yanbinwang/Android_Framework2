package com.example.debugging.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.common.config.ServerConfig
import com.example.debugging.activity.LogActivity
import com.example.framework.utils.function.value.currentTimeStamp
import com.example.framework.utils.logE
import com.example.thirdparty.utils.NotificationUtil.builder
import com.example.thirdparty.utils.NotificationUtil.getPendingIntent
import com.example.thirdparty.utils.NotificationUtil.getPendingIntentFlags
import com.example.thirdparty.utils.NotificationUtil.notify
import com.zxy.recovery.callback.RecoveryCallback
import com.zxy.recovery.core.Recovery


/**
 * 调试库
 */
@SuppressLint("StaticFieldLeak")
object DebuggingUtil {
    private var notificationBuilder: NotificationCompat.Builder? = null

    /**
     * 调用前做好isDebug的判断
     * clazz->MainActivity::class.java
     */
    @JvmStatic
    fun init(context: Context, clazz: Class<out Activity>) {
        //闪退抓捕->不能和LeakCanary共存
        Recovery.getInstance()
            //debug	是否开启debug模式
            .debug(true)
            //recoverInBackground 当应用在后台时发生Crash，是否需要进行恢复
            .recoverInBackground(false)
            //recoverStack	是否恢复整个Activity Stack，否则将恢复栈顶Activity
            .recoverStack(true)
            //mainPage	回退的界面
            .mainPage(clazz)
            //callback	发生Crash时的回调
            .recoverEnabled(true)//发布版本不跳转
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
                }
            })
            //silent	SilentMode	是否使用静默恢复，如果设置为true的情况下，那么在发生Crash时将不显示RecoveryActivity界面来进行恢复，而是自动的恢复Activity的堆栈和数据，也就是无界面恢复
            .silent(false, Recovery.SilentMode.RECOVER_ACTIVITY_STACK)
//                .skip(TestActivity.class)
            .init(context)
//        //LeakCanary 会增加应用的内存和性能开销
//        // 创建 LeakCanary 配置
//        val config = LeakCanary.Config(
//            dumpHeap = true, // 是否在检测到内存泄漏时转储堆
//            retainedVisibleThreshold = 5// 保留对象的可见阈值
//        )
//        // 应用配置
//        LeakCanary.config = config
//        // 启动 LeakCanary 显示 LeakCanary 图标
//        LeakCanary.showLeakDisplayActivityLauncherIcon(true)
        //服务器工具类初始化
        ServerUtil.init()
        //开启一个常驻通知
        val intent = Intent(context, LogActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        val pendingIntent = context.getPendingIntent(-1, intent, getPendingIntentFlags(PendingIntent.FLAG_CANCEL_CURRENT))
        notificationBuilder = context.builder(
            title = ServerConfig.serverName(),
            text = "本程序包为 " + ServerConfig.serverName() + " 包",
            autoCancel = false,
            pendingIntent = pendingIntent
        )
        notificationBuilder?.setDefaults(Notification.DEFAULT_ALL)?.setWhen(currentTimeStamp)
        val notification = notificationBuilder?.build()
        notification?.flags = Notification.FLAG_ONGOING_EVENT
        notify(2, notification)
    }

    fun updateNotificationContent(newContentText: String) {
        notificationBuilder?.setContentText(newContentText)
        val updatedNotification = notificationBuilder?.build()
        updatedNotification?.flags = Notification.FLAG_ONGOING_EVENT
        notify(2, updatedNotification)
    }

}