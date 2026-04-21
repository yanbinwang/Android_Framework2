package com.yanzhenjie.durban.widget.crop;

import android.view.MotionEvent;

import androidx.annotation.NonNull;

/**
 * 监听双指旋转手势
 */
public class RotationGestureDetector {
    // 第1、第2个手指的索引（Android 多点触摸）
    private int mPointerIndex1, mPointerIndex2;
    // 第一个手指、第二个手指的坐标：f=第一个点，s=第二个点
    private float fX, fY, sX, sY;
    // 旋转角度
    private float mAngle;
    // 是否是第一次触摸（避免刚按下就跳变）
    private boolean mIsFirstTouch;
    // 旋转监听（外部接收角度）
    private final OnRotationGestureListener mListener;
    // 无效手指索引（代表没触摸）
    private static final int INVALID_POINTER_INDEX = -1;

    public RotationGestureDetector(OnRotationGestureListener listener) {
        mListener = listener;
        mPointerIndex1 = INVALID_POINTER_INDEX;
        mPointerIndex2 = INVALID_POINTER_INDEX;
    }

    /**
     * 处理触摸事件
     */
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                sX = event.getX();
                sY = event.getY();
                mPointerIndex1 = event.findPointerIndex(event.getPointerId(0));
                mAngle = 0;
                mIsFirstTouch = true;
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                fX = event.getX();
                fY = event.getY();
                mPointerIndex2 = event.findPointerIndex(event.getPointerId(event.getActionIndex()));
                mAngle = 0;
                mIsFirstTouch = true;
                break;
            case MotionEvent.ACTION_MOVE:
                if (mPointerIndex1 != INVALID_POINTER_INDEX && mPointerIndex2 != INVALID_POINTER_INDEX && event.getPointerCount() > mPointerIndex2) {
                    float nfX, nfY, nsX, nsY;
                    nsX = event.getX(mPointerIndex1);
                    nsY = event.getY(mPointerIndex1);
                    nfX = event.getX(mPointerIndex2);
                    nfY = event.getY(mPointerIndex2);
                    if (mIsFirstTouch) {
                        mAngle = 0;
                        mIsFirstTouch = false;
                    } else {
                        calculateAngleBetweenLines(fX, fY, sX, sY, nfX, nfY, nsX, nsY);
                    }
                    if (mListener != null) {
                        mListener.onRotation(this);
                    }
                    fX = nfX;
                    fY = nfY;
                    sX = nsX;
                    sY = nsY;
                }
                break;
            case MotionEvent.ACTION_UP:
                mPointerIndex1 = INVALID_POINTER_INDEX;
                break;
            case MotionEvent.ACTION_POINTER_UP:
                mPointerIndex2 = INVALID_POINTER_INDEX;
                break;
        }
        return true;
    }

    /**
     * 计算两条线之间的角度
     */
    private float calculateAngleBetweenLines(float fx1, float fy1, float fx2, float fy2, float sx1, float sy1, float sx2, float sy2) {
        return calculateAngleDelta((float) Math.toDegrees((float) Math.atan2((fy1 - fy2), (fx1 - fx2))), (float) Math.toDegrees((float) Math.atan2((sy1 - sy2), (sx1 - sx2))));
    }

    private float calculateAngleDelta(float angleFrom, float angleTo) {
        mAngle = angleTo % 360.0f - angleFrom % 360.0f;
        if (mAngle < -180.0f) {
            mAngle += 360.0f;
        } else if (mAngle > 180.0f) {
            mAngle -= 360.0f;
        }
        return mAngle;
    }

    /**
     * 获取当前旋转角度
     */
    public float getAngle() {
        return mAngle;
    }

    /**
     * 旋转监听接口
     */
    public interface OnRotationGestureListener {

        boolean onRotation(RotationGestureDetector rotationDetector);

    }

    /**
     * 静态内部类：空实现
     */
    public static class SimpleOnRotationGestureListener implements OnRotationGestureListener {

        @Override
        public boolean onRotation(RotationGestureDetector rotationDetector) {
            return false;
        }
    }

}