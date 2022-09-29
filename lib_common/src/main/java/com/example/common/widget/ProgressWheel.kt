package com.example.common.widget

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.Creator
import android.os.SystemClock
import android.provider.Settings
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import com.example.common.R

/**
 * @description
 * @author
 */
class ProgressWheel : View {
    private var barWidth = 4
    private var rimWidth = 4
    private var circleRadius = 28
    private var barColor = -0x56000000
    private var rimColor = 0x00FFFFFF
    private var lastTimeAnimated = 0L
    private var pausedTimeWithoutGrowing = 0L
    private var mProgress = 0.0f
    private var mTargetProgress = 0.0f
    private var spinSpeed = 230.0f
    private var barExtraLength = 0f
    private var timeStartGrowing = 0.0
    private var barSpinCycleTime = 460.0
    private var shouldAnimate = false
    private var linearProgress = false
    private var fillRadius = false
    private var isSpinning = false
    private var barGrowingFromFront = true
    private var circleBounds = RectF()
    private val barLength = 16
    private val barMaxLength = 270
    private val pauseGrowingTime = 200L
    private val barPaint by lazy { Paint() }
    private val rimPaint by lazy { Paint() }
    var onProgressUpdate: ((progress: Float) -> Unit)? = null

    constructor(context: Context?) : super(context) {
        setAnimationEnabled()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        parseAttributes(context.obtainStyledAttributes(attrs, R.styleable.ProgressWheel))
        setAnimationEnabled()
    }

