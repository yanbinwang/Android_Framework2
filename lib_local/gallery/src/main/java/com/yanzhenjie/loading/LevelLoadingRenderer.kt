package com.yanzhenjie.loading

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.RectF
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import com.example.framework.utils.function.value.orZero
import com.example.framework.utils.function.value.toSafeDouble
import com.example.framework.utils.function.value.toSafeFloat
import com.example.framework.utils.function.value.toSafeLong
import com.yanzhenjie.loading.Utils.dip2px
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min

/**
 * 封装动画（如时间控制、进度计算、状态管理）
 * Created by yan
 */
class LevelLoadingRenderer(context: Context) : LoadingRenderer(context) {
    private var mStrokeInset = 0f
    private var mRotationCount = 0f
    private var mGroupRotation = 0f
    private var mEndDegrees = 0f
    private var mStartDegrees = 0f
    private var mOriginEndDegrees = 0f
    private var mOriginStartDegrees = 0f
    private var mStrokeWidth = 0f
    private var mCenterRadius = 0f
    private var mLevelColors = intArrayOf()
    private var mLevelSwipeDegrees = floatArrayOf()
    private val mPaint = Paint()
    private val mTempBounds = RectF()

    companion object {
        private const val NUM_POINTS = 5
        private const val DEGREE_360 = 360
        private const val MAX_SWIPE_DEGREES = 0.8f * DEGREE_360
        private const val FULL_GROUP_ROTATION = 3.0f * DEGREE_360
        private const val START_TRIM_DURATION_OFFSET = 0.5f
        private const val END_TRIM_DURATION_OFFSET = 1.0f
        private const val DEFAULT_CENTER_RADIUS = 12.5f
        private const val DEFAULT_STROKE_WIDTH = 2.5f
        private val LINEAR_INTERPOLATOR = LinearInterpolator()
        private val MATERIAL_INTERPOLATOR = FastOutSlowInInterpolator()
        private val ACCELERATE_INTERPOLATOR = AccelerateInterpolator()
        private val DECELERATE_INTERPOLATOR = DecelerateInterpolator()
        private val DEFAULT_LEVEL_COLORS = intArrayOf(Color.parseColor("#55ffffff"), Color.parseColor("#b1ffffff"), Color.parseColor("#ffffffff"))
        private val LEVEL_SWEEP_ANGLE_OFFSETS = floatArrayOf(1.0f, 7.0f / 8.0f, 5.0f / 8.0f)

        class Builder(private val mContext: Context) {
            var mWidth = 0
            var mHeight = 0
            var mStrokeWidth = 0
            var mCenterRadius = 0
            var mDuration = 0
            var mLevelColors = intArrayOf()

            fun setWidth(width: Int?): Builder {
                width ?: return this
                this.mWidth = width
                return this
            }

            fun setHeight(height: Int?): Builder {
                height ?: return this
                this.mHeight = height
                return this
            }

            fun setStrokeWidth(strokeWidth: Int?): Builder {
                strokeWidth ?: return this
                this.mStrokeWidth = strokeWidth
                return this
            }

            fun setCenterRadius(centerRadius: Int?): Builder {
                centerRadius ?: return this
                this.mCenterRadius = centerRadius
                return this
            }

            fun setDuration(duration: Int?): Builder {
                duration ?: return this
                this.mDuration = duration
                return this
            }

            fun setLevelColors(colors: IntArray?): Builder {
                colors ?: return this
                this.mLevelColors = colors
                return this
            }

            fun setLevelColor(color: Int?): Builder {
                color ?: return this
                return setLevelColors(intArrayOf(oneThirdAlphaColor(color), twoThirdAlphaColor(color), color))
            }

            fun build(): LevelLoadingRenderer {
                val loadingRenderer = LevelLoadingRenderer(mContext)
                loadingRenderer.apply(this)
                return loadingRenderer
            }

            private fun oneThirdAlphaColor(colorValue: Int): Int {
                val startA = (colorValue shr 24) and 0xff
                val startR = (colorValue shr 16) and 0xff
                val startG = (colorValue shr 8) and 0xff
                val startB = colorValue and 0xff
                return (startA / 3 shl 24) or (startR shl 16) or (startG shl 8) or startB
            }

            private fun twoThirdAlphaColor(colorValue: Int): Int {
                val startA = (colorValue shr 24) and 0xff
                val startR = (colorValue shr 16) and 0xff
                val startG = (colorValue shr 8) and 0xff
                val startB = colorValue and 0xff
                return (startA * 2 / 3 shl 24) or (startR shl 16) or (startG shl 8) or startB
            }
        }
    }

