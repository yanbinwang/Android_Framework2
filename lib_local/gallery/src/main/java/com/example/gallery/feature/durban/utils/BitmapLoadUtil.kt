package com.example.gallery.feature.durban.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.Point
import android.media.ExifInterface
import android.opengl.EGL14
import android.opengl.EGLConfig
import android.opengl.GLES20
import android.view.WindowManager
import java.io.FileInputStream
import java.io.IOException
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * 图片加载工具类
 * 功能：缩放计算、EXIF 旋转解析、图片变换、最大尺寸计算
 */
object BitmapLoadUtil {

    /**
     * 对图片执行矩阵变换（旋转、翻转）
     */
    @JvmStatic
    fun transformBitmap(bitmap: Bitmap, transformMatrix: Matrix): Bitmap {
        return try {
            Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), transformMatrix, true)
        } catch (_: OutOfMemoryError) {
            bitmap
        }
    }

    /**
     * 计算图片缩放比例 inSampleSize
     */
    @JvmStatic
    fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            // 图片比控件大 → 就一直 2倍、4倍、8倍、16倍 压缩
            while ((height / inSampleSize) > reqHeight || (width / inSampleSize) > reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    /**
     * 获取图片的 EXIF 方向
     */
    @JvmStatic
    fun getExifOrientation(imagePath: String): Int {
        var orientation = ExifInterface.ORIENTATION_UNDEFINED
        try {
            FileInputStream(imagePath).use { stream ->
                orientation = ImageHeaderParser(stream).getOrientation()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return orientation
    }

    /**
     * EXIF 旋转角度
     */
    @JvmStatic
    fun exifToDegrees(exifOrientation: Int): Int {
        val rotation = when (exifOrientation) {
            ExifInterface.ORIENTATION_ROTATE_90, ExifInterface.ORIENTATION_TRANSPOSE -> 90
            ExifInterface.ORIENTATION_ROTATE_180, ExifInterface.ORIENTATION_FLIP_VERTICAL -> 180
            ExifInterface.ORIENTATION_ROTATE_270, ExifInterface.ORIENTATION_TRANSVERSE -> 270
            else -> 0
        }
        return rotation
    }

    /**
     * EXIF 水平翻转值
     * 是否需要镜像翻转：0 = 翻转 1 = 不翻转
     */
    @JvmStatic
    fun exifToTranslation(exifOrientation: Int): Int {
        val translation = when (exifOrientation) {
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL, ExifInterface.ORIENTATION_FLIP_VERTICAL, ExifInterface.ORIENTATION_TRANSPOSE, ExifInterface.ORIENTATION_TRANSVERSE -> -1
            else -> 1
        }
        return translation
    }

    /**
     * 计算图片最大安全加载尺寸（屏幕对角线 * 2，不超过 GL 最大纹理）
     */
    @JvmStatic
    fun calculateMaxBitmapSize(context: Context): Int {
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = wm.defaultDisplay
        val size = Point()
        display.getSize(size)
        val width = size.x
        val height = size.y
        // 屏幕对角线长度
        var maxBitmapSize = sqrt(width.toDouble().pow(2.0) + height.toDouble().pow(2.0)).toInt()
        // 不超过 OpenGL 最大纹理限制
        val maxTextureSize = getMaxTextureSize()
        if (maxTextureSize > 0) {
            maxBitmapSize = min(maxBitmapSize, maxTextureSize)
        }
        return maxBitmapSize
    }

    /**
     * 获取设备支持的最大纹理尺寸
     */
    private fun getMaxTextureSize(): Int {
        return try {
            // 使用 EGL14 获取最大纹理尺寸
            val dpy = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY)
            val vers = IntArray(2)
            EGL14.eglInitialize(dpy, vers, 0, vers, 1)
            val configAttr = intArrayOf(EGL14.EGL_COLOR_BUFFER_TYPE, EGL14.EGL_RGB_BUFFER, EGL14.EGL_LEVEL, 0, EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT, EGL14.EGL_SURFACE_TYPE, EGL14.EGL_PBUFFER_BIT, EGL14.EGL_NONE)
            val configs = arrayOfNulls<EGLConfig>(1)
            val numConfig = IntArray(1)
            EGL14.eglChooseConfig(dpy, configAttr, 0, configs, 0, 1, numConfig, 0)
            if (numConfig[0] == 0) return 0
            val config = configs[0]
            val surfAttr = intArrayOf(EGL14.EGL_WIDTH, 64, EGL14.EGL_HEIGHT, 64, EGL14.EGL_NONE)
            val surf = EGL14.eglCreatePbufferSurface(dpy, config, surfAttr, 0)
            val ctxAttribute = intArrayOf(EGL14.EGL_CONTEXT_CLIENT_VERSION, 2, EGL14.EGL_NONE)
            val ctx = EGL14.eglCreateContext(dpy, config, EGL14.EGL_NO_CONTEXT, ctxAttribute, 0)
            EGL14.eglMakeCurrent(dpy, surf, surf, ctx)
            val maxSize = IntArray(1)
            GLES20.glGetIntegerv(GLES20.GL_MAX_TEXTURE_SIZE, maxSize, 0)
            // 释放资源
            EGL14.eglMakeCurrent(dpy, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_CONTEXT)
            EGL14.eglDestroySurface(dpy, surf)
            EGL14.eglDestroyContext(dpy, ctx)
            EGL14.eglTerminate(dpy)
            maxSize[0]
        } catch (e: Exception) {
            e.printStackTrace()
            0
        }
    }

    /**
     * 复制照片的身份证信息
     * 把【老照片的拍摄信息】复制到【新裁剪的照片】里，不让信息丢失
     */
    @JvmStatic
    fun copyExif(originalExif: ExifInterface, width: Int, height: Int, imageOutputPath: String) {
        val attributes = arrayOf(
            ExifInterface.TAG_APERTURE,
            ExifInterface.TAG_DATETIME,
            ExifInterface.TAG_DATETIME_DIGITIZED,
            ExifInterface.TAG_EXPOSURE_TIME,
            ExifInterface.TAG_FLASH,
            ExifInterface.TAG_FOCAL_LENGTH,
            ExifInterface.TAG_GPS_ALTITUDE,
            ExifInterface.TAG_GPS_ALTITUDE_REF,
            ExifInterface.TAG_GPS_DATESTAMP,
            ExifInterface.TAG_GPS_LATITUDE,
            ExifInterface.TAG_GPS_LATITUDE_REF,
            ExifInterface.TAG_GPS_LONGITUDE,
            ExifInterface.TAG_GPS_LONGITUDE_REF,
            ExifInterface.TAG_GPS_PROCESSING_METHOD,
            ExifInterface.TAG_GPS_TIMESTAMP,
            ExifInterface.TAG_ISO,
            ExifInterface.TAG_MAKE,
            ExifInterface.TAG_MODEL,
            ExifInterface.TAG_SUBSEC_TIME,
            ExifInterface.TAG_SUBSEC_TIME_DIG,
            ExifInterface.TAG_SUBSEC_TIME_ORIG,
            ExifInterface.TAG_WHITE_BALANCE
        )
        try {
            val newExif = ExifInterface(imageOutputPath)
            var value: String?
            for (attribute in attributes) {
                value = originalExif.getAttribute(attribute)
                if (!value.isNullOrEmpty()) {
                    newExif.setAttribute(attribute, value)
                }
            }
            newExif.setAttribute(ExifInterface.TAG_IMAGE_WIDTH, width.toString())
            newExif.setAttribute(ExifInterface.TAG_IMAGE_LENGTH, height.toString())
            newExif.setAttribute(ExifInterface.TAG_ORIENTATION, "0")
            newExif.saveAttributes()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

}