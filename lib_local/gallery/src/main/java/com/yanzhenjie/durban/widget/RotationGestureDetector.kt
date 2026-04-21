package com.yanzhenjie.durban.widget

import android.view.MotionEvent
import java.lang.Math.toDegrees
import kotlin.math.atan2

/**
 * 监听双指旋转手势
 * @mListener 旋转监听（外部接收角度）
 */
class RotationGestureDetector(private val mListener: OnRotationGestureListener?) {
    // 第1、第2个手指的索引（Android 多点触摸）
    private var mPointerIndex1 = 0
    private var mPointerIndex2 = 0

    // 第一个手指、第二个手指的坐标：f=第一个点，s=第二个点
    private var fX = 0f
    private var fY = 0f
    private var sX = 0f
    private var sY = 0f

    // 旋转角度
    private var mAngle = 0f

    // 是否是第一次触摸（避免刚按下就跳变）
    private var mIsFirstTouch = false

    companion object {
        // 无效手指索引（代表没触摸）
        private const val INVALID_POINTER_INDEX = -1
    }

    init {
        mPointerIndex1 = INVALID_POINTER_INDEX
        mPointerIndex2 = INVALID_POINTER_INDEX
    }

    /**
     * 处理触摸事件
     */
    fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                sX = event.x
                sY = event.y
                mPointerIndex1 = event.findPointerIndex(event.getPointerId(0))
                mAngle = 0f
                mIsFirstTouch = true
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                fX = event.x
                fY = event.y
                mPointerIndex2 = event.findPointerIndex(event.getPointerId(event.getActionIndex()))
                mAngle = 0f
                mIsFirstTouch = true
            }
            MotionEvent.ACTION_MOVE -> if (mPointerIndex1 != INVALID_POINTER_INDEX && mPointerIndex2 != INVALID_POINTER_INDEX && event.getPointerCount() > mPointerIndex2) {
                val nsX = event.getX(mPointerIndex1)
                val nsY = event.getY(mPointerIndex1)
                val nfX = event.getX(mPointerIndex2)
                val nfY = event.getY(mPointerIndex2)
                if (mIsFirstTouch) {
                    mAngle = 0f
                    mIsFirstTouch = false
                } else {
                    calculateAngleBetweenLines(fX, fY, sX, sY, nfX, nfY, nsX, nsY)
                }
                mListener?.onRotation(this)
                fX = nfX
                fY = nfY
                sX = nsX
                sY = nsY
            }
            MotionEvent.ACTION_UP -> mPointerIndex1 = INVALID_POINTER_INDEX
            MotionEvent.ACTION_POINTER_UP -> mPointerIndex2 = INVALID_POINTER_INDEX
        }
        return true
    }

    /**
     * 计算两条线之间的角度
     */
    private fun calculateAngleBetweenLines(fx1: Float, fy1: Float, fx2: Float, fy2: Float, sx1: Float, sy1: Float, sx2: Float, sy2: Float): Float {
        val angle1 = toDegrees(atan2(fy1 - fy2, fx1 - fx2).toDouble())
        val angle2 = toDegrees(atan2(sy1 - sy2, sx1 - sx2).toDouble())
        return calculateAngleDelta(angle1.toFloat(), angle2.toFloat())
    }

    private fun calculateAngleDelta(angleFrom: Float, angleTo: Float): Float {
        mAngle = angleTo % 360.0f - angleFrom % 360.0f
        if (mAngle < -180.0f) {
            mAngle += 360.0f
        } else if (mAngle > 180.0f) {
            mAngle -= 360.0f
        }
        return mAngle
    }

    /**
     * 获取当前旋转角度
     */
    fun getAngle(): Float {
        return mAngle
    }

    /**
     * 旋转监听接口
     */
    interface OnRotationGestureListener {
        fun onRotation(rotationDetector: RotationGestureDetector): Boolean
    }

}