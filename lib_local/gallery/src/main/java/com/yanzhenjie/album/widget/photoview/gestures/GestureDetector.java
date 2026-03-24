package com.yanzhenjie.album.widget.photoview.gestures;

import android.view.MotionEvent;

/**
 * 手势检测器顶层接口
 * 定义所有手势检测器的统一行为
 */
public interface GestureDetector {

    /**
     * 处理触摸事件（分发拖动、缩放、滑动）
     */
    boolean onTouchEvent(MotionEvent ev);

    /**
     * 当前是否正在双指缩放中
     */
    boolean isScaling();

    /**
     * 当前是否正在拖动中
     */
    boolean isDragging();

    /**
     * 设置手势监听（拖动、缩放、惯性滑动回调）
     */
    void setOnGestureListener(OnGestureListener listener);

}