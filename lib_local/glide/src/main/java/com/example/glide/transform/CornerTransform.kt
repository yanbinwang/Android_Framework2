package com.example.glide.transform

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.RectF
import android.graphics.Shader
import androidx.annotation.ColorInt
import com.bumptech.glide.Glide
import com.bumptech.glide.load.Transformation
import com.bumptech.glide.load.engine.Resource
import com.bumptech.glide.load.resource.bitmap.BitmapResource
import com.example.framework.utils.function.value.toSafeFloat
import java.security.MessageDigest

/**
 * author: wyb
 * date: 2019/5/6.
 * @overRide leftTop/rightTop/leftBottom/rightBottom
 * @radius 弧度
 * @bgColor 覆盖幅度的背景颜色
 */
class CornerTransform(context: Context, private var overRide: BooleanArray, private var targetRadius: Float, @field:ColorInt private val bgColor: Int = Color.WHITE) : Transformation<Bitmap> {
    // 当前Glide线程池
    private val bitmapPool = Glide.get(context).bitmapPool

    override fun transform(context: Context, resource: Resource<Bitmap>, outWidth: Int, outHeight: Int): Resource<Bitmap> {
        // 原始Bitmap
        val source = resource.get()
        val sourceWidth = source.width
        val sourceHeight = source.height
        // 计算目标宽高
        val targetWidth = if (outWidth > 0) outWidth else sourceWidth
        val targetHeight = if (outHeight > 0) outHeight else sourceHeight
        // 从缓存池获取合适的Bitmap，若为null则创建新Bitmap（确保非null）
        val config = source.config ?: Bitmap.Config.ARGB_8888
        val outBitmap = bitmapPool.get(targetWidth, targetHeight, config)
        // 获取Bitmap对应的绘制画布并清空，避免旧Bitmap的残留内容
        val canvas = Canvas(outBitmap)
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
        // 启用抗锯齿画笔
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        // 使用Matrix进行缩放，匹配fitXY的拉伸效果
        val matrix = Matrix()
        matrix.setScale(targetWidth.toSafeFloat() / sourceWidth, targetHeight.toSafeFloat() / sourceHeight)
        // 创建 shader 并应用缩放矩阵 (使用带 BitmapShader 的 paint 绘制排除角的矩形时，会显示原图的局部像素（可能是拉伸后的异常内容）。必须用 纯色画笔 覆盖，确保与背景融合：)
        val shader = BitmapShader(source, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
        shader.setLocalMatrix(matrix)
        paint.shader = shader
        // 绘制圆角矩形
        val rectF = RectF(0f, 0f, targetWidth.toSafeFloat(), targetHeight.toSafeFloat())
        canvas.drawRoundRect(rectF, targetRadius, targetRadius, paint)
        // 绘制“扣掉内部圆角的遮罩”，覆盖边缘灰色
        val maskPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = bgColor
            style = Paint.Style.FILL
        }
        // 创建遮罩路径：矩形 - 内部圆角矩形（形成环形遮罩）
        val maskPath = Path().apply {
            // 先添加外层大矩形（覆盖整个图片）
            addRect(0f, 0f, targetWidth.toFloat(), targetHeight.toFloat(), Path.Direction.CW)
            // 再减去内部圆角矩形（与图片圆角一致）
            addRoundRect(rectF, targetRadius, targetRadius, Path.Direction.CW)
            // 设置路径运算模式：DIFFERENCE = 外层 - 内层（得到环形区域）
            fillType = Path.FillType.EVEN_ODD
        }
        // 绘制遮罩（环形区域会覆盖图片边缘，包括灰色部分）
        canvas.drawPath(maskPath, maskPaint)
        // 处理需要排除的角（绘制直角）
        if (overRide[0]) {
            canvas.drawRect(0f, 0f, targetRadius, targetRadius, paint)
        }
        if (overRide[1]) {
            canvas.drawRect(targetWidth - targetRadius, 0f, targetWidth.toSafeFloat(), targetRadius, paint)
        }
        if (overRide[2]) {
            canvas.drawRect(0f, targetHeight - targetRadius, targetRadius, targetHeight.toSafeFloat(), paint)
        }
        if (overRide[3]) {
            canvas.drawRect(targetWidth - targetRadius, targetHeight - targetRadius, targetWidth.toSafeFloat(), targetHeight.toSafeFloat(), paint)
        }
//        // 回收原始资源 (不能回收, resource 代表的是 Glide 缓存池里共享的那张图, 回收后所有应用内地址引用的地方都可能出问题)
//        resource.recycle()
        // 返回新的图片
        return BitmapResource(outBitmap, bitmapPool)
    }

    /**
     * Glide 的磁盘缓存是基于「资源 + 变换」的组合来存储的
     * 如果两个不同的 Transformation（比如 “圆角半径不同” 或 “排除的角不同”），却生成了相同的缓存 Key，Glide 会错误地复用缓存
     * 结果导致：
     * 1) 图片显示不符合预期（比如用了旧的圆角参数）；
     * 2) 有概率直接加载失败（缓存内容与当前变换不匹配）。
     */
    override fun updateDiskCacheKey(messageDigest: MessageDigest) {
        // 更新“变换标识”（固定字符串，区分不同Transform）
        messageDigest.update("CornerTransform".toByteArray())
        // 更新“圆角半径”（转成字节数组）
        messageDigest.update(targetRadius.toString().toByteArray())
        // 更新“排除角数组”（遍历每个布尔值，转成字节）
        for (bool in overRide) {
            messageDigest.update(if (bool) "1".toByteArray() else "0".toByteArray())
        }
    }

}