package com.example.klinechart.utils

import android.content.Context

/**
 * Created by tian on 2016/4/11.
 */
object ViewUtil {
    fun Dp2Px(context: Context, dp: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (dp * scale + 0.5f).toInt()
    }

    fun Px2Dp(context: Context, px: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (px / scale + 0.5f).toInt()
    }
}