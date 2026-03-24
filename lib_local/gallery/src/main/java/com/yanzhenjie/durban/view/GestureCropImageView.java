package com.yanzhenjie.durban.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

import com.yanzhenjie.durban.util.RotationGestureDetector;

/**
 * 最终对外使用的裁剪View
 * 具备：单指拖动、双指缩放、双指旋转、双击放大
 */
public class GestureCropImageView extends CropImageView {
    // 双击放大分步数（默认从最小到最大需要5次双击）
    private int mDoubleTapScaleSteps = 5;
    // 双指操作的中心点坐标
    private float mMidPntX, mMidPntY;
    // 是否允许旋转、缩放
    private boolean mIsRotateEnabled = true, mIsScaleEnabled = true;
    // 系统缩放手势检测器
    private ScaleGestureDetector mScaleDetector;
    // 自定义旋转手势检测器
    private RotationGestureDetector mRotateDetector;
    // 系统手势检测器（拖动、双击、长按等）
    private GestureDetector mGestureDetector;
    // 双击放大动画时长
    private static final int DOUBLE_TAP_ZOOM_DURATION = 200;

    public GestureCropImageView(Context context) {
        super(context);
    }

    public GestureCropImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GestureCropImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /**
     * 初始化：调用父类初始化 + 初始化手势
     */
    @Override
    protected void init() {
        super.init();
        setupGestureListeners();
    }

    /**
     * 初始化所有手势监听器
     */
    private void setupGestureListeners() {
        mGestureDetector = new GestureDetector(getContext(), new GestureListener(), null, true);
        mScaleDetector = new ScaleGestureDetector(getContext(), new ScaleListener());
        mRotateDetector = new RotationGestureDetector(new RotateListener());
    }

    /**
     * 触摸事件统一分发入口
     * 处理：按下取消动画、多点计算中心点、手势分发、抬起自动对齐
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // 手指按下时，取消所有正在执行的动画
        if ((event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_DOWN) {
            cancelAllAnimations();
        }
        // 双指操作时，计算两指中心点
        if (event.getPointerCount() > 1) {
            mMidPntX = (event.getX(0) + event.getX(1)) / 2;
            mMidPntY = (event.getY(0) + event.getY(1)) / 2;
        }
        // 分发事件给单击/双击/滑动检测器
        mGestureDetector.onTouchEvent(event);
        // 开启缩放时，分发缩放事件
        if (mIsScaleEnabled) {
            mScaleDetector.onTouchEvent(event);
        }
        // 开启旋转时，分发旋转事件
        if (mIsRotateEnabled) {
            mRotateDetector.onTouchEvent(event);
        }
        // 手指抬起时，让图片自动对齐裁剪框
        if ((event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_UP) {
            setImageToWrapCropBounds();
        }
        return true;
    }

    /**
     * 计算双击后的目标缩放值
     */
    protected float getDoubleTapTargetScale() {
        return getCurrentScale() * (float) Math.pow(getMaxScale() / getMinScale(), 1.0f / mDoubleTapScaleSteps);
    }

    /**
     * 是否允许缩放
     */
    public void setScaleEnabled(boolean scaleEnabled) {
        mIsScaleEnabled = scaleEnabled;
    }

    public boolean isScaleEnabled() {
        return mIsScaleEnabled;
    }

    /**
     * 是否允许旋转
     */
    public void setRotateEnabled(boolean rotateEnabled) {
        mIsRotateEnabled = rotateEnabled;
    }

    public boolean isRotateEnabled() {
        return mIsRotateEnabled;
    }

    /**
     * 双击放大需要几步达到最大缩放
     */
    public void setDoubleTapScaleSteps(int doubleTapScaleSteps) {
        mDoubleTapScaleSteps = doubleTapScaleSteps;
    }

    public int getDoubleTapScaleSteps() {
        return mDoubleTapScaleSteps;
    }

    /**
     * 缩放手势监听
     */
    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            // 执行双指缩放，以双指中心为中心点
            postScale(detector.getScaleFactor(), mMidPntX, mMidPntY);
            return true;
        }
    }

    /**
     * 单击/双击/滑动手势监听
     */
    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            // 双击放大
            zoomImageToPosition(getDoubleTapTargetScale(), e.getX(), e.getY(), DOUBLE_TAP_ZOOM_DURATION);
            return super.onDoubleTap(e);
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            // 单指滑动拖动图片
            postTranslate(-distanceX, -distanceY);
            return true;
        }
    }

    /**
     * 旋转手势监听
     */
    private class RotateListener extends RotationGestureDetector.SimpleOnRotationGestureListener {
        @Override
        public boolean onRotation(RotationGestureDetector rotationDetector) {
            // 执行双指旋转，以双指中心为旋转中心
            postRotate(rotationDetector.getAngle(), mMidPntX, mMidPntY);
            return true;
        }
    }

}