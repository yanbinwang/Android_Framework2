package com.yanzhenjie.album.widget;

import android.content.Context;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * 正方形 FrameLayout
 * 横竖屏自动适配，保持宽高相等 = 正方形
 * 用于相册网格中需要嵌套多层视图的条目
 */
public class SquareFrameLayout extends FrameLayout {
    private final Configuration mConfig;

    public SquareFrameLayout(@NonNull Context context) {
        this(context, null, 0);
    }

    public SquareFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SquareFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mConfig = getResources().getConfiguration();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int orientation = mConfig.orientation;
        switch (orientation) {
            // 竖屏：高度 = 宽度
            case Configuration.ORIENTATION_PORTRAIT: {
                super.onMeasure(widthMeasureSpec, widthMeasureSpec);
                break;
            }
            // 横屏：宽度 = 高度
            case Configuration.ORIENTATION_LANDSCAPE: {
                super.onMeasure(heightMeasureSpec, heightMeasureSpec);
                break;
            }
            default: {
                throw new AssertionError("This should not be the case.");
            }
        }
    }

}