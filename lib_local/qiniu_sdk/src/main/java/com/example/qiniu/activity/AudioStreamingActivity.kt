package com.example.qiniu.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.hardware.Camera
import android.media.AudioFormat
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.example.framework.utils.function.value.orFalse
import com.example.framework.utils.function.value.orZero
import com.example.framework.utils.logE
import com.example.framework.utils.logI
import com.example.qiniu.R
import com.example.qiniu.plain.EncodingConfig
import com.example.qiniu.utils.Config
import com.example.qiniu.utils.Util
import com.qiniu.pili.droid.streaming.AudioSourceCallback
import com.qiniu.pili.droid.streaming.MediaStreamingManager
import com.qiniu.pili.droid.streaming.MicrophoneStreamingSetting
import com.qiniu.pili.droid.streaming.StreamStatusCallback
import com.qiniu.pili.droid.streaming.StreamingProfile
import com.qiniu.pili.droid.streaming.StreamingProfile.AVProfile
import com.qiniu.pili.droid.streaming.StreamingProfile.SendingBufferProfile
import com.qiniu.pili.droid.streaming.StreamingProfile.StreamStatusConfig
import com.qiniu.pili.droid.streaming.StreamingSessionListener
import com.qiniu.pili.droid.streaming.StreamingState
import com.qiniu.pili.droid.streaming.StreamingStateChangedListener
import java.net.URISyntaxException

/**
 * 纯音频推流的样例代码
 */
@SuppressLint("SetTextI18n")
class AudioStreamingActivity : Activity() {
    private val TAG = "AudioStreamingActivity"
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
    private var mAudioStereoEnable = false
    private var mEncodingConfig: EncodingConfig? = null
    private var mMediaStreamingManager: MediaStreamingManager? = null
    private var mMicrophoneStreamingSetting: MicrophoneStreamingSetting? = null
    private var mProfile: StreamingProfile? = null
    private var mSubThreadHandler: Handler? = null// 用于处理子线程操作

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 获取推流编码配置信息
        mEncodingConfig = intent.getSerializableExtra(Config.NAME_ENCODING_CONFIG) as? EncodingConfig?

        val intent = intent
        mPublishUrl = intent.getStringExtra(Config.PUBLISH_URL)
        mIsQuicEnabled = intent.getBooleanExtra(Config.TRANSFER_MODE_QUIC, false)
        mIsSrtEnabled = intent.getBooleanExtra(Config.TRANSFER_MODE_SRT, false)
        mAudioStereoEnable = intent.getBooleanExtra(Config.AUDIO_CHANNEL_STEREO, false)

        val handlerThread = HandlerThread(TAG)
        handlerThread.start()
        mSubThreadHandler = Handler(handlerThread.getLooper())

