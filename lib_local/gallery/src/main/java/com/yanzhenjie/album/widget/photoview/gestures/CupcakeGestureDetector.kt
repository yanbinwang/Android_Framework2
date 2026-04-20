package com.yanzhenjie.album.widget.photoview.gestures

import android.content.Context
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.ViewConfiguration
import com.example.framework.utils.function.value.orZero
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.sqrt

/**
 * Android 1.5 古董级手势检测器
 * 仅支持：单指拖动、快速滑动（惯性）
 * 不支持：双指缩放（isScaling固定返回false）
 * 所有老版本手势类的基类
 */
abstract class CupcakeGestureDetector(context: Context) : OnScaleDragListener {
    // 是否正在拖动图片
    private var mIsDragging = false
    // 速度追踪器（计算滑动速度，用于惯性）
    private var mVelocityTracker: VelocityTracker? = null
    // 系统滑动参数
    private val mConfiguration = ViewConfiguration.get(context)
    // 系统判定为滑动的最小距离（小于这个值不算拖动）
    private val mTouchSlop = mConfiguration.scaledTouchSlop
    // 触发惯性滑动的最小速度
    private val mMinimumVelocity = mConfiguration.scaledMinimumFlingVelocity
    // 上一次触摸的X/Y坐标
    protected var mLastTouchX = 0f
    protected var mLastTouchY = 0f
    // 手势监听回调
    protected var mListener: OnGestureListener? = null

    /**
     * 处理所有触摸事件
     * 实现：单指拖动 + 惯性滑动（Fling）
     */
    override fun onTouchEvent(ev: MotionEvent?): Boolean {
        ev ?: return false
        when (ev.action) {
            MotionEvent.ACTION_DOWN -> {
                // 初始化速度追踪器
                mVelocityTracker = VelocityTracker.obtain()
                mVelocityTracker?.addMovement(ev)
                // 记录初始触摸位置
                mLastTouchX = getActiveX(ev)
                mLastTouchY = getActiveY(ev)
                // 重置拖动状态
                mIsDragging = false
            }
            MotionEvent.ACTION_MOVE -> {
                val x = getActiveX(ev)
                val y = getActiveY(ev)
                // 计算移动偏移量
                val dx = x - mLastTouchX
                val dy = y - mLastTouchY
                // 如果还未判定为拖动，判断移动距离是否达到阈值
                if (!mIsDragging) {
                    // 勾股定理计算滑动距离，超过TouchSlop才算拖动
                    mIsDragging = sqrt(((dx * dx) + (dy * dy)).toDouble()) >= mTouchSlop
                }
                // 如果正在拖动，回调监听
                if (mIsDragging) {
                    mListener?.onDrag(dx, dy)
                    mLastTouchX = x
                    mLastTouchY = y
                    mVelocityTracker?.addMovement(ev)
                }
            }
            MotionEvent.ACTION_CANCEL -> {
                // 回收速度追踪器，避免内存泄漏
                mVelocityTracker?.recycle()
                mVelocityTracker = null
            }
            MotionEvent.ACTION_UP -> {
                if (mIsDragging) {
                    mLastTouchX = getActiveX(ev)
                    mLastTouchY = getActiveY(ev)
                    // 添加抬起事件并计算滑动速度
                    mVelocityTracker?.addMovement(ev)
                    mVelocityTracker?.computeCurrentVelocity(1000)
                    val vX = mVelocityTracker?.xVelocity.orZero
                    val vY = mVelocityTracker?.yVelocity.orZero
                    // 速度达标，触发惯性滑动
                    if (max(abs(vX), abs(vY)) >= mMinimumVelocity) {
                        mListener?.onFling(mLastTouchX, mLastTouchY, -vX, -vY)
                    }
                }
                // 回收速度追踪器
                mVelocityTracker?.recycle()
                mVelocityTracker = null
            }
        }
        return true
    }

    /**
     * 固定返回false：此类不支持缩放功能
     */
    override fun isScaling(): Boolean {
        return false
    }

    /**
     * 返回是否正在拖动
     */
    override fun isDragging(): Boolean {
        return mIsDragging
    }

    /**
     * 设置手势监听
     */
    override fun setOnGestureListener(listener: OnGestureListener) {
        mListener = listener
    }

    /**
     * 获取当前触摸点X/Y坐标（单指版本）
     */
    open fun getActiveX(ev: MotionEvent): Float {
        return ev.x
    }

    open fun getActiveY(ev: MotionEvent): Float {
        return ev.y
    }

}