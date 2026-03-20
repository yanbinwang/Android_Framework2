package com.yanzhenjie.album.widget;

import android.content.Context;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * 正方形布局（根据屏幕方向自动适配）
 * 竖屏：宽 = 高
 * 横屏：高 = 宽
 * 专门用于相册图片网格列表（保证每个格子都是正方形）
 */
public class SquareRelativeLayout extends RelativeLayout {
    private final Configuration mConfig;

    public SquareRelativeLayout(@NonNull Context context) {
        this(context, null, 0);
    }

    public SquareRelativeLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SquareRelativeLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        // 获取屏幕方向配置
        mConfig = getResources().getConfiguration();
    }

    /**
     * 核心：测量宽高，强制变成正方形
     * 竖屏：高 = 宽
     * 横屏：宽 = 高
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int orientation = mConfig.orientation;
        switch (orientation) {
            // 竖屏：以宽度为准，高度 = 宽度
            case Configuration.ORIENTATION_PORTRAIT: {
                super.onMeasure(widthMeasureSpec, widthMeasureSpec);
                break;
            }
            // 横屏：以高度为准，宽度 = 高度
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