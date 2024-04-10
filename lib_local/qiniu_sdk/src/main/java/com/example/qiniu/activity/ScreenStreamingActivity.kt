package com.example.qiniu.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.graphics.Color
import android.hardware.Camera
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.example.framework.utils.function.value.orFalse
import com.example.framework.utils.function.value.orZero
import com.example.framework.utils.logD
import com.example.framework.utils.logE
import com.example.framework.utils.logI
import com.example.qiniu.R
import com.example.qiniu.plain.EncodingConfig
import com.example.qiniu.utils.Config
import com.example.qiniu.utils.ToastUtils
import com.example.qiniu.utils.Util
import com.qiniu.pili.droid.streaming.PLVideoEncodeType
import com.qiniu.pili.droid.streaming.ScreenSetting
import com.qiniu.pili.droid.streaming.ScreenStreamingManager
import com.qiniu.pili.droid.streaming.StreamStatusCallback
import com.qiniu.pili.droid.streaming.StreamingProfile
import com.qiniu.pili.droid.streaming.StreamingProfile.AVProfile
import com.qiniu.pili.droid.streaming.StreamingProfile.SendingBufferProfile
import com.qiniu.pili.droid.streaming.StreamingProfile.StreamStatusConfig
import com.qiniu.pili.droid.streaming.StreamingSessionListener
import com.qiniu.pili.droid.streaming.StreamingState
import com.qiniu.pili.droid.streaming.StreamingStateChangedListener
import java.net.URISyntaxException
import java.text.SimpleDateFormat
import java.util.Date

/**
 * 录屏推流的样例代码
 */
@SuppressLint("SetTextI18n", "StaticFieldLeak")
class ScreenStreamingActivity : Activity() {
    private var mTimeTv: TextView? = null
    private var mLogTextView: TextView? = null
    private var mStatusTextView: TextView? = null
    private var mStatView: TextView? = null
    private var mShutterButton: Button? = null
    private var mStatusMsgContent: String? = null
    private var mLogContent = "\n"
    private var mShutterButtonPressed = false
    private var mPublishUrl: String? = null
    private var mIsQuicEnabled = false
    private var mIsSrtEnabled = false
    private var mIsReady = false
    private var mDateFormat: SimpleDateFormat? = null
    private var mProfile: StreamingProfile? = null
    private var mImageSwitcher: ImageSwitcher? = null
    private val mHandler = Handler()
    private val NOTIFICATION_ID = 1010100
    private val TIME_PATTERN = "yyyy-MM-dd HH:mm:ss:SSS"

