package com.example.klinechart.utils

import android.content.Context
import com.example.framework.utils.function.value.orZero
import com.example.framework.utils.function.value.toSafeInt

object ViewUtil {

    @JvmStatic
    fun dp2px(context: Context?, dp: Float): Int {
        val scale = context?.resources?.displayMetrics?.density.orZero
        return (dp * scale + 0.5f).toSafeInt()
    }

    @JvmStatic
    fun sp2px(context: Context?, sp: Float): Int {
        val scale = context?.resources?.displayMetrics?.scaledDensity.orZero
        return (sp * scale + 0.5f).toSafeInt()
    }

    @JvmStatic
    fun px2dp(context: Context?, px: Float): Int {
        val scale = context?.resources?.displayMetrics?.density.orZero
        return (px / scale + 0.5f).toSafeInt()
    }

}