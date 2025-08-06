package com.yanzhenjie.durban.view

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import com.example.framework.utils.logW
import com.yanzhenjie.durban.callback.BitmapLoadCallback
import com.yanzhenjie.durban.model.ExifInfo
import com.yanzhenjie.durban.task.BitmapLoadTask
import com.yanzhenjie.durban.util.BitmapLoadUtils
import com.yanzhenjie.durban.util.FastBitmapDrawable
import kotlin.math.atan2
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * <p>
 * This class provides base logic to setup the image, transform it with matrix (move, scale, rotate),
 * and methods to get current matrix state.
 * </p>
 * Update by Yan Zhenjie on 2017/5/23.
 */
open class TransformImageView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : AppCompatImageView(context, attrs, defStyleAttr) {
    protected val mCurrentImageCorners: FloatArray = FloatArray(RECT_CORNER_POINTS_COORDS)
    protected val mCurrentImageCenter: FloatArray = FloatArray(RECT_CENTER_POINT_COORDS)

    private val mMatrixValues = FloatArray(MATRIX_VALUES_COUNT)

    protected var mCurrentImageMatrix = Matrix()
    protected var mThisWidth = 0
    protected var mThisHeight = 0

    protected var mTransformImageListener: TransformImageListener? = null

    private var mInitialImageCorners: FloatArray?=null
    private var mInitialImageCenter: FloatArray?=null

    protected var mBitmapDecoded = false
    protected var mBitmapLaidOut = false

    private var mMaxBitmapSize = 0

    private var mImagePath: String? = null
    private var mOutputDirectory: String? = null
    private var mExifInfo: ExifInfo? = null

    companion object {
        private const val TAG = "TransformImageView"
        private const val RECT_CORNER_POINTS_COORDS = 8
        private const val RECT_CENTER_POINT_COORDS = 2
        private const val MATRIX_VALUES_COUNT = 9
    }

    init {
        setScaleType(ScaleType.MATRIX)
    }

    override fun setScaleType(scaleType: ScaleType?) {
        if (scaleType == ScaleType.MATRIX) {
            super.setScaleType(scaleType)
        } else {
            "Invalid ScaleType. Only ScaleType.MATRIX can be used".logW(TAG)
        }
    }

    override fun setImageBitmap(bitmap: Bitmap?) {
        setImageDrawable(FastBitmapDrawable(bitmap))
    }

    override fun setImageMatrix(matrix: Matrix?) {
        super.setImageMatrix(matrix)
        mCurrentImageMatrix.set(matrix)
        updateCurrentImagePoints()
    }
    getview

    /**
     * Setter for [.mMaxBitmapSize] value.
     * Be sure to call it before [.setImageURI] or other image setters.
     *
     * @param maxBitmapSize - max size for both width and height of exception that will be used in the view.
     */
    fun setMaxBitmapSize(maxBitmapSize: Int) {
        mMaxBitmapSize = maxBitmapSize
    }

    fun getMaxBitmapSize(): Int {
        if (mMaxBitmapSize <= 0) {
            mMaxBitmapSize = BitmapLoadUtils.calculateMaxBitmapSize(context)
        }
        return mMaxBitmapSize
    }

    fun getImagePath(): String? {
        return mImagePath
    }

    fun getOutputDirectory(): String? {
        return mOutputDirectory
    }

    fun getExifInfo(): ExifInfo? {
        return mExifInfo
    }

    fun setOutputDirectory(outputDirectory: String?) {
        this.mOutputDirectory = outputDirectory
    }

    /**
     * This method takes an Uri as a parameter, then calls method to decode it into Bitmap with specified size.
     *
     * @param inputImagePath - image Uri
     * @throws Exception - can throw exception if having problems with decoding Uri or OOM.
     */
    @Throws(Exception::class)
    fun setImagePath(inputImagePath: String) {
        this.mImagePath = inputImagePath
        val maxBitmapSize = getMaxBitmapSize()

        BitmapLoadTask(
            getContext(),
            maxBitmapSize,
            maxBitmapSize,
            object : BitmapLoadCallback {
                override fun onSuccessfully(bitmap: Bitmap, exifInfo: ExifInfo) {
                    mExifInfo = exifInfo
                    mBitmapDecoded = true
                    setImageBitmap(bitmap)
                }

                override fun onFailure() {
                    if (mTransformImageListener != null) {
                        mTransformImageListener!!.onLoadFailure()
                    }
                }
            }
        ).execute(inputImagePath)
    }

    /**
     * @return - current image scale value.
     * [1.0f - for original image, 2.0f - for 200% scaled image, etc.]
     */
    fun getCurrentScale(): Float {
        return getMatrixScale(mCurrentImageMatrix)
    }

    /**
     * This method calculates scale value for given Matrix object.
     */
    fun getMatrixScale(matrix: Matrix): Float {
        return sqrt(
            getMatrixValue(matrix, Matrix.MSCALE_X).toDouble().pow(2.0) + getMatrixValue(
                matrix,
                Matrix.MSKEW_Y
            ).toDouble().pow(2.0)
        ).toFloat()
    }

    /**
     * @return - current image rotation angle.
     */
    fun getCurrentAngle(): Float {
        return getMatrixAngle(mCurrentImageMatrix)
    }

    /**
     * This method calculates rotation angle for given Matrix object.
     */
    fun getMatrixAngle(matrix: Matrix): Float {
        return -(atan2(
            getMatrixValue(matrix, Matrix.MSKEW_X).toDouble(),
            getMatrixValue(matrix, Matrix.MSCALE_X).toDouble()
        ) * (180 / Math.PI)).toFloat()
    }

    fun setTransformImageListener(transformImageListener: TransformImageListener?) {
        mTransformImageListener = transformImageListener
    }

    /**
     * Interface for rotation and scale change notifying.
     */
    interface TransformImageListener {
        fun onLoadComplete()

        fun onLoadFailure()

        fun onRotate(currentAngle: Float)

        fun onScale(currentScale: Float)
    }

}