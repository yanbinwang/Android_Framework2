package com.example.qiniu.utils

import android.hardware.Camera
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.example.framework.utils.logE
import com.example.qiniu.widget.CameraPreviewFrameView
import com.qiniu.pili.droid.streaming.AVCodecType
import com.qiniu.pili.droid.streaming.CameraStreamingSetting
import com.qiniu.pili.droid.streaming.MediaStreamingManager
import com.qiniu.pili.droid.streaming.StreamingProfile
import com.qiniu.pili.droid.streaming.StreamingSessionListener
import com.qiniu.pili.droid.streaming.StreamingState.CONNECTING
import com.qiniu.pili.droid.streaming.StreamingState.DISCONNECTED
import com.qiniu.pili.droid.streaming.StreamingState.IOERROR
import com.qiniu.pili.droid.streaming.StreamingState.OPEN_CAMERA_FAIL
import com.qiniu.pili.droid.streaming.StreamingState.PREPARING
import com.qiniu.pili.droid.streaming.StreamingState.READY
import com.qiniu.pili.droid.streaming.StreamingState.SHUTDOWN
import com.qiniu.pili.droid.streaming.StreamingState.STREAMING
import com.qiniu.pili.droid.streaming.StreamingState.TORCH_INFO
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

/**
 * 七牛云推流帮助类
 * https://developer.qiniu.com/pili/3718/PLDroidMediaStreaming-quick-start
 * 1.application里初始化StreamingEnv.init(getApplicationContext(), Util.getUserId(getApplicationContext()));
 * 2.页面实现布局
 *  <com.qiniu.pili.droid.streaming.demo.ui.CameraPreviewFrameView
 *         android:id="@+id/cameraPreview_surfaceView"
 *         android:layout_width="match_parent"
 *         android:layout_height="match_parent"
 *         android:layout_gravity="center" />
 *  调取bind方法，开启直播
 */
class StreamingHelper(private val mActivity: FragmentActivity, private val publishURLFromServer: String) : CoroutineScope, LifecycleEventObserver {
    private var mMediaStreamingManager: MediaStreamingManager? = null
    private var streamingJob: Job? = null
    private val job = SupervisorJob()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    init {
        mActivity.lifecycle.addObserver(this)
    }

    /**
     * 绑定直播控件
     */
    fun bind(mCameraPreviewSurfaceView: CameraPreviewFrameView?) {
        try {
            //encoding setting
            val mProfile = StreamingProfile()
            mProfile.setVideoQuality(StreamingProfile.VIDEO_QUALITY_HIGH1)
                .setAudioQuality(StreamingProfile.AUDIO_QUALITY_MEDIUM2)
                .setEncodingSizeLevel(StreamingProfile.VIDEO_ENCODING_HEIGHT_480)
                .setEncoderRCMode(StreamingProfile.EncoderRCModes.QUALITY_PRIORITY)
                .setPublishUrl(publishURLFromServer)
            //preview setting
            val camerasetting = CameraStreamingSetting();
            camerasetting.setCameraId(Camera.CameraInfo.CAMERA_FACING_BACK)
                .setContinuousFocusModeEnabled(true)
                .setCameraPrvSizeLevel(CameraStreamingSetting.PREVIEW_SIZE_LEVEL.MEDIUM)
                .setCameraPrvSizeRatio(CameraStreamingSetting.PREVIEW_SIZE_RATIO.RATIO_16_9)
            //streaming engine init and setListener
            mMediaStreamingManager = MediaStreamingManager(mCameraPreviewSurfaceView?.context, mCameraPreviewSurfaceView, AVCodecType.SW_VIDEO_WITH_SW_AUDIO_CODEC)  // soft codec
            mMediaStreamingManager?.prepare(camerasetting, mProfile)
            mMediaStreamingManager?.setStreamingStateListener { streamingState, extra ->
                "streamingState = $streamingState extra = $extra".logE
                when (streamingState) {
                    PREPARING -> "准备中".logE
                    READY -> {
                        "准备完毕".logE
                        startStreaming()
                    }
                    CONNECTING -> "连接中".logE
                    STREAMING -> "推流中".logE
                    SHUTDOWN -> "直播中断".logE
                    IOERROR -> "网络连接失败".logE
                    OPEN_CAMERA_FAIL -> "摄像头打开失败".logE
                    DISCONNECTED -> "已经断开连接".logE
                    TORCH_INFO -> "开启闪光灯".logE
                    else -> {}
                }
            }
            mMediaStreamingManager?.setStreamingSessionListener(object : StreamingSessionListener {
                override fun onRecordAudioFailedHandled(code: Int): Boolean {
                    return false
                }

                override fun onRestartStreamingHandled(code: Int): Boolean {
                    startStreaming()
                    return false
                }

                override fun onPreviewSizeSelected(list: MutableList<Camera.Size>?): Camera.Size? {
                    return null
                }

                override fun onPreviewFpsSelected(list: MutableList<IntArray>?): Int {
                    return -1
                }
            })
            mMediaStreamingManager?.setStreamStatusCallback { status -> "StreamStatus = $status".logE }
            mMediaStreamingManager?.setAudioSourceCallback { srcBuffer, size, tsInNanoTime, isEof -> }
        } catch (_: Exception) {
        }
    }

    private fun startStreaming() {
        streamingJob?.cancel()
        streamingJob = launch(IO) {
            mMediaStreamingManager?.startStreaming()
        }
    }

    /**
     * 生命周期监听
     */
    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            Lifecycle.Event.ON_RESUME -> resume()
            Lifecycle.Event.ON_PAUSE -> pause()
            Lifecycle.Event.ON_DESTROY -> destroy()
            else -> {}
        }
    }

    /**
     * 暂停
     */
    private fun resume() {
        mMediaStreamingManager?.resume()
    }

    /**
     * 加载
     */
    private fun pause() {
        mMediaStreamingManager?.pause()
    }

    /**
     * 销毁
     */
    private fun destroy() {
        streamingJob?.cancel()
        job.cancel()
        mActivity.lifecycle.removeObserver(this)
    }

}