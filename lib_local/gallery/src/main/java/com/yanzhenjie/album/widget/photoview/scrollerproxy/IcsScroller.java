package com.yanzhenjie.album.widget.photoview.scrollerproxy;

import android.content.Context;

/**
 * Android 4.0 及以上版本的滚动计算器
 * 继承自 GingerScroller
 * 作用：啥也没干，完全复用父类逻辑，仅做版本标记
 * 纯纯的兼容层屎山代码
 */
public class IcsScroller extends GingerScroller {

    public IcsScroller(Context context) {
        super(context);
    }

    /**
     * 完全重写，但逻辑和父类一模一样
     */
    @Override
    public boolean computeScrollOffset() {
        return mScroller.computeScrollOffset();
    }

}