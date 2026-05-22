package com.example.thirdparty.share.wechat

import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat
import android.graphics.BitmapFactory
import androidx.core.graphics.scale
import com.example.common.utils.function.safeRecycle
import com.example.framework.utils.function.value.orFalse
import com.example.framework.utils.function.value.toSafeInt
import com.example.framework.utils.function.value.toSafeLong
import com.example.framework.utils.logWTF
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream
import java.io.RandomAccessFile
import java.net.HttpURLConnection
import java.net.URL

/**
 * 微信分享工具类
 * 核心功能：Bitmap/文件/流 互转、图片缩略图提取、网络资源读取
 */
object WXShareUtil {
    private const val TAG = "WXShareUtil"
    private const val MAX_DECODE_PICTURE_SIZE = 1920 * 1440 // 最大解码图片尺寸

    /**
     * 将 Bitmap 转换为 ByteArray
     * @param bitmap 待转换的位图（可为空）
     * @param needRecycle 是否需要回收原 Bitmap（释放内存）
     * @return 转换后的字节数组，转换失败返回 null
     */
    @JvmStatic
    fun bitmapToByteArray(bitmap: Bitmap?, needRecycle: Boolean?): ByteArray? {
        return try {
            ByteArrayOutputStream().use { outputStream ->
                bitmap?.compress(CompressFormat.PNG, 100, outputStream)
                if (needRecycle.orFalse) {
                    bitmap?.safeRecycle()
                }
                outputStream.toByteArray()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 从网络 URL 读取 HTML 内容为 ByteArray
     * @param url 目标 URL 地址（可为空）
     * @return 读取到的字节数组，读取失败返回 null
     */
    @JvmStatic
    fun readHtmlFromUrl(url: String?): ByteArray? {
        url ?: return null
        var inStream: InputStream? = null
        val httpConnection = try {
            val htmlUrl = URL(url)
            val connection = htmlUrl.openConnection() as? HttpURLConnection
            // 超时配置，避免卡死
            connection?.apply {
                connectTimeout = 5000
                readTimeout = 5000
                requestMethod = "GET"
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
        try {
            if (httpConnection?.responseCode == HttpURLConnection.HTTP_OK) {
                inStream = httpConnection.inputStream
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            // 关闭 HTTP 连接，避免泄漏
            httpConnection?.disconnect()
        }
        return inputStreamToByteArray(inStream)
    }

    /**
     * 将 InputStream 转换为 ByteArray（逐字节读取）
     * @param inputStream 输入流（可为空）
     * @return 转换后的字节数组，转换失败返回 null
     */
    @JvmStatic
    fun inputStreamToByteArray(inputStream: InputStream?): ByteArray? {
        inputStream ?: return null
        return try {
            inputStream.use { stream ->
                ByteArrayOutputStream().use { outputStream ->
                    var currentByte: Int
                    while ((stream.read().also { currentByte = it }) != -1) {
                        outputStream.write(currentByte)
                    }
                    outputStream.toByteArray()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 从指定文件的指定位置读取指定长度的字节数组
     * @param filePath 文件路径（可为空）
     * @param offset 读取起始偏移量（字节）
     * @param length 读取长度（字节），传入 -1 表示读取整个文件
     * @return 读取到的字节数组，读取失败返回 null
     */
    @JvmStatic
    fun readBytesFromFile(filePath: String?, offset: Int, length: Int): ByteArray? {
        filePath ?: return null
        val file = File(filePath)
        if (!file.exists()) {
            "readBytesFromFile: 文件不存在 - $filePath".logWTF(TAG)
            return null
        }
        val fileLength = file.length()
        var readLength = length
        if (readLength == -1) {
            readLength = fileLength.toSafeInt()
        }
        val offsetLong = offset.toSafeLong()
        val readLengthLong = readLength.toSafeLong()
        "readBytesFromFile: offset=$offset, length=$readLength, offset+length=${offsetLong + readLengthLong}".logWTF(TAG)
        when {
            offset < 0 -> {
                "readBytesFromFile: 非法偏移量 - $offset".logWTF(TAG)
                return null
            }
            readLength <= 0 -> {
                "readBytesFromFile: 非法读取长度 - $readLength".logWTF(TAG)
                return null
            }
            offsetLong + readLengthLong > fileLength -> {
                "readBytesFromFile: 读取范围超出文件大小 - 文件大小=$fileLength, 读取范围=${offsetLong + readLengthLong}".logWTF(TAG)
                return null
            }
        }
        return try {
            val resultBytes = ByteArray(readLength)
            RandomAccessFile(file, "r").use { raf ->
                raf.seek(offsetLong)
                raf.readFully(resultBytes)
            }
            resultBytes
        } catch (e: Exception) {
            "readBytesFromFile: 读取失败 - ${e.message}, filePath=$filePath".logWTF(TAG)
            e.printStackTrace()
            null
        }
    }

    /**
     * 提取图片缩略图（支持裁剪/缩放）
     * @param imagePath 图片文件路径（可为空）
     * @param targetHeight 目标高度（px）
     * @param targetWidth 目标宽度（px）
     * @param needCrop 是否裁剪为指定尺寸（true: 裁剪，false: 等比缩放）
     * @return 处理后的缩略图，处理失败返回 null
     */
    @JvmStatic
    fun extractThumbNail(imagePath: String?, targetHeight: Int, targetWidth: Int, needCrop: Boolean): Bitmap? {
        if (imagePath.isNullOrEmpty() || targetHeight <= 0 || targetWidth <= 0) {
            return null
        }
        val options = BitmapFactory.Options()
        return try {
            // 获取图片尺寸（不加载到内存）
            options.inJustDecodeBounds = true
            BitmapFactory.decodeFile(imagePath, options)?.safeRecycle()
            // 计算采样率（避免 OOM）
            val scaleRatioY = options.outHeight * 1.0 / targetHeight
            val scaleRatioX = options.outWidth * 1.0 / targetWidth
            "extractImageThumbnail: 目标尺寸=${targetWidth}x$targetHeight, 原图尺寸=${options.outWidth}x${options.outHeight}, 裁剪=$needCrop".logWTF(TAG)
            "extractImageThumbnail: 缩放比例X=$scaleRatioX, Y=$scaleRatioY".logWTF(TAG)
            // 基础采样率
            options.inSampleSize = when {
                needCrop -> if (scaleRatioY > scaleRatioX) scaleRatioX else scaleRatioY // 裁剪取较小比例
                else -> if (scaleRatioY < scaleRatioX) scaleRatioX else scaleRatioY    // 缩放取较大比例
            }.toSafeInt().coerceAtLeast(1) // 确保采样率 >=1
            // 二次校验采样率（防止解码后尺寸超出最大限制）
            while (options.outHeight * options.outWidth / options.inSampleSize > MAX_DECODE_PICTURE_SIZE) {
                options.inSampleSize++
            }
            // 计算实际缩放尺寸
            var actualHeight = targetHeight
            var actualWidth = targetWidth
            if (needCrop) {
                if (scaleRatioY > scaleRatioX) {
                    actualHeight = (actualWidth * options.outHeight / options.outWidth.toDouble()).toSafeInt()
                } else {
                    actualWidth = (actualHeight * options.outWidth / options.outHeight.toDouble()).toSafeInt()
                }
            } else {
                if (scaleRatioY < scaleRatioX) {
                    actualHeight = (actualWidth * options.outHeight / options.outWidth.toDouble()).toSafeInt()
                } else {
                    actualWidth = (actualHeight * options.outWidth / options.outHeight.toDouble()).toSafeInt()
                }
            }
            // 真正解码图片
            options.inJustDecodeBounds = false
            "extractImageThumbnail: 实际缩放尺寸=${actualWidth}x$actualHeight, 采样率=${options.inSampleSize}".logWTF(TAG)
            var bitmap = BitmapFactory.decodeFile(imagePath, options) ?: run {
                "extractImageThumbnail: 图片解码失败 - $imagePath".logWTF(TAG)
                return null
            }
            "extractImageThumbnail: 解码后尺寸=${bitmap.width}x${bitmap.height}".logWTF(TAG)
            // 缩放图片
            val scaledBitmap = bitmap.scale(actualWidth, actualHeight)
            bitmap.safeRecycle()
            bitmap = scaledBitmap
            // 裁剪图片
            if (needCrop) {
                val cropX = (bitmap.width - targetWidth) shr 1
                val cropY = (bitmap.height - targetHeight) shr 1
                val croppedBitmap = Bitmap.createBitmap(bitmap, cropX, cropY, targetWidth, targetHeight)
                bitmap.safeRecycle()
                bitmap = croppedBitmap
                "extractImageThumbnail: 裁剪后尺寸=${bitmap.width}x${bitmap.height}".logWTF(TAG)
            }
            bitmap
        } catch (e: OutOfMemoryError) {
            "extractImageThumbnail: 解码图片OOM - ${e.message}, path=$imagePath".logWTF(TAG)
            null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

}