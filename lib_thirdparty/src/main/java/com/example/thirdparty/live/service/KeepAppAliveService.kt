package com.example.thirdparty.live.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import com.example.common.utils.function.string
import com.example.thirdparty.R

/**
 * 保活服务
 */
class KeepAppAliveService : Service() {
    private val mNotificationManager by lazy { getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager }

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(string(R.string.notificationChannelId), string(R.string.notificationChannelName), NotificationManager.IMPORTANCE_HIGH)
            mNotificationManager?.createNotificationChannel(channel)
        }
        startForeground(1, getNotification())
        stopForeground(true)//关闭录屏的图标-可注释
    }

    private fun getNotification(): Notification {
        val builder: Notification.Builder = Notification.Builder(this)
            .setSmallIcon(R.mipmap.ic_notification)
            .setContentTitle("七牛推流")
            .setContentText("后台运行中")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(string(R.string.notificationChannelId))
        }
        return builder.build()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

}