package com.yanzhenjie.loading;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.animation.LinearInterpolator;

/**
 * 加载动画的抽象基类
 * 负责：动画循环、刷新、启动、停止、尺寸管理
 * 子类负责：具体画长成什么样（圆形、旋转、线条等）
 */
public abstract class LoadingRenderer {
    // 动画一圈的时间(默认1333ms)
    protected long mDuration;
    // 动画默认宽度/高度(56dp)
    protected float mWidth;
    protected float mHeight;
    // 绘制边界
    protected final Rect mBounds = new Rect();
    // 刷新回调（通知UI重绘）
    private Drawable.Callback mCallback;
    // 无限循环动画
    private ValueAnimator mRenderAnimator;
    // 动画执行一次1333ms
    private static final long DEFAULT_DURATION = 1333;
    // 动画更新监听
    private final ValueAnimator.AnimatorUpdateListener mAnimatorUpdateListener = animation -> {
        computeRender((float) animation.getAnimatedValue());
        invalidateSelf();
    };

    public LoadingRenderer(Context context) {
        mWidth = mHeight = DensityUtils.dip2px(context, 56F);
        mDuration = DEFAULT_DURATION;
        setupAnimators();
    }

    /**
     * 初始化动画：0~1无限循环，线性匀速
     */
    private void setupAnimators() {
        mRenderAnimator = ValueAnimator.ofFloat(0.0f, 1.0f);
        mRenderAnimator.setRepeatCount(ValueAnimator.INFINITE);
        mRenderAnimator.setRepeatMode(ValueAnimator.RESTART);
        mRenderAnimator.setDuration(mDuration);
        mRenderAnimator.setInterpolator(new LinearInterpolator());
        mRenderAnimator.addUpdateListener(mAnimatorUpdateListener);
    }

    private void invalidateSelf() {
        mCallback.invalidateDrawable(null);
    }

    /**
     * 绘制（交给子类实现）
     */
    protected void draw(Canvas canvas) {
        draw(canvas, mBounds);
    }

    protected void draw(Canvas canvas, Rect bounds) {
    }

    /**
     * 设置动画监听
     */
    protected void addRenderListener(Animator.AnimatorListener animatorListener) {
        mRenderAnimator.addListener(animatorListener);
    }

    /**
     * 子类根据 0~1 进度计算图形
     */
    protected abstract void computeRender(float renderProgress);

    /**
     * 透明度
     */
    protected abstract void setAlpha(int alpha);

    /**
     * 颜色滤镜
     */
    protected abstract void setColorFilter(ColorFilter cf);

    /**
     * 重置动画
     */
    protected abstract void reset();

    /**
     * 启动动画
     */
    public void start() {
        reset();
        mRenderAnimator.addUpdateListener(mAnimatorUpdateListener);
        mRenderAnimator.setRepeatCount(ValueAnimator.INFINITE);
        mRenderAnimator.setDuration(mDuration);
        mRenderAnimator.start();
    }

    /**
     * 停止动画
     */
    public void stop() {
        mRenderAnimator.removeUpdateListener(mAnimatorUpdateListener);
        mRenderAnimator.setRepeatCount(0);
        mRenderAnimator.setDuration(0);
        mRenderAnimator.end();
    }

    /**
     * 设置边界
     */
    public void setBounds(Rect bounds) {
        mBounds.set(bounds);
    }

    /**
     * 设置回调
     */
    public void setCallback(Drawable.Callback callback) {
        this.mCallback = callback;
    }

    /**
     * 动画是否正在运行
     */
    public boolean isRunning() {
        return mRenderAnimator.isRunning();
    }

}