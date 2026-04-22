package com.yanzhenjie.durban.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.RectF
import android.util.AttributeSet
import androidx.annotation.IntRange
import androidx.appcompat.widget.AppCompatImageView
import com.yanzhenjie.durban.app.data.BitmapLoadCallback
import com.yanzhenjie.durban.app.data.BitmapLoadTask
import com.yanzhenjie.durban.model.ExifInfo
import com.yanzhenjie.durban.utils.BitmapLoadUtil.calculateMaxBitmapSize
import com.yanzhenjie.durban.utils.RectUtil.getCenterFromRect
import com.yanzhenjie.durban.utils.RectUtil.getCornersFromRect
import kotlin.math.atan2
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * 图片变换【基类】
 * 功能：图片加载、矩阵变换（移动/缩放/旋转）、坐标计算
 * 所有裁剪View的父类
 */
open class TransformImageView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : AppCompatImageView(context, attrs, defStyleAttr) {
    // 图片最大尺寸（防止OOM）
    private var mMaxBitmapSize = 0
    // 图片路径
    private var mImagePath: String? = null
    // 输出目录
    private var mOutputDirectory: String? = null
    // 图片信息（旋转、翻转）
    private var mExifInfo: ExifInfo? = null
    // 加载线程
    private var mBitmapLoadTask: BitmapLoadTask? = null
    // 图片初始状态：四个角坐标、中心点坐标
    private var mInitialImageCorners = floatArrayOf()
    private var mInitialImageCenter = floatArrayOf()
    // 临时存储矩阵数据（避免重复创建对象）
    private val mMatrixValues = FloatArray(MATRIX_VALUES_COUNT)

    // View自身宽高
    protected var mThisWidth = 0
    protected var mThisHeight = 0
    // 状态标记：是否解码完成 / 是否布局完成
    protected var mBitmapDecoded = false
    protected var mBitmapLaidOut = false
    // 图片变换监听（加载、旋转、缩放）
    protected var mTransformImageListener: TransformImageListener? = null
    // 当前图片矩阵（核心：所有变换都存在这里）
    protected var mCurrentImageMatrix = Matrix()
    // 当前图片四个角坐标 / 中心点坐标
    protected val mCurrentImageCorners = FloatArray(RECT_CORNER_POINTS_COORDS)
    protected val mCurrentImageCenter = FloatArray(RECT_CENTER_POINT_COORDS)

    companion object {
        // 常量：坐标点数量
        private const val RECT_CORNER_POINTS_COORDS = 8
        // 中心点 → x,y
        private const val RECT_CENTER_POINT_COORDS = 2
        // 矩阵一共9个值
        private const val MATRIX_VALUES_COUNT = 9
    }

    /**
     * 初始化设置
     */
    init {
        setScaleType(ScaleType.MATRIX)
    }

    /**
     * 强制使用 MATRIX 模式
     * 因为只有矩阵才能自由缩放旋转
     */
    override fun setScaleType(scaleType: ScaleType?) {
        if (scaleType == ScaleType.MATRIX) {
            super.setScaleType(scaleType)
        }
    }

    /**
     * 设置 Bitmap
     */
    override fun setImageBitmap(bm: Bitmap?) {
        setImageDrawable(FastBitmapDrawable(bm))
    }

    /**
     * 设置矩阵，并更新坐标
     */
    override fun setImageMatrix(matrix: Matrix?) {
        super.setImageMatrix(matrix)
        mCurrentImageMatrix.set(matrix)
        // 根据矩阵，实时更新图片四个角和中心点
        mCurrentImageMatrix.mapPoints(mCurrentImageCorners, mInitialImageCorners)
        mCurrentImageMatrix.mapPoints(mCurrentImageCenter, mInitialImageCenter)
    }

