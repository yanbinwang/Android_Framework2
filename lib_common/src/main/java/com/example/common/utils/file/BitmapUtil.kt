package com.example.common.utils.file

import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import com.example.common.BaseApplication
import java.io.InputStream

/**
 * 获取asset下的图片
 */
fun String?.getBitmapFromAsset(): Bitmap? {
    this ?: return null
    val assets = BaseApplication.instance.assets
    var stream: InputStream? = null
    return try {
        stream = assets.open(this)
        BitmapFactory.decodeStream(stream)
    } catch (e: Exception) {
        null
    } finally {
        stream?.close()
    }
}

/**
 * 读取mipmap下的图片
 */
fun Context?.decodeResource(id: Int): Bitmap? {
    this ?: return null
    return BitmapFactory.decodeResource(this.resources, id)
}

/**
 * 绘制bit时对原图进行缩放
 */
fun Bitmap?.scaleBitmap(scale: Float): Bitmap? {
    this ?: return null
    val matrix = Matrix()
    matrix.postScale(scale, scale)//使用后乘
    val bit = Bitmap.createBitmap(this, 0, 0, width, height, matrix, false);
    if (!isRecycled) recycle()
    return bit
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
    val config = if (opacity != PixelFormat.OPAQUE) Bitmap.Config.ARGB_8888 else Bitmap.Config.RGB_565
    val bitmap = Bitmap.createBitmap(width, height, config)
    val canvas = Canvas(bitmap)
    setBounds(0, 0, width, height)
    draw(canvas)
    return bitmap
}