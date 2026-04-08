package com.yanzhenjie.durban.utils;

import android.content.Context;
import android.util.TypedValue;

/**
 * 屏幕尺寸转换工具类
 * dp ↔ px 互相转换
 */
public class DensityUtils {

    /**
     * 私有化构造，禁止实例化
     */
    private DensityUtils() {
    }

    /**
     * dp 转 px
     */
    public static float dip2px(Context context, float dp) {
        if (context == null) return 0;
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
    }

    /**
     * px 转 dp
     */
    public static float px2dip(Context context, int px) {
        if (context == null) return 0;
        final float density = context.getResources().getDisplayMetrics().density;
        return (int) (px / density + 0.5f);
    }

}