    /**
     * View布局完成
     */
    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        if (changed || (mBitmapDecoded && !mBitmapLaidOut)) {
            mThisWidth = (width - getPaddingRight()) - getPaddingLeft()
            mThisHeight = (height - paddingBottom) - paddingTop
            onImageLaidOut()
        }
    }

    /**
     * 销毁
     */
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        mBitmapLoadTask?.cancel(true)
        mBitmapLoadTask = null
    }

    /**
     * 图片布局完成 → 记录初始坐标
     */
    protected open fun onImageLaidOut() {
        val drawable = getDrawable() ?: return
        val w = drawable.intrinsicWidth.toFloat()
        val h = drawable.intrinsicHeight.toFloat()
        val initialImageRect = RectF(0f, 0f, w, h)
        mInitialImageCorners = getCornersFromRect(initialImageRect)
        mInitialImageCenter = getCenterFromRect(initialImageRect)
        mBitmapLaidOut = true
        mTransformImageListener?.onLoadComplete()
    }

    /**
     * 设置/获取 图片最大尺寸
     */
    fun setMaxBitmapSize(maxBitmapSize: Int) {
        mMaxBitmapSize = maxBitmapSize
    }

    fun getMaxBitmapSize(): Int {
        if (mMaxBitmapSize <= 0) {
            mMaxBitmapSize = calculateMaxBitmapSize(getContext())
        }
        return mMaxBitmapSize
    }

    /**
     * 获取 / 加载 图片路径 (异步)
     */
    @Throws(Exception::class)
    fun setImagePath(inputImagePath: String) {
        mImagePath = inputImagePath
        val maxBitmapSize = getMaxBitmapSize()
        mBitmapLoadTask?.cancel(true)
        mBitmapLoadTask = null
        mBitmapLoadTask = BitmapLoadTask(context, maxBitmapSize, maxBitmapSize, object : BitmapLoadCallback {
            override fun onSuccessfully(bitmap: Bitmap, exifInfo: ExifInfo) {
                mExifInfo = exifInfo
                mBitmapDecoded = true
                setImageBitmap(bitmap)
            }

            override fun onFailure() {
                mTransformImageListener?.onLoadFailure()
            }
        })
        mBitmapLoadTask?.execute(inputImagePath)
    }

    /**
     * 获取路径、目录、图片信息
     */
    fun getImagePath(): String? {
        return mImagePath
    }

    fun setOutputDirectory(outputDirectory: String?) {
        mOutputDirectory = outputDirectory
    }

    fun getOutputDirectory(): String? {
        return mOutputDirectory
    }

    fun getExifInfo(): ExifInfo? {
        return mExifInfo
    }

    /**
     * 获取当前缩放倍数
     */
    fun getCurrentScale(): Float {
        return getMatrixScale(mCurrentImageMatrix)
    }

    /**
     * 获取当前旋转角度
     */
    fun getCurrentAngle(): Float {
        return getMatrixAngle(mCurrentImageMatrix)
    }

    /**
     * 公式计算：矩阵 → 缩放值
     */
    fun getMatrixScale(matrix: Matrix): Float {
        return sqrt(getMatrixValue(matrix, Matrix.MSCALE_X).toDouble().pow(2.0) + getMatrixValue(matrix, Matrix.MSKEW_Y).toDouble().pow(2.0)).toFloat()
    }

    /**
     * 公式计算：矩阵 → 角度
     */
    fun getMatrixAngle(matrix: Matrix): Float {
        return -(atan2(getMatrixValue(matrix, Matrix.MSKEW_X).toDouble(), getMatrixValue(matrix, Matrix.MSCALE_X).toDouble()) * (180 / Math.PI)).toFloat()
    }

    /**
     * 获取矩阵中某个值
     */
    private fun getMatrixValue(matrix: Matrix, @IntRange(from = 0, to = MATRIX_VALUES_COUNT.toLong()) valueIndex: Int): Float {
        matrix.getValues(mMatrixValues)
        return mMatrixValues[valueIndex]
    }

    /**
     * 获取当前显示的Bitmap
     */
    fun getViewBitmap(): Bitmap? {
        return if (getDrawable() == null || getDrawable() !is FastBitmapDrawable) {
            null
        } else {
            (getDrawable() as FastBitmapDrawable).getBitmap()
        }
    }

    /**
     * 移动图片
     */
    fun postTranslate(deltaX: Float, deltaY: Float) {
        if (deltaX != 0f || deltaY != 0f) {
            mCurrentImageMatrix.postTranslate(deltaX, deltaY)
            setImageMatrix(mCurrentImageMatrix)
        }
    }

    /**
     * 缩放图片
     */
    open fun postScale(deltaScale: Float, px: Float, py: Float) {
        if (deltaScale != 0f) {
            mCurrentImageMatrix.postScale(deltaScale, deltaScale, px, py)
            setImageMatrix(mCurrentImageMatrix)
            mTransformImageListener?.onScale(getMatrixScale(mCurrentImageMatrix))
        }
    }

    /**
     * 旋转图片
     */
    fun postRotate(deltaAngle: Float, px: Float, py: Float) {
        if (deltaAngle != 0f) {
            mCurrentImageMatrix.postRotate(deltaAngle, px, py)
            setImageMatrix(mCurrentImageMatrix)
            mTransformImageListener?.onRotate(getMatrixAngle(mCurrentImageMatrix))
        }
    }

    /**
     * 设置监听
     */
    fun setTransformImageListener(transformImageListener: TransformImageListener) {
        mTransformImageListener = transformImageListener
    }

    /**
     * 图片状态监听接口
     */
    interface TransformImageListener {
        /**
         * 加载完成
         */
        fun onLoadComplete()

        /**
         * 加载失败
         */
        fun onLoadFailure()

        /**
         * 旋转回调
         */
        fun onRotate(currentAngle: Float)

        /**
         * 缩放回调
         */
        fun onScale(currentScale: Float)
    }

}