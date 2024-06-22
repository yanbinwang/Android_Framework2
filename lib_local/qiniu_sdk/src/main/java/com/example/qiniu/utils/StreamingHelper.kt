package com.example.qiniu.utils

import android.hardware.Camera
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.example.qiniu.widget.CameraPreviewFrameView
import com.qiniu.pili.droid.streaming.AVCodecType
import com.qiniu.pili.droid.streaming.CameraStreamingSetting
import com.qiniu.pili.droid.streaming.MediaStreamingManager
import com.qiniu.pili.droid.streaming.StreamingProfile
import com.qiniu.pili.droid.streaming.StreamingState
import com.qiniu.pili.droid.streaming.StreamingStateChangedListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
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
 *  调取bind方法
 */
class StreamingHelper(owner: LifecycleOwner) : CoroutineScope, LifecycleEventObserver {
    private var publishURLFromServer: String? = null
    private var mMediaStreamingManager: MediaStreamingManager? = null
    private val job = SupervisorJob()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    init {
        owner.lifecycle.addObserver(this)
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
            mMediaStreamingManager = MediaStreamingManager(
                mCameraPreviewSurfaceView?.context,
                mCameraPreviewSurfaceView,
                AVCodecType.SW_VIDEO_WITH_SW_AUDIO_CODEC
            )  // soft codec
            mMediaStreamingManager?.prepare(camerasetting, mProfile)
            mMediaStreamingManager?.setStreamingStateListener(object :
                StreamingStateChangedListener{
                override fun onStateChanged(p0: StreamingState?, p1: Any?) {
                    TODO("Not yet implemented")
                }
            })
//            mMediaStreamingManager?.setStreamingSessionListener(this)
//            mMediaStreamingManager?.setStreamStatusCallback(this)
//            mMediaStreamingManager?.setAudioSourceCallback(this)
        } catch (_: Exception) {
        }
    }


    /**
     * 设置推流地址
     */
    fun setParams(publishURLFromServer: String) {
        this.publishURLFromServer = publishURLFromServer
    }

    /**
     * 生命周期监听
     */
    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
//            Lifecycle.Event.ON_RESUME -> resume()
//            Lifecycle.Event.ON_PAUSE -> pause()
//            Lifecycle.Event.ON_DESTROY -> destroy()
            else -> {}
        }
    }
}