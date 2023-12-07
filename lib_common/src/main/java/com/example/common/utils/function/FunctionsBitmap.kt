package com.example.common.utils.function

import android.content.Context
import android.graphics.*
import android.graphics.Bitmap.CompressFormat.JPEG
import android.graphics.Bitmap.CompressFormat.PNG
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.view.View
import androidx.exifinterface.media.ExifInterface
import androidx.exifinterface.media.ExifInterface.*
import com.example.common.BaseApplication
import com.example.common.config.Constants
import com.example.common.utils.ScreenUtil
import com.example.common.utils.file.deleteDir
import com.example.common.utils.file.isMkdirs
import com.example.framework.utils.function.value.DateFormat.EN_YMDHMS
import com.example.framework.utils.function.value.convert
import com.example.framework.utils.function.value.toSafeFloat
import com.example.framework.utils.function.value.toSafeInt
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.util.*

/**
 * 获取asset下的图片
 * "share/img_order_share_logo.webp".decodeAsset()
 */
fun String?.decodeAsset(): Bitmap? {
    this ?: return null
    var stream: InputStream? = null
    return try {
        stream = BaseApplication.instance.assets.open(this)
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
fun Bitmap?.scaleBitmap(scale: Float, filter: Boolean = false): Bitmap? {
    this ?: return null
    val matrix = Matrix()
    matrix.postScale(scale, scale)
    val bit = Bitmap.createBitmap(this, 0, 0, width, height, matrix, filter)
    if (!isRecycled) recycle()
    return bit
}

/**
 * 尺寸压缩
 */
fun Bitmap?.scaleBitmap(): Bitmap? {
    this ?: return null
    return scaleBitmap(
        if (width > 720) {
            720f / width
        } else if (height > 1280) {
            1280f / height
        } else {
            1f
        }, true)
}

/**
 * 根据宽高缩放图片
 */
fun Drawable.zoomDrawable(w: Int, h: Int = w): Drawable {
    val oldBit = drawableToBitmap()
    val matrix = Matrix()
    val scaleWidth = w.toFloat() / intrinsicWidth
    val scaleHeight = h.toFloat() / intrinsicHeight
    matrix.postScale(scaleWidth, scaleHeight)
    val newBit = Bitmap.createBitmap(oldBit, 0, 0, intrinsicWidth, intrinsicHeight, matrix, true)
    oldBit.recycle()
    return BitmapDrawable(null, newBit)
}

fun Drawable.drawableToBitmap(): Bitmap {
    val config = if (opacity != PixelFormat.OPAQUE) Bitmap.Config.ARGB_8888 else Bitmap.Config.RGB_565
    val bitmap = Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, config)
    val canvas = Canvas(bitmap)
    setBounds(0, 0, intrinsicWidth, intrinsicHeight)
    draw(canvas)
    return bitmap
}

/**
 * bitmap->存储的bitmap
 * root->图片保存路径
 * fileName->图片名称（扣除jpg和png的后缀）
 * deleteDir->是否清除目录
 * format->图片类型
 * quality->压缩率
 */
fun saveBit(bitmap: Bitmap, root: String = "${Constants.APPLICATION_PATH}/保存图片", fileName: String = EN_YMDHMS.convert(Date()), deleteDir: Boolean = false, format: Bitmap.CompressFormat = JPEG, quality: Int = 100): String? {
    //存储目录文件
    val storeDir = File(root)
    //存储目录完整的手机路径
    val storeDirRoot = storeDir.absolutePath
    //先判断是否需要清空目录，再判断是否存在（不存在则创建）
    if (deleteDir) storeDirRoot.deleteDir()
    storeDirRoot.isMkdirs()
    //在目录文件夹下生成一个新的图片
    val file = File(storeDir, "${fileName}${format.getSuffix()}")
    var fileOutputStream : FileOutputStream? = null
    //开流开始写入
    try {
        fileOutputStream = FileOutputStream(file)
        //如果是Bitmap.CompressFormat.PNG，无论quality为何值，压缩后图片文件大小都不会变化
        bitmap.compress(format, if (format != PNG) quality else 100, fileOutputStream)
    } catch (_: Exception) {
    } finally {
        fileOutputStream?.flush()
        fileOutputStream?.close()
        bitmap.recycle()
    }
    return file.absolutePath
}

/**
 * 根据要保存的格式，返回对应后缀名
 * 安卓只支持一下三种
 */
private fun Bitmap.CompressFormat.getSuffix(): String {
    return when (this) {
        JPEG -> ".jpg"
        PNG -> ".png"
        else -> ".webp"
    }
}

/**
 * 旋转图片
 * 修整部分图片方向不正常
 * 取得一个新的图片文件
 */
fun Context.degreeImage(file: File, delete: Boolean = false): File {
    val degree = readDegree(file.absolutePath)
    var bitmap: Bitmap
    return if (degree != 0f) {
        val matrix = Matrix()
        matrix.postRotate(degree)
        BitmapFactory.decodeFile(file.absolutePath).let {
            bitmap = Bitmap.createBitmap(it, 0, 0, it.width, it.height, matrix, true)
            it.recycle()
        }
        val tempFile = File(applicationContext.externalCacheDir, file.name.replace(".jpg", "_degree.jpg"))
        var fileOutputStream : FileOutputStream? = null
        try {
            fileOutputStream = FileOutputStream(tempFile)
            bitmap.compress(JPEG, 100, fileOutputStream)
            if (delete) file.delete()
            tempFile
        } catch (e: IOException) {
            file
        } finally {
            fileOutputStream?.flush()
            fileOutputStream?.close()
            bitmap.recycle()
        }
    } else file
}

/**
 * 读取图片的方向
 * 部分手机拍摄需要设置手机屏幕screenOrientation
 * 不然会读取为0
 */
fun readDegree(path: String): Float {
    var degree = 0f
    var exifInterface: ExifInterface? = null
    try {
        exifInterface = ExifInterface(path)
    } catch (_: IOException) {
    }
    when (exifInterface?.getAttributeInt(TAG_ORIENTATION, ORIENTATION_NORMAL)) {
        ORIENTATION_ROTATE_90 -> degree = 90f
        ORIENTATION_ROTATE_180 -> degree = 180f
        ORIENTATION_ROTATE_270 -> degree = 270f
    }
    return degree
}

/**
 * 获取一个 View 的缓存视图
 *
 * @param view
 * @return
 */
fun View?.getBitmapFromView(w: Int? = null, h: Int? = null, needBg: Boolean = true): Bitmap? {
    this ?: return null
    //请求转换
    return try {
        val screenshot = Bitmap.createBitmap(width, height, if (needBg) Bitmap.Config.RGB_565 else Bitmap.Config.ARGB_8888)
        val canvas = Canvas(screenshot)
        if (needBg) canvas.drawColor(Color.WHITE)
        draw(canvas)
        if (w != null && h != null) {
            screenshot.resizeBitmap(w, h)
        } else {
            screenshot
        }
    } catch (e: Exception) {
        null
    }
}

/**
 * 重设bitmap大小
 */
fun Bitmap?.resizeBitmap(w: Int, h: Int): Bitmap? {
    this ?: return null
    val matrix = Matrix()
    matrix.postScale(w / width.toSafeFloat(), h / height.toSafeFloat()) //长和宽放大缩小的比例
    val resultBitmap = Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
    recycle()
    return resultBitmap
}

/**
 * 当measure完后，并不会实际改变View的尺寸，需要调用View.layout方法去进行布局
 * 按示例调用layout函数后，View的大小将会变成你想要设置成的大小
 */
fun View.loadLayout(width: Int, height: Int) {
    //整个View的大小 参数是左上角 和右下角的坐标
    layout(0, 0, width, height)
    val measuredWidth = View.MeasureSpec.makeMeasureSpec(ScreenUtil.screenWidth, View.MeasureSpec.EXACTLY)
    val measuredHeight = View.MeasureSpec.makeMeasureSpec(ScreenUtil.screenHeight, View.MeasureSpec.EXACTLY)
    measure(measuredWidth, measuredHeight)
    layout(0, 0, measuredWidth, measuredHeight)
}

//如果不设置canvas画布为白色，则生成透明
fun View.loadBitmap(): Bitmap? {
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
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