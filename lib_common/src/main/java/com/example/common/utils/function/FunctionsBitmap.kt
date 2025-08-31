package com.example.common.utils.function

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.view.View
import androidx.core.graphics.createBitmap
import androidx.core.graphics.drawable.toDrawable
import com.example.common.BaseApplication
import com.example.framework.utils.function.value.orZero
import com.example.framework.utils.function.value.toSafeFloat
import com.example.framework.utils.function.value.toSafeInt

/**
 * 读取mipmap下的图片
 */
fun Context?.decodeResource(id: Int): Bitmap? {
    this ?: return null
    return BitmapFactory.decodeResource(this.resources, id)
}

/**
 * 获取asset下的图片
 * "share/img_order_share_logo.webp".decodeAsset()
 */
fun Context?.decodeAsset(filePath: String): Bitmap? {
    this ?: return null
    return this.assets.open(filePath).use { BitmapFactory.decodeStream(it) }
}

fun String?.decodeAsset(): Bitmap? {
    this ?: return null
    return BaseApplication.instance.assets.open(this).use { BitmapFactory.decodeStream(it) }
}

/**
 * 获取路径图片的宽高
 * 当我们选择了一个图片，要等边裁剪时可使用当前方法获取对应宽高
 */
fun String?.decodeDimensions(): IntArray? {
    this ?: return null
    val options = BitmapFactory.Options()
    // 不加载图片到内存，只获取图片的尺寸信息
    options.inJustDecodeBounds = true
    BitmapFactory.decodeFile(this, options)
    // options.outWidth 和 options.outHeight 包含了图片的宽度和高度
    val width = options.outWidth
    val height = options.outHeight
    return intArrayOf(width, height)
}

fun BitmapDrawable?.decodeDimensions(): IntArray {
    this ?: return intArrayOf(0, 0)
    return try {
        intArrayOf(intrinsicWidth, intrinsicHeight)
    } catch (e: Exception) {
        e.printStackTrace()
        intArrayOf(0, 0)
    }
}

/**
 * 判断一个路径地址是否为一张图片
 * inJustDecodeBounds=true不会把图片放入内存，只会获取宽高，判断当前路径是否为图片，是的话捕获文件路径
 */
fun String?.isValidImage(): Boolean {
    return this?.let { path ->
        try {
            // 检查文件是否存在
            if (!path.isExists()) return@let false
            // 仅获取图片宽高信息
            val dimensions = path.decodeDimensions() ?: intArrayOf(0, 0)
            // 有效图片的宽高必须大于0
            dimensions[0] > 0 && dimensions[1] > 0
        } catch (e: Exception) {
            // 捕获文件操作异常
            e.printStackTrace()
            false
        }
    } ?: false
}

/**
 * 重设Bitmap大小，保持图片比例并进行边界检查
 * @param targetWidth 目标宽度，必须大于0
 * @param targetHeight 目标高度，必须大于0
 * @return 缩放后的Bitmap，若原Bitmap为空或参数无效则返回null
 */
fun Bitmap?.scaleBitmap(targetWidth: Int, targetHeight: Int): Bitmap? {
    this ?: return null
    // 校验目标尺寸有效性
    if (targetWidth <= 0 || targetHeight <= 0) {
        return null
    }
    // 原始宽高
    val originalWidth = width
    val originalHeight = height
    // 计算缩放比例，保持图片原有比例
    val scaleWidth = targetWidth.toSafeFloat() / originalWidth.toSafeFloat()
    val scaleHeight = targetHeight.toSafeFloat() / originalHeight.toSafeFloat()
    val matrix = Matrix().apply {
        postScale(scaleWidth, scaleHeight)
    }
    return try {
        // 生成缩放图片,并开启抗锯齿(filter=true)
        val resultBitmap = Bitmap.createBitmap(this, 0, 0, originalWidth, originalHeight, matrix, true)
        /**
         * 只有当新 bitmap 与原 bitmap 不是同一个对象时才回收原 bitmap
         * 当缩放比例为 1:1（目标尺寸与原图尺寸完全一致）
         * 系统对 Bitmap 进行了复用优化（虽然少见，但 Android 框架存在类似优化逻辑）
         * 这时此时如果 resultBitmap === this 时，直接调用 recycle() 会导致：
         * 新返回的 resultBitmap 也被回收，变成无效对象
         * 后续使用该 Bitmap 会抛出异常（Canvas: trying to use a recycled bitmap）
         */
        if (resultBitmap !== this) {
            recycle()
        }
        resultBitmap
    } catch (e: Exception) {
        e.printStackTrace()
        // 捕获可能的异常（如内存不足）
        null
    }
}

