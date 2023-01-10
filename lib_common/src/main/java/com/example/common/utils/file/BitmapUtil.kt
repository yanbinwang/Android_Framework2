package com.example.common.utils.file

import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.media.ExifInterface
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.view.View
import com.example.common.BaseApplication
import com.example.common.config.Constants
import com.example.framework.utils.function.value.DateFormat
import com.example.framework.utils.function.value.getDateTime
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
    return BitmapDrawable(null, newBit)
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

/**
 * bitmap->存储的bitmap
 * root->图片保存路径
 * fileName->图片名称（扣除jpg和png的后缀）
 * delete->是否清除目录
 * formatJpg->确定图片类型
 * quality->压缩率
 */
fun saveBitmap(bitmap: Bitmap, root: String = "${Constants.APPLICATION_PATH}/保存图片", fileName: String = DateFormat.EN_YMDHMS.getDateTime(Date()), delete: Boolean = false, formatJpg: Boolean = true, quality: Int = 100): String? {
    val storeDir = File(root)
    if (delete) storeDir.absolutePath.deleteDir()
    if (!storeDir.mkdirs()) storeDir.createNewFile()
    val file = File(storeDir, "${fileName}${if (formatJpg) ".jpg" else ".png"}")
    try {
        val fileOutputStream = FileOutputStream(file)
        bitmap.compress(if (formatJpg) Bitmap.CompressFormat.JPEG else Bitmap.CompressFormat.PNG, quality, fileOutputStream)
        fileOutputStream.flush()
        fileOutputStream.close()
    } catch (_: Exception) {
    }
    bitmap.recycle()
    return file.absolutePath
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
        try {
            val fileOutputStream = FileOutputStream(tempFile)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream)
            fileOutputStream.flush()
            fileOutputStream.close()
            bitmap.recycle()
            if (delete) file.delete()
            tempFile
        } catch (e: IOException) {
            file
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
    when (exifInterface?.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
        ExifInterface.ORIENTATION_ROTATE_90 -> degree = 90f
        ExifInterface.ORIENTATION_ROTATE_180 -> degree = 180f
        ExifInterface.ORIENTATION_ROTATE_270 -> degree = 270f
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
        val c = Canvas(screenshot)
        if (needBg) c.drawColor(Color.WHITE)
        draw(c)
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
    matrix.postScale(w / width.toFloat(), h / height.toFloat()) //长和宽放大缩小的比例
    val resultBitmap = Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
    recycle()
    return resultBitmap
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