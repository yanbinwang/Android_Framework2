package com.example.thirdparty.share.wechat

import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat
import android.graphics.BitmapFactory
import com.example.framework.utils.function.value.orFalse
import com.example.framework.utils.function.value.orZero
import com.example.framework.utils.function.value.toSafeInt
import com.example.framework.utils.logD
import com.example.framework.utils.logE
import com.example.framework.utils.logI
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.RandomAccessFile
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL

/**
 * 微信工具类
 */
object WXShareUtil {
    private const val TAG = "SDK_Sample.Util"
    private const val MAX_DECODE_PICTURE_SIZE = 1920 * 1440

    @JvmStatic
    fun bmpToByteArray(bmp: Bitmap?, needRecycle: Boolean?): ByteArray {
//        val output = ByteArrayOutputStream()
//        bmp?.compress(CompressFormat.PNG, 100, output)
//        if (needRecycle.orFalse) {
//            bmp?.recycle()
//        }
//        val result = output.toByteArray()
//        try {
//            output.close()
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//        return result
        return ByteArrayOutputStream().use { output ->
            bmp?.compress(CompressFormat.PNG, 100, output)
            if (needRecycle.orFalse) {
                bmp?.recycle()
            }
            output.toByteArray()
        }
    }

    @JvmStatic
    fun getHtmlByteArray(url: String?): ByteArray? {
//        val htmlUrl: URL?
//        var inStream: InputStream? = null
//        try {
//            htmlUrl = URL(url)
//            val connection = htmlUrl.openConnection()
//            val httpConnection = connection as? HttpURLConnection
//            val responseCode = httpConnection?.responseCode
//            if (responseCode == HttpURLConnection.HTTP_OK) {
//                inStream = httpConnection.inputStream
//            }
//        } catch (e: MalformedURLException) {
//            e.printStackTrace()
//        } catch (e: IOException) {
//            e.printStackTrace()
//        }
//        val data = inputStreamToByte(inStream)
//        return data
        var inStream: InputStream? = null
        try {
            val htmlUrl = URL(url)
            val connection = htmlUrl.openConnection()
            val httpConnection = connection as? HttpURLConnection
            val responseCode = httpConnection?.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                inStream = httpConnection.inputStream
            }
        } catch (e: MalformedURLException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return inputStreamToByte(inStream)
    }

    @JvmStatic
    fun inputStreamToByte(stream: InputStream?): ByteArray? {
        var imgdata: ByteArray? = null
        var bytestream: ByteArrayOutputStream? = null
        try {
            bytestream = ByteArrayOutputStream()
            var ch: Int
            while ((stream?.read().also { ch = it.orZero }) != -1) {
                bytestream.write(ch)
            }
            imgdata = bytestream.toByteArray()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                stream?.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            try {
                bytestream?.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return imgdata
    }

    @JvmStatic
    fun readFromFile(fileName: String?, offset: Int, len: Int): ByteArray? {
        var mLen = len
        if (fileName == null) {
            return null
        }
        val file = File(fileName)
        if (!file.exists()) {
            "readFromFile: file not found".logI(TAG)
            return null
        }
        if (mLen == -1) {
            mLen = file.length().toSafeInt()
        }
        "readFromFile : offset = $offset len = $mLen offset + len = ${(offset.orZero + mLen)}".logD(
            TAG
        )
        if (offset < 0) {
            "readFromFile invalid offset:$offset".logE(TAG)
            return null
        }
        if (mLen <= 0) {
            "readFromFile invalid len:$mLen".logE(TAG)
            return null
        }
        if (offset.orZero + mLen > file.length().toSafeInt()) {
            "readFromFile invalid file len:${file.length()}".logE(TAG)
            return null
        }
        var b: ByteArray? = null
        var access: RandomAccessFile? = null
        try {
            access = RandomAccessFile(fileName, "r")
            b = ByteArray(mLen)
            access.seek(offset.toLong())
            access.readFully(b)
        } catch (e: Exception) {
            "readFromFile : errMsg = ${e.message}".logE(TAG)
            e.printStackTrace()
        } finally {
            try {
                access?.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return b
    }

    @JvmStatic
    fun extractThumbNail(path: String?, height: Int, width: Int, crop: Boolean): Bitmap? {
        if (path != null && path != "" && height > 0 && width > 0) {
            val options = BitmapFactory.Options()
            try {
                options.inJustDecodeBounds = true
                BitmapFactory.decodeFile(path, options)?.recycle()
                "extractThumbNail: round=${width}x${height}, crop=${crop}".logD(TAG)
                val beY = options.outHeight * 1.0 / height
                val beX = options.outWidth * 1.0 / width
                "extractThumbNail: extract beX = $beX, beY = $beY".logD(TAG)
                options.inSampleSize = (if (crop) (if (beY > beX) beX else beY) else (if (beY < beX) beX else beY)).toSafeInt()
                if (options.inSampleSize <= 1) {
                    options.inSampleSize = 1
                }
                // NOTE: out of memory error
                while (options.outHeight * options.outWidth / options.inSampleSize > MAX_DECODE_PICTURE_SIZE) {
                    options.inSampleSize++
                }
                var newHeight = height
                var newWidth = width
                if (crop) {
                    if (beY > beX) {
                        newHeight = (newWidth * 1.0 * options.outHeight / options.outWidth).toSafeInt()
                    } else {
                        newWidth = (newHeight * 1.0 * options.outWidth / options.outHeight).toSafeInt()
                    }
                } else {
                    if (beY < beX) {
                        newHeight = (newWidth * 1.0 * options.outHeight / options.outWidth).toSafeInt()
                    } else {
                        newWidth = (newHeight * 1.0 * options.outWidth / options.outHeight).toSafeInt()
                    }
                }
                options.inJustDecodeBounds = false
                "bitmap required size=${newWidth}x${newHeight}, orig=${options.outWidth}x${options.outHeight}, sample=${options.inSampleSize}".logI(TAG)
                var bm = BitmapFactory.decodeFile(path, options)
                if (bm == null) {
                    "bitmap decode failed".logE(TAG)
                    return null
                }
                "bitmap decoded size=${bm.width}x${bm.height}".logI(TAG)
                val scale = Bitmap.createScaledBitmap(bm, newWidth, newHeight, true)
                bm.recycle()
                bm = scale
                if (crop) {
                    val cropped = Bitmap.createBitmap(bm, (bm.width - width) shr 1, (bm.height - height) shr 1, width, height)
                    bm.recycle()
                    bm = cropped
                    "bitmap croped size=${bm.width}x${bm.height}".logI(TAG)
                }
                return bm
            } catch (e: OutOfMemoryError) {
                "decode bitmap failed: ${e.message}".logE(TAG)
            }
        }
        return null
    }

}