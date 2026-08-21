package com.example.mvvm.service

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import com.example.framework.utils.function.TrackableLifecycleService
import com.example.framework.utils.logWTF

class MusicBindService : TrackableLifecycleService() {
    private val binder = MusicBinder()

//    override fun onCreate() {
//        super.onCreate()
//        // 创建符合Android 15要求的通知渠道
//        val channelId = string(R.string.notificationChannelId)
//        val channelName = string(R.string.notificationChannelName)
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            // 录屏服务建议使用低重要性，避免打扰用户
//            val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_LOW).apply {
//                description = "用于显示音频状态"
//                setSound(null, null) // 关闭通知声音
//            }
//            val notificationManager = getSystemService(NotificationManager::class.java)
//            notificationManager.createNotificationChannel(channel)
//        }
//        // 构建完整的通知
//        val notification = NotificationCompat.Builder(this, channelId)
//            .setContentTitle("正在播放") // 强制要求：标题
//            .setSmallIcon(R.mipmap.ic_launcher) // 强制要求：图标
//            .setPriority(NotificationCompat.PRIORITY_LOW)
//            .setOngoing(true) // 标记为持续通知，用户无法手动清除
//            .setSilent(true) // 静音通知
//            .build()
//        // 启动前台服务（Android 15要求必须在启动服务后5秒内调用）
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//            startForeground(notificationId, notification, FOREGROUND_SERVICE_TYPE_MICROPHONE or FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)
//        } else {
//            startForeground(notificationId, notification)
//        }
//    }

    @SuppressLint("MissingSuperCall")
    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    inner class MusicBinder : Binder() {
        fun getService(): MusicBindService {
            return this@MusicBindService
        }

        fun play(url: String) {
            "播放:${url}".logWTF("wyb")
        }

        fun pause() {
            "暂停".logWTF("wyb")
        }
    }
}