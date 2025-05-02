package com.example.debugging.utils

import android.app.Activity
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.common.config.ServerConfig
import com.example.common.network.interceptor.LoggingInterceptor
import com.example.common.utils.function.string
import com.example.debugging.R
import com.example.debugging.activity.LogActivity
import com.example.debugging.bean.RequestBean
import com.example.framework.utils.function.value.currentTimeStamp
import com.example.framework.utils.function.value.safeSize
import com.example.thirdparty.utils.NotificationUtil.getPendingIntentFlags
import com.zxy.recovery.core.Recovery
import leakcanary.LeakCanary
import java.util.Date


/**
 * 调试库
 */
object DebuggingUtil {
    /**
     * 网络请求列表
     */
    internal val requestList by lazy { ArrayList<RequestBean>() }

    /**
     * 调用前做好isDebug的判断
     * clazz->MainActivity::class.java
     */
    @JvmStatic
    fun init(context: Context, clazz: Class<out Activity>) {
        //声明异常报错抓取
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
//                .callback(new MyCrashCallback())
            //silent	SilentMode	是否使用静默恢复，如果设置为true的情况下，那么在发生Crash时将不显示RecoveryActivity界面来进行恢复，而是自动的恢复Activity的堆栈和数据，也就是无界面恢复
            .silent(false, Recovery.SilentMode.RECOVER_ACTIVITY_STACK)
//                .skip(TestActivity.class)
            .init(context)
        //LeakCanary 会增加应用的内存和性能开销
        // 创建 LeakCanary 配置
        val config = LeakCanary.Config(
            dumpHeap = true, // 是否在检测到内存泄漏时转储堆
            retainedVisibleThreshold = 5// 保留对象的可见阈值
        )
        // 应用配置
        LeakCanary.config = config
        // 启动 LeakCanary 显示 LeakCanary 图标
        LeakCanary.showLeakDisplayActivityLauncherIcon(true)
        //开启一个常驻通知
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        val intent = Intent(context, LogActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        val pendingIntent = PendingIntent.getActivity(context, -1, intent, getPendingIntentFlags(PendingIntent.FLAG_CANCEL_CURRENT))
        val notification = NotificationCompat.Builder(context, string(R.string.notificationChannelId))
            .setSmallIcon(R.mipmap.ic_notification)
            .setContentTitle(ServerConfig.serverName())
            .setContentText("本程序包为 " + ServerConfig.serverName() + " 包")
            .setAutoCancel(false)
            .setDefaults(Notification.DEFAULT_ALL)
            .setWhen(currentTimeStamp)
            .setContentIntent(pendingIntent)
            .build()
        notification.flags = Notification.FLAG_ONGOING_EVENT
        manager?.notify(2, notification)
        //请求拦截地址,只保留最近30个请求
        LoggingInterceptor.setOnInterceptorListener { url, method, header, body, code, response ->
            addRequest(url, method, header, body, code, response)
        }
    }

    /**
     * 因为debugging是继承自lib_thirdparty的，而lib_thirdparty继承common，
     * 所有的三方库以及主库都是能添加的，如有需要，可在三方库里扩展回调，debugging里获取并添加
     */
    private fun addRequest(url: String? = null, method: String? = null, header: String? = null, body: String? = null, code: Int? = null, response: String? = null) {
        val bean = RequestBean(url, method, header, body, Date().time, code, response)
        // 在列表头部插入元素
        requestList.add(0, bean)
        // 检查列表长度是否超过 30
        if (requestList.safeSize > 30) {
            // 截取前 30 个元素
            val newList = ArrayList<RequestBean>(requestList.subList(0, 30))
            // 替换原列表
            requestList.clear()
            requestList.addAll(newList)
        }
    }

}