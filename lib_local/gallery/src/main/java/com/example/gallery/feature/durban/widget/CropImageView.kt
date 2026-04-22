package com.example.gallery.feature.durban.widget

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Bitmap.CompressFormat
import android.graphics.Matrix
import android.graphics.RectF
import android.util.AttributeSet
import androidx.annotation.IntRange
import com.example.gallery.R
import com.example.gallery.feature.durban.app.data.DurbanCrop
import com.example.gallery.feature.durban.app.data.DurbanTask
import com.example.gallery.feature.durban.model.CropParameters
import com.example.gallery.feature.durban.model.ImageState
import com.example.gallery.feature.durban.utils.CubicEasing
import com.example.gallery.feature.durban.utils.RectUtil.getCornersFromRect
import com.example.gallery.feature.durban.utils.RectUtil.getRectSidesFromCorners
import com.example.gallery.feature.durban.utils.RectUtil.trapToRect
import java.lang.ref.WeakReference
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * 图片裁剪核心View
 * 功能：缩放、旋转、裁剪、保存、动画、边界控制
 * 父类：TransformImageView（负责基础矩阵变换）
 */
open class CropImageView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : TransformImageView(context, attrs, defStyleAttr) {
    // 输出图片最大宽高
    private var mMaxResultImageSizeX = 0
    private var mMaxResultImageSizeY = 0
    // 目标裁剪比例（1:1 / 4:3 / 16:9）
    private var mTargetAspectRatio = 0f
    // 最大/最小缩放倍数
    private var mMaxScale = 0f
    private var mMinScale = 0f
    // 最大缩放倍数 = 最小缩放 * 此值
    private var mMaxScaleMultiplier = DEFAULT_MAX_SCALE_MULTIPLIER
    // 图片自适应裁剪框动画时间
    private var mImageToWrapCropBoundsAnimDuration = DEFAULT_IMAGE_TO_CROP_BOUNDS_ANIM_DURATION.toLong()
    // 动画任务
    private var mWrapCropBoundsRunnable: Runnable? = null
    private var mZoomImageToPositionRunnable: Runnable? = null
    // 裁剪比例变化监听
    private var mOnCropBoundsChangeListener: OnCropBoundsChangeListener? = null
    // 裁剪区域矩形
    private val mCropRect = RectF()
    // 临时矩阵（计算用，避免频繁创建对象）
    private val mTempMatrix = Matrix()

    companion object {
        // 静态常量
        const val DEFAULT_MAX_BITMAP_SIZE = 0
        const val DEFAULT_IMAGE_TO_CROP_BOUNDS_ANIM_DURATION = 500
        const val SOURCE_IMAGE_ASPECT_RATIO = 0f
        const val DEFAULT_ASPECT_RATIO = SOURCE_IMAGE_ASPECT_RATIO
        const val DEFAULT_MAX_SCALE_MULTIPLIER = 10.0f
    }

    /**
     * 图片加载完成 → 初始化裁剪框位置
     */
    override fun onImageLaidOut() {
        super.onImageLaidOut()
        val drawable = getDrawable() ?: return
        val drawableWidth = drawable.intrinsicWidth.toFloat()
        val drawableHeight = drawable.intrinsicHeight.toFloat()
        if (mTargetAspectRatio == SOURCE_IMAGE_ASPECT_RATIO) {
            mTargetAspectRatio = drawableWidth / drawableHeight
        }
        // 根据比例计算裁剪框位置
        val height = (mThisWidth / mTargetAspectRatio).toInt()
        if (height > mThisHeight) {
            val width = (mThisHeight * mTargetAspectRatio).toInt()
            val halfDiff = (mThisWidth - width) / 2
            mCropRect.set(halfDiff.toFloat(), 0f, (width + halfDiff).toFloat(), mThisHeight.toFloat())
        } else {
            val halfDiff = (mThisHeight - height) / 2
            mCropRect.set(0f, halfDiff.toFloat(), mThisWidth.toFloat(), (height + halfDiff).toFloat())
        }
        // 计算缩放范围
        calculateImageScaleBounds(drawableWidth, drawableHeight)
        // 初始化图片位置
        setupInitialImagePosition(drawableWidth, drawableHeight)
        // 通知外部
        mOnCropBoundsChangeListener?.onCropAspectRatioChanged(mTargetAspectRatio)
        mTransformImageListener?.onScale(getCurrentScale())
        mTransformImageListener?.onRotate(getCurrentAngle())
    }

