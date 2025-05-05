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
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.example.common.base.page.Extra
import com.example.common.network.repository.withHandling
import com.example.common.utils.ScreenUtil.screenDensity
import com.example.common.utils.StorageUtil
import com.example.common.utils.StorageUtil.StorageType
import com.example.common.utils.function.deleteFile
import com.example.common.utils.function.getExtra
import com.example.framework.utils.function.value.orZero
import com.example.thirdparty.media.utils.DisplayHelper.Companion.previewHeight
import com.example.thirdparty.media.utils.DisplayHelper.Companion.previewWidth
import com.example.thirdparty.media.widget.TimerTick
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
 *    <!-- 非必要的权限，息屏加锁 -->
 *     <uses-permission android:name="android.permission.WAKE_LOCK" />
 *
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
    private var recorder: MediaRecorder? = null
    private var projection: MediaProjection? = null
    private var display: VirtualDisplay? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private val timerTick by lazy { TimerTick(this, this) }

    companion object {
        /**
         * 是否是关闭页面，由外层传入，以此判断在服务OnDestroy的时候是否需要执行停止
         */
        var isDestroy = false

        /**
         * 回调监听
         */
        private var listener: OnDisplayListener? = null

        fun setOnDisplayListener(listener: OnDisplayListener) {
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
        wakeLock = powerManager?.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "DisplayService:WakeLock")
        //获取 WakeLock  获取一个带有超时限制的唤醒锁，当超过指定的超时时间后，唤醒锁会自动释放
        wakeLock?.acquire()
        //计时器挂载弹框
        timerTick.start()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        //获取到页面OnActivityResult取得的值
        val resultCode = intent?.getIntExtra(Extra.RESULT_CODE, -1)
//        val resultData = intent?.getParcelableExtra(Extra.BUNDLE_BEAN) ?: Intent()
        val resultData = intent?.getExtra(Extra.BUNDLE_BEAN, Intent::class.java)
        startRecording(resultCode, resultData)
//        return START_STICKY
        return super.onStartCommand(intent, flags, startId)
    }

    private fun startRecording(resultCode: Int?, resultData: Intent?) {
        try {
            if (resultData == null) throw RuntimeException("resultData is Empty")
            val screenFile = StorageUtil.getOutputFile(StorageType.SCREEN)
            folderPath = screenFile?.absolutePath
            recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) MediaRecorder(this) else MediaRecorder()
            recorder?.apply {
                setVideoSource(MediaRecorder.VideoSource.SURFACE)
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setVideoEncodingBitRate(5 * previewWidth * previewHeight)
                setVideoEncoder(MediaRecorder.VideoEncoder.H264)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setVideoSize(previewWidth, previewHeight)
                setVideoFrameRate(60)
                //若api低于O，调用setOutputFile(String path),高于使用setOutputFile(File path)
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                    setOutputFile(screenFile?.absolutePath)
                } else {
                    setOutputFile(screenFile)
                }
                prepare()
            }
            projection = (getSystemService(MEDIA_PROJECTION_SERVICE) as? MediaProjectionManager)?.getMediaProjection(resultCode.orZero, resultData)
            display = projection?.createVirtualDisplay("mediaProjection", previewWidth, previewHeight, screenDensity, DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, recorder?.surface, null, null)
            recorder?.start()
            //仅在 start 成功后触发
            listener?.onStart(folderPath)
        } catch (e: Exception) {
            isDestroy = true
            releaseDisplay()//确保资源被释放（调用 stopSelf() 之后，onDestroy() 方法会在稍后的某个时刻被系统调用，而在这期间若有其他代码尝试访问未释放的资源，可能会引发异常）
            listener?.onError(e)
            stopSelf()
        }
    }

    /**
     * 停止录制
     */
    private fun stopRecording() {
        listener?.onShutter()
        lifecycleScope.launch {
            flow {
                //阻塞直到文件写入完成,切到ui线程做停止
                withContext(IO) { recorder?.stop() }
                emit(releaseDisplay())
            }.withHandling({
                listener?.onError(it.throwable as? Exception)
            }).collect {
                listener?.onStop()
            }
        }
//        var exception: Exception? = null
//        try {
//            //阻塞直到文件写入完成
//            recorder?.stop()
//            releaseDisplay()
//        } catch (e: Exception) {
//            exception = e
//        } finally {
//            if (null != exception) {
//                listener?.onError(exception)
//            } else {
//                listener?.onStop()
//            }
//        }
    }

    private fun releaseDisplay() {
        recorder?.reset()//重置状态（可选）
        recorder?.release()//释放底层资源
        recorder = null//置空引用
        display?.release()
        display = null
        projection?.stop()
        projection = null
    }

//    override fun onBind(intent: Intent?): IBinder? {
//        return null
//    }

    override fun onDestroy() {
        super.onDestroy()
        timerTick.destroy()
        wakeLock?.let {
            if (it.isHeld) {
                it.release()
            }
        }
        wakeLock = null
        if (isDestroy) {
            isDestroy = false
            releaseDisplay()
            folderPath.deleteFile()
        } else {
            stopRecording()
        }
    }

    /**
     * 录屏回调监听
     */
    interface OnDisplayListener {
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