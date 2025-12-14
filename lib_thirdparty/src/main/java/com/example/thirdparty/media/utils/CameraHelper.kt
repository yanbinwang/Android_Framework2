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
import com.example.thirdparty.media.service.receiver.KeyEventReceiver
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

/**
 *  Created by wangyanbin
 *  相机帮助类
 *  https://github.com/natario1/CameraView
 */
@SuppressLint("UnspecifiedRegisterReceiverFlag")
class CameraHelper(private val observer: LifecycleOwner, private val hasReceiver: Boolean = false) : LifecycleEventObserver {
    private var isPictureShutter = false
    private var sourcePath: String? = null // 源文件路径->拍照模式记录的是上一次的图片路径，录像记录的是上一次预创建的路径---》每次都会覆盖
    private var cvFinder: CameraView? = null
    private var onTakePictureListener: OnTakePictureListener? = null
    private var onTakeVideoListener: OnTakeVideoListener? = null
    private val mSound by lazy { MediaActionSound() }
    private val mReceiver by lazy { KeyEventReceiver() }
    private val mContext get() = cvFinder?.context

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
    fun bind(cvFinder: CameraView?) {
        this.cvFinder = cvFinder
        cvFinder?.apply {
            // 绑定生命周期
            setLifecycleOwner(observer)
            // 是否保持屏幕高亮
            keepScreenOn = true
            // 录像是否录制声音
            playSounds = true
            // 禁止掉换
            useDeviceOrientation = false
            // 录制开启声音
            audio = Audio.ON
            // 相机底层类型
            engine = Engine.CAMERA2
            // 绘制相机的装载控件
            preview = Preview.GL_SURFACE
            // 打开时镜头默认后置
            facing = Facing.BACK
            // 闪光灯自动
            flash = Flash.AUTO
            // 区分页面传入的相机view属于哪种模式
            if (mode == Mode.PICTURE) {
                addCameraListener(object : CameraListener() {
                    override fun onPictureShutter() {
                        super.onPictureShutter()
                        isPictureShutter = true
                        onTakePictureListener?.onShutter()
                    }

                    override fun onPictureTaken(result: PictureResult) {
                        super.onPictureTaken(result)
                        // 部分手机不会进入onPictureShutter监听
                        if (!isPictureShutter) {
                            onTakePictureListener?.onShutter()
                        }
                        // 在sd卡的Picture文件夹下创建对应的文件
                        val outputFile = StorageUtil.getOutputFile(IMAGE)
                        if (null != outputFile) {
                            result.toFile(outputFile) {
                                if (it?.exists().orFalse) {
                                    sourcePath = it?.absolutePath
                                    onTakePictureListener?.onTaken(sourcePath)
                                } else {
                                    onTakePictureListener?.onTaken(null)
                                }
                            }
                        } else {
                            onTakePictureListener?.onTaken(null)
                        }
                    }
                })
            } else {
                addCameraListener(object : CameraListener() {
                    // startVideo正式开启录制
                    override fun onVideoRecordingStart() {
                        super.onVideoRecordingStart()
                        onTakeVideoListener?.onRecordingStart(sourcePath)
                    }

                    // stopVideo方法按下后会触发，此刻可能正在处理录制的文件，onVideoTaken并不会立刻调取
                    override fun onVideoRecordingEnd() {
                        super.onVideoRecordingEnd()
                        onTakeVideoListener?.onRecordingEnd()
                    }

                    // 正式完成录制的回调，获取路径
                    override fun onVideoTaken(result: VideoResult) {
                        super.onVideoTaken(result)
                        onTakeVideoListener?.onTaken(result.file.path)
                    }
                })
            }
        }
        if (hasReceiver) {
//            val intentFilter = IntentFilter()
//            intentFilter.addAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)
//            mContext?.registerReceiver(eventReceiver, intentFilter)
            mContext.doOnReceiver(observer, mReceiver, IntentFilter().apply {
                addAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)
                addAction(Intent.ACTION_SCREEN_ON)
                addAction(Intent.ACTION_SCREEN_OFF)
            })
        }
    }

    /**
     * 是否正处于拍摄中的状态
     */
    fun isTaking(): Boolean {
        return if (cvFinder?.mode == Mode.PICTURE) {
            cvFinder?.isTakingPicture.orFalse
        } else {
            cvFinder?.isTakingVideo.orFalse
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
        if (isTaking()) return
        closeFlash()
        cvFinder?.toggleFacing()
    }

    /**
     * 开关闪光灯
     */
    fun flash() {
        if (isTaking()) return
        if (cvFinder?.facing == Facing.FRONT) {
            R.string.cameraFlashError.shortToast()
        } else {
            cvFinder?.let {
                it.flash = if (it.flash == Flash.TORCH) {
                    Flash.OFF
                } else {
                    Flash.TORCH
                }
            }
            onTakePictureListener?.onFlash(cvFinder?.flash == Flash.TORCH)
            onTakeVideoListener?.onFlash(cvFinder?.flash == Flash.TORCH)
        }
    }

    /**
     * 关灯
     */
    fun closeFlash() {
        if (isTaking()) return
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
        if (isTaking()) {
            R.string.cameraPictureShutter.shortToast()
            return
        }
        cvFinder?.let {
            mSound.play(MediaActionSound.SHUTTER_CLICK)
            isPictureShutter = false
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
        if (isTaking()) {
            R.string.cameraVideoShutter.shortToast()
            return
        }
        cvFinder?.let {
            val outputFile = StorageUtil.getOutputFile(VIDEO)
            if (null != outputFile) {
                sourcePath = outputFile.absolutePath
                if (snapshot) {
                    it.takeVideoSnapshot(outputFile)
                } else {
                    it.takeVideo(outputFile)
                }
            } else {
                onTakeVideoListener?.onTaken(null)
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
        /**
         * 开始存储
         */
        fun onShutter()

        /**
         * 拿取到文件
         */
        fun onTaken(sourcePath: String?)

        /**
         * 闪光灯开关
         */
        fun onFlash(isOpen: Boolean)
    }

    interface OnTakeVideoListener {
        /**
         * 开始录制->返回路径可以开一个协程或者计时器实时监控文件大小
         */
        fun onRecordingStart(sourcePath: String?)

        /**
         * 开始存储
         */
        fun onRecordingEnd()

        /**
         * 拿取到文件
         */
        fun onTaken(sourcePath: String?)

        /**
         * 闪光灯开关
         */
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
                sourcePath = null
                cvFinder = null
                source.lifecycle.removeObserver(this)
            }
            else -> {}
        }
    }

}