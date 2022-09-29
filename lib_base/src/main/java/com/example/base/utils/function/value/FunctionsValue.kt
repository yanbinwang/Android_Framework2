package com.example.base.utils.function.value

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.PixelFormat
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable

//------------------------------------方法工具类------------------------------------
/**
 * Boolean防空
 * */
val Boolean?.orFalse get() = this ?: false

/**
 * Boolean防空
 * */
val Boolean?.orTrue get() = this ?: true

/**
 * 转Boolean
 * */
fun Any?.toBoolean(default: Boolean = false) = this as? Boolean ?: default

/**
 * 防空转换Boolean
 */
fun CharSequence?.toSafeBoolean(default: Boolean = false): Boolean {
    if (this.isNullOrEmpty() || this == ".") return default
    return try {
        this.toString().toBoolean()
    } catch (e: Exception) {
        default
    }
}

/**
 * 根据宽高缩放图片
 */
fun Drawable.zoomDrawable(w: Int, h: Int = w): Drawable {
    val width = intrinsicWidth
    val height = intrinsicHeight
    val oldbmp = drawableToBitmap()
    val matrix = Matrix()
    val scaleWidth = w.toFloat() / width
    val scaleHeight = h.toFloat() / height
    matrix.postScale(scaleWidth, scaleHeight)
    val newbmp = Bitmap.createBitmap(oldbmp, 0, 0, width, height, matrix, true)
    return BitmapDrawable(null, newbmp)
}

fun Drawable.drawableToBitmap(): Bitmap {
    val width = intrinsicWidth
    val height = intrinsicHeight
    val config =
        if (opacity != PixelFormat.OPAQUE) Bitmap.Config.ARGB_8888 else Bitmap.Config.RGB_565
    val bitmap = Bitmap.createBitmap(width, height, config)
    val canvas = Canvas(bitmap)
    setBounds(0, 0, width, height)
    draw(canvas)
    return bitmap
}