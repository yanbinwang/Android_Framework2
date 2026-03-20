package com.yanzhenjie.album.widget.photoview.scrollerproxy;

import android.content.Context;
import android.widget.Scroller;

/**
 * Android 2.3 以下古董系统专用滚动计算器
 * 使用旧版 Scroller，不支持完整的越界回弹
 * 现代设备 100% 用不到
 */
public class PreGingerScroller extends ScrollerProxy {
    // 旧版系统滚动计算器（无 OverScroll 效果）
    private final Scroller mScroller;

    /**
     * 构造方法，初始化旧版 Scroller
     */
    public PreGingerScroller(Context context) {
        mScroller = new Scroller(context);
    }

    @Override
    public boolean computeScrollOffset() {
        return mScroller.computeScrollOffset();
    }

    @Override
    public void fling(int startX, int startY, int velocityX, int velocityY, int minX, int maxX, int minY, int maxY, int overX, int overY) {
        mScroller.fling(startX, startY, velocityX, velocityY, minX, maxX, minY, maxY);
    }

    @Override
    public void forceFinished(boolean finished) {
        mScroller.forceFinished(finished);
    }

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