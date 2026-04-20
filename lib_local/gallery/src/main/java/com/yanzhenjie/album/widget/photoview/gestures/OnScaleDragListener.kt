package com.yanzhenjie.album.widget.photoview.gestures

import android.view.MotionEvent

/**
 * 手势检测器顶层接口
 * 定义所有手势检测器的统一行为
 */
interface OnScaleDragListener {

    /**
     * 处理触摸事件（分发拖动、缩放、滑动）
     */
    fun onTouchEvent(ev: MotionEvent): Boolean

    /**
     * 当前是否正在双指缩放中
     */
    fun isScaling(): Boolean

    /**
     * 当前是否正在拖动中
     */
    fun isDragging(): Boolean

    /**
     * 设置手势监听（拖动、缩放、惯性滑动回调）
     */
    fun setOnGestureListener(listener: OnGestureListener)

}