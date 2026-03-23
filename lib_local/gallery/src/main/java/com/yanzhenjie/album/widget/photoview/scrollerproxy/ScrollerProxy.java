package com.yanzhenjie.album.widget.photoview.scrollerproxy;

/**
 * 滚动计算器代理类
 * 作用：对不同 Android 版本的滚动效果（惯性滑动、回弹）做兼容
 * 属于老式版本兼容层，现代系统可直接简化
 */
public abstract class ScrollerProxy {

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