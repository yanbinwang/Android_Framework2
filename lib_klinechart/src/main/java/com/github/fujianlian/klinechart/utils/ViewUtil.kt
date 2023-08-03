package com.github.fujianlian.klinechart.utils

import android.content.Context
import com.example.framework.utils.function.value.orZero
import com.example.framework.utils.function.value.toSafeInt

/**
 * Created by tian on 2016/4/11.
 */
object ViewUtil {
    @JvmStatic
    fun Dp2Px(context: Context?, dp: Float): Int {
        val scale = context?.resources?.displayMetrics?.density.orZero
        return (dp * scale + 0.5f).toSafeInt()
    }

    @JvmStatic
    fun Px2Dp(context: Context?, px: Float): Int {
        val scale = context?.resources?.displayMetrics?.density.orZero
        return (px / scale + 0.5f).toSafeInt()
    }
}