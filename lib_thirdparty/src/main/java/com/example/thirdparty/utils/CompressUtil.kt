package com.example.thirdparty.utils

import android.content.Context
import android.graphics.Bitmap
import com.example.common.utils.function.mb
import com.example.common.utils.i18n.string
import com.example.thirdparty.R
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.format
import id.zelory.compressor.constraint.quality
import id.zelory.compressor.constraint.resolution
import java.io.File

/**
 * @description 图片上传压缩
 * @author yan
 */
object CompressUtil {
    private const val resWidth = 1000
    private const val resHeight = 1000

    suspend fun compressFile(context: Context, pathname: String?): File {
        pathname ?: throw RuntimeException("文件路径为空")
        return compressFile(context, File(pathname))
    }

    @JvmStatic
    suspend fun compressFile(context: Context, file: File?): File {
        file ?: throw RuntimeException("文件为空")
        if (file.length() > 10.mb) {
            throw RuntimeException(string(R.string.compressError))
        }
        // 内部已切换到了io线程
        return try {
            Compressor.compress(context, file) {
                // 限制压缩后图片的最大分辨率 宽度不会超过 resWidth，高度不会超过 resHeight
                resolution(resWidth, resHeight)
                // 控制图片压缩时的质量保留程度
                quality(80)
                // 指定压缩后图片的输出格式
                format(Bitmap.CompressFormat.JPEG)
            }
        } catch (e: Exception) {
//            CrashlyticsUtil.recordException("compressFile", e)
            throw e
        }
    }

}