package com.example.thirdparty.media.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.media.MediaRecorder
import android.os.Build
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import com.example.common.utils.StorageUtil
import com.example.common.utils.StorageUtil.StorageType.AUDIO

class RecordingService : LifecycleService() {
    private var recorder: MediaRecorder? = null
    private var wakeLock: PowerManager.WakeLock? = null

    companion object {
        private var listener: OnRecorderListener? = null

        fun setOnRecorderListener(listener: OnRecorderListener) {
            this.listener = listener
        }
    }

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            startForeground(1, Notification())
        } else {
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as? NotificationManager
            notificationManager?.createNotificationChannel(NotificationChannel(packageName, packageName, NotificationManager.IMPORTANCE_DEFAULT))
            val builder = NotificationCompat.Builder(this, packageName)
            //id不为0即可，该方法表示将服务设置为前台服务
            startForeground(1, builder.build())
        }
//        stopForeground(true)//关闭录屏的图标-可注释
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
     * 外层点击停止录制后结束服务，自动停止当前录制
     */
    override fun onDestroy() {
        super.onDestroy()
        wakeLock?.release()
        stopRecording()
    }

    /**
     * 开始录制，启动服务的时候开始
     */
    private fun startRecording() {
        val recordFile = StorageUtil.getOutputFile(AUDIO)
        val sourcePath = recordFile?.absolutePath
        recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) MediaRecorder(this) else MediaRecorder()
        try {
            recorder?.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)//设置麦克风
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                //若api低于O，调用setOutputFile(String path),高于使用setOutputFile(File path)
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                    setOutputFile(sourcePath)
                } else {
                    setOutputFile(recordFile)
                }
                prepare()
                start()
                //仅在 start 成功后触发
                listener?.onStart(sourcePath)
            }
        } catch (e: Exception) {
            releaseRecorder()
            listener?.onError(e)
        }
    }

    /**
     * 停止录制
     */
    private fun stopRecording() {
        listener?.onShutter()
        var exception: Exception? = null
        try {
            //阻塞直到文件写入完成
            recorder?.stop()
            releaseRecorder()
        } catch (e: Exception) {
            exception = e
        } finally {
            if (null != exception) {
                listener?.onError(exception)
            } else {
                listener?.onStop()
            }
        }
    }

    /**
     * 释放资源
     */
    private fun releaseRecorder() {
        recorder?.apply {
            reset()//重置状态（可选）
            release()//释放底层资源
            recorder = null//置空引用
        }
    }

    /**
     * 录制回调监听
     */
    interface OnRecorderListener {
        /**
         * 开始录制
         */
        fun onStart(sourcePath: String?)

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
        fun onError(e: Exception)
    }

}