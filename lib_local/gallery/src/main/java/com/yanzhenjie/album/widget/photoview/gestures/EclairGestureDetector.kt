package com.yanzhenjie.album.widget.photoview.gestures;

import android.content.Context;
import android.view.MotionEvent;

/**
 * Android 2.1 专用手势检测器
 * 主要作用：处理【多点触控】时的手指切换逻辑
 * 继承自 CupcakeGestureDetector
 */
public class EclairGestureDetector extends CupcakeGestureDetector {
    // 当前有效的手指ID
    private int mActivePointerId = INVALID_POINTER_ID;
    // 当前有效的手指索引
    private int mActivePointerIndex = 0;
    // 无效手指ID常量
    private static final int INVALID_POINTER_ID = -1;

    public EclairGestureDetector(Context context) {
        super(context);
    }

    /**
     * 获取当前有效手指的 X 坐标
     * 兼容处理，异常时返回默认手指坐标
     */
    @Override
    float getActiveX(MotionEvent ev) {
        try {
            return ev.getX(mActivePointerIndex);
        } catch (Exception e) {
            return ev.getX();
        }
    }

    /**
     * 获取当前有效手指的 Y 坐标
     */
    @Override
    float getActiveY(MotionEvent ev) {
        try {
            return ev.getY(mActivePointerIndex);
        } catch (Exception e) {
            return ev.getY();
        }
    }

    /**
     * 处理触摸事件
     * 核心：处理多指触摸时，抬起一根手指后，自动切换到另一根手指继续拖动
     */
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        final int action = ev.getAction();
        switch (action & MotionEvent.ACTION_MASK) {
            // 第一根手指按下
            case MotionEvent.ACTION_DOWN:
                mActivePointerId = ev.getPointerId(0);
                break;
            // 手指抬起 / 事件取消
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                mActivePointerId = INVALID_POINTER_ID;
                break;
            // 非第一根手指抬起（多指操作）
            case MotionEvent.ACTION_POINTER_UP:
                // 获取抬起的手指索引
                final int pointerIndex = getPointerIndex(ev.getAction());
                final int pointerId = ev.getPointerId(pointerIndex);
                // 如果抬起的是当前有效的手指
                if (pointerId == mActivePointerId) {
                    // 自动切换到另一根手指
                    final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
                    mActivePointerId = ev.getPointerId(newPointerIndex);
                    // 更新最后触摸位置
                    mLastTouchX = ev.getX(newPointerIndex);
                    mLastTouchY = ev.getY(newPointerIndex);
                }
                break;
        }
        // 找到当前有效手指的索引
        mActivePointerIndex = ev.findPointerIndex(mActivePointerId != INVALID_POINTER_ID ? mActivePointerId : 0);
        // 交给父类处理拖动逻辑，自带防崩溃
        try {
            return super.onTouchEvent(ev);
        } catch (IllegalArgumentException e) {
            // 兼容系统库崩溃
            return true;
        }
    }

    /**
     * 获取多点触控的“手指索引”
     * 用于：双指缩放、多指滑动
     */
    private static int getPointerIndex(int action) {
        return (action & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
    }

}