    companion object {
        private var mIsPictureStreaming = false
        private var mTimes = 0
        private var mEncodingConfig: EncodingConfig? = null
        private var mScreenStreamingManager: ScreenStreamingManager? = null
        private var mSubThreadHandler: Handler? = null// 用于处理子线程操作
        private val TAG = "ScreenStreamingActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = Config.SCREEN_ORIENTATION
        // 获取推流编码配置信息
        mEncodingConfig = intent.getSerializableExtra(Config.NAME_ENCODING_CONFIG) as EncodingConfig?
        val intent = intent
        mPublishUrl = intent.getStringExtra(Config.PUBLISH_URL)
        mIsQuicEnabled = intent.getBooleanExtra(Config.TRANSFER_MODE_QUIC, false)
        mIsSrtEnabled = intent.getBooleanExtra(Config.TRANSFER_MODE_SRT, false)
        mDateFormat = SimpleDateFormat(TIME_PATTERN)
        val handlerThread = HandlerThread(TAG)
        handlerThread.start()
        mSubThreadHandler = Handler(handlerThread.getLooper())
        // 初始化视图控件
        initView()
        // 初始化 StreamingProfile，StreamingProfile 为推流相关的配置类，详情可参考 https://developer.qiniu.com/pili/sdk/3719/PLDroidMediaStreaming-function-using#4
        initEncodingProfile()
        // 初始化 MediaStreamingManager，使用姿势可参考 https://developer.qiniu.com/pili/sdk/3719/PLDroidMediaStreaming-function-using#6
        initStreamingManager()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mSubThreadHandler != null) {
            mSubThreadHandler?.looper?.quit()
        }
        // 销毁推流 Manager 的资源
        mScreenStreamingManager?.destroy()
        mHandler.removeCallbacksAndMessages(null)
    }

    /**
     * 初始化录屏推流 demo 相关的视图控件
     */
    protected fun initView() {
        setContentView(R.layout.activity_screen_streaming)
        mTimeTv = findViewById(R.id.time_tv)
        val picStreamingBtn = findViewById<Button>(R.id.pic_streaming_btn)
        mLogTextView = findViewById(R.id.log_info)
        mLogTextView?.setTextColor(Color.WHITE)
        mStatusTextView = findViewById(R.id.streamingStatus)
        mStatView = findViewById(R.id.stream_status)
        mStatView?.setTextColor(Color.WHITE)
        mShutterButton = findViewById(R.id.toggleRecording_button)
        mShutterButton?.setOnClickListener(View.OnClickListener {
            if (!mIsReady) {
                Toast.makeText(this@ScreenStreamingActivity, "需要在 READY 状态后才可以开始推流！！！", Toast.LENGTH_SHORT).show()
                return@OnClickListener
            }
            if (mShutterButtonPressed) {
                stopStreamingInternal()
            } else {
                startStreamingInternal()
            }
        })
        picStreamingBtn.setOnClickListener {
            mProfile?.pictureStreamingFps = 10f
            togglePictureStreaming()
        }
        mHandler.post(mUpdateTimeTvRunnable)
    }

    /**
     * 初始化编码配置项 [StreamingProfile]
     */
    private fun initEncodingProfile() {
        mProfile = StreamingProfile()
        // 设置推流地址
        try {
            mProfile?.setPublishUrl(mPublishUrl)
        } catch (e: URISyntaxException) {
            e.printStackTrace()
        }
        // 是否开启 QUIC 推流。
        // QUIC 是基于 UDP 开发的可靠传输协议，在弱网下拥有更好的推流效果，相比于 TCP 拥有更低的延迟，可抵抗更高的丢包率。
        mProfile?.setQuicEnable(mIsQuicEnabled)
        mProfile?.setSrtEnabled(mIsSrtEnabled)

        // 自定义配置音频的采样率、码率以及声道数的对象，如果使用预设配置，则无需实例化
        var aProfile: StreamingProfile.AudioProfile? = null
        // 自定义配置视频的帧率、码率、GOP 以及 H264 Profile 的对象，如果使用预设配置，则无需实例化
        var vProfile: StreamingProfile.VideoProfile? = null
        if (!mEncodingConfig?.mIsAudioOnly.orFalse) {
            // 设置视频质量参数
            if (mEncodingConfig?.mIsVideoQualityPreset.orFalse) {
                // 使用预设的视频质量等级
                // 预设等级可以参考 https://developer.qiniu.com/pili/sdk/3719/PLDroidMediaStreaming-function-using#4 的 4.2 小节
                mProfile?.setVideoQuality(mEncodingConfig?.mVideoQualityPreset.orZero)
            } else {
                // 使用自定义视频质量参数配置，自定义配置优先级高于预设等级配置
                vProfile = StreamingProfile.VideoProfile(mEncodingConfig?.mVideoQualityCustomFPS.orZero, mEncodingConfig?.mVideoQualityCustomBitrate.orZero * 1024, mEncodingConfig?.mVideoQualityCustomMaxKeyFrameInterval.orZero, mEncodingConfig?.mVideoQualityCustomProfile)
            }

            // 设置推流编码尺寸
            if (mEncodingConfig?.mIsVideoSizePreset.orFalse) {
                // 使用预设的视频尺寸
                // 预设尺寸可以参考 https://developer.qiniu.com/pili/sdk/3719/PLDroidMediaStreaming-function-using#4 的 4.7 小节
                mProfile?.setEncodingSizeLevel(mEncodingConfig?.mVideoSizePreset.orZero)
            } else {
                // 使用自定义视频编码尺寸，自定义配置优先级高于预设等级配置
                mProfile?.setPreferredVideoEncodingSize(mEncodingConfig?.mVideoSizeCustomWidth.orZero, mEncodingConfig?.mVideoSizeCustomHeight.orZero)
            }

            // 设置推流 Orientation
            mProfile?.setEncodingOrientation(if (mEncodingConfig?.mVideoOrientationPortrait.orFalse) StreamingProfile.ENCODING_ORIENTATION.PORT else StreamingProfile.ENCODING_ORIENTATION.LAND)
            // 软编场景下设置码流控制方式
            // QUALITY_PRIORITY 场景下为了保证推流质量，实际码率可能会高于目标码率
            // BITRATE_PRIORITY 场景下，会优先保证目标码率的稳定性
            mProfile?.setEncoderRCMode(if (mEncodingConfig?.mVideoRateControlQuality.orFalse) StreamingProfile.EncoderRCModes.QUALITY_PRIORITY else StreamingProfile.EncoderRCModes.BITRATE_PRIORITY)
            // 设置是否开启帧率控制
            mProfile?.setFpsControllerEnable(mEncodingConfig?.mVideoFPSControl.orFalse)
            mProfile?.setYuvFilterMode(mEncodingConfig?.mYuvFilterMode)
            // 设置码率调整模式，如果开启自适应码率，则需指定自适应码率的上下限（当前仅支持 150kbps ~ 2000kbps 区间内的设置）。
            mProfile?.setBitrateAdjustMode(mEncodingConfig?.mBitrateAdjustMode)
            if (mEncodingConfig?.mBitrateAdjustMode == StreamingProfile.BitrateAdjustMode.Auto) {
                mProfile?.setVideoAdaptiveBitrateRange(mEncodingConfig?.mAdaptiveBitrateMin.orZero * 1024, mEncodingConfig?.mAdaptiveBitrateMax.orZero * 1024)
            }
        }
        // 设置视频编码格式（H.264/H.265）
        mProfile?.setVideoEncodeType(mEncodingConfig?.mVideoEncodeType)

        // 设置音频质量参数
        if (mEncodingConfig?.mIsAudioQualityPreset.orFalse) {
            // 使用预设的音频质量等级
            // 预设等级可以参考 https://developer.qiniu.com/pili/sdk/3719/PLDroidMediaStreaming-function-using#4 的 4.3 小节
            mProfile?.setAudioQuality(mEncodingConfig?.mAudioQualityPreset.orZero)
        } else {
            // 使用自定义音频质量参数
            aProfile = StreamingProfile.AudioProfile(mEncodingConfig?.mAudioQualityCustomSampleRate.orZero, mEncodingConfig?.mAudioQualityCustomBitrate.orZero * 1024)
        }

        // 传入自定义音视频质量配置
        if (aProfile != null || vProfile != null) {
            val avProfile = AVProfile(vProfile, aProfile)
            mProfile?.setAVProfile(avProfile)
        }

        // 设置图片推流的图片地址
        if (mEncodingConfig?.mIsPictureStreamingEnabled.orFalse) {
            if (mEncodingConfig?.mPictureStreamingFilePath == null) {
                mProfile?.setPictureStreamingResourceId(R.drawable.pause_publish)
            } else {
                mProfile?.setPictureStreamingFilePath(mEncodingConfig?.mPictureStreamingFilePath)
            }
        }

        // 其他配置项
        mProfile?.setDnsManager(Util.getMyDnsManager())
            ?.setStreamStatusConfig(StreamStatusConfig(3))
            ?.setSendingBufferProfile(SendingBufferProfile(0.2f, 0.8f, 3.0f, (20 * 1000).toLong()))
    }

    /**
     * 初始化推流管理类
     */
    private fun initStreamingManager() {
        val screenSetting = ScreenSetting()
        screenSetting.setSize(mEncodingConfig?.mVideoSizeCustomWidth.orZero, mEncodingConfig?.mVideoSizeCustomHeight.orZero)
        screenSetting.setDpi(1)
        mScreenStreamingManager = ScreenStreamingManager()
        mScreenStreamingManager?.setStreamingSessionListener(mStreamingSessionListener)
        mScreenStreamingManager?.setStreamingStateListener(mStreamingStateChangedListener)
        mScreenStreamingManager?.setStreamStatusCallback(mStreamStatusCallback)
        mScreenStreamingManager?.setNativeLoggingEnabled(false)
        mScreenStreamingManager?.setNotification(NOTIFICATION_ID, createNotification())
        mScreenStreamingManager?.prepare(this, screenSetting, null, mProfile)
    }

    /**
     * 某些特定推流事件的回调接口
     */
    private val mStreamingSessionListener = object : StreamingSessionListener {
        /**
         * 音频采集失败时回调此接口
         *
         * @param code 错误码
         * @return true 表示您已处理该事件，反之则表示未处理
         */
        override fun onRecordAudioFailedHandled(code: Int): Boolean {
            return false
        }

        /**
         * 重连提示回调，当收到此回调时，您可以在这里进行重连的操作
         *
         * 当网络不可达时，首先会回调 StreamingState#DISCONNECTED 状态，当重连环境准备好时会回调此方法
         *
         * @param code 错误码
         * @return true 表示您已处理该事件，反之则表示未处理，未处理则会触发 StreamingState#SHUTDOWN 状态回调
         */
        override fun onRestartStreamingHandled(code: Int): Boolean {
            "onRestartStreamingHandled".logI(TAG)
            return false
        }

        /**
         * 相机支持的采集分辨率回调，外部导入推流无需处理
         */
        override fun onPreviewSizeSelected(list: List<Camera.Size>): Camera.Size? {
            return null
        }

        /**
         * 相机支持的采集帧率列表，外部导入推流无需处理
         */
        override fun onPreviewFpsSelected(list: List<IntArray>): Int {
            return 0
        }
    }

    /**
     * 码流信息回调，回调当前推流的音视频码率、帧率等信息
     *
     * 注意：回调在非 UI 线程，UI 操作需要做特殊处理！！！
     */
    private val mStreamStatusCallback =
        StreamStatusCallback { streamStatus ->
            runOnUiThread {
                mStatView?.text = "bitrate:${streamStatus.totalAVBitrate / 1024} kbps audio:${streamStatus.audioFps} fps video:${streamStatus.videoFps} fps dropped:${streamStatus.droppedVideoFrames}"
            }
        }

    /**
     * 推流状态改变时的回调
     */
    private val mStreamingStateChangedListener = StreamingStateChangedListener { streamingState, extra ->
        "StreamingState streamingState:$streamingState,extra:$extra".logI(TAG)
        when (streamingState) {
            StreamingState.PREPARING -> mStatusMsgContent = getString(R.string.string_state_preparing)
            StreamingState.READY -> {
                /**
                 * 注意：开启推流的操作需要在 READY 状态后！！！
                 */
                mIsReady = true
                mStatusMsgContent = getString(R.string.string_state_ready)
            }
            StreamingState.CONNECTING -> mStatusMsgContent = getString(R.string.string_state_connecting)
            StreamingState.STREAMING -> {
                mStatusMsgContent = getString(R.string.string_state_streaming)
                setShutterButtonEnabled(true)
                setShutterButtonPressed(true)
            }
            StreamingState.SHUTDOWN -> {
                mStatusMsgContent = getString(R.string.string_state_ready)
                setShutterButtonEnabled(true)
                setShutterButtonPressed(false)
            }
            StreamingState.IOERROR -> {
                /**
                 * 在 `startStreaming` 时，如果网络不可用，则会回调此状态
                 * 您可以在适当延时后重新推流或者就此停止推流
                 */
                mLogContent += "IOERROR\n"
                mStatusMsgContent = getString(R.string.string_state_ready)
                setShutterButtonEnabled(true)
                startStreamingInternal(2000)
            }
            StreamingState.DISCONNECTED ->
                /**
                 * 网络连接断开时触发，收到此回调后，您可以在 `onRestartStreamingHandled` 回调里处理重连逻辑
                 */
                mLogContent += "DISCONNECTED\n"
            StreamingState.UNKNOWN -> mStatusMsgContent = getString(R.string.string_state_ready)
            StreamingState.VIDEO_ENCODER_READY -> runOnUiThread {
                ToastUtils.s(applicationContext, "编码器初始化完成：" + (extra as PLVideoEncodeType).name)
            }
            StreamingState.SENDING_BUFFER_EMPTY, StreamingState.SENDING_BUFFER_FULL, StreamingState.AUDIO_RECORDING_FAIL -> {}
            StreamingState.INVALID_STREAMING_URL -> "Invalid streaming url:$extra".logE(TAG)
            StreamingState.UNAUTHORIZED_STREAMING_URL -> {
                "Unauthorized streaming url:$extra".logE(TAG)
                mLogContent += "Unauthorized Url\n"
            }
            StreamingState.UNAUTHORIZED_PACKAGE -> mLogContent += "Unauthorized package\n"
            StreamingState.REQUEST_SCREEN_CAPTURING_FAIL -> Toast.makeText(this@ScreenStreamingActivity, "Request screen capturing fail", Toast.LENGTH_LONG).show()
            else -> {}
        }
        runOnUiThread {
            if (mLogTextView != null) {
                mLogTextView?.text = mLogContent
            }
            mStatusTextView?.text = mStatusMsgContent
        }
    }

    private fun createNotification(): Notification {
        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this.applicationContext, "screenRecorder")
        } else {
            Notification.Builder(this.applicationContext)
        }
        val intent = Intent(this, ScreenStreamingActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        builder.setSmallIcon(R.drawable.qiniu_logo)
            .setContentTitle("七牛推流")
            .setContentText("正在录屏ing")
            .setContentIntent(pendingIntent)
            .setShowWhen(true)
            .setVisibility(Notification.VISIBILITY_PRIVATE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as? NotificationManager
            val channel = NotificationChannel("screenRecorder", "screenRecorder", NotificationManager.IMPORTANCE_HIGH)
            notificationManager?.createNotificationChannel(channel)
        }
        return builder.build()
    }

    private val mUpdateTimeTvRunnable = object : Runnable {
        override fun run() {
            mTimeTv?.text = mDateFormat?.format(Date())
            mHandler.postDelayed(this, 100)
        }
    }

    /**
     * 在图片推流过程中切换图片，仅供 demo 演示，您可以根据产品定义自行实现
     */
    private class ImageSwitcher : Runnable {
        override fun run() {
            if (!mIsPictureStreaming) {
                "is not picture streaming!!!".logD(TAG)
                return
            }
            if (mTimes % 2 == 0) {
                if (mEncodingConfig?.mPictureStreamingFilePath != null) {
                    mScreenStreamingManager?.setPictureStreamingFilePath(mEncodingConfig?.mPictureStreamingFilePath)
                } else {
                    mScreenStreamingManager?.setPictureStreamingResourceId(R.drawable.qiniu_logo)
                }
            } else {
                mScreenStreamingManager?.setPictureStreamingResourceId(R.drawable.pause_publish)
            }
            mTimes++
            if (mSubThreadHandler != null) {
                mSubThreadHandler?.postDelayed(this, 1000)
            }
        }
    }

    /**
     * 切换图片推流
     *
     * 注意：该场景下图片推流为耗时操作，需要放到子线程执行
     */
    private fun togglePictureStreaming() {
        mSubThreadHandler?.post(Runnable {
            val isOK = mScreenStreamingManager?.togglePictureStreaming().orFalse
            if (!isOK) {
                Toast.makeText(this@ScreenStreamingActivity, "toggle picture streaming failed!", Toast.LENGTH_SHORT).show()
                return@Runnable
            }
            mIsPictureStreaming = !mIsPictureStreaming
            mTimes = 0
            if (mIsPictureStreaming) {
                if (mImageSwitcher == null) {
                    mImageSwitcher = ImageSwitcher()
                }
                mSubThreadHandler?.postDelayed(mImageSwitcher ?: return@Runnable, 1000)
            }
        })
    }

    /**
     * 开始推流
     * 注意：开始推流的操作一定要在 onStateChanged.READY 状态回调后执行！！！
     */
    private fun startStreamingInternal() {
        startStreamingInternal(0)
    }

    private fun startStreamingInternal(delayMillis: Long) {
        if (mScreenStreamingManager == null) {
            return
        }
        setShutterButtonEnabled(false)
        // startStreaming 为耗时操作，建议放到子线程执行
        if (mSubThreadHandler != null) {
            mSubThreadHandler?.postDelayed({
                val res = mScreenStreamingManager?.startStreaming().orFalse
                runOnUiThread {
                    setShutterButtonPressed(res)
                    if (!res) {
                        setShutterButtonEnabled(true)
                    }
                }
            }, delayMillis)
        }
    }

    /**
     * 停止推流
     */
    private fun stopStreamingInternal() {
        if (mShutterButtonPressed && mSubThreadHandler != null) {
            // disable the shutter button before stopStreaming
            setShutterButtonEnabled(false)
            mSubThreadHandler?.post {
                val res = mScreenStreamingManager?.stopStreaming().orFalse
                runOnUiThread {
                    if (!res) {
                        mShutterButtonPressed = true
                        setShutterButtonEnabled(true)
                    }
                    setShutterButtonPressed(mShutterButtonPressed)
                }
            }
        }
    }

    private fun setShutterButtonEnabled(enable: Boolean) {
        runOnUiThread {
            mShutterButton?.isFocusable = enable
            mShutterButton?.isClickable = enable
            mShutterButton?.setEnabled(enable)
        }
    }

    private fun setShutterButtonPressed(pressed: Boolean) {
        runOnUiThread {
            mShutterButtonPressed = pressed
            mShutterButton?.setPressed(pressed)
        }
    }

}