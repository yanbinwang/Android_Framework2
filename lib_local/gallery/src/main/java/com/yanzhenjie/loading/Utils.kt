package com.yanzhenjie.loading;

import android.content.Context;
import android.util.DisplayMetrics;

/**
 * <p>Core utils.</p>
 * Created by Yan Zhenjie on 2017/5/17.
 */
public class Utils {

    public static float dip2px(Context context, float dp) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return (float) ((displayMetrics.density + 0.5) * dp);
    }

    public static float px2dip(Context context, int px) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return (float) (px / (displayMetrics.density + 0.5));
    }

}