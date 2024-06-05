package com.example.thirdparty.compressor

import android.graphics.Bitmap
import com.example.common.BaseApplication
import com.example.common.utils.builder.shortToast
import com.example.common.utils.i18n.string
import com.example.thirdparty.R
import com.example.thirdparty.firebase.CrashlyticsUtil
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.format
import id.zelory.compressor.constraint.quality
import id.zelory.compressor.constraint.resolution
import java.io.File

/**
 * yan
 */
 object CompressUtil {
    private var resWidth = 1000
    private var resHeight = 1000

    suspend fun compressFile(file: File?): File? {
        return try {
            file ?: throw Exception()
            if (file.length() > 10 * 1024 * 1024) {
                string(R.string.compressError).shortToast()
                return null
            }
            Compressor.compress(BaseApplication.instance, file) {
                resolution(resWidth, resHeight)
                quality(80)
                format(Bitmap.CompressFormat.JPEG)
            }
        } catch (e: Exception) {
            CrashlyticsUtil.postError("compressFile", e)
            null
        }
    }

}