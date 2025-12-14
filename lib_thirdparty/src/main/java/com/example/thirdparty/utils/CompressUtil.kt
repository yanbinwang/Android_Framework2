package com.example.thirdparty.utils

import android.content.Context
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
    private const val resWidth = 1000
    private const val resHeight = 1000

    @JvmStatic
    suspend fun compressFile(context: Context = BaseApplication.instance.applicationContext, sourcePath: String?): File? {
        sourcePath ?: throw RuntimeException("文件为空")
        return compressFile(context, File(sourcePath))
    }

    @JvmStatic
    suspend fun compressFile(context: Context = BaseApplication.instance.applicationContext, file: File?): File? {
        file ?: throw RuntimeException("文件为空")
        return withContext(IO) {
            try {
                if (file.length() > 10.mb) {
                    string(R.string.compressError).shortToast()
                    null
                } else {
                    Compressor.compress(context, file) {
                        // 限制压缩后图片的最大分辨率 宽度不会超过 resWidth，高度不会超过 resHeight
                        resolution(resWidth, resHeight)
                        // 控制图片压缩时的质量保留程度
                        quality(80)
                        // 指定压缩后图片的输出格式
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