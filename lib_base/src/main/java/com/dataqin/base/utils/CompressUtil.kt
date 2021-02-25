package com.dataqin.base.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import com.dataqin.base.utils.LogUtil.e
import java.io.*

/**
 * Created by WangYanBin on 2020/7/23.
 * 图片工具类
 */
object CompressUtil {
    private const val defaultLength = 512

    @JvmStatic
    fun compressImg(image: Bitmap?): ByteArrayOutputStream? {
        return compressImg(image, defaultLength)
    }

    @JvmStatic
    fun compressImg(bitmap: Bitmap?, length: Int): ByteArrayOutputStream? {
        var bitmap = bitmap!!
        bitmap = compressImgBySize(bitmap)!!
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
        var options = 100
        while (byteArrayOutputStream.toByteArray().size / 1024 > length) {
            byteArrayOutputStream.reset()
            options -= 10
            bitmap.compress(Bitmap.CompressFormat.JPEG, options, byteArrayOutputStream)
        }
        e("length", byteArrayOutputStream.toByteArray().size.toString())
        return byteArrayOutputStream
    }

    @JvmStatic
    fun compressImgBySize(bitmap: Bitmap?): Bitmap? {
        var bitmap = bitmap!!
        var size = 1f
        val width = bitmap.width
        val height = bitmap.height
        val matrix = Matrix()
        if (width > 720) {
            size = 720f / width
            e("w", bitmap.width.toString() + "")
            e("h", bitmap.height.toString() + "")
        } else if (height > 1280) {
            size = 1280f / height
            e("w", bitmap.width.toString() + "")
            e("h", bitmap.height.toString() + "")
        }
        matrix.postScale(size, size)
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true)
        return bitmap
    }

    @JvmStatic
    fun scale(context: Context?, mFile: File?): File? {
        val fileMaxSize = 100 * 1024.toLong()
        return scale(context, mFile, fileMaxSize)
    }

    @JvmStatic
    fun scale(context: Context?, mFile: File?, fileMaxSize: Long): File? {
        val fileSize = mFile!!.length()
        var scaleSize = 1f
        return if (fileSize >= fileMaxSize) {
            try {
                val options = BitmapFactory.Options()
                options.inJustDecodeBounds = true
                var bitmap: Bitmap?
                val width = options.outWidth
                val height = options.outHeight
                if (width > 720) {
                    scaleSize = width / 480.toFloat()
                } else if (height > 1280) {
                    scaleSize = width / 1080.toFloat()
                }
                options.inJustDecodeBounds = false
                options.inSampleSize = (scaleSize + 0.5).toInt()
                bitmap = BitmapFactory.decodeFile(mFile.path, options)
                bitmap = compressImgBySize(bitmap)
                val fTemp = File(
                    context!!.applicationContext.externalCacheDir,
                    System.currentTimeMillis().toString() + "img.jpg"
                )
                val fileOutputStream: FileOutputStream
                fileOutputStream = try {
                    FileOutputStream(fTemp)
                } catch (e: FileNotFoundException) {
                    e.printStackTrace()
                    return mFile
                }
                bitmap!!.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream)
                fileOutputStream.flush()
                fileOutputStream.close()
                bitmap.recycle()
                fTemp
            } catch (e: IOException) {
                e.printStackTrace()
                mFile
            }
        } else {
            mFile
        }
    }

    @JvmStatic
    fun scale(context: Context?, bitmap: Bitmap?): Bitmap? {
        var bitmap = bitmap!!
        val fileSize: Long = getBitmapSize(bitmap).toLong()
        var scaleSize = 1f
        val fileMaxSize = 100 * 1024.toLong()
        return if (fileSize >= fileMaxSize) {
            try {
                val options = BitmapFactory.Options()
                options.inJustDecodeBounds = true
                val width = options.outWidth
                val height = options.outHeight
                if (width > 720) {
                    scaleSize = width / 480.toFloat()
                } else if (height > 1280) {
                    scaleSize = width / 1080.toFloat()
                }
                options.inJustDecodeBounds = false
                options.inSampleSize = (scaleSize + 0.5).toInt()
                bitmap = compressImgBySize(bitmap)!!
                val file = File(
                    context!!.applicationContext.externalCacheDir,
                    System.currentTimeMillis().toString() + "img.jpg"
                )
                val fileOutputStream: FileOutputStream
                fileOutputStream = try {
                    FileOutputStream(file)
                } catch (e: FileNotFoundException) {
                    e.printStackTrace()
                    return bitmap
                }
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream)
                fileOutputStream.flush()
                fileOutputStream.close()
                bitmap
            } catch (e: IOException) {
                bitmap
            }
        } else {
            bitmap
        }
    }

    @JvmStatic
    fun getBitmapSize(bitmap: Bitmap?): Int {
        return bitmap!!.allocationByteCount
    }

    @Throws(IOException::class)
    fun getBytes(inputStream: InputStream): ByteArray? {
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
    fun degreeImage(context: Context, mFile: File): File? {
        val degree = readImageDegree(mFile.path)
        var bitmap: Bitmap
        return if (degree != 0) {
            //旋转图片
            val matrix = Matrix()
            matrix.postRotate(degree.toFloat())
            bitmap = BitmapFactory.decodeFile(mFile.path)
            bitmap = Bitmap.createBitmap(
                bitmap,
                0,
                0,
                bitmap.width,
                bitmap.height,
                matrix,
                true
            )
            val fTemp = File(
                context.applicationContext.externalCacheDir,
                System.currentTimeMillis().toString() + "img.jpg"
            )
            val fileOutputStream: FileOutputStream
            try {
                fileOutputStream = FileOutputStream(fTemp)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream)
                fileOutputStream.flush()
                fileOutputStream.close()
                bitmap.recycle()
                fTemp
            } catch (e: IOException) {
                e.printStackTrace()
                mFile
            }
        } else {
            mFile
        }
    }

    //读取图片的方向
    private fun readImageDegree(path: String): Int {
        var degree = 0
        // 读取图片文件信息的类ExifInterface
        var exifInterface: ExifInterface? = null
        try {
            exifInterface = ExifInterface(path)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        if (exifInterface != null) {
            val orientation = exifInterface.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> degree = 90
                ExifInterface.ORIENTATION_ROTATE_180 -> degree = 180
                ExifInterface.ORIENTATION_ROTATE_270 -> degree = 270
            }
        }
        return degree
    }

}