    init {
        mStrokeWidth = dip2px(context, DEFAULT_STROKE_WIDTH)
        mCenterRadius = dip2px(context, DEFAULT_CENTER_RADIUS)
        mLevelSwipeDegrees = FloatArray(3)
        mLevelColors = DEFAULT_LEVEL_COLORS
        setupPaint()
        addRenderListener(object : AnimatorListenerAdapter() {
            override fun onAnimationRepeat(animation: Animator) {
                super.onAnimationRepeat(animation)
                storeOriginals()
                mStartDegrees = mEndDegrees
                mRotationCount = (mRotationCount + 1) % (NUM_POINTS)
            }

            override fun onAnimationStart(animation: Animator) {
                super.onAnimationStart(animation)
                mRotationCount = 0f
            }
        })
    }

    private fun setupPaint() {
        mPaint.isAntiAlias = true
        mPaint.strokeWidth = mStrokeWidth
        mPaint.style = Paint.Style.STROKE
        mPaint.strokeCap = Paint.Cap.ROUND
        initStrokeInset(mWidth.toSafeFloat(), mHeight.toSafeFloat())
    }

    fun setCircleColors(r1: Int, r2: Int, r3: Int) {
        mLevelColors = intArrayOf(r1, r2, r3)
    }

    override fun draw(canvas: Canvas?) {
        super.draw(canvas)
        val saveCount = canvas?.save().orZero
        mTempBounds.set(mBounds)
        mTempBounds.inset(mStrokeInset, mStrokeInset)
        canvas?.rotate(mGroupRotation, mTempBounds.centerX(), mTempBounds.centerY())
        for (i in 0..2) {
            if (mLevelSwipeDegrees[i] != 0f) {
                mPaint.setColor(mLevelColors[i])
                canvas?.drawArc(mTempBounds, mEndDegrees, mLevelSwipeDegrees[i], false, mPaint)
            }
        }
        canvas?.restoreToCount(saveCount)
    }

