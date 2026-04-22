package com.example.gallery.feature.durban.widget

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Region
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.IntDef
import androidx.annotation.IntRange
import androidx.core.content.ContextCompat
import androidx.core.graphics.withSave
import com.example.gallery.R
import com.example.gallery.feature.durban.utils.RectUtil.getCenterFromRect
import com.example.gallery.feature.durban.utils.RectUtil.getCornersFromRect
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * 裁剪遮罩层View
 * 功能：绘制半透明背景、裁剪框、网格线、支持自由拖拽裁剪框
 */
@SuppressLint("ClickableViewAccessibility")
class OverlayView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : View(context, attrs, defStyleAttr) {
    // 自由裁剪模式
    @FreestyleMode
    private var mFreestyleCropMode = DEFAULT_FREESTYLE_CROP_MODE
    // 裁剪网格行列数
    private var mCropGridRowCount = 0
    private var mCropGridColumnCount = 0
    // 遮罩半透明颜色
    private var mDimmedColor = 0
    // 当前触摸的裁剪框角点索引
    private var mCurrentTouchCornerIndex = -1
    // 触摸识别阈值
    private var mTouchPointThreshold = getResources().getDimensionPixelSize(R.dimen.gallery_dp_30);
    // 裁剪框最小尺寸
    private var mCropRectMinSize = getResources().getDimensionPixelSize(R.dimen.gallery_dp_100)
    // 裁剪框角点触摸区域长度
    private var mCropRectCornerTouchAreaLineLength = getResources().getDimensionPixelSize(R.dimen.gallery_dp_10)
    // 目标宽高比
    private var mTargetAspectRatio = 0f
    // 上一次触摸坐标
    private var mPreviousTouchX = -1f
    private var mPreviousTouchY = -1f
    // 网格线坐标点
    private var mGridPoints = floatArrayOf()
    // 是否显示裁剪框、网格
    private var mShowCropFrame = false
    private var mShowCropGrid = false
    // 是否圆形遮罩
    private var mCircleDimmedLayer = false
    // 是否需要初始化裁剪边界
    private var mShouldSetupCropBounds = false
    // 裁剪框变化回调
    private var mCallback: OnOverlayChangeListener? = null
    // 圆形裁剪路径
    private val mCircularPath = Path()
    // 半透明背景画笔
    private val mDimmedStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    // 裁剪网格画笔
    private val mCropGridPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    // 裁剪框画笔
    private val mCropFramePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    // 裁剪框角点画笔
    private val mCropFrameCornersPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    // 裁剪框矩形
    private val mCropViewRect = RectF()
    // 临时矩形
    private val mTempRect = RectF()
    // View宽高
    private var mThisWidth = 0
    private var mThisHeight = 0
    // 裁剪框四个角坐标、中心坐标
    private var mCropGridCorners = floatArrayOf()
    private var mCropGridCenter = floatArrayOf()

    companion object {
        // 自由裁剪模式常量
        const val FREESTYLE_CROP_MODE_DISABLE = 0 // 禁用自由裁剪
        const val FREESTYLE_CROP_MODE_ENABLE = 1 // 启用自由裁剪
        const val FREESTYLE_CROP_MODE_ENABLE_WITH_PASS_THROUGH = 2 // 启用并支持触摸穿透
        const val DEFAULT_FREESTYLE_CROP_MODE = FREESTYLE_CROP_MODE_DISABLE // 默认模式

        /**
         * 自由裁剪模式注解
         */
        @Retention(AnnotationRetention.SOURCE)
        @IntDef(FREESTYLE_CROP_MODE_DISABLE, FREESTYLE_CROP_MODE_ENABLE, FREESTYLE_CROP_MODE_ENABLE_WITH_PASS_THROUGH)
        annotation class FreestyleMode
    }

