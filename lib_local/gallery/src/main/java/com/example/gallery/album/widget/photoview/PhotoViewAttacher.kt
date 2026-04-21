package com.example.gallery.album.widget.photoview

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.Matrix.ScaleToFit
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.view.View
import android.view.View.OnLongClickListener
import android.view.View.OnTouchListener
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.Interpolator
import android.widget.ImageView
import android.widget.ImageView.ScaleType
import androidx.core.view.MotionEventCompat
import com.example.framework.utils.function.value.orFalse
import com.example.gallery.album.widget.photoview.IPhotoView.Companion.DEFAULT_MAX_SCALE
import com.example.gallery.album.widget.photoview.IPhotoView.Companion.DEFAULT_MID_SCALE
import com.example.gallery.album.widget.photoview.IPhotoView.Companion.DEFAULT_MIN_SCALE
import com.example.gallery.album.widget.photoview.IPhotoView.Companion.DEFAULT_ZOOM_DURATION
import com.example.gallery.album.widget.photoview.gestures.FroyoGestureDetector
import com.example.gallery.album.widget.photoview.gestures.OnGestureListener
import com.example.gallery.album.widget.photoview.gestures.OnScaleDragListener
import com.example.gallery.album.widget.photoview.scrollerproxy.GingerScroller
import com.example.gallery.album.widget.photoview.scrollerproxy.ScrollerProxy
import java.lang.ref.WeakReference
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt

/**
 * PhotoView 核心控制器
 * 所有缩放、拖动、旋转、惯性滑动、双击放大、矩阵计算 都在这里实现
 */
@SuppressLint("ClickableViewAccessibility")
class PhotoViewAttacher : IPhotoView, OnTouchListener, OnGestureListener, OnGlobalLayoutListener {
    // ImageView 显示区域
    private var mIvTop = 0
    private var mIvRight = 0
    private var mIvBottom = 0
    private var mIvLeft = 0
    // 图片滚动边缘状态
    private var mScrollEdge = EDGE_BOTH
    // 基础旋转角度
    private var mBaseRotation = 0f
    // 是否允许缩放
    private var mZoomEnabled = false
    // 滑动边缘父控件拦截配置
    private var mAllowParentInterceptOnEdge = true
    private var mBlockParentIntercept = false
    // 图片显示模式
    private var mScaleType = ScaleType.FIT_CENTER
    // 缩放动画插值器
    private var mInterpolator: Interpolator = AccelerateDecelerateInterpolator()
    // 矩阵对象（复用，避免频繁创建）
    private val mBaseMatrix = Matrix() // 基础矩阵（居中、适应屏幕）
    private val mDrawMatrix = Matrix() // 最终绘制矩阵
    private val mSuppMatrix = Matrix() // 缩放/平移/旋转矩阵
    private val mDisplayRect = RectF() // 图片显示区域
    private val mMatrixValues = FloatArray(9) // 矩阵数值存储
    // 缩放动画时长
    private var ZOOM_DURATION = DEFAULT_ZOOM_DURATION
    // 缩放级别配置
    private var mMinScale = DEFAULT_MIN_SCALE // 最小缩放
    private var mMidScale = DEFAULT_MID_SCALE // 中等缩放（双击第一级）
    private var mMaxScale = DEFAULT_MAX_SCALE // 最大缩放
    // 弱引用持有 ImageView，防止内存泄漏
    private var mImageView: WeakReference<ImageView>? = null
    // 惯性滑动任务
    private var mCurrentFlingRunnable: FlingRunnable? = null
    // 各种监听
    private var mMatrixChangeListener: OnMatrixChangedListener? = null
    private var mPhotoTapListener: OnPhotoTapListener? = null
    private var mViewTapListener: OnViewTapListener? = null
    private var mLongClickListener: OnLongClickListener? = null
    private var mScaleChangeListener: OnScaleChangeListener? = null
    private var mSingleFlingListener: OnSingleFlingListener? = null
    // 手势检测器
    private var mGestureDetector: GestureDetector? = null
    private var mScaleDragDetector: OnScaleDragListener? = null

