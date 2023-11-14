package com.example.thirdparty.media.utils.helper

import android.annotation.SuppressLint
import android.content.Intent
import android.content.IntentFilter
import android.media.MediaActionSound
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.example.common.utils.builder.shortToast
import com.example.thirdparty.R
import com.example.thirdparty.media.service.KeyEventReceiver
import com.example.thirdparty.media.utils.MediaUtil.MediaType.IMAGE
import com.example.thirdparty.media.utils.MediaUtil.MediaType.VIDEO
import com.example.thirdparty.media.utils.MediaUtil
import com.otaliastudios.cameraview.CameraListener
import com.otaliastudios.cameraview.CameraView
import com.otaliastudios.cameraview.PictureResult
import com.otaliastudios.cameraview.VideoResult
import com.otaliastudios.cameraview.controls.Audio
import com.otaliastudios.cameraview.controls.Engine
import com.otaliastudios.cameraview.controls.Facing
import com.otaliastudios.cameraview.controls.Flash
import com.otaliastudios.cameraview.controls.Preview
import java.io.File

/**
 *  Created by wangyanbin
 *  相机帮助类
 *  https://github.com/natario1/CameraView
 */
@SuppressLint("UnspecifiedRegisterReceiverFlag")
class CameraHelper(private val activity: FragmentActivity, private val cvFinder: CameraView, private val hasReceiver: Boolean = false) : LifecycleEventObserver {
    private var onTakePictureListener: OnTakePictureListener? = null
    private var onTakeVideoListener: OnTakeVideoListener? = null
    private val sound by lazy { MediaActionSound() }
    private val keyEventReceiver by lazy { KeyEventReceiver() }

    init {
        activity.lifecycle.addObserver(this)
        cvFinder.setLifecycleOwner(activity)
        cvFinder.apply {
            keepScreenOn = true//是否保持屏幕高亮
            playSounds = true//录像是否录制声音
            useDeviceOrientation = false//禁止掉换
            audio = Audio.ON//录制开启声音
            engine = Engine.CAMERA2//相机底层类型
            preview = Preview.GL_SURFACE//绘制相机的装载控件
            facing = Facing.BACK//打开时镜头默认后置
            flash = Flash.AUTO//闪光灯自动
        }
        if (hasReceiver) {
            val intentFilter = IntentFilter()
            intentFilter.addAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)
            activity.registerReceiver(keyEventReceiver, intentFilter)
        }
    }

    /**
     * 镜头复位
     */
    fun reset() = run { cvFinder.zoom = 0f }

    /**
     * 镜头翻转
     */
    fun toggleFacing() {
        closeFlash()
        cvFinder.toggleFacing()
    }

    /**
     * 开关闪光灯
     */
    fun flash() {
        if (cvFinder.facing == Facing.FRONT) {
            R.string.camera_flash_error.shortToast()
        } else {
            cvFinder.apply { flash = if (flash == Flash.TORCH) Flash.OFF else Flash.TORCH }
            onTakePictureListener?.onFlash(cvFinder.flash == Flash.TORCH)
            onTakeVideoListener?.onFlash(cvFinder.flash == Flash.TORCH)
        }
    }

    /**
     * 关灯
     */
    fun closeFlash() {
        if (cvFinder.facing == Facing.BACK) {
            cvFinder.flash = Flash.OFF
            onTakePictureListener?.onFlash(false)
            onTakeVideoListener?.onFlash(false)
        }
    }

    /**
     * 拍照
     */
    fun takePicture(snapshot: Boolean = true) {
        cvFinder.apply {
            if (isTakingPicture) {
                R.string.camera_picture_shutter.shortToast()
                return
            }
            sound.play(MediaActionSound.SHUTTER_CLICK)
            if (snapshot) takePictureSnapshot() else takePicture()
            addCameraListener(object : CameraListener() {
                override fun onPictureShutter() {
                    super.onPictureShutter()
                    onTakePictureListener?.onShutter()
                }

                override fun onPictureTaken(result: PictureResult) {
                    super.onPictureTaken(result)
                    //在sd卡的Picture文件夹下创建对应的文件
                    MediaUtil.getOutputFile(IMAGE).apply {
                        if (null != this) {
                            result.toFile(this) { if (null != it) onTakePictureListener?.onSuccess(it) else onTakePictureListener?.onFailed() }
                        } else {
                            onTakePictureListener?.onFailed()
                        }
                    }
                }
            })
        }
    }

    /**
     * 开始录像
     */
    fun takeVideo(snapshot: Boolean = true) {
        cvFinder.apply {
            if (isTakingVideo) {
                R.string.camera_video_shutter.shortToast()
                return
            }
            val videoFile = MediaUtil.getOutputFile(VIDEO)
            if (null != videoFile) {
                if (snapshot) takeVideoSnapshot(videoFile) else takeVideo(videoFile)
                addCameraListener(object : CameraListener() {
                    override fun onVideoRecordingStart() {
                        super.onVideoRecordingStart()
                        onTakeVideoListener?.onRecording(videoFile.absolutePath)
                    }

                    //stopVideo方法按下后会触发，此刻可能正在处理录制的文件，onVideoTaken并不会立刻调取
                    override fun onVideoRecordingEnd() {
                        super.onVideoRecordingEnd()
                        onTakeVideoListener?.onShutter()
                    }

                    //正式完成录制的回调，获取路径
                    override fun onVideoTaken(result: VideoResult) {
                        super.onVideoTaken(result)
                        onTakeVideoListener?.onResult(result.file.path)
                    }
                })
            } else onTakeVideoListener?.onResult(null)
        }
    }

    /**
     * 停止录像
     */
    fun stopVideo() {
        cvFinder.stopVideo()
    }

    /**
     * 设置监听
     */
    fun setOnTakePictureListener(onTakePictureListener: OnTakePictureListener) {
        this.onTakePictureListener = onTakePictureListener
    }

    fun setOnTakeVideoListener(onTakeVideoListener: OnTakeVideoListener) {
        this.onTakeVideoListener = onTakeVideoListener
    }

    interface OnTakePictureListener {
        fun onShutter()

        fun onSuccess(sourceFile: File?)

        fun onFailed()

        fun onFlash(isOpen: Boolean)
    }

    interface OnTakeVideoListener {
        fun onRecording(sourcePath: String?)

        fun onShutter()

        fun onResult(sourcePath: String?)

        fun onFlash(isOpen: Boolean)
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            Lifecycle.Event.ON_PAUSE -> closeFlash()
            Lifecycle.Event.ON_DESTROY -> {
                if (hasReceiver) activity.unregisterReceiver(keyEventReceiver)
                activity.lifecycle.removeObserver(this)
            }
            else -> {}
        }
    }

}