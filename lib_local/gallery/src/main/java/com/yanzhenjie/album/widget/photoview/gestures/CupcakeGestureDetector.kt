package com.yanzhenjie.album.widget.photoview.gestures;

import android.content.Context;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.ViewConfiguration;

/**
 * Android 1.5 古董级手势检测器
 * 仅支持：单指拖动、快速滑动（惯性）
 * 不支持：双指缩放（isScaling固定返回false）
 * 所有老版本手势类的基类
 */
public class CupcakeGestureDetector implements GestureDetector {
    // 是否正在拖动图片
    private boolean mIsDragging;
    // 速度追踪器（计算滑动速度，用于惯性）
    private VelocityTracker mVelocityTracker;
    // 系统判定为滑动的最小距离（小于这个值不算拖动）
    private final float mTouchSlop;
    // 触发惯性滑动的最小速度
    private final float mMinimumVelocity;
    // 上一次触摸的X/Y坐标
    protected float mLastTouchX;
    protected float mLastTouchY;
    // 手势监听回调
    protected OnGestureListener mListener;

    /**
     * 构造方法：初始化系统滑动参数
     */
    public CupcakeGestureDetector(Context context) {
        final ViewConfiguration configuration = ViewConfiguration.get(context);
        mMinimumVelocity = configuration.getScaledMinimumFlingVelocity();
        mTouchSlop = configuration.getScaledTouchSlop();
    }

    /**
     * 固定返回false：此类不支持缩放功能
     */
    @Override
    public boolean isScaling() {
        return false;
    }

    /**
     * 返回是否正在拖动
     */
    @Override
    public boolean isDragging() {
        return mIsDragging;
    }

    /**
     * 核心：处理所有触摸事件
     * 实现：单指拖动 + 惯性滑动（Fling）
     */
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            // 手指按下
            case MotionEvent.ACTION_DOWN: {
                // 初始化速度追踪器
                mVelocityTracker = VelocityTracker.obtain();
                if (null != mVelocityTracker) {
                    mVelocityTracker.addMovement(ev);
                }
                // 记录初始触摸位置
                mLastTouchX = getActiveX(ev);
                mLastTouchY = getActiveY(ev);
                // 重置拖动状态
                mIsDragging = false;
                break;
            }
            // 手指移动
            case MotionEvent.ACTION_MOVE: {
                final float x = getActiveX(ev);
                final float y = getActiveY(ev);
                // 计算移动偏移量
                final float dx = x - mLastTouchX, dy = y - mLastTouchY;
                // 如果还未判定为拖动，判断移动距离是否达到阈值
                if (!mIsDragging) {
                    // 勾股定理计算滑动距离，超过TouchSlop才算拖动
                    mIsDragging = Math.sqrt((dx * dx) + (dy * dy)) >= mTouchSlop;
                }
                // 如果正在拖动，回调监听
                if (mIsDragging) {
                    mListener.onDrag(dx, dy);
                    mLastTouchX = x;
                    mLastTouchY = y;
                    if (null != mVelocityTracker) {
                        mVelocityTracker.addMovement(ev);
                    }
                }
                break;
            }
            // 触摸取消
            case MotionEvent.ACTION_CANCEL: {
                // 回收速度追踪器，避免内存泄漏
                if (null != mVelocityTracker) {
                    mVelocityTracker.recycle();
                    mVelocityTracker = null;
                }
                break;
            }
            // 手指抬起
            case MotionEvent.ACTION_UP: {
                if (mIsDragging) {
                    if (null != mVelocityTracker) {
                        mLastTouchX = getActiveX(ev);
                        mLastTouchY = getActiveY(ev);
                        // 添加抬起事件并计算滑动速度
                        mVelocityTracker.addMovement(ev);
                        mVelocityTracker.computeCurrentVelocity(1000);
                        final float vX = mVelocityTracker.getXVelocity(), vY = mVelocityTracker.getYVelocity();
                        // 速度达标，触发惯性滑动
                        if (Math.max(Math.abs(vX), Math.abs(vY)) >= mMinimumVelocity) {
                            mListener.onFling(mLastTouchX, mLastTouchY, -vX, -vY);
                        }
                    }
                }
                // 回收速度追踪器
                if (null != mVelocityTracker) {
                    mVelocityTracker.recycle();
                    mVelocityTracker = null;
                }
                break;
            }
        }
        return true;
    }

    /**
     * 设置手势监听
     */
    @Override
    public void setOnGestureListener(OnGestureListener listener) {
        this.mListener = listener;
    }

    /**
     * 获取当前触摸点X/Y坐标（单指版本）
     */
    float getActiveX(MotionEvent ev) {
        return ev.getX();
    }

    float getActiveY(MotionEvent ev) {
        return ev.getY();
    }

}