    companion object {
        // 常量
        private const val SINGLE_TOUCH = 1
        private const val EDGE_NONE = -1
        private const val EDGE_LEFT = 0
        private const val EDGE_RIGHT = 1
        private const val EDGE_BOTH = 2

        /**
         * 检查缩放级别合法性：最小 < 中等 < 最大
         */
        private fun checkZoomLevels(minZoom: Float, midZoom: Float, maxZoom: Float) {
            require(!(minZoom >= midZoom)) { "Minimum zoom has to be less than Medium zoom. Call setMinimumZoom() with a more appropriate value" }
            require(!(midZoom >= maxZoom)) { "Medium zoom has to be less than Maximum zoom. Call setMaximumZoom() with a more appropriate value" }
        }

        /**
         * 判断 ImageView 是否有图片
         */
        private fun hasDrawable(imageView: ImageView?): Boolean {
            return null != imageView && null != imageView.getDrawable()
        }

        /**
         * 判断 ScaleType 是否支持（不支持 MATRIX）
         */
        private fun isSupportedScaleType(scaleType: ScaleType?): Boolean {
            if (null == scaleType) {
                return false
            }
            require(scaleType != ScaleType.MATRIX) { scaleType.name + " is not supported in PhotoView" }
            return true
        }

        /**
         * 强制把 ImageView 的 ScaleType 设为 MATRIX（必须用矩阵才能缩放）
         */
        private fun setImageViewScaleTypeMatrix(imageView: ImageView?) {
            if (null != imageView && imageView !is IPhotoView) {
                if (ScaleType.MATRIX != imageView.scaleType) {
                    imageView.setScaleType(ScaleType.MATRIX)
                }
            }
        }
    }

    /**
     * 构造方法
     * @param imageView 绑定的图片控件
     * @param zoomable  是否允许缩放
     */
    constructor(imageView: ImageView) : this(imageView, true)

    constructor(imageView: ImageView, zoomable: Boolean) {
        mImageView = WeakReference(imageView)
        // 开启绘制缓存
        imageView.setDrawingCacheEnabled(true)
        // 设置触摸监听
        imageView.setOnTouchListener(this)
        // 监听布局完成
        val observer = imageView.getViewTreeObserver()
        if (null != observer) observer.addOnGlobalLayoutListener(this)
        // 强制使用矩阵模式
        setImageViewScaleTypeMatrix(imageView)
        if (imageView.isInEditMode) {
            return
        }
        // 创建手势检测器
        mScaleDragDetector = FroyoGestureDetector(imageView.context)
        mScaleDragDetector?.setOnGestureListener(this)
        // 创建系统手势检测器，处理长按、快速滑动
        mGestureDetector = GestureDetector(imageView.context, object : SimpleOnGestureListener() {
            /**
             * 长按回调
             */
            override fun onLongPress(e: MotionEvent) {
                mLongClickListener?.onLongClick(getImageView())
            }

            /**
             * 快速滑动回调
             */
            override fun onFling(e1: MotionEvent?, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
                if (getScale() > DEFAULT_MIN_SCALE) {
                    return false
                }
                if (MotionEventCompat.getPointerCount(e1) > SINGLE_TOUCH || MotionEventCompat.getPointerCount(e2) > SINGLE_TOUCH) {
                    return false
                }
                return mSingleFlingListener?.onFling(e1, e2, velocityX, velocityY).orFalse
            }
        })
        // 设置默认双击监听器
        mGestureDetector?.setOnDoubleTapListener(DefaultOnDoubleTapListener(this))
        mBaseRotation = 0.0f
        // 设置是否可缩放
        setZoomable(zoomable)
    }

    /**
     * 是否允许缩放
     */
    override fun canZoom(): Boolean {
        return mZoomEnabled
    }

    /**
     * 获取图片当前显示区域
     */
    override fun getDisplayRect(): RectF? {
        checkMatrixBounds()
        return getDisplayRect(getDrawMatrix())
    }

    /**
     * 直接设置显示矩阵
     */
    override fun setDisplayMatrix(finalMatrix: Matrix?): Boolean {
        requireNotNull(finalMatrix) { "Matrix cannot be null" }
        val imageView = getImageView() ?: return false
        if (null == imageView.getDrawable()) return false
        mSuppMatrix.set(finalMatrix)
        setImageViewMatrix(getDrawMatrix())
        checkMatrixBounds()
        return true
    }

    /**
     * 获取显示矩阵
     */
    override fun getDisplayMatrix(matrix: Matrix) {
        matrix.set(getDrawMatrix())
    }