    /**
     * View布局完成，计算宽高
     */
    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        if (changed) {
            mThisWidth = (width - paddingRight) - paddingLeft
            mThisHeight = (height - paddingBottom) - paddingTop
            if (mShouldSetupCropBounds) {
                mShouldSetupCropBounds = false
                setTargetAspectRatio(mTargetAspectRatio)
            }
        }
    }

    /**
     * 绘制：半透明背景 + 裁剪网格 + 裁剪框
     */
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawDimmedLayer(canvas)
        drawCropGrid(canvas)
    }

    /**
     * 触摸事件：处理自由裁剪模式下的裁剪框拖拽、缩放
     */
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (mCropViewRect.isEmpty || mFreestyleCropMode == FREESTYLE_CROP_MODE_DISABLE) {
            return false
        }
        var x = event.x
        var y = event.y
        // 手指按下：判断是否触摸到裁剪框角点
        if ((event.action and MotionEvent.ACTION_MASK) == MotionEvent.ACTION_DOWN) {
            mCurrentTouchCornerIndex = getCurrentTouchIndex(x, y)
            val shouldHandle = mCurrentTouchCornerIndex != -1
            if (!shouldHandle) {
                mPreviousTouchX = -1f
                mPreviousTouchY = -1f
            } else if (mPreviousTouchX < 0) {
                mPreviousTouchX = x
                mPreviousTouchY = y
            }
            return shouldHandle
        }
        // 手指移动：更新裁剪框大小/位置
        if ((event.action and MotionEvent.ACTION_MASK) == MotionEvent.ACTION_MOVE) {
            if (event.pointerCount == 1 && mCurrentTouchCornerIndex != -1) {
                // 限制触摸范围在View内
                x = min(max(x, paddingLeft.toFloat()), (width - paddingRight).toFloat())
                y = min(max(y, paddingTop.toFloat()), (height - paddingBottom).toFloat())
                updateCropViewRect(x, y)
                mPreviousTouchX = x
                mPreviousTouchY = y
                return true
            }
        }
        // 手指抬起：重置状态，通知裁剪框变化
        if ((event.action and MotionEvent.ACTION_MASK) == MotionEvent.ACTION_UP) {
            mPreviousTouchX = -1f
            mPreviousTouchY = -1f
            mCurrentTouchCornerIndex = -1
            mCallback?.onCropRectUpdated(mCropViewRect)
        }
        return false
    }

    /**
     * 根据触摸点更新裁剪框矩形（缩放/移动）
     * 角点顺序：0左上 1右上 2右下 3左下 4内部拖动
     */
    private fun updateCropViewRect(touchX: Float, touchY: Float) {
        mTempRect.set(mCropViewRect)
        when (mCurrentTouchCornerIndex) {
            0 -> mTempRect.set(touchX, touchY, mCropViewRect.right, mCropViewRect.bottom)
            1 -> mTempRect.set(mCropViewRect.left, touchY, touchX, mCropViewRect.bottom)
            2 -> mTempRect.set(mCropViewRect.left, mCropViewRect.top, touchX, touchY)
            3 -> mTempRect.set(touchX, mCropViewRect.top, mCropViewRect.right, touchY)
            4 -> {
                mTempRect.offset(touchX - mPreviousTouchX, touchY - mPreviousTouchY)
                if (mTempRect.left > left && mTempRect.top > top && mTempRect.right < right && mTempRect.bottom < bottom) {
                    mCropViewRect.set(mTempRect)
                    updateGridPoints()
                    postInvalidate()
                }
                return
            }
        }
        // 限制最小尺寸
        val changeHeight = mTempRect.height() >= mCropRectMinSize
        val changeWidth = mTempRect.width() >= mCropRectMinSize
        mCropViewRect.set(if (changeWidth) mTempRect.left else mCropViewRect.left, if (changeHeight) mTempRect.top else mCropViewRect.top, if (changeWidth) mTempRect.right else mCropViewRect.right, if (changeHeight) mTempRect.bottom else mCropViewRect.bottom)
        if (changeHeight || changeWidth) {
            updateGridPoints()
            postInvalidate()
        }
    }

    /**
     * 获取触摸点对应的裁剪框角点索引
     */
    private fun getCurrentTouchIndex(touchX: Float, touchY: Float): Int {
        var closestPointIndex = -1
        var closestPointDistance = mTouchPointThreshold.toDouble()
        var i = 0
        while (i < 8) {
            // 计算触摸点到四个角的距离
            val distanceToCorner = sqrt((touchX - mCropGridCorners[i]).toDouble().pow(2.0) + (touchY - mCropGridCorners[i + 1]).toDouble().pow(2.0))
            if (distanceToCorner < closestPointDistance) {
                closestPointDistance = distanceToCorner
                closestPointIndex = i / 2
            }
            i += 2
        }
        // 在裁剪框内部且允许自由裁剪 → 返回拖动模式
        if (mFreestyleCropMode == FREESTYLE_CROP_MODE_ENABLE && closestPointIndex < 0 && mCropViewRect.contains(touchX, touchY)) {
            return 4
        }
        return closestPointIndex
    }

    /**
     * 绘制半透明遮罩层（圆形/矩形）
     */
    private fun drawDimmedLayer(canvas: Canvas) {
        canvas.withSave {
            // 裁剪掉中间区域，只留四周半透明
            if (mCircleDimmedLayer) {
                clipPath(mCircularPath, Region.Op.DIFFERENCE)
            } else {
                clipRect(mCropViewRect, Region.Op.DIFFERENCE)
            }
            drawColor(mDimmedColor)
        }
        // 圆形遮罩绘制1px边框修复抗锯齿
        if (mCircleDimmedLayer) {
            canvas.drawCircle(mCropViewRect.centerX(), mCropViewRect.centerY(), min(mCropViewRect.width(), mCropViewRect.height()) / 2f, mDimmedStrokePaint)
        }
    }

    /**
     * 绘制裁剪网格 + 裁剪框 + 角点
     */
    private fun drawCropGrid(canvas: Canvas) {
        // 绘制网格线
        if (mShowCropGrid) {
            if (mGridPoints.isEmpty() && !mCropViewRect.isEmpty) {
                mGridPoints = FloatArray((mCropGridRowCount) * 4 + (mCropGridColumnCount) * 4)
                var index = 0
                // 绘制水平网格线
                for (i in 0 until mCropGridRowCount) {
                    mGridPoints[index++] = mCropViewRect.left
                    mGridPoints[index++] = (mCropViewRect.height() * ((i.toFloat() + 1.0f) / (mCropGridRowCount + 1).toFloat())) + mCropViewRect.top
                    mGridPoints[index++] = mCropViewRect.right
                    mGridPoints[index++] = (mCropViewRect.height() * ((i.toFloat() + 1.0f) / (mCropGridRowCount + 1).toFloat())) + mCropViewRect.top
                }
                // 绘制垂直网格线
                for (i in 0 until mCropGridColumnCount) {
                    mGridPoints[index++] = (mCropViewRect.width() * ((i.toFloat() + 1.0f) / (mCropGridColumnCount + 1).toFloat())) + mCropViewRect.left
                    mGridPoints[index++] = mCropViewRect.top
                    mGridPoints[index++] = (mCropViewRect.width() * ((i.toFloat() + 1.0f) / (mCropGridColumnCount + 1).toFloat())) + mCropViewRect.left
                    mGridPoints[index++] = mCropViewRect.bottom
                }
            }
            if (mGridPoints.isNotEmpty()) {
                canvas.drawLines(mGridPoints, mCropGridPaint)
            }
        }
        // 绘制裁剪框
        if (mShowCropFrame) {
            canvas.drawRect(mCropViewRect, mCropFramePaint)
        }
        // 自由模式下绘制角点
        if (mFreestyleCropMode != FREESTYLE_CROP_MODE_DISABLE) {
            canvas.withSave {
                // 只绘制四个角，不绘制边
                mTempRect.set(mCropViewRect)
                mTempRect.inset(mCropRectCornerTouchAreaLineLength.toFloat(), -mCropRectCornerTouchAreaLineLength.toFloat())
                clipRect(mTempRect, Region.Op.DIFFERENCE)
                mTempRect.set(mCropViewRect)
                mTempRect.inset(-mCropRectCornerTouchAreaLineLength.toFloat(), mCropRectCornerTouchAreaLineLength.toFloat())
                clipRect(mTempRect, Region.Op.DIFFERENCE)
                drawRect(mCropViewRect, mCropFrameCornersPaint)
            }
        }
    }

    /**
     * 解析XML自定义属性
     */
    fun processStyledAttributes(a: TypedArray) {
        mCircleDimmedLayer = a.getBoolean(R.styleable.durban_CropView_durban_circle_dimmed_layer, false)
        mDimmedColor = a.getColor(R.styleable.durban_CropView_durban_dimmed_color, ContextCompat.getColor(context, R.color.durbanCropDimmed))
        mDimmedStrokePaint.color = mDimmedColor
        mDimmedStrokePaint.style = Paint.Style.STROKE
        mDimmedStrokePaint.strokeWidth = 1f
        initCropFrameStyle(a)
        mShowCropFrame = a.getBoolean(R.styleable.durban_CropView_durban_show_frame, true)
        initCropGridStyle(a)
        mShowCropGrid = a.getBoolean(R.styleable.durban_CropView_durban_show_grid, true)
    }

    /**
     * 初始化裁剪框画笔
     */
    private fun initCropFrameStyle(a: TypedArray) {
        val cropFrameStrokeSize = a.getDimensionPixelSize(R.styleable.durban_CropView_durban_frame_stroke_size, resources.getDimensionPixelSize(R.dimen.gallery_dp_1))
        val cropFrameColor = a.getColor(R.styleable.durban_CropView_durban_frame_color, ContextCompat.getColor(context, R.color.durbanCropFrameLine))
        mCropFramePaint.strokeWidth = cropFrameStrokeSize.toFloat()
        mCropFramePaint.color = cropFrameColor
        mCropFramePaint.style = Paint.Style.STROKE
        mCropFrameCornersPaint.strokeWidth = (cropFrameStrokeSize * 3).toFloat()
        mCropFrameCornersPaint.color = cropFrameColor
        mCropFrameCornersPaint.style = Paint.Style.STROKE
    }

    /**
     * 初始化裁剪框画笔
     */
    private fun initCropGridStyle(a: TypedArray) {
        val cropGridStrokeSize = a.getDimensionPixelSize(R.styleable.durban_CropView_durban_grid_stroke_size, resources.getDimensionPixelSize(R.dimen.gallery_dp_1))
        val cropGridColor = a.getColor(R.styleable.durban_CropView_durban_grid_color, ContextCompat.getColor(context, R.color.durbanCropGridLine))
        mCropGridPaint.strokeWidth = cropGridStrokeSize.toFloat()
        mCropGridPaint.color = cropGridColor
        mCropGridRowCount = a.getInt(R.styleable.durban_CropView_durban_grid_row_count, 2)
        mCropGridColumnCount = a.getInt(R.styleable.durban_CropView_durban_grid_column_count, 2)
    }

    /**
     * 获取裁剪框矩形
     */
    fun getCropViewRect(): RectF {
        return mCropViewRect
    }

    fun isFreestyleCropEnabled(): Boolean {
        return mFreestyleCropMode == FREESTYLE_CROP_MODE_ENABLE
    }

    fun setFreestyleCropEnabled(freestyleCropEnabled: Boolean) {
        mFreestyleCropMode = if (freestyleCropEnabled) FREESTYLE_CROP_MODE_ENABLE else FREESTYLE_CROP_MODE_DISABLE
    }

    /**
     * 获取/设置自由裁剪模式
     */
    @FreestyleMode
    fun getFreestyleCropMode(): Int {
        return mFreestyleCropMode
    }

    fun setFreestyleCropMode(@FreestyleMode mFreestyleCropMode: Int) {
        this.mFreestyleCropMode = mFreestyleCropMode
        postInvalidate()
    }

    /**
     * 设置是否圆形遮罩
     */
    fun setCircleDimmedLayer(circleDimmedLayer: Boolean) {
        mCircleDimmedLayer = circleDimmedLayer
    }

    /**
     * 设置裁剪网格行数
     */
    fun setCropGridRowCount(@IntRange(from = 0) cropGridRowCount: Int) {
        mCropGridRowCount = cropGridRowCount
        mGridPoints = floatArrayOf()
    }

    /**
     * 设置裁剪网格列数
     */
    fun setCropGridColumnCount(@IntRange(from = 0) cropGridColumnCount: Int) {
        mCropGridColumnCount = cropGridColumnCount
        mGridPoints = floatArrayOf()
    }

    /**
     * 设置是否显示裁剪框
     */
    fun setShowCropFrame(showCropFrame: Boolean) {
        mShowCropFrame = showCropFrame
    }

    /**
     * 设置是否显示裁剪网格
     */
    fun setShowCropGrid(showCropGrid: Boolean) {
        mShowCropGrid = showCropGrid
    }

    /**
     * 设置遮罩颜色
     */
    fun setDimmedColor(@ColorInt dimmedColor: Int) {
        mDimmedColor = dimmedColor
    }

    /**
     * 设置裁剪框线条宽度
     */
    fun setCropFrameStrokeWidth(@IntRange(from = 0) width: Int) {
        mCropFramePaint.strokeWidth = width.toFloat()
    }

    /**
     * 设置网格线条宽度
     */
    fun setCropGridStrokeWidth(@IntRange(from = 0) width: Int) {
        mCropGridPaint.strokeWidth = width.toFloat()
    }

    /**
     * 设置裁剪框颜色
     */
    fun setCropFrameColor(@ColorInt color: Int) {
        mCropFramePaint.setColor(color)
    }

    /**
     * 设置网格颜色
     */
    fun setCropGridColor(@ColorInt color: Int) {
        mCropGridPaint.setColor(color)
    }

    /**
     * 设置裁剪框宽高比
     */
    fun setTargetAspectRatio(targetAspectRatio: Float) {
        mTargetAspectRatio = targetAspectRatio
        if (mThisWidth > 0) {
            setupCropBounds()
            postInvalidate()
        } else {
            mShouldSetupCropBounds = true
        }
    }

    /**
     * 根据宽高比计算并设置裁剪框位置大小
     */
    fun setupCropBounds() {
        val height = (mThisWidth / mTargetAspectRatio).toInt()
        if (height > mThisHeight) {
            val width = (mThisHeight * mTargetAspectRatio).toInt()
            val halfDiff = (mThisWidth - width) / 2
            mCropViewRect.set((paddingLeft + halfDiff).toFloat(), paddingTop.toFloat(), (paddingLeft + width + halfDiff).toFloat(), (paddingTop + mThisHeight).toFloat())
        } else {
            val halfDiff = (mThisHeight - height) / 2
            mCropViewRect.set(paddingLeft.toFloat(), (paddingTop + halfDiff).toFloat(), (paddingLeft + mThisWidth).toFloat(), (paddingTop + height + halfDiff).toFloat())
        }
        mCallback?.onCropRectUpdated(mCropViewRect)
        updateGridPoints()
    }

    /**
     * 更新裁剪框角点、中心点、圆形路径
     */
    private fun updateGridPoints() {
        mCropGridCorners = getCornersFromRect(mCropViewRect)
        mCropGridCenter = getCenterFromRect(mCropViewRect)
        mGridPoints = floatArrayOf()
        mCircularPath.reset()
        mCircularPath.addCircle(mCropViewRect.centerX(), mCropViewRect.centerY(), min(mCropViewRect.width(), mCropViewRect.height()) / 2f, Path.Direction.CW)
    }

    /**
     * 设置裁剪框变化监听
     */
    fun setOverlayViewChangeListener(callback: OnOverlayChangeListener?) {
        mCallback = callback
    }

    /**
     * 裁剪覆盖视图变化监听器
     * 作用：监听裁剪框（矩形选区）位置、大小发生实时变化时回调
     */
    interface OnOverlayChangeListener {
        /**
         * 裁剪框矩形已更新
         * @param cropRect 最新的裁剪框矩形坐标（左、上、右、下）
         */
        fun onCropRectUpdated(cropRect: RectF)
    }

}