    /**
     * 安全缩放（不超过最大/最小范围）
     */
    override fun postScale(deltaScale: Float, px: Float, py: Float) {
        if (deltaScale > 1 && getCurrentScale() * deltaScale <= getMaxScale()) {
            super.postScale(deltaScale, px, py)
        } else if (deltaScale < 1 && getCurrentScale() * deltaScale >= getMinScale()) {
            super.postScale(deltaScale, px, py)
        }
    }

    /**
     * 判断图片是否填满裁剪框
     */
    fun isImageWrapCropBounds(): Boolean {
        return isImageWrapCropBounds(mCurrentImageCorners)
    }

    fun isImageWrapCropBounds(imageCorners: FloatArray): Boolean {
        mTempMatrix.reset()
        mTempMatrix.setRotate(-getCurrentAngle())
        val unRotatedImageCorners = imageCorners.copyOf(imageCorners.size)
        mTempMatrix.mapPoints(unRotatedImageCorners)
        val unRotatedCropBoundsCorners = getCornersFromRect(mCropRect)
        mTempMatrix.mapPoints(unRotatedCropBoundsCorners)
        return trapToRect(unRotatedImageCorners).contains(trapToRect(unRotatedCropBoundsCorners))
    }

    /**
     * 缩放动画
     */
    fun zoomImageToPosition(scale: Float, centerX: Float, centerY: Float, durationMs: Long) {
        var scale = scale
        if (scale > getMaxScale()) {
            scale = getMaxScale()
        }
        val oldScale = getCurrentScale()
        val deltaScale = scale - oldScale
        post(ZoomImageToPosition(WeakReference(this), durationMs, oldScale, deltaScale, centerX, centerY).also { mZoomImageToPositionRunnable = it })
    }

    /**
     * 读取XML自定义属性
     */
    fun processStyledAttributes(a: TypedArray) {
        val targetAspectRatioX = abs(a.getFloat(R.styleable.durban_CropView_durban_aspect_ratio_x, DEFAULT_ASPECT_RATIO))
        val targetAspectRatioY = abs(a.getFloat(R.styleable.durban_CropView_durban_aspect_ratio_y, DEFAULT_ASPECT_RATIO))
        mTargetAspectRatio = if (targetAspectRatioX == SOURCE_IMAGE_ASPECT_RATIO || targetAspectRatioY == SOURCE_IMAGE_ASPECT_RATIO) {
            SOURCE_IMAGE_ASPECT_RATIO
        } else {
            targetAspectRatioX / targetAspectRatioY
        }
    }

    /**
     * 裁剪并保存图片
     */
    fun cropAndSaveImage(compressFormat: CompressFormat, compressQuality: Int, task: DurbanTask, listener: DurbanTask.BitmapCropCallback) {
        // 取消所有动画
        cancelAllAnimations()
        // 让图片适应裁剪框
        setImageToWrapCropBounds(false)
        // 封装当前图片状态
        val imageState = ImageState(
            mCropRect,
            trapToRect(mCurrentImageCorners),
            getCurrentScale(),
            getCurrentAngle()
        )
        // 封装裁剪参数
        val cropParameters = CropParameters(
            mMaxResultImageSizeX,
            mMaxResultImageSizeY,
            compressFormat,
            compressQuality,
            getImagePath(),
            getOutputDirectory(),
            getExifInfo()
        )
        // 异步裁剪并保存
        val bitmap = getViewBitmap()
        if (null != bitmap) {
            val cropData = DurbanCrop(bitmap, imageState, cropParameters)
            task.cropExecute(cropData, listener)
        } else {
            listener.onFailure(AssertionError("图片保存失败"))
        }
    }

    /**
     * 获取缩放范围
     */
    fun getMaxScale(): Float {
        return mMaxScale
    }

    fun getMinScale(): Float {
        return mMinScale
    }

