package com.github.fujianlian.klinechart.utils

import android.content.Context
import com.example.common.BaseApplication

/**
 * Created by tian on 2016/4/11.
 */
object ViewUtil {

    fun Px2Dp(context: Context, px: Float): Float {
        val scale = context.resources.displayMetrics.density
        return (px / scale + 0.5f)
    }

    fun Float?.toDp(): Float {
        this ?: return 0f
        return Px2Dp(BaseApplication.instance, this)
    }
}
