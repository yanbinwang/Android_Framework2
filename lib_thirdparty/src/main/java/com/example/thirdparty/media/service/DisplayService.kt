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
import com.example.common.utils.StorageUtil
import com.example.common.utils.StorageUtil.StorageType
import com.example.thirdparty.media.utils.DisplayHelper.Companion.previewHeight
import com.example.thirdparty.media.utils.DisplayHelper.Companion.previewWidth
import com.example.thirdparty.media.widget.TimerTick

/**
 *  Created by wangyanbin
 *  https://zhuanlan.zhihu.com/p/75974735
 *  录屏服务
 *  <!-- 屏幕录制 -->
 *  <service
 *      android:name="com.sqkj.home.service.DisplayService"
 *      android:enabled="true"
 *      android:exported="false"
 *      android:configChanges="keyboardHidden|orientation|screenSize"//告诉系统，当指定的配置发生变化时，不要销毁并重新创建该服务，而是让服务自己处理这些变化
 *      android:foregroundServiceType="mediaProjection"--》 Q开始后台服务需要配置，否则录制不正常  />
 *
 *   <!-- 必要的权限 -->
 *     <uses-permission android:name="android.permission.RECORD_AUDIO" />
 *     <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
 *     <uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
 *     <uses-permission android:name="android.permission.MEDIA_PROJECTION" />
 *     android:foregroundServiceType 的值：如果服务还涉及其他类型的前台服务操作，如播放音乐、位置跟踪等，可以使用竖线 | 分隔多个类型
 *     例如 android:foregroundServiceType="mediaProjection|mediaPlayback"。
 *
 *     Android 8.0（API 级别 26）及以上
 *     1. dataSync
 *       说明：表示该前台服务用于执行数据同步操作，例如与服务器同步联系人、日历等数据。当应用进行数据同步时，将服务标记为 dataSync 类型，系统可以更好地管理资源，确保同步操作在合适的时机进行。
 *     2. location
 *       说明：用于表明该前台服务需要持续获取设备的位置信息。比如导航应用在后台持续定位用户位置时，可将服务设置为 location 类型，这样系统会在设备进入低功耗模式时，仍然允许服务获取位置数据。
 *     3. mediaPlayback
 *       说明：适用于进行媒体播放的前台服务，如音乐播放器应用。设置为该类型后，系统会保证在服务运行期间，媒体播放操作能够正常进行，并且在某些情况下会提供相应的媒体控制界面。
 *     4. mediaProjection
 *       说明：当服务涉及屏幕录制或媒体投影功能时使用。例如，屏幕录制应用在录制屏幕时，将服务设置为 mediaProjection 类型，系统会为其提供必要的权限和资源支持。
 *
 *     Android 9（API 级别 28）新增
 *     1. phoneCall
 *       说明：表示该前台服务与电话通话相关，如进行 VoIP 通话、电话录音等操作。将服务设置为 phoneCall 类型，系统会确保在通话过程中服务的正常运行，避免被系统意外终止。
 *
 *     Android 10（API 级别 29）新增
 *     1. connectedDevice
 *       说明：用于与外部设备连接的前台服务，如蓝牙设备连接、USB 设备连接等。当服务负责管理与外部设备的连接和数据交互时，设置为 connectedDevice 类型，系统会提供相应的资源和权限支持。
 *     2. emergency
 *       说明：适用于处理紧急情况的前台服务，如紧急报警、医疗急救等应用。设置为该类型的服务具有较高的优先级，系统会尽量保证其在各种情况下都能正常运行。
 *     3. health
 *       说明：用于与健康监测相关的前台服务，如心率监测、运动追踪等应用。将服务设置为 health 类型，系统会为其提供稳定的运行环境，确保健康数据的准确采集。
 *
 *     Android 11（API 级别 30）新增
 *     1. softwareRendering
 *       说明：当服务需要使用软件渲染时，可设置为该类型。例如，一些图形处理应用在进行复杂的图形渲染时，可能会使用软件渲染方式，此时将服务标记为 softwareRendering 类型，系统会为其分配相应的资源。
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
        val screenFile = StorageUtil.getOutputFile(StorageType.SCREEN)
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
            mediaRecorder?.stop()
            mediaRecorder?.reset()
            mediaRecorder?.release()
            mediaRecorder = null
            virtualDisplay?.release()
            virtualDisplay = null
            mediaProjection?.stop()
            mediaProjection = null
        } catch (_: Exception) {
        }
        listener.invoke(folderPath, false)
    }

}