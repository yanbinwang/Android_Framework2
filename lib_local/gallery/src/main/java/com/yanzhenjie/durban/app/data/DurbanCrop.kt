package com.yanzhenjie.durban.app.data

import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.RectF
import android.media.ExifInterface
import androidx.core.graphics.scale
import com.yanzhenjie.durban.model.CropParameters
import com.yanzhenjie.durban.model.ImageState
import com.yanzhenjie.durban.utils.BitmapLoadUtil.copyExif
import com.yanzhenjie.durban.utils.FileUtil
import com.yanzhenjie.durban.utils.FileUtil.copyFile
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * 图片裁剪异步任务
 * 作用：在子线程执行 图片缩放 → 旋转 → 裁剪 → 保存 → 回调结果
 */
class DurbanCrop(viewBitmap: Bitmap, imageState: ImageState, cropParameters: CropParameters) {
    // 界面显示的 Bitmap
    private var mViewBitmap = viewBitmap
    // 当前图片缩放比例 & 旋转角度
    private var mCurrentScale = imageState.currentScale
    private val mCurrentAngle = imageState.currentAngle
    // 裁剪框区域 & 图片当前区域
    private val mCropRect = imageState.cropRect ?: RectF()
    private val mCurrentImageRect = imageState.currentImageRect ?: RectF()
    // 输出图片最大宽高限制
    private val mMaxResultImageSizeX = cropParameters.maxResultImageSizeX
    private val mMaxResultImageSizeY = cropParameters.maxResultImageSizeY
    // 压缩质量
    private val mCompressQuality = cropParameters.compressQuality
    // 输入 & 输出路径
    private val mInputImagePath = cropParameters.imagePath.orEmpty()
    private val mOutputDirectory = cropParameters.imageOutputPath.orEmpty()
    // 图片压缩格式
    private val mCompressFormat = cropParameters.compressFormat ?: Bitmap.CompressFormat.JPEG
    // 裁剪后图片宽高
    private var mCroppedImageWidth = 0
    private var mCroppedImageHeight = 0

    /**
     * 裁剪逻辑：缩放 → 旋转 → 裁剪 → 保存
     */
    suspend fun crop(): Triple<String, Int, Int> {
        return withContext(IO) {
            // 检查输出目录是否存在
            FileUtil.validateDirectory(mOutputDirectory)
            // 生成随机文件名
            val fileName = FileUtil.randomImageName(mCompressFormat)
            val outputImagePath = File(mOutputDirectory, fileName).absolutePath
            // 如果需要，先缩小图片
            if (mMaxResultImageSizeX > 0 && mMaxResultImageSizeY > 0) {
                val cropWidth = mCropRect.width() / mCurrentScale
                val cropHeight = mCropRect.height() / mCurrentScale
                if (cropWidth > mMaxResultImageSizeX || cropHeight > mMaxResultImageSizeY) {
                    val scaleX = mMaxResultImageSizeX / cropWidth
                    val scaleY = mMaxResultImageSizeY / cropHeight
                    val resizeScale = min(scaleX, scaleY)
                    val resizedBitmap = mViewBitmap.scale((mViewBitmap.getWidth() * resizeScale).roundToInt(), (mViewBitmap.getHeight() * resizeScale).roundToInt(), false)
                    if (mViewBitmap != resizedBitmap) {
                        mViewBitmap.recycle()
                    }
                    mViewBitmap = resizedBitmap
                    mCurrentScale /= resizeScale
                }
            }
            // 如果需要，旋转图片
            if (mCurrentAngle != 0f) {
                val tempMatrix = Matrix()
                tempMatrix.setRotate(mCurrentAngle, mViewBitmap.getWidth().toFloat() / 2, mViewBitmap.getHeight().toFloat() / 2)
                val rotatedBitmap = Bitmap.createBitmap(mViewBitmap, 0, 0, mViewBitmap.getWidth(), mViewBitmap.getHeight(), tempMatrix, true)
                if (mViewBitmap != rotatedBitmap) {
                    mViewBitmap.recycle()
                }
                mViewBitmap = rotatedBitmap
            }

            // 计算裁剪偏移量 & 最终宽高
            val cropOffsetX = ((mCropRect.left - mCurrentImageRect.left) / mCurrentScale).roundToInt()
            val cropOffsetY = ((mCropRect.top - mCurrentImageRect.top) / mCurrentScale).roundToInt()
            mCroppedImageWidth = (mCropRect.width() / mCurrentScale).roundToInt()
            mCroppedImageHeight = (mCropRect.height() / mCurrentScale).roundToInt()
            // 判断是否需要真正裁剪
            val shouldCrop: Boolean = shouldCrop(mCroppedImageWidth, mCroppedImageHeight)
            if (shouldCrop) {
                // 执行裁剪
                val croppedBitmap = Bitmap.createBitmap(mViewBitmap, cropOffsetX, cropOffsetY, mCroppedImageWidth, mCroppedImageHeight)
                try {
                    FileOutputStream(outputImagePath).use { stream ->
                        croppedBitmap.compress(mCompressFormat, mCompressQuality, stream)
                    }
                } catch (_: Exception) {
                    throw AssertionError("图片保存失败")
                } finally {
                    croppedBitmap.recycle()
                }
                // 如果是JPG，复制EXIF信息
                if (mCompressFormat == Bitmap.CompressFormat.JPEG) {
                    val originalExif = ExifInterface(mInputImagePath)
                    copyExif(originalExif, mCroppedImageWidth, mCroppedImageHeight, outputImagePath)
                }
            } else {
                // 无需裁剪，直接复制文件
                copyFile(mInputImagePath, outputImagePath)
            }
            // 回收图片
            if (!mViewBitmap.isRecycled) {
                mViewBitmap.recycle()
            }
            Triple(outputImagePath, mCroppedImageWidth, mCroppedImageHeight)
        }
    }

    /**
     * 判断是否需要执行裁剪
     * 误差1像素内都认为不需要裁剪，直接拷贝
     */
    private fun shouldCrop(width: Int, height: Int): Boolean {
        var pixelError = 1
        pixelError += (max(width, height) / 1000f).roundToInt()
        return (mMaxResultImageSizeX > 0 && mMaxResultImageSizeY > 0) || abs(mCropRect.left - mCurrentImageRect.left) > pixelError || abs(mCropRect.top - mCurrentImageRect.top) > pixelError || abs(mCropRect.bottom - mCurrentImageRect.bottom) > pixelError || abs(mCropRect.right - mCurrentImageRect.right) > pixelError
    }

}