package com.example.thirdparty.utils

import android.graphics.Bitmap
import com.example.common.BaseApplication
import com.example.common.utils.builder.shortToast
import com.example.common.utils.function.mb
import com.example.common.utils.function.string
import com.example.thirdparty.R
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.format
import id.zelory.compressor.constraint.quality
import id.zelory.compressor.constraint.resolution
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import java.io.File

/**
 * @description 图片上传压缩
 * @author yan
 */
object CompressUtil {
    private var resWidth = 1000
    private var resHeight = 1000

    @JvmStatic
    suspend fun compressFile(sourcePath: String?): File? {
        sourcePath ?: throw RuntimeException("文件为空")
        return compressFile(File(sourcePath))
    }

    @JvmStatic
    suspend fun compressFile(file: File?): File? {
        file ?: throw RuntimeException("文件为空")
        return withContext(IO) {
            try {
                if (file.length() > 10.mb) {
                    string(R.string.compressError).shortToast()
                    null
                } else {
                    Compressor.compress(BaseApplication.instance, file) {
                        resolution(resWidth, resHeight)
                        quality(80)
                        format(Bitmap.CompressFormat.JPEG)
                    }
                }
            } catch (e: Exception) {
//                CrashlyticsUtil.recordException("compressFile", e)
                throw e
            }
        }
    }

}