        // 初始化视图控件
        initView()
        // 初始化麦克风采集配置
        initMicrophoneSetting()
        // 初始化 StreamingProfile，StreamingProfile 为推流相关的配置类，详情可参考 https://developer.qiniu.com/pili/sdk/3719/PLDroidMediaStreaming-function-using#4
        initEncodingProfile()
        // 初始化 MediaStreamingManager，使用姿势可参考 https://developer.qiniu.com/pili/sdk/3719/PLDroidMediaStreaming-function-using#6
        initStreamingManager()
    }

    override fun onResume() {
        super.onResume()
        mMediaStreamingManager?.resume()
    }

    override fun onPause() {
        super.onPause()
        mIsReady = false
        mShutterButtonPressed = false
        mMediaStreamingManager?.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mSubThreadHandler != null) {
            mSubThreadHandler?.looper?.quit()
        }
        // 销毁推流 Manager 的资源
        mMediaStreamingManager?.destroy()
    }

    /**
     * 初始化音频推流 demo 相关的视图控件
     */
    private fun initView() {
        setContentView(R.layout.activity_audio_streaming)
        mLogTextView = findViewById(R.id.log_info)
        mStatusTextView = findViewById(R.id.streamingStatus)
        mStatView = findViewById(R.id.stream_status)
        mShutterButton = findViewById(R.id.toggleRecording_button)
        mShutterButton?.setOnClickListener(View.OnClickListener {
            if (!mIsReady) {
                Toast.makeText(this@AudioStreamingActivity, "需要在 READY 状态后才可以开始推流！！！", Toast.LENGTH_SHORT).show()
                return@OnClickListener
            }
            if (mShutterButtonPressed) {
                stopStreamingInternal()
            } else {
                startStreamingInternal()
            }
        })
    }

    /**
     * 初始化编码配置项 {@link StreamingProfile}
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
        if (aProfile != null) {
            val avProfile = AVProfile(null, aProfile)
            mProfile?.setAVProfile(avProfile)
        }

        // 其他配置项
        mProfile?.setDnsManager(Util.getMyDnsManager())
            ?.setStreamStatusConfig(StreamStatusConfig(3))
            ?.setSendingBufferProfile(SendingBufferProfile(0.2f, 0.8f, 3.0f, (20 * 1000).toLong()))
    }

    /**
     * 初始化麦克风配置
     */
    private fun initMicrophoneSetting() {
        mMicrophoneStreamingSetting = MicrophoneStreamingSetting()
        if (mAudioStereoEnable) {
            /**
             * 注意 !!! [AudioFormat.CHANNEL_IN_STEREO] 并不能保证在所有设备上都可以正常运行.
             */
            mMicrophoneStreamingSetting?.setChannelConfig(AudioFormat.CHANNEL_IN_STEREO)
        }
    }

    /**
     * 初始化推流管理类
     */
    private fun initStreamingManager() {
        mMediaStreamingManager = MediaStreamingManager(this, mEncodingConfig?.mCodecType)
        mMediaStreamingManager?.prepare(null, mMicrophoneStreamingSetting, null, mProfile)
        mMediaStreamingManager?.setStreamingSessionListener(mStreamingSessionListener)
        mMediaStreamingManager?.setStreamStatusCallback(mStreamStatusCallback)
        mMediaStreamingManager?.setAudioSourceCallback(mAudioSourceCallback)
        mMediaStreamingManager?.setStreamingStateListener(mStreamingStateChangedListener)
    }

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
            startStreamingInternal(2000)
            return true
        }

        /**
         * 相机支持的采集分辨率回调，音频推流无需处理
         */
        override fun onPreviewSizeSelected(list: MutableList<Camera.Size>?): Camera.Size? {
            return null
        }

        /**
         * 相机支持的采集帧率列表，音频推流无需处理
         */
        override fun onPreviewFpsSelected(list: MutableList<IntArray>?): Int {
            return 0
        }
    }

    /**
     * 码流信息回调，回调当前推流的音视频码率、帧率等信息
     *
     * 注意：回调在非 UI 线程，UI 操作需要做特殊处理！！！
     */
    private val mStreamStatusCallback = StreamStatusCallback { streamStatus ->
        runOnUiThread {
            mStatView?.text = "bitrate:${streamStatus?.totalAVBitrate.orZero / 1024} kbps audio:${streamStatus?.audioFps} fps"
        }
    }

    /**
     * 音频采集数据的回调，您可以在此回调中处理音频数据，如变声等。
     */
    private val mAudioSourceCallback = AudioSourceCallback { srcBuffer, size, tsInNanoTime, isEof ->
        /**
         * 音频 buffer 回调
         *
         * @param srcBuffer 音频数据
         * @param size  音频数据的大小
         * @param tsInNanoTime 时间戳，单位：ns
         * @param isEof 是否是流结尾
         */
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
            StreamingState.SENDING_BUFFER_EMPTY, StreamingState.SENDING_BUFFER_FULL, StreamingState.AUDIO_RECORDING_FAIL -> {}
            StreamingState.INVALID_STREAMING_URL -> "Invalid streaming url:$extra".logE(TAG)
            StreamingState.UNAUTHORIZED_STREAMING_URL -> {
                "Unauthorized streaming url:$extra".logE(TAG)
                mLogContent += "Unauthorized Url\n"
            }
            StreamingState.UNAUTHORIZED_PACKAGE -> mLogContent += "Unauthorized package\n"
            else -> {}
        }
        runOnUiThread {
            if (mLogTextView != null) {
                mLogTextView?.text = mLogContent
            }
            mStatusTextView?.text = mStatusMsgContent
        }
    }

    /**
     * 开始推流
     * 注意：开始推流的操作一定要在 onStateChanged.READY 状态回调后执行！！！
     */
    private fun startStreamingInternal() {
        startStreamingInternal(0)
    }

    private fun startStreamingInternal(delayMillis: Long) {
        if (mMediaStreamingManager == null) {
            return
        }
        setShutterButtonEnabled(false)
        // startStreaming 为耗时操作，建议放到子线程执行
        if (mSubThreadHandler != null) {
            mSubThreadHandler?.postDelayed({
                val res = mMediaStreamingManager?.startStreaming().orFalse
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
                val res = mMediaStreamingManager?.stopStreaming().orFalse
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