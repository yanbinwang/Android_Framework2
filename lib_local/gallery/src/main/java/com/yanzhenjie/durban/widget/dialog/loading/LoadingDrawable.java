package com.yanzhenjie.durban.widget.dialog.loading;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;

/**
 * 加载动画 Drawable 包装类
 * 作用：将 LoadingRenderer 的绘制能力包装为系统可识别的 Drawable，并实现动画控制
 */
public class LoadingDrawable extends Drawable implements Animatable {
    // 实际执行绘制与动画逻辑的渲染器
    private final LoadingRenderer mLoadingRender;

    /**
     * 构造方法：传入实际的渲染器，并绑定刷新回调
     */
    public LoadingDrawable(LoadingRenderer loadingRender) {
        mLoadingRender = loadingRender;
        // 设置刷新回调，通知 Drawable 自身重绘
        mLoadingRender.setCallback(new Callback() {
            @Override
            public void invalidateDrawable(@NonNull Drawable d) {
                invalidateSelf();
            }

            @Override
            public void scheduleDrawable(@NonNull Drawable d, @NonNull Runnable what, long when) {
                scheduleSelf(what, when);
            }

            @Override
            public void unscheduleDrawable(@NonNull Drawable d, @NonNull Runnable what) {
                unscheduleSelf(what);
            }
        });
    }

    /**
     * 当 Drawable 显示区域变化时，同步给渲染器
     */
    @Override
    protected void onBoundsChange(@NonNull Rect bounds) {
        super.onBoundsChange(bounds);
        mLoadingRender.setBounds(bounds);
    }

    /**
     * 绘制：委托给渲染器执行
     */
    @Override
    public void draw(@NonNull Canvas canvas) {
        if (!getBounds().isEmpty()) {
            mLoadingRender.draw(canvas);
        }
    }

    /**
     * 开始动画：委托给渲染器
     */
    @Override
    public void start() {
        mLoadingRender.start();
    }

    /**
     * 停止动画：委托给渲染器
     */
    @Override
    public void stop() {
        mLoadingRender.stop();
    }

    /**
     * 设置透明度：委托给渲染器
     */
    @Override
    public void setAlpha(int alpha) {
        mLoadingRender.setAlpha(alpha);
    }

    /**
     * 设置颜色滤镜：委托给渲染器
     */
    @Override
    public void setColorFilter(ColorFilter cf) {
        mLoadingRender.setColorFilter(cf);
    }

    /**
     * 获取透明度格式：半透明
     */
    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

    /**
     * 获取Drawable固有宽度（用于布局自适应）
     */
    @Override
    public int getIntrinsicHeight() {
        return (int) mLoadingRender.mHeight;
    }

    /**
     * 获取Drawable固有高度（用于布局自适应）
     */
    @Override
    public int getIntrinsicWidth() {
        return (int) mLoadingRender.mWidth;
    }

    /**
     * 动画是否正在运行
     */
    @Override
    public boolean isRunning() {
        return mLoadingRender.isRunning();
    }

}