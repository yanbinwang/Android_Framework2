package com.example.thirdparty.compressor

import android.graphics.Bitmap
import com.example.common.BaseApplication
import com.example.common.utils.builder.shortToast
import com.example.common.utils.file.mb
import com.example.common.utils.function.string
import com.example.thirdparty.R
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.format
import id.zelory.compressor.constraint.quality
import id.zelory.compressor.constraint.resolution
import java.io.File

/**
 * 图片压缩库
 */
object CompressUtil {
    private var resWidth = 1000
    private var resHeight = 1000

    @JvmStatic
    suspend fun compressFile(filePath: String?, megabyte: Long = 10): File? {
        return compressFile(File(filePath.orEmpty()), megabyte)
    }

    @JvmStatic
    suspend fun compressFile(file: File?, megabyte: Long = 10): File? {
        return try {
            file ?: throw Exception()
            if (file.length() > megabyte.mb) {
                string(R.string.albumImageError, megabyte.mb.toString()).shortToast()
                return null
            }
            Compressor.compress(BaseApplication.instance, file) {
                resolution(resWidth, resHeight)
                quality(80)
                format(Bitmap.CompressFormat.JPEG)
            }
        } catch (e: Exception) {
//            CrashlyticsUtil.postError("compressFile", e)
            null
        }
    }

}