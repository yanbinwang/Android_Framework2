package com.example.gallery.feature.durban.widget

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.ScaleGestureDetector.SimpleOnScaleGestureListener
import kotlin.math.pow

/**
 * 对外使用的裁剪View
 * 具备：单指拖动、双指缩放、双指旋转、双击放大
 */
@SuppressLint("ClickableViewAccessibility")
class GestureCropImageView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : CropImageView(context, attrs, defStyleAttr) {
    // 双击放大分步数（默认从最小到最大需要5次双击）
    private var mDoubleTapScaleSteps = 5
    // 双指操作的中心点坐标
    private var mMidPntX = 0f
    private var mMidPntY = 0f
    // 是否允许旋转、缩放
    private var mIsRotateEnabled = true
    private var mIsScaleEnabled = true
    // 系统缩放手势检测器
    private var mScaleDetector: ScaleGestureDetector? = null
    // 自定义旋转手势检测器
    private var mRotateDetector: RotationGestureDetector? = null
    // 系统手势检测器（拖动、双击、长按等）
    private var mGestureDetector: GestureDetector? = null

    companion object {
        // 双击放大动画时长
        private const val DOUBLE_TAP_ZOOM_DURATION = 200
    }

    /**
     * 初始化手势
     */
    init {
        setupGestureListeners()
    }

    /**
     * 触摸事件统一分发入口
     * 处理：按下取消动画、多点计算中心点、手势分发、抬起自动对齐
     */
    override fun onTouchEvent(event: MotionEvent): Boolean {
        // 手指按下时，取消所有正在执行的动画
        if ((event.action and MotionEvent.ACTION_MASK) == MotionEvent.ACTION_DOWN) {
            cancelAllAnimations()
        }
        // 双指操作时，计算两指中心点
        if (event.pointerCount > 1) {
            mMidPntX = (event.getX(0) + event.getX(1)) / 2
            mMidPntY = (event.getY(0) + event.getY(1)) / 2
        }
        // 分发事件给单击/双击/滑动检测器
        mGestureDetector?.onTouchEvent(event)
        // 开启缩放时，分发缩放事件
        if (mIsScaleEnabled) {
            mScaleDetector?.onTouchEvent(event)
        }
        // 开启旋转时，分发旋转事件
        if (mIsRotateEnabled) {
            mRotateDetector?.onTouchEvent(event)
        }
        // 手指抬起时，让图片自动对齐裁剪框
        if ((event.action and MotionEvent.ACTION_MASK) == MotionEvent.ACTION_UP) {
            setImageToWrapCropBounds()
        }
        return true
    }

    /**
     * 是否允许缩放
     */
    fun setScaleEnabled(scaleEnabled: Boolean) {
        mIsScaleEnabled = scaleEnabled
    }

    fun isScaleEnabled(): Boolean {
        return mIsScaleEnabled
    }

    /**
     * 是否允许旋转
     */
    fun setRotateEnabled(rotateEnabled: Boolean) {
        mIsRotateEnabled = rotateEnabled
    }

    fun isRotateEnabled(): Boolean {
        return mIsRotateEnabled
    }

    /**
     * 双击放大需要几步达到最大缩放
     */
    fun setDoubleTapScaleSteps(doubleTapScaleSteps: Int) {
        mDoubleTapScaleSteps = doubleTapScaleSteps
    }

    fun getDoubleTapScaleSteps(): Int {
        return mDoubleTapScaleSteps
    }

    /**
     * 初始化所有手势监听器
     */
    private fun setupGestureListeners() {
        mGestureDetector = GestureDetector(context, GestureListener(), null, true)
        mScaleDetector = ScaleGestureDetector(context, ScaleListener())
        mRotateDetector = RotationGestureDetector(RotateListener())
    }

    /**
     * 计算双击后的目标缩放值
     */
    private fun getDoubleTapTargetScale(): Float {
        return getCurrentScale() * (getMaxScale() / getMinScale()).toDouble().pow((1.0f / mDoubleTapScaleSteps).toDouble()).toFloat()
    }

    /**
     * 缩放手势监听
     */
    private inner class ScaleListener: SimpleOnScaleGestureListener(){

        override fun onScale(detector: ScaleGestureDetector): Boolean {
            // 执行双指缩放，以双指中心为中心点
            postScale(detector.getScaleFactor(), mMidPntX, mMidPntY)
            return true
        }
    }

    private inner class GestureListener: SimpleOnGestureListener(){

        override fun onDoubleTap(e: MotionEvent): Boolean {
            // 双击放大
            zoomImageToPosition(getDoubleTapTargetScale(), e.x, e.y, DOUBLE_TAP_ZOOM_DURATION.toLong())
            return super.onDoubleTap(e)
        }

        override fun onScroll(e1: MotionEvent?, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
            // 单指滑动拖动图片
            postTranslate(-distanceX, -distanceY)
            return true
        }
    }

    /**
     * 旋转手势监听
     */
    private inner class RotateListener: RotationGestureDetector.OnRotationGestureListener {
        override fun onRotation(rotationDetector: RotationGestureDetector): Boolean {
            // 执行双指旋转，以双指中心为旋转中心
            postRotate(rotationDetector.getAngle(), mMidPntX, mMidPntY)
            return true
        }
    }

}