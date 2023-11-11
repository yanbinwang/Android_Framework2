package com.example.thirdparty.media.utils

import android.graphics.Bitmap
import com.example.common.BaseApplication
import com.example.common.utils.builder.shortToast
import com.example.common.utils.file.getFileFromUri
import com.example.common.utils.function.string
import com.example.thirdparty.R
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.format
import id.zelory.compressor.constraint.quality
import id.zelory.compressor.constraint.resolution
import java.io.File

/**
 * yan
 */
internal object CompressUtil {
    private var resWidth = 1000
    private var resHeight = 1000

    suspend fun compressFile(uri: String?): File? {
        return try {
            val oriFile = uri.getFileFromUri() ?: throw Exception()
            if (oriFile.length() > 10 * 1024 * 1024) {
                string(R.string.album_error).shortToast()
                return null
            }
            Compressor.compress(BaseApplication.instance, oriFile) {
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