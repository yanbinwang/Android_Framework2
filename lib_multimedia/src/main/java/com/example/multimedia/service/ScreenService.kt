package com.example.multimedia.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.MediaRecorder
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.common.base.page.Extra
import com.example.common.utils.ScreenUtil.screenDensity
import com.example.framework.utils.function.value.orZero
import com.example.multimedia.utils.MediaType
import com.example.multimedia.utils.MultimediaUtil
import com.example.multimedia.utils.helper.ScreenHelper.Companion.previewHeight
import com.example.multimedia.utils.helper.ScreenHelper.Companion.previewWidth
import com.example.multimedia.utils.helper.TimeTickHelper

/**
 *  Created by wangyanbin
 *  录屏服务
 *  <!-- 屏幕录制 -->
 *  <service
 *      android:name="com.sqkj.home.service.ScreenService"
 *      android:enabled="true"
 *      android:exported="false"
 *      android:foregroundServiceType="mediaProjection"--》 Q开始后台服务需要配置，否则录制不正常  />
 */
class ScreenService : Service() {
    private var resultCode = 0
    private var resultData: Intent? = null
    private var mediaProjection: MediaProjection? = null
    private var mediaRecorder: MediaRecorder? = null
    private var virtualDisplay: VirtualDisplay? = null
    private val timerFactory by lazy { TimeTickHelper(this) }

    companion object {
        internal var onShutter: (filePath: String?, recoding: Boolean) -> Unit = { _, _ -> }

        /**
         * filePath->开始录制时，会返回源文件存储地址(此时记录一下)停止录制时一定为空，此时做ui操作
         * recoding->true表示开始录屏，此时可以显示页面倒计时，false表示录屏结束，此时可以做停止的操作
         */
        fun setOnScreenListener(onShutter: (filePath: String?, recoding: Boolean) -> Unit) {
            this.onShutter = onShutter
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
        timerFactory.start()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        try {
            resultCode = intent?.getIntExtra(Extra.RESULT_CODE, -1).orZero
            resultData = intent?.getParcelableExtra(Extra.BUNDLE_BEAN)
            mediaProjection = createMediaProjection()
            mediaRecorder = createMediaRecorder()
            virtualDisplay = createVirtualDisplay()
            mediaRecorder?.start()
        } catch (_: Exception) {
        }
        return START_STICKY
    }

    private fun createMediaProjection(): MediaProjection? {
        return (getSystemService(MEDIA_PROJECTION_SERVICE) as? MediaProjectionManager)?.getMediaProjection(resultCode, resultData ?: Intent())
    }

    private fun createMediaRecorder(): MediaRecorder {
        val screenFile = MultimediaUtil.getOutputFile(MediaType.SCREEN)
        onShutter.invoke(screenFile?.absolutePath,true)
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
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) setOutputFile(screenFile?.absolutePath) else setOutputFile(screenFile)
                prepare()
            } catch (_: Exception) {
            }
        }
    }

    private fun createVirtualDisplay(): VirtualDisplay? {
        return mediaProjection?.createVirtualDisplay("mediaProjection", previewWidth, previewHeight, screenDensity, DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, mediaRecorder?.surface, null, null)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            timerFactory.destroy()
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
        onShutter.invoke("",false)
    }

}