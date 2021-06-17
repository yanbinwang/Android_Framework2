package com.example.common.utils

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.text.TextUtils
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.common.BaseApplication
import com.example.common.R
import com.example.common.constant.Constants
import java.lang.ref.WeakReference

/**
 * author:wyb
 * 通知工具类
 */
class NotificationFactory private constructor() {
    private val context by lazy { BaseApplication.instance?.applicationContext }
    private val notificationManager by lazy { context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager }
    private val builder by lazy { NotificationCompat.Builder(context!!, Constants.PUSH_CHANNEL_ID) }

    companion object {
        @JvmStatic
        val instance: NotificationFactory by lazy {
            NotificationFactory()
        }
    }

    /**
     * 构建常规通知栏
     */
    fun normal(title: String, text: String, smallIcon: Int, largeIcon: Int, intent: Intent? = null, id: String = "") {
        builder.apply {
            color = ContextCompat.getColor(context!!, R.color.black)//6.0提示框白色小球的颜色
            setTicker(title)//状态栏显示的提示
            setContentTitle(title)//通知栏标题
            setContentText(text)//通知正文
            setAutoCancel(true)//可以点击通知栏的删除按钮删除
            setSmallIcon(smallIcon)//状态栏显示的小图标
            setLargeIcon(BitmapFactory.decodeResource(context?.resources, largeIcon))//状态栏下拉显示的大图标
            setDefaults(NotificationCompat.DEFAULT_ALL)
            setContentIntent(PendingIntent.getActivity(context, 1, intent ?: Intent(), PendingIntent.FLAG_ONE_SHOT))//intent为空说明此次为普通推送
        }
        val notification = builder.build()
        createChannel()
        notificationManager.notify(if (TextUtils.isEmpty(id)) 0 else id.hashCode(), notification)
    }

    /**
     * 构建进度条通知栏
     */
    fun progress(progress: Int, title: String, text: String, smallIcon: Int, largeIcon: Int, id: String = "") {
        builder.apply {
            color = ContextCompat.getColor(context!!, R.color.black)//6.0提示框白色小球的颜色
            setProgress(100, progress, false)
            setTicker(title)//状态栏显示的提示
            setContentTitle(title)//通知栏标题
            setContentText(text)//通知正文
            setAutoCancel(true)//可以点击通知栏的删除按钮删除
            setSmallIcon(smallIcon)//状态栏显示的小图标
            setLargeIcon(BitmapFactory.decodeResource(context?.resources, largeIcon))//状态栏下拉显示的大图标
            setWhen(System.currentTimeMillis())
        }
        val notification = builder.build()
        notification?.flags = Notification.FLAG_AUTO_CANCEL or Notification.FLAG_ONLY_ALERT_ONCE
        createChannel()
        notificationManager.notify(id.toInt(), notification)
    }

    /**
     * 获取渠道
     * 8.0+系统需要创建一个推送渠道
     */
    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) notificationManager.createNotificationChannel(NotificationChannel(Constants.PUSH_CHANNEL_ID, Constants.PUSH_CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH))
    }

    /**
     * 跳转通知的设置界面
     */
    fun setting(activity: Activity) {
        val weakActivity = WeakReference(activity)
        val intent = Intent()
        when {
            //8.0+
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> {
                intent.action = "android.settings.APP_NOTIFICATION_SETTINGS"
                intent.putExtra("android.provider.extra.APP_PACKAGE", Constants.APPLICATION_ID)
            }
            //5.0-7.0
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP -> {
                intent.action = "android.settings.APP_NOTIFICATION_SETTINGS"
                intent.putExtra("app_package", Constants.APPLICATION_ID)
                intent.putExtra("app_uid", weakActivity.get()?.applicationInfo?.uid)
            }
            //其他
            else -> {
                intent.action = "android.settings.APPLICATION_DETAILS_SETTINGS"
                intent.data = Uri.fromParts("package", Constants.APPLICATION_ID, null)
            }
        }
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        weakActivity.get()?.startActivity(intent)
    }

    /**
     * 判断当前是否开启通知，方便用户接受推送消息
     */
    fun isEnabled(context: Context): Boolean {
        return try {
            NotificationManagerCompat.from(context).areNotificationsEnabled()
        } catch (e: Exception) {
            false
        }
    }

}