    override fun getMinimumScale(): Float {
        return mMinScale
    }

    override fun getMediumScale(): Float {
        return mMidScale
    }

    override fun getMaximumScale(): Float {
        return mMaxScale
    }

    /**
     * 获取当前缩放比例
     */
    override fun getScale(): Float {
        return sqrt((getValue(mSuppMatrix, Matrix.MSCALE_X).toDouble().pow(2.0).toFloat() + getValue(mSuppMatrix, Matrix.MSKEW_Y).toDouble().pow(2.0).toFloat()).toDouble()).toFloat()
    }

    override fun getScaleType(): ScaleType {
        return mScaleType
    }

    override fun setAllowParentInterceptOnEdge(allow: Boolean) {
        mAllowParentInterceptOnEdge = allow
    }

    override fun setMinimumScale(minimumScale: Float) {
        checkZoomLevels(minimumScale, mMidScale, mMaxScale)
        mMinScale = minimumScale
    }

    override fun setMediumScale(mediumScale: Float) {
        checkZoomLevels(mMinScale, mediumScale, mMaxScale)
        mMidScale = mediumScale
    }

    override fun setMaximumScale(maximumScale: Float) {
        checkZoomLevels(mMinScale, mMidScale, maximumScale)
        mMaxScale = maximumScale
    }

    override fun setScaleLevels(minimumScale: Float, mediumScale: Float, maximumScale: Float) {
        checkZoomLevels(minimumScale, mediumScale, maximumScale)
        mMinScale = minimumScale
        mMidScale = mediumScale
        mMaxScale = maximumScale
    }

    override fun setOnLongClickListener(listener: OnLongClickListener) {
        mLongClickListener = listener
    }

    override fun setOnMatrixChangeListener(listener: OnMatrixChangedListener) {
        mMatrixChangeListener = listener
    }

    override fun setOnPhotoTapListener(listener: OnPhotoTapListener) {
        mPhotoTapListener = listener
    }

    override fun setOnViewTapListener(listener: OnViewTapListener) {
        mViewTapListener = listener
    }

    /**
     * 旋转图片到指定角度
     */
    override fun setRotationTo(rotationDegree: Float) {
        mSuppMatrix.setRotate(rotationDegree % 360)
        checkAndDisplayMatrix()
    }

    /**
     * 在当前角度基础上再旋转
     */
    override fun setRotationBy(rotationDegree: Float) {
        mSuppMatrix.postRotate(rotationDegree % 360)
        checkAndDisplayMatrix()
    }

    override fun setScale(scale: Float) {
        setScale(scale, false)
    }

    override fun setScale(scale: Float, animate: Boolean) {
        val imageView = getImageView() ?: return
        setScale(scale, (imageView.right).toFloat() / 2, (imageView.bottom).toFloat() / 2, animate)
    }

    override fun setScale(scale: Float, focalX: Float, focalY: Float, animate: Boolean) {
        val imageView = getImageView() ?: return
        if (scale !in mMinScale..mMaxScale) {
            return
        }
        if (animate) {
            imageView.post(AnimatedZoomRunnable(getScale(), scale, focalX, focalY))
        } else {
            mSuppMatrix.setScale(scale, scale, focalX, focalY)
            checkAndDisplayMatrix()
        }
    }

    override fun setScaleType(scaleType: ScaleType) {
        if (isSupportedScaleType(scaleType) && scaleType != mScaleType) {
            mScaleType = scaleType
            update()
        }
    }

    override fun setZoomable(zoomable: Boolean) {
        mZoomEnabled = zoomable
        update()
    }

    /**
     * 获取当前可见区域的截图
     */
    override fun getVisibleRectangleBitmap(): Bitmap? {
        val imageView = getImageView()
        return imageView?.drawingCache
    }

    override fun setZoomTransitionDuration(milliseconds: Int) {
        ZOOM_DURATION = if (milliseconds < 0) DEFAULT_ZOOM_DURATION else milliseconds
    }

    override fun getIPhotoViewImplementation(): IPhotoView {
        return this
    }

    /**
     * 设置双击监听器
     */
    override fun setOnDoubleTapListener(newOnDoubleTapListener: GestureDetector.OnDoubleTapListener?) {
        if (newOnDoubleTapListener != null) {
            mGestureDetector?.setOnDoubleTapListener(newOnDoubleTapListener)
        } else {
            mGestureDetector?.setOnDoubleTapListener(DefaultOnDoubleTapListener(this))
        }
    }

