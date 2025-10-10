package com.example.thirdparty.media.service

import android.view.OrientationEventListener
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import kotlin.ranges.until

/**
 * 手机方向角监听器
 */
class OrientationObserver(mActivity: FragmentActivity) : LifecycleEventObserver {
    // 当前方向，默认0度
    private var currentOrientation = 0
    private var isListening = false
    // 回调监听器
    private var orientationChangedListener: OnOrientationChangedListener? = null
    private var sensorUnavailableListener: OnSensorUnavailableListener? = null
    private val orientationListener = object : OrientationEventListener(mActivity) {
        override fun onOrientationChanged(orientation: Int) {
            // orientation的范围是0-359，-1表示未就绪
            if (orientation == ORIENTATION_UNKNOWN) {
                return
            }
            // 将角度转换为固定的四个方向（0,90,180,270）
            val newOrientation = getFixedOrientation(orientation)
            // 只有方向发生变化时才通知回调
            if (newOrientation != currentOrientation) {
                currentOrientation = newOrientation
                orientationChangedListener?.onOrientationChanged(newOrientation)
            }
        }
    }

    init {
        mActivity.lifecycle.addObserver(this)
    }

    /**
     * 将0-359的角度转换为固定的四个方向
     */
    private fun getFixedOrientation(orientation: Int): Int {
        return when {
            // 正常竖屏
            orientation >= 315 || orientation < 45 -> 0
            // 向左横屏
            orientation in 45 until 135 -> 90
            // 上下颠倒
            orientation in 135 until 225 -> 180
            // 向右横屏
            else -> 270
        }
    }

    /**
     * 开始监听方向变化
     */
    fun startListening(onOrientationChanged: OnOrientationChangedListener? = null, onSensorUnavailable: OnSensorUnavailableListener? = null) {
        this.orientationChangedListener = onOrientationChanged
        this.sensorUnavailableListener = onSensorUnavailable
        if (orientationListener.canDetectOrientation()) {
            orientationListener.enable()
            isListening = true
        } else {
            // 无法检测方向，传感器不可用
            onSensorUnavailable?.onSensorUnavailable()
        }
    }

    /**
     * 停止监听方向变化
     */
    fun stopListening() {
        if (isListening) {
            orientationListener.disable()
            isListening = false
        }
        orientationChangedListener = null
        sensorUnavailableListener = null
    }

    /**
     * 获取当前方向
     */
    fun getCurrentOrientation(): Int {
        return currentOrientation
    }

    /**
     * 检查是否可以检测方向
     */
    fun canDetectOrientation(): Boolean {
        return orientationListener.canDetectOrientation()
    }

    /**
     * 方向变化回调接口
     */
    fun interface OnOrientationChangedListener {
        // 0, 90, 180, 270
        fun onOrientationChanged(orientation: Int)
    }

    /**
     * 传感器不可用回调接口
     */
    fun interface OnSensorUnavailableListener {
        fun onSensorUnavailable()
    }

    /**
     * 生命周期回调
     */
    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            Lifecycle.Event.ON_RESUME -> startListening()
            Lifecycle.Event.ON_PAUSE -> stopListening()
            Lifecycle.Event.ON_DESTROY -> {
                stopListening()
                source.lifecycle.removeObserver(this)
            }
            else -> {}
        }
    }

}