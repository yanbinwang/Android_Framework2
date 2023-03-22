package com.example.common.utils

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.common.BaseApplication
import com.example.common.R
import com.example.common.config.Constants
import com.example.common.utils.function.color
import com.example.framework.utils.function.value.toSafeInt

/**
 * @description 通知工具类
 * @author yan
 */
class NotificationFactory private constructor() {
    private val context by lazy { BaseApplication.instance.applicationContext }
    private val manager by lazy { context?.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager }
    private val builder by lazy { NotificationCompat.Builder(context, Constants.PUSH_CHANNEL_ID) }

    companion object {
        val instance by lazy { NotificationFactory() }
        /**
         * 推送id自增长，避免相同id之前的通知被顶掉
         */
        private var notificationId = 100
            get() {
                return ++field
            }
        /**
         * 返回request自增长，避免拉起的页面取得值会是之前历史页面的值
         */
        private var requestCode = 100
            get() {
                return ++field
            }
    }

    /**
     * 构建常规通知栏
     */
    fun normal(title: String, text: String, smallIcon: Int, largeIcon: Int, intent: Intent? = Intent(), id: String = "") {
        builder.apply {
            color = color(R.color.black)//6.0提示框白色小球的颜色
            setTicker(title)//状态栏显示的提示
            setContentTitle(title)//通知栏标题
            setContentText(text)//通知正文
            setAutoCancel(true)//可以点击通知栏的删除按钮删除
            setSmallIcon(smallIcon)//状态栏显示的小图标
            setLargeIcon(BitmapFactory.decodeResource(context?.resources, largeIcon))//状态栏下拉显示的大图标
            setDefaults(NotificationCompat.DEFAULT_ALL)
            setContentIntent(PendingIntent.getActivity(context, requestCode, intent, getPendingIntentFlags(PendingIntent.FLAG_UPDATE_CURRENT)))//intent为空说明此次为普通推送
        }
        manager?.apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) createNotificationChannel(NotificationChannel(Constants.PUSH_CHANNEL_ID, Constants.PUSH_CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH))
            notify(if (id.isEmpty()) notificationId else id.hashCode(), builder.build())
        }
    }

    /**
     * 构建进度条通知栏
     */
    fun progress(progress: Int, title: String, text: String, smallIcon: Int, largeIcon: Int, id: String) {
        builder.apply {
            color = color(R.color.black)//6.0提示框白色小球的颜色
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
        notification.flags = Notification.FLAG_AUTO_CANCEL or Notification.FLAG_ONLY_ALERT_ONCE
        manager?.apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) createNotificationChannel(NotificationChannel(Constants.PUSH_CHANNEL_ID, Constants.PUSH_CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH))
            notify(id.toSafeInt(), notification)
        }
    }

    private fun getPendingIntentFlags(baseFlags: Int): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) baseFlags or PendingIntent.FLAG_MUTABLE else baseFlags
    }

}