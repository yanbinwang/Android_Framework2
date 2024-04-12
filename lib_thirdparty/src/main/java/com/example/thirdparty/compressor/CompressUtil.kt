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

    @JvmStatic
    suspend fun compressFile(file: File?, megabyte: Long = 10, resWidth: Int = 1000, resHeight: Int = 1000, quality: Int = 80): File? {
        return try {
            file ?: throw Exception()
            if (file.length() > megabyte.mb) {
                string(R.string.albumImageError, megabyte.mb.toString()).shortToast()
                return null
            }
            Compressor.compress(BaseApplication.instance, file) {
                resolution(resWidth, resHeight)
                quality(quality)
                format(Bitmap.CompressFormat.JPEG)
            }
        } catch (e: Exception) {
//            CrashlyticsUtil.postError("compressFile", e)
            null
        }
    }

}