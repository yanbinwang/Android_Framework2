package com.example.base.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import com.example.base.utils.LogUtil.e
import java.io.*
import java.util.*

/**
 * Created by WangYanBin on 2020/7/23.
 * 图片工具类
 */
object CompressUtil {

    @JvmOverloads
    @JvmStatic
    fun compress(bitmap: Bitmap, length: Int = 512): ByteArrayOutputStream {
        val bitmapTmp = compressBySize(bitmap)
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmapTmp.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
        var options = 100
        while (byteArrayOutputStream.toByteArray().size / 1024 > length) {
            byteArrayOutputStream.reset()
            options -= 10
            bitmapTmp.compress(Bitmap.CompressFormat.JPEG, options, byteArrayOutputStream)
        }
        e("length", byteArrayOutputStream.toByteArray().size.toString())
        return byteArrayOutputStream
    }

    @JvmStatic
    fun compressBySize(bitmap: Bitmap): Bitmap {
        var size = 1f
        val width = bitmap.width
        val height = bitmap.height
        val matrix = Matrix()
        if (width > 720) {
            size = 720f / width
            e("w", bitmap.width.toString())
            e("h", bitmap.height.toString())
        } else if (height > 1280) {
            size = 1280f / height
            e("w", bitmap.width.toString())
            e("h", bitmap.height.toString())
        }
        matrix.postScale(size, size)
        return Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true)
    }

    @JvmOverloads
    @JvmStatic
    fun scale(context: Context, file: File, fileMaxSize: Long = 100 * 1024): File {
        val fileSize = file.length()
        var scaleSize = 1f
        return if (fileSize >= fileMaxSize) {
            try {
                val options = BitmapFactory.Options()
                options.inJustDecodeBounds = true
                val width = options.outWidth
                val height = options.outHeight
                if (width > 720) {
                    scaleSize = width / 480f
                } else if (height > 1280) {
                    scaleSize = width / 1080f
                }
                options.inJustDecodeBounds = false
                options.inSampleSize = (scaleSize + 0.5).toInt()
                var bitmap = BitmapFactory.decodeFile(file.path, options)
                bitmap = compressBySize(bitmap)
//                val tempFile = File(context.applicationContext?.externalCacheDir, "${DateUtil.getDateTime("yyyyMMdd_HHmmss", Date())}.jpg")
                val tempFile = File(context.getExternalFilesDir(null)?.absolutePath, "${DateUtil.getDateTime("yyyyMMdd_HHmmss", Date())}.jpg")
                val fileOutputStream = try {
                    FileOutputStream(tempFile)
                } catch (e: FileNotFoundException) {
                    return file
                }
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream)
                fileOutputStream.flush()
                fileOutputStream.close()
                bitmap.recycle()
                tempFile
            } catch (e: IOException) {
                file
            }
        } else file
    }

    @JvmStatic
    fun scale(context: Context, bitmap: Bitmap): Bitmap {
        var bitmapTmp = bitmap
        val fileSize = getBitmapSize(bitmapTmp)
        var scaleSize = 1f
        val fileMaxSize = 100 * 1024
        return if (fileSize >= fileMaxSize) {
            try {
                val options = BitmapFactory.Options()
                options.inJustDecodeBounds = true
                val width = options.outWidth
                val height = options.outHeight
                if (width > 720) {
                    scaleSize = width / 480f
                } else if (height > 1280) {
                    scaleSize = width / 1080f
                }
                options.inJustDecodeBounds = false
                options.inSampleSize = (scaleSize + 0.5).toInt()
                bitmapTmp = compressBySize(bitmapTmp)
//                val file = File(context.applicationContext?.externalCacheDir, "${DateUtil.getDateTime("yyyyMMdd_HHmmss", Date())}.jpg")
                val file = File(context.getExternalFilesDir(null)?.absolutePath, "${DateUtil.getDateTime("yyyyMMdd_HHmmss", Date())}.jpg")
                val fileOutputStream = try {
                    FileOutputStream(file)
                } catch (e: FileNotFoundException) {
                    return bitmapTmp
                }
                bitmapTmp.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream)
                fileOutputStream.flush()
                fileOutputStream.close()
                bitmapTmp
            } catch (e: IOException) {
                bitmapTmp
            }
        } else bitmapTmp
    }

    @JvmStatic
    fun getBitmapSize(bitmap: Bitmap) = bitmap.allocationByteCount

    @Throws(IOException::class)
    fun getBytes(inputStream: InputStream): ByteArray {
        val byteArrayOutputStream = ByteArrayOutputStream()
        val buffer = ByteArray(1024) // 用数据装
        var len: Int
        while (inputStream.read(buffer).also { len = it } != -1) {
            byteArrayOutputStream.write(buffer, 0, len)
        }
        byteArrayOutputStream.close()
        return byteArrayOutputStream.toByteArray()
    }

    @JvmStatic
    fun degree(context: Context, file: File): File {
        val degree = readDegree(file.path)
        var bitmap: Bitmap
        return if (degree != 0f) {
            //旋转图片
            val matrix = Matrix()
            matrix.postRotate(degree)
            bitmap = BitmapFactory.decodeFile(file.path)
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
//            val tempFile = File(context.applicationContext?.externalCacheDir, DateUtil.getDateTime("yyyyMMdd_HHmmss", Date()) + ".jpg")
            val tempFile = File(context.getExternalFilesDir(null)?.absolutePath, DateUtil.getDateTime("yyyyMMdd_HHmmss", Date()) + ".jpg")
            val fileOutputStream: FileOutputStream
            try {
                fileOutputStream = FileOutputStream(tempFile)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream)
                fileOutputStream.flush()
                fileOutputStream.close()
                bitmap.recycle()
                tempFile
            } catch (e: IOException) {
                file
            }
        } else file
    }

    //读取图片的方向
    private fun readDegree(path: String): Float {
        var degree = 0f
        //读取图片文件信息的类ExifInterface
        var exifInterface: ExifInterface? = null
        try {
            exifInterface = ExifInterface(path)
        } catch (ignored: IOException) {
        } finally {
            when (exifInterface?.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                ExifInterface.ORIENTATION_ROTATE_90 -> degree = 90f
                ExifInterface.ORIENTATION_ROTATE_180 -> degree = 180f
                ExifInterface.ORIENTATION_ROTATE_270 -> degree = 270f
            }
        }
        return degree
    }

}