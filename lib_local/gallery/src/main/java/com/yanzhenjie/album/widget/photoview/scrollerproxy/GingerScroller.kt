package com.yanzhenjie.album.widget.photoview.scrollerproxy;

import android.content.Context;
import android.widget.OverScroller;

/**
 * Android 2.3 ~ 4.0 版本使用的滚动计算器
 * 直接使用系统 OverScroller 实现惯性滑动、回弹效果
 * 是现代系统都能使用的稳定实现
 */
public class GingerScroller extends ScrollerProxy {
    // 系统自带的滚动计算器（支持越界回弹、惯性滑动）
    protected final OverScroller mScroller;

    /**
     * 构造方法，初始化系统 OverScroller
     */
    public GingerScroller(Context context) {
        mScroller = new OverScroller(context);
    }

    @Override
    public boolean computeScrollOffset() {
        return mScroller.computeScrollOffset();
    }

    @Override
    public void fling(int startX, int startY, int velocityX, int velocityY, int minX, int maxX, int minY, int maxY, int overX, int overY) {
        mScroller.fling(startX, startY, velocityX, velocityY, minX, maxX, minY, maxY, overX, overY);
    }

    @Override
    public void forceFinished(boolean finished) {
        mScroller.forceFinished(finished);
    }

    @Override
    public boolean isFinished() {
        return mScroller.isFinished();
    }

    @Override
    public int getCurrX() {
        return mScroller.getCurrX();
    }

    @Override
    public int getCurrY() {
        return mScroller.getCurrY();
    }

}