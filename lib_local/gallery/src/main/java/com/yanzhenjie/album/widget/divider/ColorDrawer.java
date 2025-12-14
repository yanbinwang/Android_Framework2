package com.yanzhenjie.album.widget.divider;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;

import androidx.annotation.ColorInt;

/**
 * Created by YanZhenjie on 2018/4/20.
 */
public class ColorDrawer extends Drawer {

    public ColorDrawer(int color, int width, int height) {
        super(new ColorDrawable(opaqueColor(color)), width, height);
    }

    /**
     * The target color is packaged in an opaque color.
     *
     * @param color color.
     * @return color.
     */
    @ColorInt
    public static int opaqueColor(@ColorInt int color) {
        int alpha = Color.alpha(color);
        if (alpha == 0) return color;
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        return Color.argb(255, red, green, blue);
    }

}