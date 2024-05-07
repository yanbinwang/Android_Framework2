package com.example.thirdparty.media.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.MediaRecorder
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import com.example.common.base.page.Extra
import com.example.common.utils.ScreenUtil.screenDensity
import com.example.common.utils.function.getExtra
import com.example.framework.utils.function.value.orZero
import com.example.thirdparty.media.utils.MediaUtil
import com.example.thirdparty.media.utils.MediaUtil.MediaType
import com.example.thirdparty.media.utils.helper.DisplayHelper.Companion.previewHeight
import com.example.thirdparty.media.utils.helper.DisplayHelper.Companion.previewWidth
import com.example.thirdparty.media.widget.TimerTick

/**
 *  Created by wangyanbin
 *  录屏服务
 *  <!-- 屏幕录制 -->
 *  <service
 *      android:name="com.sqkj.home.service.DisplayService"
 *      android:enabled="true"
 *      android:exported="false"
 *      android:foregroundServiceType="mediaProjection"--》 Q开始后台服务需要配置，否则录制不正常  />
 */
class DisplayService : LifecycleService() {
    private var folderPath: String? = null
    private var mediaProjection: MediaProjection? = null
    private var mediaRecorder: MediaRecorder? = null
    private var virtualDisplay: VirtualDisplay? = null
    private val timerTick by lazy { TimerTick(this) }

    companion object {
        internal var listener: (folderPath: String?, isRecoding: Boolean) -> Unit = { _, _ -> }

        /**
         * filePath->开始录制时，会返回源文件存储地址(此时记录一下)停止录制时一定为空，此时做ui操作
         * recoding->true表示开始录屏，此时可以显示页面倒计时，false表示录屏结束，此时可以做停止的操作
         */
        fun setOnDisplayListener(listener: (folderPath: String?, isRecoding: Boolean) -> Unit) {
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
        timerTick.start(lifecycle)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        try {
            val resultCode = intent?.getIntExtra(Extra.RESULT_CODE, -1)
//            val resultData = intent?.getParcelableExtra(Extra.BUNDLE_BEAN) ?: Intent()
            val resultData = intent?.getExtra(Extra.BUNDLE_BEAN, Intent::class.java)
            mediaProjection = createMediaProjection(resultCode, resultData)
            mediaRecorder = createMediaRecorder()
            virtualDisplay = createVirtualDisplay()
            mediaRecorder?.start()
        } catch (_: Exception) {
        }
//        return START_STICKY
        return super.onStartCommand(intent, flags, startId)
    }

    private fun createMediaProjection(resultCode: Int?, resultData: Intent?): MediaProjection? {
        resultData ?: return null
        return (getSystemService(MEDIA_PROJECTION_SERVICE) as? MediaProjectionManager)?.getMediaProjection(resultCode.orZero, resultData)
    }

    private fun createMediaRecorder(): MediaRecorder {
        val screenFile = MediaUtil.getOutputFile(MediaType.SCREEN)
        folderPath = screenFile?.absolutePath
        return (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) MediaRecorder(this) else MediaRecorder()).apply {
            setVideoSource(MediaRecorder.VideoSource.SURFACE)
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setVideoEncodingBitRate(5 * previewWidth * previewHeight)
            setVideoEncoder(MediaRecorder.VideoEncoder.H264)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setVideoSize(previewWidth, previewHeight)
            setVideoFrameRate(60)
            try {
                //若api低于O，调用setOutputFile(String path),高于使用setOutputFile(File path)
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                    setOutputFile(screenFile?.absolutePath)
                } else {
                    setOutputFile(screenFile)
                }
                prepare()
            } catch (_: Exception) {
            }
            listener.invoke(folderPath, true)
        }
    }

    private fun createVirtualDisplay(): VirtualDisplay? {
        return mediaProjection?.createVirtualDisplay("mediaProjection", previewWidth, previewHeight, screenDensity, DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, mediaRecorder?.surface, null, null)
    }

//    override fun onBind(intent: Intent?): IBinder? {
//        return null
//    }

    override fun onDestroy() {
        super.onDestroy()
        try {
//            timerTick.destroy()
            virtualDisplay?.release()
            virtualDisplay = null
            mediaRecorder?.stop()
            mediaRecorder?.reset()
            mediaRecorder?.release()
            mediaRecorder = null
            mediaProjection?.stop()
            mediaProjection = null
        } catch (_: Exception) {
        }
        listener.invoke(folderPath, false)
    }

}