    /**
     * 设置缩放变化监听
     */
    override fun setOnScaleChangeListener(onScaleChangeListener: OnScaleChangeListener) {
        mScaleChangeListener = onScaleChangeListener
    }

    /**
     * 设置单指快速滑动监听
     */
    override fun setOnSingleFlingListener(onSingleFlingListener: OnSingleFlingListener) {
        mSingleFlingListener = onSingleFlingListener
    }

    /**
     * 触摸事件处理
     */
    override fun onTouch(v: View?, ev: MotionEvent): Boolean {
        var handled = false
        if (mZoomEnabled && hasDrawable(v as ImageView?)) {
            val parent = v?.parent
            when (ev?.action) {
                // 按下时禁止父控件拦截，保证图片能拖动
                MotionEvent.ACTION_DOWN -> {
                    parent?.requestDisallowInterceptTouchEvent(true)
                    cancelFling()
                }
                // 抬起时，如果缩放小于最小值，自动回弹到最小
                MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
                    if (getScale() < mMinScale) {
                        val rect = getDisplayRect()
                        if (null != rect) {
                            v?.post(AnimatedZoomRunnable(getScale(), mMinScale, rect.centerX(), rect.centerY()))
                            handled = true
                        }
                    }
                }
            }
            // 处理缩放、拖动
            if (null != mScaleDragDetector) {
                val wasScaling = mScaleDragDetector?.isScaling().orFalse
                val wasDragging = mScaleDragDetector?.isDragging().orFalse
                handled = mScaleDragDetector?.onTouchEvent(ev).orFalse
                val didntScale = !wasScaling && !mScaleDragDetector?.isScaling().orFalse
                val didntDrag = !wasDragging && !mScaleDragDetector?.isDragging().orFalse
                mBlockParentIntercept = didntScale && didntDrag
            }
            // 处理双击、长按
            if (null != mGestureDetector && mGestureDetector?.onTouchEvent(ev).orFalse) {
                handled = true
            }
        }
        return handled
    }

    /**
     * 拖动回调
     */
    override fun onDrag(dx: Float, dy: Float) {
        if (mScaleDragDetector?.isScaling().orFalse) return
        val imageView = getImageView()
        mSuppMatrix.postTranslate(dx, dy)
        checkAndDisplayMatrix()
        // 边缘时允许父控件拦截（解决ViewPager滑动冲突）
        val parent = imageView?.parent
        if (mAllowParentInterceptOnEdge && !mScaleDragDetector?.isScaling().orFalse && !mBlockParentIntercept) {
            if (mScrollEdge == EDGE_BOTH || (mScrollEdge == EDGE_LEFT && dx >= 1f) || (mScrollEdge == EDGE_RIGHT && dx <= -1f)) {
                parent?.requestDisallowInterceptTouchEvent(false)
            }
        } else {
            parent?.requestDisallowInterceptTouchEvent(true)
        }
    }

    /**
     * 快速滑动（惯性）回调
     */
    override fun onFling(startX: Float, startY: Float, velocityX: Float, velocityY: Float) {
        val imageView = getImageView() ?: return
        mCurrentFlingRunnable = FlingRunnable(imageView.context)
        mCurrentFlingRunnable?.fling(getImageViewWidth(imageView), getImageViewHeight(imageView), velocityX.toInt(), velocityY.toInt())
        imageView.post(mCurrentFlingRunnable)
    }

    /**
     * 缩放回调
     */
    override fun onScale(scaleFactor: Float, focusX: Float, focusY: Float) {
        if ((getScale() < mMaxScale || scaleFactor < 1f) && (getScale() > mMinScale || scaleFactor > 1f)) {
            if (null != mScaleChangeListener) {
                mScaleChangeListener?.onScaleChange(scaleFactor, focusX, focusY)
            }
            mSuppMatrix.postScale(scaleFactor, scaleFactor, focusX, focusY)
            checkAndDisplayMatrix()
        }
    }

    /**
     * 布局完成回调
     * 重新计算图片居中、缩放、位置
     */
    override fun onGlobalLayout() {
        val imageView = getImageView() ?: return
        if (mZoomEnabled) {
            val top = imageView.top
            val right = imageView.right
            val bottom = imageView.bottom
            val left = imageView.left
            // 位置发生变化，重新更新矩阵
            if (top != mIvTop || bottom != mIvBottom || left != mIvLeft || right != mIvRight) {
                updateBaseMatrix(imageView.getDrawable())
                mIvTop = top
                mIvRight = right
                mIvBottom = bottom
                mIvLeft = left
            }
        } else {
            updateBaseMatrix(imageView.getDrawable())
        }
    }

    /**
     * 清理资源，防止内存泄漏
     * 在界面销毁时调用
     */
    fun cleanup() {
        if (null == mImageView) return
        val imageView = mImageView?.get()
        if (null != imageView) {
            val observer = imageView.getViewTreeObserver()
            if (null != observer && observer.isAlive) {
                observer.removeGlobalOnLayoutListener(this)
            }
            imageView.setOnTouchListener(null)
            cancelFling()
        }
        mGestureDetector?.setOnDoubleTapListener(null)
        mMatrixChangeListener = null
        mPhotoTapListener = null
        mViewTapListener = null
        mImageView = null
    }

    /**
     * 设置基础旋转角度
     */
    fun setBaseRotation(degrees: Float) {
        mBaseRotation = degrees % 360
        update()
        setRotationBy(mBaseRotation)
        checkAndDisplayMatrix()
    }

    /**
     * 获取绑定的 ImageView
     */
    fun getImageView(): ImageView? {
        var imageView: ImageView? = null
        if (null != mImageView) {
            imageView = mImageView?.get()
        }
        if (null == imageView) {
            cleanup()
        }
        return imageView
    }

    fun getOnPhotoTapListener(): OnPhotoTapListener? {
        return mPhotoTapListener
    }

    fun getOnViewTapListener(): OnViewTapListener? {
        return mViewTapListener
    }

    /**
     * 设置缩放动画插值器
     */
    fun setZoomInterpolator(interpolator: Interpolator) {
        mInterpolator = interpolator
    }

    /**
     * 更新图片显示（重新计算矩阵、位置）
     */
    fun update() {
        val imageView = getImageView() ?: return
        if (mZoomEnabled) {
            setImageViewScaleTypeMatrix(imageView)
            updateBaseMatrix(imageView.getDrawable())
        } else {
            resetMatrix()
        }
    }

    /**
     * 获取缩放/平移矩阵
     */
    fun getSuppMatrix(matrix: Matrix) {
        matrix.set(mSuppMatrix)
    }

    /**
     * 获取最终绘制矩阵
     */
    private fun getDrawMatrix(): Matrix {
        mDrawMatrix.set(mBaseMatrix)
        mDrawMatrix.postConcat(mSuppMatrix)
        return mDrawMatrix
    }

    /**
     * 取消惯性滑动
     */
    private fun cancelFling() {
        mCurrentFlingRunnable?.cancelFling()
        mCurrentFlingRunnable = null
    }

    /**
     * 获取图片矩阵
     */
    fun getImageMatrix(): Matrix {
        return mDrawMatrix
    }

    /**
     * 检查矩阵边界并应用
     */
    private fun checkAndDisplayMatrix() {
        if (checkMatrixBounds()) {
            setImageViewMatrix(getDrawMatrix())
        }
    }

    /**
     * 检查 ImageView 缩放模式是否正确
     */
    private fun checkImageViewScaleType() {
        val imageView = getImageView()
        if (null != imageView && imageView !is IPhotoView) {
            check(ScaleType.MATRIX == imageView.getScaleType()) { "The ImageView's ScaleType has been changed since attaching a PhotoViewAttacher. You should call " + "setScaleType on the PhotoViewAttacher instead of on the ImageView" }
        }
    }

    /**
     * 检查矩阵边界，限制图片不能划出屏幕
     */
    private fun checkMatrixBounds(): Boolean {
        val imageView = getImageView() ?: return false
        val rect = getDisplayRect(getDrawMatrix()) ?: return false
        val height = rect.height()
        val width = rect.width()
        var deltaX = 0f
        var deltaY = 0f
        val viewHeight = getImageViewHeight(imageView)
        if (height <= viewHeight) {
            deltaY = when (mScaleType) {
                ScaleType.FIT_START -> -rect.top
                ScaleType.FIT_END -> viewHeight - height - rect.top
                else -> (viewHeight - height) / 2 - rect.top
            }
        } else if (rect.top > 0) {
            deltaY = -rect.top
        } else if (rect.bottom < viewHeight) {
            deltaY = viewHeight - rect.bottom
        }
        val viewWidth = getImageViewWidth(imageView)
        if (width <= viewWidth) {
            deltaX = when (mScaleType) {
                ScaleType.FIT_START -> -rect.left
                ScaleType.FIT_END -> viewWidth - width - rect.left
                else -> (viewWidth - width) / 2 - rect.left
            }
            mScrollEdge = EDGE_BOTH
        } else if (rect.left > 0) {
            mScrollEdge = EDGE_LEFT
            deltaX = -rect.left
        } else if (rect.right < viewWidth) {
            deltaX = viewWidth - rect.right
            mScrollEdge = EDGE_RIGHT
        } else {
            mScrollEdge = EDGE_NONE
        }
        mSuppMatrix.postTranslate(deltaX, deltaY)
        return true
    }

    /**
     * 计算矩阵对应的图片显示区域
     */
    private fun getDisplayRect(matrix: Matrix): RectF? {
        val imageView = getImageView()
        if (null != imageView) {
            val d = imageView.getDrawable()
            if (null != d) {
                mDisplayRect.set(0f, 0f, d.intrinsicWidth.toFloat(), d.intrinsicHeight.toFloat())
                matrix.mapRect(mDisplayRect)
                return mDisplayRect
            }
        }
        return null
    }

    /**
     * 从矩阵中获取指定数值
     */
    private fun getValue(matrix: Matrix, whichValue: Int): Float {
        matrix.getValues(mMatrixValues)
        return mMatrixValues[whichValue]
    }

    /**
     * 重置矩阵
     */
    private fun resetMatrix() {
        mSuppMatrix.reset()
        setRotationBy(mBaseRotation)
        setImageViewMatrix(getDrawMatrix())
        checkMatrixBounds()
    }

    /**
     * 把矩阵应用到 ImageView
     */
    private fun setImageViewMatrix(matrix: Matrix) {
        val imageView = getImageView() ?: return
        checkImageViewScaleType()
        imageView.setImageMatrix(matrix)
        val displayRect = getDisplayRect(matrix)
        if (null != displayRect) {
            mMatrixChangeListener?.onMatrixChanged(displayRect)
        }
    }

    /**
     * 计算基础矩阵（图片居中、适应屏幕）
     */
    private fun updateBaseMatrix(d: Drawable?) {
        val imageView = getImageView()
        if (null == imageView || null == d) {
            return
        }
        val viewWidth = getImageViewWidth(imageView).toFloat()
        val viewHeight = getImageViewHeight(imageView).toFloat()
        val drawableWidth = d.intrinsicWidth
        val drawableHeight = d.intrinsicHeight
        mBaseMatrix.reset()
        val widthScale = viewWidth / drawableWidth
        val heightScale = viewHeight / drawableHeight
        when (mScaleType) {
            ScaleType.CENTER -> {
                mBaseMatrix.postTranslate((viewWidth - drawableWidth) / 2f, (viewHeight - drawableHeight) / 2f)
            }
            ScaleType.CENTER_CROP -> {
                val scale = max(widthScale, heightScale)
                mBaseMatrix.postScale(scale, scale)
                mBaseMatrix.postTranslate((viewWidth - drawableWidth * scale) / 2f, (viewHeight - drawableHeight * scale) / 2f)
            }
            ScaleType.CENTER_INSIDE -> {
                val scale = min(1.0f, min(widthScale, heightScale))
                mBaseMatrix.postScale(scale, scale)
                mBaseMatrix.postTranslate((viewWidth - drawableWidth * scale) / 2f, (viewHeight - drawableHeight * scale) / 2f)
            }
            else -> {
                var mTempSrc = RectF(0f, 0f, drawableWidth.toFloat(), drawableHeight.toFloat())
                val mTempDst = RectF(0f, 0f, viewWidth, viewHeight)
                if (mBaseRotation.toInt() % 180 != 0) {
                    mTempSrc = RectF(0f, 0f, drawableHeight.toFloat(), drawableWidth.toFloat())
                }
                when (mScaleType) {
                    ScaleType.FIT_CENTER -> mBaseMatrix.setRectToRect(mTempSrc, mTempDst, ScaleToFit.CENTER)
                    ScaleType.FIT_START -> mBaseMatrix.setRectToRect(mTempSrc, mTempDst, ScaleToFit.START)
                    ScaleType.FIT_END -> mBaseMatrix.setRectToRect(mTempSrc, mTempDst, ScaleToFit.END)
                    ScaleType.FIT_XY -> mBaseMatrix.setRectToRect(mTempSrc, mTempDst, ScaleToFit.FILL)
                    else -> {}
                }
            }
        }
        resetMatrix()
    }

    /**
     * 获取 ImageView 有效宽度/高度（去除内边距）
     */
    private fun getImageViewWidth(imageView: ImageView?): Int {
        if (null == imageView) return 0
        return imageView.width - imageView.getPaddingLeft() - imageView.getPaddingRight()
    }

    private fun getImageViewHeight(imageView: ImageView?): Int {
        if (null == imageView) return 0
        return imageView.height - imageView.paddingTop - imageView.paddingBottom
    }

    /**
     * 缩放动画
     */
    private inner class AnimatedZoomRunnable(private val currentZoom: Float, private val targetZoom: Float, private val focalX: Float, private val focalY: Float) : Runnable {
        private val mStartTime = System.currentTimeMillis()

        override fun run() {
            val imageView = getImageView() ?: return
            val t = interpolate()
            val scale = currentZoom + t * (targetZoom - currentZoom)
            val deltaScale = scale / getScale()
            onScale(deltaScale, focalX, focalY)
            if (t < 1f) {
                imageView.postOnAnimation(this)
            }
        }

        private fun interpolate(): Float {
            var t = 1f * (System.currentTimeMillis() - mStartTime) / ZOOM_DURATION
            t = min(1f, t)
            t = mInterpolator.getInterpolation(t)
            return t
        }
    }

    /**
     * 惯性滑动
     */
    private inner class FlingRunnable(context: Context) : Runnable {
        private var mCurrentX = 0
        private var mCurrentY = 0
        private val mScroller: ScrollerProxy = GingerScroller(context)

        fun cancelFling() {
            mScroller.forceFinished(true)
        }

        fun fling(viewWidth: Int, viewHeight: Int, velocityX: Int, velocityY: Int) {
            val rect = getDisplayRect() ?: return
            val startX = (-rect.left).roundToInt()
            val minX: Int
            val maxX: Int
            val minY: Int
            val maxY: Int
            if (viewWidth < rect.width()) {
                minX = 0
                maxX = (rect.width() - viewWidth).roundToInt()
            } else {
                maxX = startX
                minX = maxX
            }
            val startY = (-rect.top).roundToInt()
            if (viewHeight < rect.height()) {
                minY = 0
                maxY = (rect.height() - viewHeight).roundToInt()
            } else {
                maxY = startY
                minY = maxY
            }
            mCurrentX = startX
            mCurrentY = startY
            if (startX != maxX || startY != maxY) {
                mScroller.fling(startX, startY, velocityX, velocityY, minX, maxX, minY, maxY, 0, 0)
            }
        }

        override fun run() {
            if (mScroller.isFinished()) {
                return
            }
            val imageView = getImageView()
            if (null != imageView && mScroller.computeScrollOffset()) {
                val newX = mScroller.getCurrX()
                val newY = mScroller.getCurrY()
                mSuppMatrix.postTranslate((mCurrentX - newX).toFloat(), (mCurrentY - newY).toFloat())
                setImageViewMatrix(getDrawMatrix())
                mCurrentX = newX
                mCurrentY = newY
                imageView.postOnAnimation(this)
            }
        }
    }

    /**
     * 内部接口
     */
    interface OnMatrixChangedListener {
        fun onMatrixChanged(rect: RectF?)
    }

    interface OnScaleChangeListener {
        fun onScaleChange(scaleFactor: Float, focusX: Float, focusY: Float)
    }

    interface OnPhotoTapListener {
        fun onPhotoTap(view: View?, x: Float, y: Float)

        fun onOutsidePhotoTap()
    }

    interface OnViewTapListener {
        fun onViewTap(v: View?, x: Float, y: Float)
    }

    interface OnSingleFlingListener {
        fun onFling(e1: MotionEvent?, e2: MotionEvent?, velocityX: Float, velocityY: Float): Boolean
    }

}