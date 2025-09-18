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
        // 直接使用ImageView的目标尺寸(outWidth/outHeight)，不做自行计算，与fitXY模式匹配，图片会被拉伸到这个尺寸
        val targetWidth = outWidth
        val targetHeight = outHeight
        // 如果目标尺寸无效，使用原图尺寸
        val finalWidth = if (targetWidth <= 0) source.width else targetWidth
        val finalHeight = if (targetHeight <= 0) source.height else targetHeight
        // 根据目标尺寸计算正确的圆角半径，避免因图片尺寸变化导致圆角比例错误
        val scaleFactor = (finalWidth.toSafeFloat() / source.width).coerceAtLeast(finalHeight.toSafeFloat() / source.height)
        val adjustedRadius = radius * scaleFactor
        // 从缓存池获取合适的Bitmap
        val outBitmap = mBitmapPool.get(finalWidth, finalHeight, Bitmap.Config.ARGB_8888)
        // 绘制画布
        val canvas = Canvas(outBitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        // 使用Matrix进行缩放，匹配fitXY的拉伸效果
        val matrix = Matrix()
        matrix.setScale(finalWidth.toFloat() / source.width, finalHeight.toFloat() / source.height)
        // 创建 shader 并应用缩放矩阵
        val shader = BitmapShader(source, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
        shader.setLocalMatrix(matrix)
        paint.shader = shader
        // 绘制圆角矩形
        val rectF = RectF(0f, 0f, finalWidth.toFloat(), finalHeight.toFloat())
        canvas.drawRoundRect(rectF, adjustedRadius, adjustedRadius, paint)
        // 处理需要排除的角（绘制直角）
        if (exceptLeftTop) {
            canvas.drawRect(0f, 0f, adjustedRadius, adjustedRadius, paint)
        }
        if (exceptRightTop) {
            canvas.drawRect(finalWidth - adjustedRadius, 0f, finalWidth.toFloat(), adjustedRadius, paint)
        }
        if (exceptLeftBottom) {
            canvas.drawRect(0f, finalHeight - adjustedRadius, adjustedRadius, finalHeight.toFloat(), paint)
        }
        if (exceptRightBottom) {
            canvas.drawRect(finalWidth - adjustedRadius, finalHeight - adjustedRadius, finalWidth.toFloat(), finalHeight.toFloat(), paint)
        }
        // 回收原始资源
        resource.recycle()
        return BitmapResource(outBitmap, mBitmapPool)
//        var finalWidth: Int
//        var finalHeight: Int
//        // 输出目标的宽高或高宽比例
//        var ratio: Float
//        // 输出宽度>输出高度,求高宽比
//        if (outWidth > outHeight) {
//            ratio = outHeight.toSafeFloat() / outWidth.toSafeFloat()
//            finalWidth = source.width
//            // 固定原图宽度,求最终高度
//            finalHeight = (source.width.toSafeFloat() * ratio).toSafeInt()
//            if (finalHeight > source.height) {
//                // 求出的最终高度>原图高度,求宽高比
//                ratio = outWidth.toSafeFloat() / outHeight.toSafeFloat()
//                finalHeight = source.height
//                // 固定原图高度,求最终宽度
//                finalWidth = (source.height.toSafeFloat() * ratio).toSafeInt()
//            }
//            // 输出宽度 < 输出高度,求宽高比
//        } else if (outWidth < outHeight) {
//            ratio = outWidth.toSafeFloat() / outHeight.toSafeFloat()
//            finalHeight = source.height
//            // 固定原图高度,求最终宽度
//            finalWidth = (source.height.toSafeFloat() * ratio).toSafeInt()
//            // 求出的最终宽度 > 原图宽度,求高宽比
//            if (finalWidth > source.width) {
//                ratio = outHeight.toSafeFloat() / outWidth.toSafeFloat()
//                finalWidth = source.width
//                finalHeight = (source.width.toSafeFloat() * ratio).toSafeInt()
//            }
//            // 输出宽度=输出高度
//        } else {
//            finalHeight = source.height
//            finalWidth = finalHeight
//        }
//        // 修正圆角
//        radius *= finalHeight.toSafeFloat() / outHeight.toSafeFloat()
//        val outBitmap = mBitmapPool.get(finalWidth, finalHeight, Bitmap.Config.ARGB_8888)
//        val canvas = Canvas(outBitmap)
//        val paint = Paint()
//        // 关联画笔绘制的原图bitmap
//        val shader = BitmapShader(source, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
//        // 计算中心位置,进行偏移
//        val width = (source.width - finalWidth) / 2
//        val height = (source.height - finalHeight) / 2
//        if (width != 0 || height != 0) {
//            val matrix = Matrix()
//            matrix.setTranslate((-width).toSafeFloat(), (-height).toSafeFloat())
//            shader.setLocalMatrix(matrix)
//        }
//        paint.shader = shader
//        paint.isAntiAlias = true
//        val rectF = RectF(0.0f, 0.0f, canvas.width.toSafeFloat(), canvas.height.toSafeFloat())
//        // 先绘制圆角矩形
//        canvas.drawRoundRect(rectF, radius, radius, paint)
//        // 左上角不为圆角
//        if (exceptLeftTop) canvas.drawRect(0f, 0f, radius, radius, paint)
//        // 右上角不为圆角
//        if (exceptRightTop) canvas.drawRect(canvas.width - radius, 0f, radius, radius, paint)
//        // 左下角不为圆角
//        if (exceptLeftBottom) canvas.drawRect(0f, canvas.height - radius, radius, canvas.height.toSafeFloat(), paint)
//        // 右下角不为圆角
//        if (exceptRightBottom) canvas.drawRect(canvas.width - radius, canvas.height - radius, canvas.width.toSafeFloat(), canvas.height.toSafeFloat(), paint)
//        return BitmapResource(outBitmap, mBitmapPool)
    }

    override fun updateDiskCacheKey(messageDigest: MessageDigest) {}

}