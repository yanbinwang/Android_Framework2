package com.yanzhenjie.album.widget;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ProgressBar;

import androidx.annotation.ColorInt;

/**
 * Created by YanZhenjie on 2018/4/11.
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
     * Set the color of the Bar.
     *
     * @param color color.
     */
    public void setColorFilter(@ColorInt int color) {
        Drawable drawable = getIndeterminateDrawable();
        drawable = drawable.mutate();
        drawable.setColorFilter(color, PorterDuff.Mode.SRC_IN);
        setIndeterminateDrawable(drawable);
    }

}