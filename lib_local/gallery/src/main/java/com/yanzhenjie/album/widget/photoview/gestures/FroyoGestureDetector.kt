package com.yanzhenjie.album.widget.photoview.gestures;

import android.content.Context;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

import androidx.annotation.NonNull;

/**
 * Android 2.2 及以上版本使用的手势检测器
 * 利用系统原生 ScaleGestureDetector 实现双指缩放
 * 同时继承了拖动、快速滑动功能
 */
public class FroyoGestureDetector extends EclairGestureDetector {
    // 系统原生的缩放手势检测器（专门处理双指缩放）
    protected final ScaleGestureDetector mDetector;

    public FroyoGestureDetector(Context context) {
        super(context);
        // 创建缩放手势监听
        ScaleGestureDetector.OnScaleGestureListener mScaleListener = new ScaleGestureDetector.OnScaleGestureListener() {
            /**
             * 缩放进行中
             */
            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                // 获取缩放系数
                float scaleFactor = detector.getScaleFactor();
                // 防止非法数值（异常保护）
                if (Float.isNaN(scaleFactor) || Float.isInfinite(scaleFactor)) {
                    return false;
                }
                // 回调给外部：缩放系数 + 缩放中心点
                mListener.onScale(scaleFactor, detector.getFocusX(), detector.getFocusY());
                return true;
            }

            /**
             * 缩放开始（必须return true才能接收缩放事件）
             */
            @Override
            public boolean onScaleBegin(@NonNull ScaleGestureDetector detector) {
                return true;
            }

            /**
             * 缩放结束（无需处理）
             */
            @Override
            public void onScaleEnd(@NonNull ScaleGestureDetector detector) {
            }
        };
        // 初始化系统缩放检测器
        mDetector = new ScaleGestureDetector(context, mScaleListener);
    }

    /**
     * 是否正在双指缩放
     */
    @Override
    public boolean isScaling() {
        return mDetector.isInProgress();
    }

    /**
     * 处理触摸事件
     * 先交给缩放检测器，再交给父类处理拖动、滑动
     * 加 try-catch 防止系统库崩溃
     */
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        try {
            // 先处理缩放
            mDetector.onTouchEvent(ev);
            // 再交给父类处理拖拽、滑动
            return super.onTouchEvent(ev);
        } catch (IllegalArgumentException e) {
            // 修复系统兼容库的崩溃问题
            return true;
        }
    }

}