    fun getTargetAspectRatio(): Float {
        return mTargetAspectRatio
    }

    /**
     * 设置裁剪区域
     */
    fun setCropRect(cropRect: RectF) {
        // 计算比例
        mTargetAspectRatio = cropRect.width() / cropRect.height()
        // 设置裁剪区域（减去内边距）
        mCropRect.set(cropRect.left - getPaddingLeft(), cropRect.top - paddingTop, cropRect.right - getPaddingRight(), cropRect.bottom - paddingBottom)
        // 重新计算缩放范围
        calculateImageScaleBounds()
        // 让图片适应新裁剪框
        setImageToWrapCropBounds()
    }

    /**
     * 设置裁剪比例
     */
    fun setTargetAspectRatio(targetAspectRatio: Float) {
        val drawable = getDrawable()
        if (drawable == null) {
            mTargetAspectRatio = targetAspectRatio
            return
        }
        // 使用图片原始比例
        mTargetAspectRatio = if (targetAspectRatio == SOURCE_IMAGE_ASPECT_RATIO) {
            drawable.intrinsicWidth / drawable.intrinsicHeight.toFloat()
        } else {
            targetAspectRatio
        }
        // 通知外部：比例已改变
        mOnCropBoundsChangeListener?.onCropAspectRatioChanged(mTargetAspectRatio)
    }

    /**
     * 设置输出图片最大尺寸
     */
    fun setMaxResultImageSizeX(@IntRange(from = 10) maxResultImageSizeX: Int) {
        mMaxResultImageSizeX = maxResultImageSizeX
    }

    fun setMaxResultImageSizeY(@IntRange(from = 10) maxResultImageSizeY: Int) {
        mMaxResultImageSizeY = maxResultImageSizeY
    }

    /**
     * 动画时间配置
     */
    fun setImageToWrapCropBoundsAnimDuration(@IntRange(from = 100) imageToWrapCropBoundsAnimDuration: Long) {
        if (imageToWrapCropBoundsAnimDuration > 0) {
            mImageToWrapCropBoundsAnimDuration = imageToWrapCropBoundsAnimDuration
        } else {
            throw IllegalArgumentException("Animation duration cannot be negative value.")
        }
    }

    /**
     * 最大放大倍数
     */
    fun setMaxScaleMultiplier(maxScaleMultiplier: Float) {
        mMaxScaleMultiplier = maxScaleMultiplier
    }

    /**
     * 缩放方法
     */
    fun zoomOutImage(deltaScale: Float) {
        zoomOutImage(deltaScale, mCropRect.centerX(), mCropRect.centerY())
    }

    fun zoomOutImage(scale: Float, centerX: Float, centerY: Float) {
        if (scale >= getMinScale()) {
            postScale(scale / getCurrentScale(), centerX, centerY)
        }
    }

    fun zoomInImage(deltaScale: Float) {
        zoomInImage(deltaScale, mCropRect.centerX(), mCropRect.centerY())
    }

    fun zoomInImage(scale: Float, centerX: Float, centerY: Float) {
        if (scale <= getMaxScale()) {
            postScale(scale / getCurrentScale(), centerX, centerY)
        }
    }

    /**
     * 旋转（以裁剪框中心为中心）
     */
    fun postRotate(deltaAngle: Float) {
        postRotate(deltaAngle, mCropRect.centerX(), mCropRect.centerY())
    }

    /**
     * 取消所有动画
     */
    fun cancelAllAnimations() {
        removeCallbacks(mWrapCropBoundsRunnable)
        removeCallbacks(mZoomImageToPositionRunnable)
    }

    /**
     * 让图片填满裁剪框
     */
    fun setImageToWrapCropBounds() {
        setImageToWrapCropBounds(true)
    }