    override fun computeRender(renderProgress: Float) {
        if (renderProgress <= START_TRIM_DURATION_OFFSET) {
            val startTrimProgress = (renderProgress) / START_TRIM_DURATION_OFFSET
            mStartDegrees = mOriginStartDegrees + MAX_SWIPE_DEGREES * MATERIAL_INTERPOLATOR.getInterpolation(startTrimProgress)
            val mSwipeDegrees = mEndDegrees - mStartDegrees
            val levelSwipeDegreesProgress = abs(mSwipeDegrees) / MAX_SWIPE_DEGREES
            val level1Increment = DECELERATE_INTERPOLATOR.getInterpolation(levelSwipeDegreesProgress) - LINEAR_INTERPOLATOR.getInterpolation(levelSwipeDegreesProgress)
            val level3Increment = ACCELERATE_INTERPOLATOR.getInterpolation(levelSwipeDegreesProgress) - LINEAR_INTERPOLATOR.getInterpolation(levelSwipeDegreesProgress)
            mLevelSwipeDegrees[0] = -mSwipeDegrees * LEVEL_SWEEP_ANGLE_OFFSETS[0] * (1.0f + level1Increment)
            mLevelSwipeDegrees[1] = -mSwipeDegrees * LEVEL_SWEEP_ANGLE_OFFSETS[1] * 1.0f
            mLevelSwipeDegrees[2] = -mSwipeDegrees * LEVEL_SWEEP_ANGLE_OFFSETS[2] * (1.0f + level3Increment)
        }
        if (renderProgress > START_TRIM_DURATION_OFFSET) {
            val endTrimProgress = (renderProgress - START_TRIM_DURATION_OFFSET) / (END_TRIM_DURATION_OFFSET - START_TRIM_DURATION_OFFSET)
            mEndDegrees = mOriginEndDegrees + MAX_SWIPE_DEGREES * MATERIAL_INTERPOLATOR.getInterpolation(endTrimProgress)
            val mSwipeDegrees = mEndDegrees - mStartDegrees
            val levelSwipeDegreesProgress = abs(mSwipeDegrees) / MAX_SWIPE_DEGREES
            if (levelSwipeDegreesProgress > LEVEL_SWEEP_ANGLE_OFFSETS[1]) {
                mLevelSwipeDegrees[0] = -mSwipeDegrees
                mLevelSwipeDegrees[1] = MAX_SWIPE_DEGREES * LEVEL_SWEEP_ANGLE_OFFSETS[1]
                mLevelSwipeDegrees[2] = MAX_SWIPE_DEGREES * LEVEL_SWEEP_ANGLE_OFFSETS[2]
            } else if (levelSwipeDegreesProgress > LEVEL_SWEEP_ANGLE_OFFSETS[2]) {
                mLevelSwipeDegrees[0] = 0f
                mLevelSwipeDegrees[1] = -mSwipeDegrees
                mLevelSwipeDegrees[2] = MAX_SWIPE_DEGREES * LEVEL_SWEEP_ANGLE_OFFSETS[2]
            } else {
                mLevelSwipeDegrees[0] = 0f
                mLevelSwipeDegrees[1] = 0f
                mLevelSwipeDegrees[2] = -mSwipeDegrees
            }
        }
        mGroupRotation = ((FULL_GROUP_ROTATION / NUM_POINTS) * renderProgress) + (FULL_GROUP_ROTATION * (mRotationCount / NUM_POINTS))
    }

    override fun setAlpha(alpha: Int) {
        mPaint.setAlpha(alpha)
    }

    override fun setColorFilter(cf: ColorFilter?) {
        mPaint.setColorFilter(cf)
    }

    override fun reset() {
        resetOriginals()
    }

    private fun initStrokeInset(width: Float, height: Float) {
        val minSize = min(width, height)
        val strokeInset = minSize / 2.0f - mCenterRadius
        val minStrokeInset = ceil((mStrokeWidth / 2.0f).toSafeDouble()).toSafeFloat()
        mStrokeInset = max(strokeInset, minStrokeInset)
    }

    private fun storeOriginals() {
        mOriginEndDegrees = mEndDegrees
        mOriginStartDegrees = mEndDegrees
    }

    private fun resetOriginals() {
        mOriginEndDegrees = 0f
        mOriginStartDegrees = 0f
        mEndDegrees = 0f
        mStartDegrees = 0f
        mLevelSwipeDegrees[0] = 0f
        mLevelSwipeDegrees[1] = 0f
        mLevelSwipeDegrees[2] = 0f
    }

    private fun apply(builder: Builder) {
        this.mWidth = if (builder.mWidth > 0) builder.mWidth.toSafeFloat() else this.mWidth
        this.mHeight = if (builder.mHeight > 0) builder.mHeight.toSafeFloat() else this.mHeight
        this.mStrokeWidth = if (builder.mStrokeWidth > 0) builder.mStrokeWidth.toSafeFloat() else this.mStrokeWidth
        this.mCenterRadius = if (builder.mCenterRadius > 0) builder.mCenterRadius.toSafeFloat() else this.mCenterRadius
        this.mDuration = if (builder.mDuration > 0) builder.mDuration.toSafeLong() else this.mDuration
        this.mLevelColors = builder.mLevelColors
        setupPaint()
        initStrokeInset(this.mWidth, this.mHeight)
    }

}