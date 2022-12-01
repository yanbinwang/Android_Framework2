package com.example.common.utils.file

import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.media.ExifInterface
import com.example.base.utils.function.value.DateFormat
import com.example.base.utils.function.value.getDateTime
import com.example.common.BaseApplication
import com.example.common.constant.Constants
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.util.*

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
    matrix.postScale(scale, scale)
    val bit = Bitmap.createBitmap(this, 0, 0, width, height, matrix, false)
    if (!isRecycled) recycle()
    return bit
}

/**
 * 尺寸压缩
 */
fun Bitmap?.scaleBitmap(): Bitmap? {
    this ?: return null
    var size = 1f
    val matrix = Matrix()
    if (width > 720) {
        size = 720f / width
    } else if (height > 1280) {
        size = 1280f / height
    }
    matrix.postScale(size, size)
    val bitmap = Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
    recycle()
    return bitmap
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

/**
 * bitmap->存储的bitmap
 * root->图片保存路径
 * fileName->图片名称（扣除jpg和png的后缀）
 * delete->是否清除目录
 * formatJpg->确定图片类型
 * quality->压缩率
 */
fun saveBitmap(bitmap: Bitmap, root: String = "${Constants.APPLICATION_FILE_PATH}/图片", fileName: String = DateFormat.EN_YMDHMS.getDateTime(Date()), delete: Boolean = false, formatJpg: Boolean = true, quality: Int = 100): String? {
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
    return if (degree != 0) {
        val matrix = Matrix()
        matrix.postRotate(degree.toFloat())
        BitmapFactory.decodeFile(file.absolutePath).let {
            bitmap = Bitmap.createBitmap(it, 0, 0, it.width, it.height, matrix, true)
            it.recycle()
        }
        val tempFile =
            File(applicationContext.externalCacheDir, file.name.replace(".jpg", "_degree.jpg"))
        val fileOutputStream: FileOutputStream
        try {
            fileOutputStream = FileOutputStream(tempFile)
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
fun readDegree(path: String): Int {
    var degree = 0
    var exifInterface: ExifInterface? = null
    try {
        exifInterface = ExifInterface(path)
    } catch (_: IOException) {
    }
    when (exifInterface?.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
        ExifInterface.ORIENTATION_ROTATE_90 -> degree = 90
        ExifInterface.ORIENTATION_ROTATE_180 -> degree = 180
        ExifInterface.ORIENTATION_ROTATE_270 -> degree = 270
    }
    return degree
}