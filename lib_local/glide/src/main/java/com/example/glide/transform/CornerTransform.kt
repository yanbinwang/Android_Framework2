package com.example.glide.transform

import android.content.Context
import android.graphics.*
import com.bumptech.glide.Glide
import com.bumptech.glide.load.Transformation
import com.bumptech.glide.load.engine.Resource
import com.bumptech.glide.load.resource.bitmap.BitmapResource
import com.example.framework.utils.function.value.toSafeFloat
import com.example.framework.utils.function.value.toSafeInt
import java.security.MessageDigest

/**
 * author: wyb
 * date: 2019/5/6.
 */
class CornerTransform(context: Context, private var radius: Float) : Transformation<Bitmap> {
    private var exceptLeftTop = false
    private var exceptRightTop = false
    private var exceptLeftBottom = false
    private var exceptRightBottom = false
    private val mBitmapPool = Glide.get(context).bitmapPool

    fun setExceptCorner(overRide: BooleanArray) {
        setExceptCorner(overRide[0], overRide[1], overRide[2], overRide[3])
    }

    fun setExceptCorner(leftTop: Boolean, rightTop: Boolean, leftBottom: Boolean, rightBottom: Boolean) {
        this.exceptLeftTop = leftTop
        this.exceptRightTop = rightTop
        this.exceptLeftBottom = leftBottom
        this.exceptRightBottom = rightBottom
    }

    override fun transform(context: Context, resource: Resource<Bitmap>, outWidth: Int, outHeight: Int): Resource<Bitmap> {
        val source = resource.get()
        var finalWidth: Int
        var finalHeight: Int
        var ratio: Float //输出目标的宽高或高宽比例
        if (outWidth > outHeight) { //输出宽度>输出高度,求高宽比
            ratio = outHeight.toSafeFloat() / outWidth.toSafeFloat()
            finalWidth = source.width
            finalHeight = (source.width.toSafeFloat() * ratio).toSafeInt() //固定原图宽度,求最终高度
            if (finalHeight > source.height) { //求出的最终高度>原图高度,求宽高比
                ratio = outWidth.toSafeFloat() / outHeight.toSafeFloat()
                finalHeight = source.height
                finalWidth = (source.height.toSafeFloat() * ratio).toSafeInt() //固定原图高度,求最终宽度
            }
        } else if (outWidth < outHeight) { //输出宽度 < 输出高度,求宽高比
            ratio = outWidth.toSafeFloat() / outHeight.toSafeFloat()
            finalHeight = source.height
            finalWidth = (source.height.toSafeFloat() * ratio).toSafeInt() //固定原图高度,求最终宽度
            if (finalWidth > source.width) { //求出的最终宽度 > 原图宽度,求高宽比
                ratio = outHeight.toSafeFloat() / outWidth.toSafeFloat()
                finalWidth = source.width
                finalHeight = (source.width.toSafeFloat() * ratio).toSafeInt()
            }
        } else { //输出宽度=输出高度
            finalHeight = source.height
            finalWidth = finalHeight
        }
        //修正圆角
        radius *= finalHeight.toSafeFloat() / outHeight.toSafeFloat()
        val outBitmap = mBitmapPool.get(finalWidth, finalHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(outBitmap)
        val paint = Paint()
        //关联画笔绘制的原图bitmap
        val shader = BitmapShader(source, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
        //计算中心位置,进行偏移
        val width = (source.width - finalWidth) / 2
        val height = (source.height - finalHeight) / 2
        if (width != 0 || height != 0) {
            val matrix = Matrix()
            matrix.setTranslate((-width).toSafeFloat(), (-height).toSafeFloat())
            shader.setLocalMatrix(matrix)
        }
        paint.shader = shader
        paint.isAntiAlias = true
        val rectF = RectF(0.0f, 0.0f, canvas.width.toSafeFloat(), canvas.height.toSafeFloat())
        canvas.drawRoundRect(rectF, radius, radius, paint) //先绘制圆角矩形
        if (exceptLeftTop) canvas.drawRect(0f, 0f, radius, radius, paint)//左上角不为圆角
        if (exceptRightTop) canvas.drawRect(canvas.width - radius, 0f, radius, radius, paint)//右上角不为圆角
        if (exceptLeftBottom) canvas.drawRect(0f, canvas.height - radius, radius, canvas.height.toSafeFloat(), paint)//左下角不为圆角
        if (exceptRightBottom) canvas.drawRect(canvas.width - radius, canvas.height - radius, canvas.width.toSafeFloat(), canvas.height.toSafeFloat(), paint)//右下角不为圆角
        return BitmapResource(outBitmap, mBitmapPool)
    }

    override fun updateDiskCacheKey(messageDigest: MessageDigest) {}

}