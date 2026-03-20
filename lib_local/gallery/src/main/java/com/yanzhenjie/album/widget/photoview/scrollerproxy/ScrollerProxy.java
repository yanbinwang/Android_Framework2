package com.yanzhenjie.album.widget.photoview.scrollerproxy;

import android.content.Context;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;

/**
 * 滚动计算器代理类
 * 作用：对不同 Android 版本的滚动效果（惯性滑动、回弹）做兼容
 * 属于老式版本兼容层，现代系统可直接简化
 */
public abstract class ScrollerProxy {

    /**
     * 根据当前系统版本，返回对应版本的滚动计算器
     * @param context 上下文
     * @return 适配版本的 Scroller 代理
     */
    public static ScrollerProxy getScroller(Context context) {
        // Android 2.3 以下
        if (VERSION.SDK_INT < VERSION_CODES.GINGERBREAD) {
            return new PreGingerScroller(context);
            // Android 2.3 ~ 4.0
        } else if (VERSION.SDK_INT < VERSION_CODES.ICE_CREAM_SANDWICH) {
            return new GingerScroller(context);
            // Android 4.0 以上（现代系统）
        } else {
            return new IcsScroller(context);
        }
    }

    /**
     * 计算滚动偏移（系统滚动时调用）
     */
    public abstract boolean computeScrollOffset();

    /**
     * 执行惯性滚动（Fling）
     */
    public abstract void fling(int startX, int startY, int velocityX, int velocityY, int minX, int maxX, int minY, int maxY, int overX, int overY);

    /**
     * 强制结束滚动
     */
    public abstract void forceFinished(boolean finished);

    /**
     * 滚动是否已经结束
     */
    public abstract boolean isFinished();

    /**
     * 获取当前滚动到的 X 坐标
     */
    public abstract int getCurrX();

    /**
     * 获取当前滚动到的 Y 坐标
     */
    public abstract int getCurrY();

}