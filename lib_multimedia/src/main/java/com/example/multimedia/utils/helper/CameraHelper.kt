package com.example.multimedia.utils.helper

import android.media.MediaActionSound
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.example.common.utils.builder.shortToast
import com.example.multimedia.R
import com.example.multimedia.utils.MediaType.IMAGE
import com.example.multimedia.utils.MediaType.VIDEO
import com.example.multimedia.utils.MultimediaUtil
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
class CameraHelper(private val cvFinder: CameraView) : LifecycleEventObserver {
    private val sound by lazy { MediaActionSound() }

    init {
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
    }

    /**
     * 绑定相机生命周期
     */
    fun addLifecycleObserver(owner: LifecycleOwner) {
        cvFinder.setLifecycleOwner(owner)
        owner.lifecycle.addObserver(this)
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
        if (cvFinder.facing == Facing.FRONT) R.string.camera_flash_error.shortToast()
        cvFinder.apply { flash = if (flash == Flash.TORCH) Flash.OFF else Flash.TORCH }
    }

    /**
     * 关灯
     */
    fun closeFlash() {
        if (cvFinder.facing == Facing.BACK) cvFinder.flash = Flash.OFF
    }

    /**
     * 拍照
     */
    fun takePicture(onStart: () -> Unit = {}, onShutter: () -> Unit = {}, onSuccess: (sourceFile: File?) -> Unit = {}, onFailed: () -> Unit = {}, snapshot: Boolean = true) {
        cvFinder.apply {
            if (isTakingPicture) {
                R.string.camera_picture_shutter.shortToast()
                return
            }
            sound.play(MediaActionSound.SHUTTER_CLICK)
            onStart()
            if (snapshot) takePictureSnapshot() else takePicture()
            addCameraListener(object : CameraListener() {
                override fun onPictureShutter() {
                    super.onPictureShutter()
                    onShutter()
                }

                override fun onPictureTaken(result: PictureResult) {
                    super.onPictureTaken(result)
                    //在sd卡的Picture文件夹下创建对应的文件
                    MultimediaUtil.getOutputFile(IMAGE).apply {
                        if (null != this) {
                            result.toFile(this) { if (null != it) onSuccess(it) else onFailed() }
                        } else {
                            onFailed()
                        }
                    }
                }
            })
        }
    }

    /**
     * 录像
     */
    fun takeVideo(onStart: () -> Unit = {}, onRecording: (sourcePath: String?) -> Unit = {}, onStop: (sourcePath: String?) -> Unit = {}, snapshot: Boolean = true) {
        cvFinder.apply {
            if (isTakingVideo) {
                R.string.camera_video_shutter.shortToast()
                return
            }
            val videoFile = MultimediaUtil.getOutputFile(VIDEO)
            if (null != videoFile) {
                onStart()
                if (snapshot) takeVideoSnapshot(videoFile) else takeVideo(videoFile)
                addCameraListener(object : CameraListener() {
                    //正式完成录制的回调，获取路径
                    override fun onVideoTaken(result: VideoResult) {
                        super.onVideoTaken(result)
                        onStop(result.file.path)
                    }

                    override fun onVideoRecordingStart() {
                        super.onVideoRecordingStart()
                        onRecording(videoFile.absolutePath)
                    }
                })
            } else onStop(null)
        }
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
//            Lifecycle.Event.ON_RESUME ->
            Lifecycle.Event.ON_PAUSE -> closeFlash()
            Lifecycle.Event.ON_DESTROY -> source.lifecycle.removeObserver(this)
            else -> {}
        }
    }

}