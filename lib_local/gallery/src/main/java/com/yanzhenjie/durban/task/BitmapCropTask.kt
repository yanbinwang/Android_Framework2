package com.yanzhenjie.durban.task

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat
import android.graphics.Matrix
import android.graphics.RectF
import android.media.ExifInterface
import android.os.AsyncTask
import com.example.framework.utils.function.value.orFalse
import com.example.framework.utils.function.value.orZero
import com.yanzhenjie.durban.callback.BitmapCropCallback
import com.yanzhenjie.durban.error.StorageError
import com.yanzhenjie.durban.model.CropParameters
import com.yanzhenjie.durban.model.ImageState
import com.yanzhenjie.durban.util.FileUtils
import com.yanzhenjie.durban.util.ImageHeaderParser
import com.yanzhenjie.loading.dialog.LoadingDialog
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class BitmapCropTask(
    context: Context,
    viewBitmap: Bitmap,
    imageState: ImageState,
    cropParameters: CropParameters,
    cropCallback: BitmapCropCallback
) : AsyncTask<Void, Void, BitmapCropTask.PathWorkerResult>() {
    private var mViewBitmap = viewBitmap
    private var mCurrentScale: Float? = null
    private var mCurrentAngle: Float? = null
    private var mCroppedImageWidth: Int? = null
    private var mCroppedImageHeight: Int? = null
    private var mDialog: LoadingDialog? = null
    private var mCompressQuality: Int? = null
    private var mMaxResultImageSizeX: Int? = null
    private var mMaxResultImageSizeY: Int? = null
    private var mCropRect: RectF? = null
    private var mCurrentImageRect: RectF? = null
    private var mInputImagePath: String? = null
    private var mOutputDirectory: String? = null
    private var mCompressFormat: CompressFormat? = null
    private var mCallback: BitmapCropCallback? = cropCallback

    class PathWorkerResult(val path: String?, val exception: Exception?)

    init {
        mDialog = LoadingDialog(context)
        mCropRect = imageState.mCropRect
        mCurrentImageRect = imageState.mCurrentImageRect
        mCurrentScale = imageState.mCurrentScale
        mCurrentAngle = imageState.mCurrentAngle
        mMaxResultImageSizeX = cropParameters.mMaxResultImageSizeX
        mMaxResultImageSizeY = cropParameters.mMaxResultImageSizeY
        mCompressFormat = cropParameters.mCompressFormat
        mCompressQuality = cropParameters.mCompressQuality
        mInputImagePath = cropParameters.mImagePath
        mOutputDirectory = cropParameters.mImageOutputPath
    }

    override fun onPreExecute() {
        if (!mDialog?.isShowing.orFalse) mDialog?.show()
    }

    override fun onPostExecute(result: PathWorkerResult?) {
        if (mDialog?.isShowing.orFalse) mDialog?.dismiss()
        if (mCallback != null) {
            if (result?.exception == null) {
                mCallback?.onBitmapCropped(
                    result?.path.orEmpty(), mCroppedImageWidth.orZero,
                    mCroppedImageHeight.orZero
                )
            } else {
                mCallback?.onCropFailure(result.exception)
            }
        }
    }

    override fun doInBackground(vararg params: Void?): PathWorkerResult {
        try {
            val imagePath: String = crop()
            return PathWorkerResult(imagePath, null)
        } catch (e: Exception) {
            return PathWorkerResult(null, e)
        }
    }

    @Throws(Exception::class)
    private fun crop(): String {
        FileUtils.validateDirectory(mOutputDirectory)
        val fileName = FileUtils.randomImageName(mCompressFormat)
        val outputImagePath = File(mOutputDirectory, fileName).absolutePath
        // Downsize if needed
        if (mMaxResultImageSizeX.orZero > 0 && mMaxResultImageSizeY.orZero > 0) {
            val cropWidth = mCropRect?.width().orZero / mCurrentScale.orZero
            val cropHeight = mCropRect?.height().orZero / mCurrentScale.orZero
            if (cropWidth > mMaxResultImageSizeX.orZero || cropHeight > mMaxResultImageSizeY.orZero) {
                val scaleX = mMaxResultImageSizeX.orZero / cropWidth
                val scaleY = mMaxResultImageSizeY.orZero / cropHeight
                val resizeScale = min(scaleX.toDouble(), scaleY.toDouble()).toFloat()
                val resizedBitmap = Bitmap.createScaledBitmap(
                    mViewBitmap, Math.round(
                        mViewBitmap.width * resizeScale
                    ), Math.round(mViewBitmap.height * resizeScale), false
                )
                if (mViewBitmap != resizedBitmap) mViewBitmap.recycle()
                mViewBitmap = resizedBitmap
                mCurrentScale = mCurrentScale.orZero / resizeScale
            }
        }
        // Rotate if needed
        if (mCurrentAngle != 0f) {
            val tempMatrix = Matrix()
            tempMatrix.setRotate(
                mCurrentAngle.orZero,
                (mViewBitmap.width / 2).toFloat(),
                (mViewBitmap.height / 2).toFloat()
            )
            val rotatedBitmap = Bitmap.createBitmap(
                mViewBitmap,
                0,
                0,
                mViewBitmap.width,
                mViewBitmap.height,
                tempMatrix,
                true
            )
            if (mViewBitmap != rotatedBitmap) mViewBitmap.recycle()
            mViewBitmap = rotatedBitmap
        }
        val cropOffsetX =
            Math.round((mCropRect?.left.orZero - mCurrentImageRect?.left.orZero) / mCurrentScale.orZero)
        val cropOffsetY = Math.round((mCropRect?.top.orZero - mCurrentImageRect?.top.orZero) / mCurrentScale.orZero)
        mCroppedImageWidth = Math.round(mCropRect?.width().orZero / mCurrentScale.orZero)
        mCroppedImageHeight = Math.round(mCropRect?.height().orZero / mCurrentScale.orZero)
        val shouldCrop = shouldCrop(mCroppedImageWidth, mCroppedImageHeight)
        if (shouldCrop) {
            val croppedBitmap = Bitmap.createBitmap(
                mViewBitmap, cropOffsetX, cropOffsetY,
                mCroppedImageWidth.orZero,
                mCroppedImageHeight.orZero
            )
            var outputStream: OutputStream? = null
            try {
                outputStream = FileOutputStream(outputImagePath)
                croppedBitmap.compress(mCompressFormat, mCompressQuality, outputStream)
            } catch (e: Exception) {
                throw StorageError("")
            } finally {
                croppedBitmap.recycle()
                FileUtils.close(outputStream)
            }
            if (mCompressFormat == CompressFormat.JPEG) {
                val originalExif = ExifInterface(mInputImagePath)
                ImageHeaderParser.copyExif(
                    originalExif,
                    mCroppedImageWidth, mCroppedImageHeight, outputImagePath
                )
            }
        } else {
            FileUtils.copyFile(mInputImagePath, outputImagePath)
        }
        if (mViewBitmap != null && !mViewBitmap.isRecycled) mViewBitmap.recycle()
        return outputImagePath
    }

    /**
     * Check whether an image should be cropped at all or just file can be copied to the destination path.
     * For each 1000 pixels there is one pixel of error due to matrix calculations etc.
     *
     * @param width  - crop area width
     * @param height - crop area height
     * @return - true if image must be cropped, false - if original image fits requirements
     */
    private fun shouldCrop(width: Int?, height: Int?): Boolean {
        var pixelError = 1
        (pixelError += Math.round(max(width.toDouble(), height.toDouble()) / 1000f)).toInt()
        return (mMaxResultImageSizeX > 0 && mMaxResultImageSizeY > 0) || (abs((mCropRect.left - mCurrentImageRect.left).toDouble()) > pixelError) || (abs(
            (mCropRect.top - mCurrentImageRect.top).toDouble()
        ) > pixelError) || (abs((mCropRect.bottom - mCurrentImageRect.bottom).toDouble()) > pixelError) || (abs(
            (mCropRect.right - mCurrentImageRect.right).toDouble()
        ) > pixelError)
    }
}