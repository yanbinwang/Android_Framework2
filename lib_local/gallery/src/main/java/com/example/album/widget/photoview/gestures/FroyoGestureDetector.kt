package com.example.album.widget.photoview.gestures

import android.content.Context
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.ScaleGestureDetector.OnScaleGestureListener
import java.lang.Float
import kotlin.Boolean
import kotlin.IllegalArgumentException

/**
 * Android 2.2 及以上版本使用的手势检测器
 * 利用系统原生 ScaleGestureDetector 实现双指缩放
 * 同时继承了拖动、快速滑动功能
 */
class FroyoGestureDetector(context: Context) : EclairGestureDetector(context) {
    // 系统原生的缩放手势检测器（专门处理双指缩放）
    private val mDetector = ScaleGestureDetector(context, object : OnScaleGestureListener {
        /**
         * 缩放进行中
         */
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            // 获取缩放系数
            val scaleFactor = detector.getScaleFactor()
            // 防止非法数值（异常保护）
            if (Float.isNaN(scaleFactor) || Float.isInfinite(scaleFactor)) {
                return false
            }
            // 回调给外部：缩放系数 + 缩放中心点
            mListener?.onScale(scaleFactor, detector.focusX, detector.focusY)
            return true
        }

        /**
         * 缩放开始（必须return true才能接收缩放事件）
         */
        override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
            return true
        }

        /**
         * 缩放结束（无需处理）
         */
        override fun onScaleEnd(detector: ScaleGestureDetector) {
        }
    })

    /**
     * 处理触摸事件
     * 先交给缩放检测器，再交给父类处理拖动、滑动
     * 加 try-catch 防止系统库崩溃
     */
    override fun onTouchEvent(ev: MotionEvent?): Boolean {
        ev ?: return false
        try {
            // 先处理缩放
            mDetector.onTouchEvent(ev)
            // 再交给父类处理拖拽、滑动
            return super.onTouchEvent(ev)
        } catch (_: IllegalArgumentException) {
            // 修复系统兼容库的崩溃问题
            return true
        }
    }

    /**
     * 是否正在双指缩放
     */
    override fun isScaling(): Boolean {
        return mDetector.isInProgress
    }

}