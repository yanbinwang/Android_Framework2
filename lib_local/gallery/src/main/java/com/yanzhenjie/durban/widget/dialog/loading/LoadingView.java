package com.yanzhenjie.durban.widget.dialog.loading;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;

/**
 * 加载动画专用 View
 * 作用：将 LoadingDrawable 包装成可直接使用的控件，自动管理动画生命周期
 */
public class LoadingView extends AppCompatImageView {
    // 动画 Drawable 包装类
    private LoadingDrawable mLoadingDrawable;
    // 具体的动画渲染器（三层圆环）
    private LevelLoadingRenderer mLoadingRenderer;

    /**
     * 构造方法
     */
    public LoadingView(Context context) {
        super(context);
    }

    public LoadingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mLoadingRenderer = new LevelLoadingRenderer(context);
        mLoadingDrawable = new LoadingDrawable(mLoadingRenderer);
        setImageDrawable(mLoadingDrawable);
    }

    /**
     * 挂载到窗口时自动开启动画
     */
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        startAnimation();
    }

    /**
     * 从窗口移除时自动停止动画
     */
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopAnimation();
    }

    /**
     * 可见性变化时：显示→启动，隐藏→停止
     */
    @Override
    protected void onVisibilityChanged(@NonNull View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (visibility == View.VISIBLE) {
            startAnimation();
        } else {
            stopAnimation();
        }
    }

    /**
     * 启动动画
     */
    private void startAnimation() {
        if (mLoadingDrawable != null) {
            mLoadingDrawable.start();
        }
    }

    /**
     * 停止动画
     */
    private void stopAnimation() {
        if (mLoadingDrawable != null) {
            mLoadingDrawable.stop();
        }
    }

    /**
     * 设置圆环三段颜色
     */
    public void setCircleColors(int r1, int r2, int r3) {
        mLoadingRenderer.setCircleColors(r1, r2, r3);
    }

}