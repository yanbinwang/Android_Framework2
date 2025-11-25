package com.yanzhenjie.loading

import android.content.Context

/**
 * 设备独立像素 (dp/dip) 和 像素 (px) 之间进行单位转换
 * Created by yan
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