    /**
     * 自动调整图片位置与缩放，使其填满裁剪框
     */
    fun setImageToWrapCropBounds(animate: Boolean) {
        if (mBitmapLaidOut && !isImageWrapCropBounds()) {
            val currentX = mCurrentImageCenter[0]
            val currentY = mCurrentImageCenter[1]
            val currentScale = getCurrentScale()
            var deltaX = mCropRect.centerX() - currentX
            var deltaY = mCropRect.centerY() - currentY
            var deltaScale = 0f
            // 临时计算
            mTempMatrix.reset()
            mTempMatrix.setTranslate(deltaX, deltaY)
            val tempCurrentImageCorners = mCurrentImageCorners.copyOf(mCurrentImageCorners.size)
            mTempMatrix.mapPoints(tempCurrentImageCorners)
            // 是否仅仅移动就可以填满
            val willImageWrapCropBoundsAfterTranslate = isImageWrapCropBounds(tempCurrentImageCorners)
            if (willImageWrapCropBoundsAfterTranslate) {
                val imageIndents = calculateImageIndents()
                deltaX = -(imageIndents[0] + imageIndents[2])
                deltaY = -(imageIndents[1] + imageIndents[3])
            } else {
                // 需要同时移动 + 缩放
                val tempCropRect = RectF(mCropRect)
                mTempMatrix.reset()
                mTempMatrix.setRotate(getCurrentAngle())
                mTempMatrix.mapRect(tempCropRect)
                val currentImageSides = getRectSidesFromCorners(mCurrentImageCorners)
                deltaScale = max(tempCropRect.width() / currentImageSides[0], tempCropRect.height() / currentImageSides[1])
                deltaScale = deltaScale * currentScale - currentScale
            }
            // 执行动画 / 直接移动
            if (animate) {
                post(WrapCropBoundsRunnable(WeakReference(this), mImageToWrapCropBoundsAnimDuration, currentX, currentY, deltaX, deltaY, currentScale, deltaScale, willImageWrapCropBoundsAfterTranslate).also { mWrapCropBoundsRunnable = it })
            } else {
                postTranslate(deltaX, deltaY)
                if (!willImageWrapCropBoundsAfterTranslate) {
                    zoomInImage(currentScale + deltaScale, mCropRect.centerX(), mCropRect.centerY())
                }
            }
        }
    }

    /**
     * 设置/获取裁剪框比例变化监听器
     */
    fun setCropBoundsChangeListener(onCropBoundsChangeListener: OnCropBoundsChangeListener) {
        mOnCropBoundsChangeListener = onCropBoundsChangeListener
    }

    fun getCropBoundsChangeListener(): OnCropBoundsChangeListener? {
        return mOnCropBoundsChangeListener
    }

    /**
     * 计算图片与裁剪框的间距
     */
    private fun calculateImageIndents(): FloatArray {
        mTempMatrix.reset()
        mTempMatrix.setRotate(-getCurrentAngle())
        val unRotatedImageCorners = mCurrentImageCorners.copyOf(mCurrentImageCorners.size)
        val unRotatedCropBoundsCorners = getCornersFromRect(mCropRect)
        mTempMatrix.mapPoints(unRotatedImageCorners)
        mTempMatrix.mapPoints(unRotatedCropBoundsCorners)
        val unRotatedImageRect = trapToRect(unRotatedImageCorners)
        val unRotatedCropRect = trapToRect(unRotatedCropBoundsCorners)
        val deltaLeft = unRotatedImageRect.left - unRotatedCropRect.left
        val deltaTop = unRotatedImageRect.top - unRotatedCropRect.top
        val deltaRight = unRotatedImageRect.right - unRotatedCropRect.right
        val deltaBottom = unRotatedImageRect.bottom - unRotatedCropRect.bottom
        val indents = FloatArray(4)
        indents[0] = if (deltaLeft > 0) deltaLeft else 0f
        indents[1] = if (deltaTop > 0) deltaTop else 0f
        indents[2] = if (deltaRight < 0) deltaRight else 0f
        indents[3] = if (deltaBottom < 0) deltaBottom else 0f
        mTempMatrix.reset()
        mTempMatrix.setRotate(getCurrentAngle())
        mTempMatrix.mapPoints(indents)
        return indents
    }

    /**
     * 计算最小/最大缩放
     */
    private fun calculateImageScaleBounds() {
        val drawable = getDrawable() ?: return
        calculateImageScaleBounds(drawable.intrinsicWidth.toFloat(), drawable.intrinsicHeight.toFloat())
    }

