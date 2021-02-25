package com.dataqin.base.utils

import android.content.Context
import android.graphics.Point
import android.util.TypedValue

/**
 * 转换手机分辨率的工具类
 *
 * @author wyb
 */
object DisplayUtil {

    //dip转px
    @JvmStatic
    fun dip2px(context: Context, dipValue: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (dipValue * scale + 0.5f).toInt()
    }

    //px转dip
    @JvmStatic
    fun px2dip(context: Context, pxValue: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (pxValue / scale + 0.5f).toInt()
    }

    //获取屏幕长宽比
    @JvmStatic
    fun getScreenRate(context: Context): Float {
        val pixel = getScreenMetrics(context)
        val height = pixel.y.toFloat()
        val width = pixel.x.toFloat()
        return height / width
    }

    //获取屏幕宽度和高度，单位为px
    @JvmStatic
    private fun getScreenMetrics(context: Context): Point {
        val dm = context.resources.displayMetrics
        val widthScreen = dm.widthPixels
        val heightScreen = dm.heightPixels
        return Point(widthScreen, heightScreen)
    }

    //获取本地的dp值
    @JvmStatic
    fun getXmlDef(context: Context, id: Int): Int {
        val value = TypedValue()
        context.resources.getValue(id, value, true)
        return TypedValue.complexToFloat(value.data).toInt()
    }

}