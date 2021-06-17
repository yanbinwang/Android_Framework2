package com.example.base.utils

import android.content.Context
import android.graphics.Point
import android.util.TypedValue

//------------------------------------转换手机分辨率的工具类------------------------------------
/**
 * dip转px
 */
fun Context.dip2px(dipValue: Float): Int {
    val scale = resources.displayMetrics.density
    return (dipValue * scale + 0.5f).toInt()
}

/**
 * px转dip
 */
fun Context.px2dip(pxValue: Float): Int {
    val scale = resources.displayMetrics.density
    return (pxValue / scale + 0.5f).toInt()
}

/**
 * 获取屏幕长宽比
 */
fun Context.getScreenRate(): Float {
    val pixel = getScreenMetrics()
    val height = pixel.y.toFloat()
    val width = pixel.x.toFloat()
    return height / width
}

/**
 * 获取屏幕宽度和高度，单位为px
 */
private fun Context.getScreenMetrics(): Point {
    val dm = resources.displayMetrics
    val widthScreen = dm.widthPixels
    val heightScreen = dm.heightPixels
    return Point(widthScreen, heightScreen)
}

/**
 * 获取本地的dp值
 */
fun Context.getXmlDef(id: Int): Int {
    val value = TypedValue()
    resources.getValue(id, value, true)
    return TypedValue.complexToFloat(value.data).toInt()
}