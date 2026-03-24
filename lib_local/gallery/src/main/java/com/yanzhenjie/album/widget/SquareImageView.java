package com.yanzhenjie.album.widget;

import android.content.Context;
import android.content.res.Configuration;
import android.util.AttributeSet;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

/**
 * 正方形 ImageView
 * 根据屏幕方向自动把自身设置为正方形
 * 用于相册里的图片缩略图，保证图片显示为正方形、不拉伸、不变形
 */
public class SquareImageView extends AppCompatImageView {
    private final Configuration mConfig;

    public SquareImageView(Context context) {
        this(context, null, 0);
    }

    public SquareImageView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SquareImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mConfig = getResources().getConfiguration();
    }

    /**
     * 核心：测量时强制宽高相等
     * 竖屏：高度 = 宽度
     * 横屏：宽度 = 高度
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int orientation = mConfig.orientation;
        switch (orientation) {
            // 竖屏：以宽度为基准，变成正方形
            case Configuration.ORIENTATION_PORTRAIT: {
                super.onMeasure(widthMeasureSpec, widthMeasureSpec);
                break;
            }
            // 横屏：以高度为基准，变成正方形
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