    private fun calculateImageScaleBounds(drawableWidth: Float, drawableHeight: Float) {
        val widthScale = min(mCropRect.width() / drawableWidth, mCropRect.width() / drawableHeight)
        val heightScale = min(mCropRect.height() / drawableHeight, mCropRect.height() / drawableWidth)
        mMinScale = min(widthScale, heightScale)
        mMaxScale = mMinScale * mMaxScaleMultiplier
    }

    /**
     * 初始化图片居中
     */
    private fun setupInitialImagePosition(drawableWidth: Float, drawableHeight: Float) {
        val cropRectWidth = mCropRect.width()
        val cropRectHeight = mCropRect.height()
        val widthScale = mCropRect.width() / drawableWidth
        val heightScale = mCropRect.height() / drawableHeight
        val initialMinScale = max(widthScale, heightScale)
        val tw = (cropRectWidth - drawableWidth * initialMinScale) / 2.0f + mCropRect.left
        val th = (cropRectHeight - drawableHeight * initialMinScale) / 2.0f + mCropRect.top
        mCurrentImageMatrix.reset()
        mCurrentImageMatrix.postScale(initialMinScale, initialMinScale)
        mCurrentImageMatrix.postTranslate(tw, th)
        setImageMatrix(mCurrentImageMatrix)
    }

    /**
     * 动画：自动填满裁剪框
     */
    private class WrapCropBoundsRunnable(
        private val cropImageView: WeakReference<CropImageView>,
        private val durationMs: Long,
        private val oldX: Float,
        private val oldY: Float,
        private val centerDiffX: Float,
        private val centerDiffY: Float,
        private val oldScale: Float,
        private val deltaScale: Float,
        private val willBeImageInBoundsAfterTranslate: Boolean
    ) : Runnable {
        private val startTime = System.currentTimeMillis()

        override fun run() {
            val cropImageView = cropImageView.get() ?: return
            val now = System.currentTimeMillis()
            val currentMs = min(durationMs, now - startTime).toFloat()
            val newX = CubicEasing.easeOut(currentMs, 0f, centerDiffX, durationMs.toFloat())
            val newY = CubicEasing.easeOut(currentMs, 0f, centerDiffY, durationMs.toFloat())
            val newScale = CubicEasing.easeInOut(currentMs, 0f, deltaScale, durationMs.toFloat())
            if (currentMs < durationMs) {
                cropImageView.postTranslate(
                    newX - (cropImageView.mCurrentImageCenter[0] - oldX),
                    newY - (cropImageView.mCurrentImageCenter[1] - oldY)
                )
                if (!willBeImageInBoundsAfterTranslate) {
                    cropImageView.zoomInImage(
                        oldScale + newScale,
                        cropImageView.mCropRect.centerX(),
                        cropImageView.mCropRect.centerY()
                    )
                }
                if (!cropImageView.isImageWrapCropBounds()) {
                    cropImageView.post(this)
                }
            }
        }

    }

    /**
     * 动画：缩放图片
     */
    private class ZoomImageToPosition(
        private val cropImageView: WeakReference<CropImageView>,
        private val durationMs: Long,
        private val oldScale: Float,
        private val deltaScale: Float,
        private val destX: Float,
        private val destY: Float
    ) : Runnable {
        private val startTime = System.currentTimeMillis()

        override fun run() {
            val cropImageView = cropImageView.get() ?: return
            val now = System.currentTimeMillis()
            val currentMs = min(durationMs, now - startTime).toFloat()
            val newScale = CubicEasing.easeInOut(currentMs, 0f, deltaScale, durationMs.toFloat())
            if (currentMs < durationMs) {
                cropImageView.zoomInImage(oldScale + newScale, destX, destY)
                cropImageView.post(this)
            } else {
                cropImageView.setImageToWrapCropBounds()
            }
        }
    }

    /**
     * 裁剪框比例变化监听器
     * 作用：监听裁剪框的宽高比发生改变时回调
     */
    interface OnCropBoundsChangeListener {
        /**
         * 裁剪框宽高比已改变
         * @param cropRatio 新的宽高比 = 宽度 / 高度
         */
        fun onCropAspectRatioChanged(cropRatio: Float)
    }

}