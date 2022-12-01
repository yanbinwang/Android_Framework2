package com.example.common.utils.file

import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import com.example.base.utils.function.value.DateFormat
import com.example.base.utils.function.value.getDateTime
import com.example.common.BaseApplication
import com.example.common.constant.Constants
import java.io.File
import java.io.FileOutputStream
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

/**
 * bitmap->存储的bitmap
 * root->图片保存路径
 * fileName->图片名称（扣除jpg和png的后缀）
 * formatJpg->确定图片类型
 * quality->压缩率
 * clear->是否清除本地路径
 */
fun saveBitmap(bitmap: Bitmap, root: String = "${Constants.APPLICATION_FILE_PATH}/图片", fileName: String = DateFormat.EN_YMDHMS.getDateTime(Date()), delete: Boolean = false, formatJpg: Boolean = true, quality: Int = 100): String? {
    val storeDir = File(root)
    if (delete) storeDir.absolutePath.deleteDir()//删除路径下所有文件
    if (!storeDir.mkdirs()) storeDir.createNewFile()//需要权限
    val file = File(storeDir, "${fileName}${if (formatJpg) ".jpg" else ".png"}")
    try {
        //通过io流的方式来压缩保存图片
        val fileOutputStream = FileOutputStream(file)
        bitmap.compress(if (formatJpg) Bitmap.CompressFormat.JPEG else Bitmap.CompressFormat.PNG, quality, fileOutputStream)//png的话100不响应，但是可以维持图片透明度
        fileOutputStream.flush()
        fileOutputStream.close()
    } catch (_: Exception) {
    }
    bitmap.recycle()
    return file.absolutePath
}