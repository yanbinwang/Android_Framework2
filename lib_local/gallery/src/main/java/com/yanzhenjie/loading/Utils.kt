package com.yanzhenjie.loading

import android.content.Context

/**
 * <p>Core utils.</p>
 * Created by Yan Zhenjie on 2017/5/17.
 */
object Utils {

    @JvmStatic
    fun dip2px(context: Context, dp: Float): Float {
        val displayMetrics = context.resources.displayMetrics
        return ((displayMetrics.density + 0.5) * dp).toFloat()
    }

    @JvmStatic
    fun px2dip(context: Context, px: Int): Float {
        val displayMetrics = context.resources.displayMetrics
        return (px / (displayMetrics.density + 0.5)).toFloat()
    }

}