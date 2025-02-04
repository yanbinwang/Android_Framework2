package com.example.thirdparty.media.utils

import android.annotation.SuppressLint
import android.content.Intent
import android.content.IntentFilter
import android.media.MediaActionSound
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.example.common.utils.StorageUtil
import com.example.common.utils.StorageUtil.StorageType.IMAGE
import com.example.common.utils.StorageUtil.StorageType.VIDEO
import com.example.common.utils.builder.shortToast
import com.example.framework.utils.function.doOnReceiver
import com.example.framework.utils.function.value.orFalse
import com.example.thirdparty.R
import com.example.thirdparty.media.service.KeyEventReceiver
import com.otaliastudios.cameraview.CameraListener
import com.otaliastudios.cameraview.CameraView
import com.otaliastudios.cameraview.PictureResult
import com.otaliastudios.cameraview.VideoResult
import com.otaliastudios.cameraview.controls.Audio
import com.otaliastudios.cameraview.controls.Engine
import com.otaliastudios.cameraview.controls.Facing
import com.otaliastudios.cameraview.controls.Flash
import com.otaliastudios.cameraview.controls.Mode
import com.otaliastudios.cameraview.controls.Preview
import java.io.File

/**
 *  Created by wangyanbin
 *  相机帮助类
 *  https://github.com/natario1/CameraView
 */
@SuppressLint("UnspecifiedRegisterReceiverFlag")
class CameraHelper(private val observer: LifecycleOwner, private val hasReceiver: Boolean = false) : LifecycleEventObserver {
    private var sourcePath = ""
    private var cvFinder: CameraView? = null
    private var onTakePictureListener: OnTakePictureListener? = null
    private var onTakeVideoListener: OnTakeVideoListener? = null
    private val actionSound by lazy { MediaActionSound() }
    private val eventReceiver by lazy { KeyEventReceiver() }
    private val mContext get() = cvFinder?.context
    private val isTaking get() = isTakingPicture || isTakingVideo
    val isTakingPicture get() = cvFinder?.isTakingPicture.orFalse
    val isTakingVideo get() = cvFinder?.isTakingVideo.orFalse

    init {
        observer.lifecycle.addObserver(this)
    }

    /**
     * 绑定页面
     *     <com.otaliastudios.cameraview.CameraView
     *             android:id="@+id/camera"
     *             android:layout_width="match_parent"
     *             android:layout_height="match_parent"
     *             app:cameraAutoFocusMarker="@string/cameraview_default_autofocus_marker"
     *             app:cameraAutoFocusResetDelay="1"
     *             app:cameraGestureLongTap="autoFocus"
     *             app:cameraGesturePinch="zoom"
     *             app:cameraGestureTap="autoFocus"
     *             app:cameraMode="picture" />
     *
     *     <com.otaliastudios.cameraview.CameraView
     *         android:id="@+id/camera"
     *         android:layout_width="match_parent"
     *         android:layout_height="match_parent"
     *         app:cameraAutoFocusMarker="@string/cameraview_default_autofocus_marker"
     *         app:cameraAutoFocusResetDelay="1"
     *         app:cameraGestureLongTap="autoFocus"
     *         app:cameraGesturePinch="zoom"
     *         app:cameraGestureTap="autoFocus"
     *         app:cameraMode="video"
     *         app:cameraVideoCodec="h264" />
     */
    fun bind(cvFinder: CameraView) {
        this.cvFinder = cvFinder
        cvFinder.apply {
            setLifecycleOwner(observer)
            keepScreenOn = true//是否保持屏幕高亮
            playSounds = true//录像是否录制声音
            useDeviceOrientation = false//禁止掉换
            audio = Audio.ON//录制开启声音
            engine = Engine.CAMERA2//相机底层类型
            preview = Preview.GL_SURFACE//绘制相机的装载控件
            facing = Facing.BACK//打开时镜头默认后置
            flash = Flash.AUTO//闪光灯自动
            if (mode == Mode.PICTURE) {
                addCameraListener(object : CameraListener() {
                    override fun onPictureShutter() {
                        super.onPictureShutter()
                        onTakePictureListener?.onShutter()
                    }

                    override fun onPictureTaken(result: PictureResult) {
                        super.onPictureTaken(result)
                        //在sd卡的Picture文件夹下创建对应的文件
                        StorageUtil.getOutputFile(IMAGE).apply {
                            if (null != this) {
                                result.toFile(this) {
                                    if (null != it) {
                                        onTakePictureListener?.onSuccess(it)
                                    } else {
                                        onTakePictureListener?.onFailed()
                                    }
                                }
                            } else {
                                onTakePictureListener?.onFailed()
                            }
                        }
                    }
                })
            } else {
                addCameraListener(object : CameraListener() {
                    override fun onVideoRecordingStart() {
                        super.onVideoRecordingStart()
                        onTakeVideoListener?.onRecording(sourcePath)
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
            }
        }
        if (hasReceiver) {
//            val intentFilter = IntentFilter()
//            intentFilter.addAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)
//            mContext?.registerReceiver(eventReceiver, intentFilter)
            mContext.doOnReceiver(observer, eventReceiver, IntentFilter().apply { addAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS) })
        }
    }

    /**
     * 镜头复位
     */
    fun reset() {
        cvFinder?.zoom = 0f
    }

    /**
     * 镜头翻转
     */
    fun toggleFacing() {
        if (isTaking) return
        closeFlash()
        cvFinder?.toggleFacing()
    }

    /**
     * 开关闪光灯
     */
    fun flash() {
        if (isTaking) return
        if (cvFinder?.facing == Facing.FRONT) {
            R.string.cameraFlashError.shortToast()
        } else {
            cvFinder?.apply { flash = if (flash == Flash.TORCH) Flash.OFF else Flash.TORCH }
            onTakePictureListener?.onFlash(cvFinder?.flash == Flash.TORCH)
            onTakeVideoListener?.onFlash(cvFinder?.flash == Flash.TORCH)
        }
    }

    /**
     * 关灯
     */
    fun closeFlash() {
        if (isTaking) return
        if (cvFinder?.facing == Facing.BACK) {
            cvFinder?.flash = Flash.OFF
            onTakePictureListener?.onFlash(false)
            onTakeVideoListener?.onFlash(false)
        }
    }

    /**
     * 拍照
     */
    fun takePicture(snapshot: Boolean = true) {
        if (isTakingPicture) {
            R.string.cameraPictureShutter.shortToast()
            return
        }
        cvFinder?.let {
            actionSound.play(MediaActionSound.SHUTTER_CLICK)
            if (snapshot) {
                it.takePictureSnapshot()
            } else {
                it.takePicture()
            }
        }
    }

    /**
     * 开始录像
     */
    fun takeVideo(snapshot: Boolean = true) {
        if (isTakingVideo) {
            R.string.cameraVideoShutter.shortToast()
            return
        }
        cvFinder?.let {
            StorageUtil.getOutputFile(VIDEO).apply {
                if (null != this) {
                    sourcePath = absolutePath
                    if (snapshot) {
                        it.takeVideoSnapshot(this)
                    } else {
                        it.takeVideo(this)
                    }
                } else {
                    onTakeVideoListener?.onResult(null)
                }
            }
        }
    }

    /**
     * 停止录像
     */
    fun stopVideo() {
        cvFinder?.stopVideo()
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
//                if (hasReceiver.orFalse) {
//                    try {
//                        mContext?.unregisterReceiver(eventReceiver)
//                    } catch (_: Exception) {
//                    }
//                }
                cvFinder = null
                observer.lifecycle.removeObserver(this)
            }
            else -> {}
        }
    }

}