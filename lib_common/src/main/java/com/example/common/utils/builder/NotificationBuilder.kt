package com.example.common.utils.builder

import android.app.*
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.common.constant.Constants
import java.lang.ref.WeakReference

/**
 * author:wyb
 * 通知工具类
 */
class NotificationBuilder(activity: Activity) {
    private var mBuilder: NotificationCompat.Builder? = null
    private val mNotificationManager: NotificationManager
    private val mActivity = WeakReference(activity)

    init {
        mNotificationManager = mActivity.get()
            ?.getSystemService(android.content.Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    //构建通知栏
    fun createNotification(id: Int, smallIcon: Int, largeIcon: Bitmap, title: String, text: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(Constants.PUSH_CHANNEL_ID, Constants.PUSH_CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH)
            channel.enableVibration(false)
            channel.enableLights(false)
            mNotificationManager.createNotificationChannel(channel)
        }
        mBuilder = NotificationCompat.Builder(mActivity.get()!!, Constants.PUSH_CHANNEL_ID)
        mBuilder!!.setContentTitle(title).setContentText(text).setSmallIcon(smallIcon)
            .setLargeIcon(largeIcon).setOngoing(true).setAutoCancel(true)
            .setWhen(System.currentTimeMillis())
        mNotificationManager.notify(id, mBuilder!!.build())
    }

    //构建通知栏跳转
    fun setPendingIntent(id: Int, title: String, text: String, setupApkIntent: Intent) {
        val contentIntent = PendingIntent.getActivity(mActivity.get(), 0, setupApkIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        mBuilder!!.setContentIntent(contentIntent).setContentTitle(title).setContentText(text).setProgress(0, 0, false).setDefaults(Notification.DEFAULT_ALL)
        val notification = mBuilder!!.build()
        notification.flags = Notification.FLAG_AUTO_CANCEL
        mNotificationManager.notify(id, notification)
    }

    //构建进度条
    fun setProgress(id: Int, progress: Int, title: String, text: String) {
        mBuilder!!.setContentTitle(title).setContentText(text).setProgress(100, progress, false).setWhen(System.currentTimeMillis())
        val notification = mBuilder!!.build()
        notification.flags = Notification.FLAG_AUTO_CANCEL or Notification.FLAG_ONLY_ALERT_ONCE
        mNotificationManager.notify(id, notification)
    }

    //取消构建
    fun setCancel(id: Int, title: String, text: String) {
        mBuilder!!.setContentTitle(title).setContentText(text)
        val notification = mBuilder!!.build()
        notification.flags = Notification.FLAG_AUTO_CANCEL
        mNotificationManager.notify(id, notification)
    }

}