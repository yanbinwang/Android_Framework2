package com.example.thirdparty.media.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.media.MediaRecorder
import android.os.Build
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import com.example.common.utils.StorageUtil
import com.example.common.utils.StorageUtil.StorageType.AUDIO
import com.example.common.utils.function.deleteFile
import com.example.framework.utils.function.TrackableLifecycleService
import com.example.framework.utils.function.string
import com.example.thirdparty.R
import com.example.thirdparty.utils.NotificationUtil.notificationId

/**
 *  <service
 *      android:name="com.sqkj.home.service.RecordingService"
 *      android:enabled="true"
 *      android:exported="false"
 *      android:configChanges="keyboardHidden|orientation|screenSize"//告诉系统，当指定的配置发生变化时，不要销毁并重新创建该服务，而是让服务自己处理这些变化
 *      android:foregroundServiceType="mediaPlayback"--》 Q开始后台服务需要配置，否则录制不正常  />
 */
class RecordingService : TrackableLifecycleService() {
    private var folderPath: String? = null
    private var recorder: MediaRecorder? = null
    private var wakeLock: PowerManager.WakeLock? = null

    companion object {
        /**
         * 是否是关闭页面，由外层传入，以此判断在服务OnDestroy的时候是否需要执行停止
         */
        var isDestroy = false

        private var listener: OnRecorderListener? = null

        fun setOnRecorderListener(listener: OnRecorderListener) {
            this.listener = listener
        }
    }

    override fun onCreate() {
        super.onCreate()
        // 1. 创建符合Android 15要求的通知渠道
        val channelId = string(R.string.notificationChannelId)
        val channelName = string(R.string.notificationChannelName)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // 录屏服务建议使用低重要性，避免打扰用户
            val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_LOW).apply {
                description = "用于显示音频录制状态"
                setSound(null, null) // 关闭通知声音
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
        // 2. 构建完整的通知（必须包含图标、标题）
        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("正在录音") // 强制要求：标题
            .setSmallIcon(R.mipmap.ic_launcher) // 强制要求：图标（替换为你的资源）
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true) // 标记为持续通知，用户无法手动清除
            .setSilent(true) // 静音通知
            .build()
        // 3. 启动前台服务（Android 15要求必须在启动服务后5秒内调用）
        startForeground(notificationId, notification)
//        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
//            startForeground(1, Notification())
//        } else {
//            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as? NotificationManager
//            notificationManager?.createNotificationChannel(NotificationChannel(packageName, packageName, NotificationManager.IMPORTANCE_DEFAULT))
//            val builder = NotificationCompat.Builder(this, packageName)
//            //id不为0即可，该方法表示将服务设置为前台服务
//            startForeground(1, builder.build())
//        }
////        stopForeground(true)//关闭录屏的图标-可注释
        //获取 PowerManager 实例
        val powerManager = getSystemService(POWER_SERVICE) as? PowerManager
        //创建一个 PARTIAL_WAKE_LOCK 类型的 WakeLock，它可以让 CPU 保持唤醒状态，但允许屏幕和键盘背光关闭
        wakeLock = powerManager?.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "RecordingService:WakeLock")
        //获取 WakeLock  获取一个带有超时限制的唤醒锁，当超过指定的超时时间后，唤醒锁会自动释放
        wakeLock?.acquire()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startRecording()
        return super.onStartCommand(intent, flags, startId)
    }

    /**
     * 开始录制，启动服务的时候开始
     */
    private fun startRecording() {
        val recordFile = StorageUtil.getOutputFile(AUDIO)
        folderPath = recordFile?.absolutePath
        recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) MediaRecorder(this) else MediaRecorder()
        try {
            recorder?.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)//设置麦克风
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                //若api低于O，调用setOutputFile(String path),高于使用setOutputFile(File path)
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                    setOutputFile(folderPath)
                } else {
                    setOutputFile(recordFile)
                }
                prepare()
                start()
                //仅在 start 成功后触发
                listener?.onStart(folderPath)
            }
        } catch (e: Exception) {
            isDestroy = true
            releaseRecorder()//确保资源被释放（调用 stopSelf() 之后，onDestroy() 方法会在稍后的某个时刻被系统调用，而在这期间若有其他代码尝试访问未释放的资源，可能会引发异常）
            listener?.onError(e)
            stopSelf()
        }
    }

    /**
     * 停止录制
     */
    private fun stopRecording() {
        listener?.onShutter()
        recorder?.runCatching {
            stop()//阻塞直到文件写入完成
            releaseRecorder()
        }?.onSuccess {
            listener?.onStop()
        }?.onFailure {
            listener?.onError(it as? Exception)
        }
    }

    /**
     * 释放资源
     */
    private fun releaseRecorder() {
        recorder?.reset()//重置状态（可选）
        recorder?.release()//释放底层资源
        recorder = null//置空引用
    }

    /**
     * 外层点击停止录制后结束服务，自动停止当前录制
     */
    override fun onDestroy() {
        super.onDestroy()
        wakeLock?.let {
            if (it.isHeld) {
                it.release()
            }
        }
        wakeLock = null
        if (isDestroy) {
            isDestroy = false
            releaseRecorder()
            folderPath.deleteFile()
        } else {
            stopRecording()
        }
    }

    /**
     * 录制回调监听
     */
    interface OnRecorderListener {
        /**
         * 开始录制
         */
        fun onStart(folderPath: String?)

        /**
         * 开始存储
         */
        fun onShutter()

        /**
         * 停止录制
         */
        fun onStop()

        /**
         * 报错
         */
        fun onError(e: Exception?)
    }

}