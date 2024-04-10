package com.example.qiniu.activity

import android.app.Activity
import android.hardware.Camera
import android.os.Bundle
import com.example.framework.utils.logE
import com.example.framework.utils.logI
import com.example.qiniu.R
import com.example.qiniu.widget.CameraPreviewFrameView
import com.qiniu.pili.droid.streaming.AVCodecType
import com.qiniu.pili.droid.streaming.AudioSourceCallback
import com.qiniu.pili.droid.streaming.CameraStreamingSetting
import com.qiniu.pili.droid.streaming.MediaStreamingManager
import com.qiniu.pili.droid.streaming.StreamStatusCallback
import com.qiniu.pili.droid.streaming.StreamingProfile
import com.qiniu.pili.droid.streaming.StreamingSessionListener
import com.qiniu.pili.droid.streaming.StreamingState
import com.qiniu.pili.droid.streaming.StreamingStateChangedListener
import java.net.URISyntaxException
import java.nio.ByteBuffer

/**
 * 七牛云简易推流demo
 * https://developer.qiniu.com/pili/3718/PLDroidMediaStreaming-quick-start
 *  <uses-permission android:name="android.permission.INTERNET" />
 *  <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
 *  <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
 *  <uses-permission android:name="android.permission.RECORD_AUDIO" />
 *  <uses-permission android:name="android.permission.CAMERA" />
 *  <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
 *  <uses-permission android:name="android.permission.MODIFY_AUDIO_SETTINGS" />
 *  <uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
 *  <uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
 *  <uses-feature android:name="android.hardware.camera.autofocus" />
 *  <uses-feature
 *      android:glEsVersion="0x00020000"
 *      android:required="true" />
 */
class StreamingByCameraActivity : Activity(), StreamingStateChangedListener, StreamStatusCallback, AudioSourceCallback, StreamingSessionListener {
    private var mCameraPreviewSurfaceView: CameraPreviewFrameView? = null
    private var mMediaStreamingManager: MediaStreamingManager? = null
    private var mProfile: StreamingProfile? = null
    private val TAG = "StreamingByCameraActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_swcamera_streaming)
        init()
    }

    private fun init() {
        //get form you server
        val publishURLFromServer = "rtmpp://xxxx/xx/x"
        mCameraPreviewSurfaceView = findViewById(R.id.cameraPreview_surfaceView)
        try {
            //encoding setting
            mProfile = StreamingProfile()
            mProfile?.setVideoQuality(StreamingProfile.VIDEO_QUALITY_HIGH1)
                ?.setAudioQuality(StreamingProfile.AUDIO_QUALITY_MEDIUM2)
                ?.setEncodingSizeLevel(StreamingProfile.VIDEO_ENCODING_HEIGHT_480)
                ?.setEncoderRCMode(StreamingProfile.EncoderRCModes.QUALITY_PRIORITY)
                ?.setPublishUrl(publishURLFromServer)
            //preview setting
            val camerasetting = CameraStreamingSetting()
            camerasetting.setCameraId(Camera.CameraInfo.CAMERA_FACING_BACK)
                .setContinuousFocusModeEnabled(true)
                .setCameraPrvSizeLevel(CameraStreamingSetting.PREVIEW_SIZE_LEVEL.MEDIUM)
                .setCameraPrvSizeRatio(CameraStreamingSetting.PREVIEW_SIZE_RATIO.RATIO_16_9)
            //streaming engine init and setListener
            mMediaStreamingManager = MediaStreamingManager(this, mCameraPreviewSurfaceView, AVCodecType.SW_VIDEO_WITH_SW_AUDIO_CODEC) // soft codec
            mMediaStreamingManager?.prepare(camerasetting, mProfile)
            mMediaStreamingManager?.setStreamingStateListener(this)
            mMediaStreamingManager?.setStreamingSessionListener(this)
            mMediaStreamingManager?.setStreamStatusCallback(this)
            mMediaStreamingManager?.setAudioSourceCallback(this)
        } catch (e: URISyntaxException) {
            e.printStackTrace()
        }
    }

    override fun onResume() {
        super.onResume()
        mMediaStreamingManager?.resume()
    }

    override fun onPause() {
        super.onPause()
        mMediaStreamingManager?.pause()
    }

    override fun onBackPressed() {
        super.onBackPressed()
//        val intent = Intent(this, MainActivity::class.java)
//        startActivity(intent)
    }

    override fun onStateChanged(streamingState: StreamingState?, extra: Any?) {
        "streamingState = $streamingState" + "extra = $extra".logE(TAG)
        when (streamingState) {
            StreamingState.PREPARING -> "PREPARING".logE(TAG)
            StreamingState.READY -> {
                "READY".logE(TAG)
                // start streaming when READY
                Thread {
                    if (mMediaStreamingManager != null) {
                        mMediaStreamingManager!!.startStreaming()
                    }
                }.start()
            }
            StreamingState.CONNECTING -> "连接中".logE(TAG)
            StreamingState.STREAMING -> "推流中".logE(TAG)
            StreamingState.SHUTDOWN -> "直播中断".logE(TAG)
            StreamingState.IOERROR -> "网络连接失败".logE(TAG)
            StreamingState.OPEN_CAMERA_FAIL -> "摄像头打开失败".logE(TAG)
            StreamingState.DISCONNECTED -> "已经断开连接".logE(TAG)
            StreamingState.TORCH_INFO -> "开启闪光灯".logE(TAG)
            else -> {}
        }
    }

    override fun notifyStreamStatusChanged(status: StreamingProfile.StreamStatus?) {
        "StreamStatus = $status".logE(TAG)
    }

    override fun onAudioSourceAvailable(srcBuffer: ByteBuffer?, size: Int, tsInNanoTime: Long, isEof: Boolean) {
    }

    override fun onRecordAudioFailedHandled(code: Int): Boolean {
        "onRecordAudioFailedHandled".logE(TAG)
        return false;
    }

    override fun onRestartStreamingHandled(code: Int): Boolean {
        "onRestartStreamingHandled".logI(TAG)
        Thread {
            if (mMediaStreamingManager != null) {
                mMediaStreamingManager?.startStreaming()
            }
        }.start()
        return false
    }

    override fun onPreviewSizeSelected(list: MutableList<Camera.Size>?): Camera.Size? {
        return null
    }

    override fun onPreviewFpsSelected(list: MutableList<IntArray>?): Int {
        return -1
    }

}