/**
 * 按比例缩放 Bitmap
 * @param scale 缩放比例，必须大于 0
 * @param filter 是否启用抗锯齿，true 表示边缘更平滑
 * @return 缩放后的 Bitmap，若原 Bitmap 为空或参数无效则返回 null
 */
fun Bitmap?.scaleBitmap(scale: Float, filter: Boolean = false): Bitmap? {
    this ?: return null
    if (scale <= 0) {
        return null
    }
    val matrix = Matrix().apply {
        postScale(scale, scale)
    }
    return try {
        val resultBitmap = Bitmap.createBitmap(this, 0, 0, width, height, matrix, filter)
        if (resultBitmap !== this) {
            recycle()
        }
        resultBitmap
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

/**
 * 按尺寸阈值进行图片压缩（宽≤720，高≤1280）
 * 当宽超过720时按宽度比例缩放，当高超过1280时按高度比例缩放，两者都超时按宽比例缩放
 * @return 压缩后的Bitmap，原Bitmap为空时返回null
 */
fun Bitmap?.scaleBitmap(): Bitmap? {
    this ?: return null
    val maxWidth = 720f
    val maxHeight = 1280f
    val scale = when {
        width > maxWidth -> maxWidth / width
        height > maxHeight -> maxHeight / height
        else -> 1f
    }
    return scaleBitmap(scale, true)
}

/**
 * 根据目标宽高缩放 Drawable
 * @param context 上下文（用于创建 Drawable）
 * @param targetWidth 目标宽度（像素）
 * @param targetHeight 目标高度（像素，默认等于宽度）
 * @return 缩放后的 Drawable（原 Drawable 无法缩放时返回自身）
 */
fun Drawable.scaleToSize(context: Context, targetWidth: Int, targetHeight: Int = targetWidth): Drawable {
    // 校验参数有效性
    if (targetWidth <= 0 || targetHeight <= 0 || intrinsicWidth <= 0 || intrinsicHeight <= 0) {
        return this
    }
    // 按指定宽高缩放 Drawable
    val sourceBitmap = toBitmap()
    val matrix = Matrix()
    val scaleWidth = targetWidth.toSafeFloat() / intrinsicWidth.toSafeFloat()
    val scaleHeight = targetHeight.toSafeFloat() / intrinsicHeight.toSafeFloat()
    matrix.postScale(scaleWidth, scaleHeight)
    return try {
        val newBitmap = Bitmap.createBitmap(sourceBitmap, 0, 0, intrinsicWidth, intrinsicHeight, matrix, true)
        // 回收临时 Bitmap（注意：newBitmap 若与 sourceBitmap 是同一对象则不回收）
        if (newBitmap !== sourceBitmap) {
            sourceBitmap.recycle()
        }
        newBitmap.toDrawable(context.resources)
    } catch (e: Exception) {
        e.printStackTrace()
        // 异常时回收临时 Bitmap
        sourceBitmap.recycle()
        // 失败时返回原 Drawable
        this
    }
}

fun Drawable.toBitmap(): Bitmap {
    // 若本身是 BitmapDrawable，直接返回其 Bitmap（避免重复绘制）
    if (this is BitmapDrawable) {
        val config = bitmap.config ?: Bitmap.Config.ARGB_8888
        // 复制一份避免外部修改原 Bitmap
        return bitmap.copy(config, true)
    }
    // 处理 intrinsic 尺寸为 0 的情况
    val width = if (intrinsicWidth > 0) intrinsicWidth else 1
    val height = if (intrinsicHeight > 0) intrinsicHeight else 1
    // 根据透明度选择配置
    val config = if (opacity != PixelFormat.OPAQUE) Bitmap.Config.ARGB_8888 else Bitmap.Config.RGB_565
    return try {
        val bitmap = createBitmap(width, height, config)
        val canvas = Canvas(bitmap)
        setBounds(0, 0, width, height)
        draw(canvas)
        bitmap
    } catch (e: Exception) {
        // 捕获内存不足等异常
        e.printStackTrace()
        // 极端情况返回最小 Bitmap
        createBitmap(1, 1, config)
    }
}

/**
 * 获取一个 View 的缓存视图
 */
fun View?.getBitmap(targetWidth: Int? = null, targetHeight: Int? = null, needBg: Boolean = true): Bitmap? {
    this ?: return null
    //请求转换
    return try {
        val screenshot = createBitmap(width, height, if (needBg) Bitmap.Config.RGB_565 else Bitmap.Config.ARGB_8888)
        val canvas = Canvas(screenshot)
        if (needBg) canvas.drawColor(Color.WHITE)
        draw(canvas)
        if (targetWidth != null && targetHeight != null) {
            screenshot.scaleBitmap(targetWidth, targetHeight)
        } else {
            screenshot
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

/**
 * 当measure完后，并不会实际改变View的尺寸，需要调用View.layout方法去进行布局
 * 按示例调用layout函数后，View的大小将会变成你想要设置成的大小
 */
fun View.loadLayout(width: Int, height: Int) {
    //整个View的大小 参数是左上角 和右下角的坐标
    layout(0, 0, width, height)
    val measuredWidth = View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY)
    val measuredHeight = View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY)
    measure(measuredWidth, measuredHeight)
    layout(0, 0, getMeasuredWidth(), getMeasuredHeight())
}

/**
 * 如果不设置canvas画布为白色，则生成透明
 */
fun View.loadBitmap(): Bitmap {
    val bitmap = createBitmap(width, height)
    val canvas = Canvas(bitmap)
    canvas.drawColor(Color.WHITE)
    layout(0, 0, width, height)
    draw(canvas)
    return bitmap
}

/**
 * 画笔默认取中心点坐标，所以要除2
 * 只有继承了当前画笔接口的类才能使用以下方法
 */
interface PaintImpl {

    /**
     * x:距左距离
     * y:距上距离
     * 以左侧为基准点绘制对应文字
     */
    fun Paint.drawTextLeft(x: Number?, y: Number?, text: String, canvas: Canvas) {
        val measureHeight = measureSize(text).second
        canvas.drawText(text, x.toSafeFloat(), (y.toSafeFloat() + measureHeight / 2), this)
    }

    /**
     * x:距左距离
     * y:距上距离
     * 以中心为基准点绘制对应文字
     */
    fun Paint.drawTextCenter(x: Number?, y: Number?, text: String, canvas: Canvas) {
        val size = measureSize(text)
        canvas.drawText(text, (x.toSafeFloat() - size.first / 2), (y.toSafeFloat() + size.second / 2), this)
    }

    /**
     * 测绘绘制文字宽高
     * first-》宽
     * second-》高
     */
    fun Paint.measureSize(text: String): Pair<Float, Float> {
        val measureWidth = measureText(text)
        val measureHeight = fontMetrics.bottom - fontMetrics.top
        return measureWidth to measureHeight
    }

    /**
     * text本身默认绘制是一行的，不会自动换行，使用此方法传入指定宽度换行
     */
    fun TextPaint.drawTextStatic(maxTextWidth: Number?, text: String, canvas: Canvas, dx: Number? = 0, dy: Number? = 0, spacingmult: Number? = 1f) {
        //spacingmult 是行间距的倍数，通常情况下填 1 就好；
        //spacingadd 是行间距的额外增加值，通常情况下填 0 就好
        val layout = StaticLayout(text, this, maxTextWidth.toSafeInt(), Layout.Alignment.ALIGN_NORMAL, spacingmult.toSafeFloat(), 0f, false)
        canvas.save()
        //StaticLayout默认画在Canvas的(0,0)点，如果需要调整位置只能在draw之前移Canvas的起始坐标
        canvas.translate(dx.toSafeFloat(), dy.toSafeFloat())
        layout.draw(canvas)
    }

    /**
     * 获取一个预设的文字画笔
     */
    fun getTextPaint(textSize: Float, color: Int = Color.WHITE, typeface: Typeface = Typeface.DEFAULT): TextPaint {
        val paint = TextPaint()
        paint.isAntiAlias = true
        paint.textSize = textSize
        paint.color = color
        paint.typeface = typeface
//        paint.typeface = ResourcesCompat.getFont(BaseApplication.instance, fontId)
        return paint
    }

}