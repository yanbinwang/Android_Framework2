package com.example.gallery.feature.durban.app.data

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import com.example.gallery.feature.durban.bean.ExifInfo
import com.example.gallery.feature.durban.utils.BitmapLoadUtil
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext

/**
 * 图片加载异步任务
 * 作用：在子线程中按指定大小压缩加载图片，并自动纠正图片旋转方向
 */
class DurbanLoad(private val requiredWidth: Int, private val requiredHeight: Int) {

    /**
     * 加载图片
     * 成功 -> bitmap 加载完成的图片对象 / exifInfo 图片的EXIF信息（旋转角度、宽高、方向等）
     * 失败 -> 文件不存在、图片损坏、内存不足等
     */
    suspend fun load(imagePath: String): Pair<Bitmap, ExifInfo> {
        return withContext(IO) {
            // 只读取图片宽高，不加载到内存
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            BitmapFactory.decodeFile(imagePath, options)
            // 图片无效
            if (options.outWidth == -1 || options.outHeight == -1) throw AssertionError("图片无效")
            // 计算缩放比例
            options.inSampleSize = BitmapLoadUtil.calculateInSampleSize(options, requiredWidth, requiredHeight)
            options.inJustDecodeBounds = false
            var decodeSampledBitmap: Bitmap? = null
            var decodeAttemptSuccess = false
            // 尝试加载图片，失败则增大缩放比例
            while (!decodeAttemptSuccess) {
                try {
                    decodeSampledBitmap = BitmapFactory.decodeFile(imagePath, options)
                    decodeAttemptSuccess = true
                } catch (_: Throwable) {
                    options.inSampleSize *= 2
                }
            }
            decodeSampledBitmap ?: throw AssertionError("图片加载失败")
            // 读取图片EXIF信息（旋转、方向）
            val exifOrientation = BitmapLoadUtil.getExifOrientation(imagePath)
            val exifDegrees = BitmapLoadUtil.exifToDegrees(exifOrientation)
            val exifTranslation = BitmapLoadUtil.exifToTranslation(exifOrientation)
            val exifInfo = ExifInfo(exifOrientation, exifDegrees, exifTranslation)
            // 纠正图片旋转
            val matrix = Matrix()
            if (exifDegrees != 0) {
                matrix.preRotate(exifDegrees.toFloat())
            }
            if (exifTranslation != 1) {
                matrix.postScale(exifTranslation.toFloat(), 1f)
            }
            if (!matrix.isIdentity) {
                BitmapLoadUtil.transformBitmap(decodeSampledBitmap, matrix) to exifInfo
            } else {
                decodeSampledBitmap to exifInfo
            }
        }
    }

}