    private fun setAnimationEnabled() {
        val animationValue = Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f)
        shouldAnimate = animationValue != 0f
    }

    private fun parseAttributes(a: TypedArray) {
        // We transform the default values from DIP to pixels
        val metrics = context.resources.displayMetrics
        barWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, barWidth.toFloat(), metrics).toInt()
        rimWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, rimWidth.toFloat(), metrics).toInt()
        circleRadius = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, circleRadius.toFloat(), metrics).toInt()
        circleRadius = a.getDimension(R.styleable.ProgressWheel_matProg_circleRadius, circleRadius.toFloat()).toInt()
        fillRadius = a.getBoolean(R.styleable.ProgressWheel_matProg_fillRadius, false)
        barWidth = a.getDimension(R.styleable.ProgressWheel_matProg_barWidth, barWidth.toFloat()).toInt()
        rimWidth = a.getDimension(R.styleable.ProgressWheel_matProg_rimWidth, rimWidth.toFloat()).toInt()
        val baseSpinSpeed = a.getFloat(R.styleable.ProgressWheel_matProg_spinSpeed, spinSpeed / 360.0f)
        spinSpeed = baseSpinSpeed * 360
        barSpinCycleTime = a.getInt(R.styleable.ProgressWheel_matProg_barSpinCycleTime, barSpinCycleTime.toInt()).toDouble()
        barColor = a.getColor(R.styleable.ProgressWheel_matProg_barColor, barColor)
        rimColor = a.getColor(R.styleable.ProgressWheel_matProg_rimColor, rimColor)
        linearProgress = a.getBoolean(R.styleable.ProgressWheel_matProg_linearProgress, false)
        if (a.getBoolean(R.styleable.ProgressWheel_matProg_progressIndeterminate, false)) spin()
        // Recycle
        a.recycle()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val viewWidth = circleRadius + this.paddingLeft + this.paddingRight
        val viewHeight = circleRadius + this.paddingTop + this.paddingBottom
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
        //Measure Width
        val width = when (widthMode) {
            //Must be this size
            MeasureSpec.EXACTLY -> widthSize
            //Can't be bigger than...
            MeasureSpec.AT_MOST -> Math.min(viewWidth, widthSize)
            //Be whatever you want
            else -> viewWidth
        }
        //Measure Height
        val height = if (heightMode == MeasureSpec.EXACTLY || widthMode == MeasureSpec.EXACTLY) {
            //Must be this size
            heightSize
        } else if (heightMode == MeasureSpec.AT_MOST) {
            //Can't be bigger than...
            Math.min(viewHeight, heightSize)
        } else {
            //Be whatever you want
            viewHeight
        }
        setMeasuredDimension(width, height)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.drawArc(circleBounds, 360f, 360f, false, rimPaint)
        var mustInvalidate = false
        if (!shouldAnimate) return

        if (isSpinning) {
            //Draw the spinning bar
            mustInvalidate = true
            val deltaTime = SystemClock.uptimeMillis() - lastTimeAnimated
            val deltaNormalized = deltaTime * spinSpeed / 1000.0f
            updateBarLength(deltaTime)
            mProgress += deltaNormalized
            if (mProgress > 360) {
                mProgress -= 360f
                // A full turn has been completed
                // we run the callback with -1 in case we want to
                // do something, like changing the color
                runCallback(-1.0f)
            }
            lastTimeAnimated = SystemClock.uptimeMillis()
            var from = mProgress - 90
            var length = barLength + barExtraLength
            if (isInEditMode) {
                from = 0f
                length = 135f
            }
            canvas?.drawArc(circleBounds, from, length, false, barPaint)
        } else {
            val oldProgress = mProgress
            if (mProgress != mTargetProgress) {
                //We smoothly increase the progress bar
                mustInvalidate = true
                val deltaTime = (SystemClock.uptimeMillis() - lastTimeAnimated).toFloat() / 1000
                val deltaNormalized = deltaTime * spinSpeed
                mProgress = Math.min(mProgress + deltaNormalized, mTargetProgress)
                lastTimeAnimated = SystemClock.uptimeMillis()
            }
            if (oldProgress != mProgress) {
                runCallback()
            }
            var offset = 0.0f
            var progress = mProgress
            if (!linearProgress) {
                val factor = 2.0f
                offset = (1.0f - Math.pow((1.0f - mProgress / 360.0f).toDouble(), (2.0f * factor).toDouble())).toFloat() * 360.0f
                progress = (1.0f - Math.pow((1.0f - mProgress / 360.0f).toDouble(), factor.toDouble())).toFloat() * 360.0f
            }
            if (isInEditMode) progress = 360f
            canvas?.drawArc(circleBounds, offset - 90, progress, false, barPaint)
        }
        if (mustInvalidate) {
            invalidate()
        }
    }

    private fun updateBarLength(deltaTimeInMilliSeconds: Long) {
        if (pausedTimeWithoutGrowing >= pauseGrowingTime) {
            timeStartGrowing += deltaTimeInMilliSeconds.toDouble()
            if (timeStartGrowing > barSpinCycleTime) {
                // We completed a size change cycle
                // (growing or shrinking)
                timeStartGrowing -= barSpinCycleTime
                //if(barGrowingFromFront) {
                pausedTimeWithoutGrowing = 0
                //}
                barGrowingFromFront = !barGrowingFromFront
            }
            val distance = Math.cos((timeStartGrowing / barSpinCycleTime + 1) * Math.PI).toFloat() / 2 + 0.5f
            val destLength = (barMaxLength - barLength).toFloat()
            if (barGrowingFromFront) {
                barExtraLength = distance * destLength
            } else {
                val newLength = destLength * (1 - distance)
                mProgress += barExtraLength - newLength
                barExtraLength = newLength
            }
        } else {
            pausedTimeWithoutGrowing += deltaTimeInMilliSeconds
        }
    }

    private fun runCallback() {
        val normalizedProgress = Math.round(mProgress * 100 / 360.0f).toFloat() / 100
        onProgressUpdate?.invoke(normalizedProgress)
    }

    private fun runCallback(value: Float) {
        onProgressUpdate?.invoke(value)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        setupBounds(w, h)
        setupPaints()
        invalidate()
    }

    private fun setupBounds(layout_width: Int, layout_height: Int) {
        val paddingTop = paddingTop
        val paddingBottom = paddingBottom
        val paddingLeft = paddingLeft
        val paddingRight = paddingRight
        circleBounds = if (!fillRadius) {
            // Width should equal to Height, find the min value to setup the circle
            val minValue = Math.min(layout_width - paddingLeft - paddingRight, layout_height - paddingBottom - paddingTop)
            val circleDiameter = Math.min(minValue, circleRadius * 2 - barWidth * 2)
            // Calc the Offset if needed for centering the wheel in the available space
            val xOffset = (layout_width - paddingLeft - paddingRight - circleDiameter) / 2 + paddingLeft
            val yOffset = (layout_height - paddingTop - paddingBottom - circleDiameter) / 2 + paddingTop
            RectF(
                (xOffset + barWidth).toFloat(),
                (yOffset + barWidth).toFloat(),
                (xOffset + circleDiameter - barWidth).toFloat(),
                (yOffset + circleDiameter - barWidth).toFloat())
        } else {
            RectF(
                (paddingLeft + barWidth).toFloat(),
                (paddingTop + barWidth).toFloat(),
                (layout_width - paddingRight - barWidth).toFloat(),
                (layout_height - paddingBottom - barWidth).toFloat())
        }
    }

    private fun setupPaints() {
        barPaint.color = barColor
        barPaint.isAntiAlias = true
        barPaint.style = Paint.Style.STROKE
        barPaint.strokeWidth = barWidth.toFloat()
        rimPaint.color = rimColor
        rimPaint.isAntiAlias = true
        rimPaint.style = Paint.Style.STROKE
        rimPaint.strokeWidth = rimWidth.toFloat()
    }

    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)
        if (visibility == VISIBLE) lastTimeAnimated = SystemClock.uptimeMillis()
    }

    override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()
        val ss = WheelSavedState(superState!!)
        // We save everything that can be changed at runtime
        ss.mProgress = mProgress
        ss.mTargetProgress = mTargetProgress
        ss.isSpinning = isSpinning
        ss.spinSpeed = spinSpeed
        ss.barWidth = barWidth
        ss.barColor = barColor
        ss.rimWidth = rimWidth
        ss.rimColor = rimColor
        ss.circleRadius = circleRadius
        ss.linearProgress = linearProgress
        ss.fillRadius = fillRadius
        return ss
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state !is WheelSavedState) {
            super.onRestoreInstanceState(state)
            return
        }
        val ss = state
        super.onRestoreInstanceState(ss.superState)
        mProgress = ss.mProgress
        mTargetProgress = ss.mTargetProgress
        isSpinning = ss.isSpinning
        spinSpeed = ss.spinSpeed
        barWidth = ss.barWidth
        barColor = ss.barColor
        rimWidth = ss.rimWidth
        rimColor = ss.rimColor
        circleRadius = ss.circleRadius
        linearProgress = ss.linearProgress
        fillRadius = ss.fillRadius
        lastTimeAnimated = SystemClock.uptimeMillis()
    }

    fun setCallback() {
        if (!isSpinning) runCallback()
    }

    fun isSpinning(): Boolean {
        return isSpinning
    }

    fun resetCount() {
        mProgress = 0.0f
        mTargetProgress = 0.0f
        invalidate()
    }

    fun stopSpinning() {
        isSpinning = false
        mProgress = 0.0f
        mTargetProgress = 0.0f
        invalidate()
    }

    fun spin() {
        lastTimeAnimated = SystemClock.uptimeMillis()
        isSpinning = true
        invalidate()
    }

    fun setInstantProgress(progress: Float) {
        var progress = progress
        if (isSpinning) {
            mProgress = 0.0f
            isSpinning = false
        }
        if (progress > 1.0f) {
            progress -= 1.0f
        } else if (progress < 0) {
            progress = 0f
        }
        if (progress == mTargetProgress) {
            return
        }
        mTargetProgress = Math.min(progress * 360.0f, 360.0f)
        mProgress = mTargetProgress
        lastTimeAnimated = SystemClock.uptimeMillis()
        invalidate()
    }

    fun getProgress(): Float {
        return if (isSpinning) -1f else mProgress / 360.0f
    }

    fun setProgress(progress: Float) {
        var pro = progress
        if (isSpinning) {
            mProgress = 0.0f
            isSpinning = false
            runCallback()
        }
        if (pro > 1.0f) {
            pro -= 1.0f
        } else if (progress < 0) {
            pro = 0f
        }
        if (pro == mTargetProgress) return
        // If we are currently in the right position
        // we set again the last time animated so the
        // animation starts smooth from here
        if (mProgress == mTargetProgress) lastTimeAnimated = SystemClock.uptimeMillis()
        mTargetProgress = Math.min(pro * 360.0f, 360.0f)
        invalidate()
    }

    fun setLinearProgress(isLinear: Boolean) {
        linearProgress = isLinear
        if (!isSpinning) invalidate()
    }

    fun setBarWidth(barWidth: Int) {
        this.barWidth = barWidth
        if (!isSpinning) invalidate()
    }

    fun setRimWidth(rimWidth: Int) {
        this.rimWidth = rimWidth
        if (!isSpinning) invalidate()
    }

    fun setCircleRadius(circleRadius: Int) {
        this.circleRadius = circleRadius
        if (!isSpinning) invalidate()
    }

    fun setSpinSpeed(spinSpeed: Float) {
        this.spinSpeed = spinSpeed * 360.0f
    }

    fun setBarColor(barColor: Int) {
        this.barColor = barColor
        setupPaints()
        if (!isSpinning) invalidate()
    }

    fun setRimColor(rimColor: Int) {
        this.rimColor = rimColor
        setupPaints()
        if (!isSpinning) invalidate()
    }

    fun getBarWidth(): Int {
        return barWidth
    }

    fun getRimWidth(): Int {
        return rimWidth
    }

    fun getCircleRadius(): Int {
        return circleRadius
    }

    fun getSpinSpeed(): Float {
        return spinSpeed / 360.0f
    }

    fun getBarColor(): Int {
        return barColor
    }

    fun getRimColor(): Int {
        return rimColor
    }

    interface ProgressCallback {
        fun onProgressUpdate(progress: Float)
    }

    internal class WheelSavedState : BaseSavedState {
        var barColor = 0
        var barWidth = 0
        var rimColor = 0
        var rimWidth = 0
        var circleRadius = 0
        var spinSpeed = 0f
        var mProgress = 0f
        var mTargetProgress = 0f
        var isSpinning = false
        var fillRadius = false
        var linearProgress = false

        constructor(superState: Parcelable) : super(superState)

        constructor(parcel: Parcel) : super(parcel) {
            mProgress = parcel.readFloat()
            mTargetProgress = parcel.readFloat()
            isSpinning = parcel.readByte().toInt() != 0
            spinSpeed = parcel.readFloat()
            barWidth = parcel.readInt()
            barColor = parcel.readInt()
            rimWidth = parcel.readInt()
            rimColor = parcel.readInt()
            circleRadius = parcel.readInt()
            linearProgress = parcel.readByte().toInt() != 0
            fillRadius = parcel.readByte().toInt() != 0
        }

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            out.writeFloat(mProgress)
            out.writeFloat(mTargetProgress)
            out.writeByte((if (isSpinning) 1 else 0).toByte())
            out.writeFloat(spinSpeed)
            out.writeInt(barWidth)
            out.writeInt(barColor)
            out.writeInt(rimWidth)
            out.writeInt(rimColor)
            out.writeInt(circleRadius)
            out.writeByte((if (linearProgress) 1 else 0).toByte())
            out.writeByte((if (fillRadius) 1 else 0).toByte())
        }

        companion object {
            //required field that makes Parcelables from a Parcel
            @JvmField
            val CREATOR = object : Creator<WheelSavedState> {
                override fun createFromParcel(parcel: Parcel): WheelSavedState {
                    return WheelSavedState(parcel)
                }

                override fun newArray(size: Int): Array<WheelSavedState?> {
                    return arrayOfNulls(size)
                }
            }
        }
    }

}