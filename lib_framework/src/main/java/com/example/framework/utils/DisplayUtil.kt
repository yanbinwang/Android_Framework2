package com.example.framework.utils

import android.content.Context
import android.graphics.Point
import android.util.TypedValue

/**
 * 转换手机分辨率的工具类
 *
 * @author wyb
 */
object DisplayUtil {
    private val TAG = "DisplayUtil"

    //dip转px
    fun dip2px(context: Context, dipValue: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (dipValue * scale + 0.5f).toInt()
    }

    //px转dip
    fun px2dip(context: Context, pxValue: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (pxValue / scale + 0.5f).toInt()
    }

    //获取屏幕长宽比
    fun getScreenRate(context: Context): Float {
        val P = getScreenMetrics(context)
        val H = P.y.toFloat()
        val W = P.x.toFloat()
        return H / W
    }

    //获取屏幕宽度和高度，单位为px
    private fun getScreenMetrics(context: Context): Point {
        val dm = context.resources.displayMetrics
        val w_screen = dm.widthPixels
        val h_screen = dm.heightPixels
        return Point(w_screen, h_screen)
    }

    //获取本地的dp值
    fun getXmlDef(context: Context, id: Int): Int {
        val value = TypedValue()
        context.resources.getValue(id, value, true)
        return TypedValue.complexToFloat(value.data).toInt()
    }

}