package com.yanzhenjie.album.widget;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ProgressBar;

import androidx.annotation.ColorInt;

/**
 * 可自定义颜色的 ProgressBar
 * 专门给 LoadingDialog 加载弹窗使用
 */
public class ColorProgressBar extends ProgressBar {

    public ColorProgressBar(Context context) {
        super(context);
    }

    public ColorProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ColorProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * 给加载条设置颜色
     */
    public void setColorFilter(@ColorInt int color) {
        // 获取系统自带的旋转动画条
        Drawable drawable = getIndeterminateDrawable();
        // 关键：mutate() 让这个 Drawable 独立，不影响其他地方的 ProgressBar
        drawable = drawable.mutate();
        // 着色
        drawable.setColorFilter(color, PorterDuff.Mode.SRC_IN);
        // 设置回去
        setIndeterminateDrawable(drawable);
    }

}