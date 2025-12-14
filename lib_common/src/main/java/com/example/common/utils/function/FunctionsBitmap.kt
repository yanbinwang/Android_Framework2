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
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import androidx.annotation.ColorInt
import androidx.core.graphics.createBitmap
import androidx.core.graphics.drawable.toDrawable
import com.example.common.BaseApplication
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
 * 配置高清解码参数
 * val options = BitmapFactory.Options().apply {
 * // 强制用最高精度格式（支持透明+全色域，4字节/像素）
 * inPreferredConfig = Bitmap.Config.ARGB_8888
 * // 禁用系统自动缩放（避免加载时就压缩像素）
 * inScaled = false
 * // 禁用内存复用（避免复用低精度 Bitmap 的内存，导致细节丢失）
 * inMutable = false
 * }
 */
fun Context?.decodeAsset(filePath: String, opts: BitmapFactory.Options? = null): Bitmap? {
    this ?: return null
    return this.assets.open(filePath).use {
        BitmapFactory.decodeStream(it, null, opts)
    }
}

fun String?.decodeAsset(opts: BitmapFactory.Options? = null): Bitmap? {
    this ?: return null
    return BaseApplication.instance.assets.open(this).use {
        BitmapFactory.decodeStream(it, null, opts)
    }
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
            if (!path.isPathExists()) return@let false
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
 * 提取Bitmap在x轴中心点颜色
 */
fun Bitmap?.getCenterPixelColor(): Int {
    // 如果bitmap为空，返回默认颜色值
    this ?: return Color.WHITE
    // 计算中心坐标
    val centerX = width / 2
    // Y轴取第1个像素（索引从0开始，所以是0）
    val topY = 0
    // 确保坐标在有效范围内
    return if (width > 0 && height > 0 && centerX in 0 until width && topY in 0 until height) {
        getPixel(centerX, topY)
    } else {
        Color.WHITE
    }
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
    // 如果目标尺寸与原始尺寸相同，直接返回原Bitmap
    if (targetWidth == originalWidth && targetHeight == originalHeight) {
        return this
    }
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
         * 当缩放比例为 1:1（目标尺寸与原图尺寸完全一致,如果传入的 targetWidth 等于 Bitmap 原始宽度、targetHeight 等于原始高度（比如原始 Bitmap 是 500x500，目标尺寸也是 500x500），就属于 “1:1 缩放）
         * 系统对 Bitmap 进行了复用优化（虽然少见，但 Android 框架存在类似优化逻辑）
         * 这时此时如果 resultBitmap === this 时，直接调用 recycle() 会导致：
         * 新返回的 resultBitmap 也被回收，变成无效对象
         * 后续使用该 Bitmap 会抛出异常（Canvas: trying to use a recycled bitmap）
         */
        if (resultBitmap !== this) {
            safeRecycle()
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
            safeRecycle()
        }
        resultBitmap
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

///**
// * 按尺寸阈值进行图片压缩（宽≤720，高≤1280）
// * 当宽超过720时按宽度比例缩放，当高超过1280时按高度比例缩放，两者都超时按宽比例缩放
// * @return 压缩后的Bitmap，原Bitmap为空时返回null
// */
//fun Bitmap?.scaleBitmap(): Bitmap? {
//    this ?: return null
//    val maxWidth = 720f
//    val maxHeight = 1280f
//    val scale = when {
//        width > maxWidth -> maxWidth / width
//        height > maxHeight -> maxHeight / height
//        else -> 1f
//    }
//    return scaleBitmap(scale, true)
//}

/**
 * 安全回收Bitmap的扩展函数
 */
fun Bitmap?.safeRecycle() {
    this ?: return
    if (!isRecycled) {
        recycle()
    }
}

/**
 * 安全获取Bitmap的扩展函数
 */
fun Drawable?.getBitmap(): Bitmap? {
    return (this as? BitmapDrawable)?.bitmap
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
            sourceBitmap.safeRecycle()
        }
        newBitmap.toDrawable(context.resources)
    } catch (e: Exception) {
        e.printStackTrace()
        // 异常时回收临时 Bitmap
        sourceBitmap.safeRecycle()
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
    return try {
        // RGB_565 格式（仅支持 RGB 三色，不支持透明，内存占用是 ARGB_8888 的一半）
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
 */
fun View.loadLayout(targetWidth: Int, targetHeight: Int = WRAP_CONTENT) {
    // 强制触发View测量（同步执行，不依赖系统回调）
    val widthSpec = View.MeasureSpec.makeMeasureSpec(targetWidth, View.MeasureSpec.EXACTLY)
    val heightSpec = if (targetHeight < 0) {
        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
    } else {
        View.MeasureSpec.makeMeasureSpec(targetHeight, View.MeasureSpec.EXACTLY)
    }
    measure(widthSpec, heightSpec)
    // 强制布局（确保View有位置和尺寸）
    if (measuredWidth <= 0 || measuredHeight <= 0) {
        throw IllegalStateException("View测量失败，尺寸为0")
    }
    layout(0, 0, measuredWidth, measuredHeight)
}

/**
 * 如果不设置canvas画布为白色，则生成透明
 */
fun View.loadBitmap(targetWidth: Int = measuredWidth, targetHeight: Int = measuredHeight, @ColorInt color: Int = Color.TRANSPARENT): Bitmap {
    // 创建与 View 测量后尺寸一致的 Bitmap（ARGB_8888 格式，保证画质）
    val bitmap = createBitmap(targetWidth, targetHeight)
    // 创建与Bitmap关联的Canvas
    val canvas = Canvas(bitmap)
    // 绘制背景（避免透明背景，可根据需求修改颜色）
    canvas.drawColor(color)
    // 强制View布局到指定位置和尺寸
    layout(0, 0, width, height)
    // 将 View 绘制到 Canvas（此时 View 已完成布局，尺寸有效）
    draw(canvas)
    // 返回生成的Bitmap
    return bitmap
}

/**
 * 画笔默认取中心点坐标，所以要除2
 * 只有继承了当前画笔接口的类才能使用以下方法
 * private fun Bitmap.drawShareBitMap(info: BitmapInfo, refCode: String?): Bitmap {
 *     val paint = Paint()
 *     val canvasHeight = height + 170
 *     val bitmap = Bitmap.createBitmap(width, canvasHeight, Bitmap.Config.RGB_565)
 *     val canvas = Canvas(bitmap)
 *     canvas.drawColor(Color.WHITE)
 *     canvas.drawBitmap(this, 0f, 0f, paint)
 *     //底部logo
 *     "share/img_order_share_logo.webp".getBitmapFromAsset()?.let { canvas.drawBitmap(it, 30f, 812f, paint) }
 *     //邀請碼標題
 *     val refPaint = getTextPaint(32f, MyApplication.instance.color(R.color.inviteFriendTxt), fontId = R.font.font_bold)
 *     val refTxt = string(R.string.orderShareRefCode)
 *     val refWidth = refPaint.measureText(refTxt)
 *     refPaint.drawTextLeft(29, 899, refTxt, canvas)
 *     //邀請碼
 *     getTextPaint(32f, MyApplication.instance.color(R.color.inviteFriendTxt), R.font.font_bold).drawTextLeft(refWidth + 29 + 15, 899, refCode.orNoData, canvas)
 *     //二維碼
 *     QRCodeBuilder().content(string(R.string.orderShareQrUrl)).size(126).build()?.let { canvas.drawBitmap(it, 532f, 806f, paint) }
 *     recycle()
 *     return bitmap
 * }
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