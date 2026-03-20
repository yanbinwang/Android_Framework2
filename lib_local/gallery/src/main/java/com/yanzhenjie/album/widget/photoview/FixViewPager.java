package com.yanzhenjie.album.widget.photoview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.viewpager.widget.ViewPager;

/**
 * 修复版 ViewPager
 * 唯一作用：捕捉旧版本系统 / 第三方ROM 触发的系统级崩溃
 * 业务功能 = 普通 ViewPager
 */
public class FixViewPager extends ViewPager {

    public FixViewPager(Context context) {
        super(context);
    }

    public FixViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * 重写触摸拦截方法
     * 作用：捕获低版本系统/厂商ROM触发的【系统级BUG崩溃】
     */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        try {
            return super.onInterceptTouchEvent(ev);
        } catch (IllegalArgumentException e) {
            // 出现异常直接返回false，不崩溃
            return false;
        }
    }

}