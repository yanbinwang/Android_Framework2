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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleService
import com.example.common.base.page.Extra
import com.example.common.utils.ScreenUtil.screenDensity
import com.example.framework.utils.function.value.orZero
import com.example.thirdparty.media.utils.MediaUtil
import com.example.thirdparty.media.utils.helper.ScreenHelper.Companion.previewHeight
import com.example.thirdparty.media.utils.helper.ScreenHelper.Companion.previewWidth
import com.example.thirdparty.media.widget.TimerTick

class ScreenService2 : LifecycleService(), LifecycleEventObserver {
    private var folderPath = ""
    private var resultCode = 0
    private var resultData: Intent? = null
    private var mediaProjection: MediaProjection? = null
    private var mediaRecorder: MediaRecorder? = null
    private var virtualDisplay: VirtualDisplay? = null
    private val timerTick by lazy { TimerTick(this) }

    companion object {
        private var onShutter: (filePath: String?, recoding: Boolean) -> Unit = { _, _ -> }

        /**
         * filePath->开始录制时，会返回源文件存储地址(此时记录一下)停止录制时一定为空，此时做ui操作
         * recoding->true表示开始录屏，此时可以显示页面倒计时，false表示录屏结束，此时可以做停止的操作
         */
        fun setOnScreenListener(onShutter: (filePath: String?, recoding: Boolean) -> Unit) {
            this.onShutter = onShutter
        }
    }

    init {
        lifecycle.addObserver(this)
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
        return super.onStartCommand(intent, flags, startId)
    }

    private fun createMediaProjection(): MediaProjection? {
        return (getSystemService(MEDIA_PROJECTION_SERVICE) as? MediaProjectionManager)?.getMediaProjection(resultCode, resultData ?: Intent())
    }

    private fun createMediaRecorder(): MediaRecorder {
        val screenFile = MediaUtil.getOutputFile(MediaUtil.MediaType.SCREEN)
        folderPath = screenFile?.absolutePath.orEmpty()
        ScreenService.onShutter.invoke(folderPath,true)
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

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            Lifecycle.Event.ON_CREATE -> {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                    startForeground(1, Notification())
                } else {
                    val notificationManager = getSystemService(NOTIFICATION_SERVICE) as? NotificationManager
                    notificationManager?.createNotificationChannel(NotificationChannel(packageName, packageName, NotificationManager.IMPORTANCE_DEFAULT))
                    val builder = NotificationCompat.Builder(this, packageName)
                    //id不为0即可，该方法表示将服务设置为前台服务
                    startForeground(1, builder.build())
                }
//                stopForeground(true)//关闭录屏的图标-可注释
                timerTick.start()
            }
            Lifecycle.Event.ON_DESTROY -> {
                try {
                    timerTick.destroy()
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
                onShutter.invoke(folderPath, false)
                lifecycle.removeObserver(this)
            }
            else -> {}
        }
    }

}