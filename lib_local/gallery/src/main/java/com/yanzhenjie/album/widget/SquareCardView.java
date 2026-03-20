package com.yanzhenjie.album.widget;

import android.content.Context;
import android.content.res.Configuration;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;

/**
 * Created by YanZhenjie on 2018/4/18.
 */
public class SquareCardView extends CardView {
    private final Configuration mConfig;

    public SquareCardView(@NonNull Context context) {
        this(context, null, 0);
    }

    public SquareCardView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SquareCardView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
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