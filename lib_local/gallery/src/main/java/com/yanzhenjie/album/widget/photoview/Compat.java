package com.yanzhenjie.album.widget.photoview;

import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.view.MotionEvent;
import android.view.View;

/**
 * 系统版本兼容工具类
 * 专门给【图片预览、缩放、拖拽、惯性滑动】等复杂控件提供底层兼容
 */
public class Compat {
    // 60帧刷新间隔（16ms）
    private static final int SIXTY_FPS_INTERVAL = 1000 / 60;

    /**
     * 兼容上下版本：在下一帧动画时执行任务
     * 用于：平滑缩放、滑动、惯性滚动
     */
    public static void postOnAnimation(View view, Runnable runnable) {
        if (VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN) {
            view.postOnAnimation(runnable);
        } else {
            view.postDelayed(runnable, SIXTY_FPS_INTERVAL);
        }
    }

    /**
     * 获取多点触控的“手指索引”
     * 用于：双指缩放、多指滑动
     */
    public static int getPointerIndex(int action) {
        return (action & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
    }

}