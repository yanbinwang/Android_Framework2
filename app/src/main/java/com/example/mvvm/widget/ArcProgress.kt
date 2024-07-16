package com.example.mvvm.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.os.Bundle
import android.os.Parcelable
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import com.example.common.utils.function.color
import com.example.common.utils.function.pt
import com.example.common.utils.function.ptFloat
import com.example.mvvm.R
import kotlin.math.cos

class ArcProgress @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : View(context, attrs, defStyleAttr) {
    private var textColor = 0
    private var progress = 0
    private var max = 0
    private var min_size = 0
    private var finishedStrokeColor = 0
    private var unfinishedStrokeColor = 0
    private var strokeWidth = 0f
    private var suffixTextSize = 0f
    private var bottomTextSize = 0f
    private var arcAngle = 0f
    private var textSize = 0f
    private var suffixTextPadding = 0f
    private var arcBottomHeight = 0f
    private var default_text_size = 0f
    private var default_suffix_padding = 0f
    private var default_bottom_text_size = 0f
    private var default_stroke_width = 0f
    private var default_suffix_text_size = 0f
    private var suffixText = "%"
    private var bottomText = ""
    private val default_max = 100
    private val default_arc_angle = 360 * 0.7f //0.8改0.7
    private val default_suffix_text = "%"
    private val default_finished_color = Color.WHITE
    private val default_unfinished_color = Color.rgb(72, 106, 176)
    private val default_text_color = Color.rgb(66, 145, 241)
    private val rectF = RectF()
    private val paint by lazy { Paint() }
    private val textPaint by lazy { TextPaint() }

    companion object {
        private const val INSTANCE_STATE = "saved_instance"
        private const val INSTANCE_STROKE_WIDTH = "stroke_width"
        private const val INSTANCE_SUFFIX_TEXT_SIZE = "suffix_text_size"
        private const val INSTANCE_SUFFIX_TEXT_PADDING = "suffix_text_padding"
        private const val INSTANCE_BOTTOM_TEXT_SIZE = "bottom_text_size"
        private const val INSTANCE_BOTTOM_TEXT = "bottom_text"
        private const val INSTANCE_TEXT_SIZE = "text_size"
        private const val INSTANCE_TEXT_COLOR = "text_color"
        private const val INSTANCE_PROGRESS = "progress"
        private const val INSTANCE_MAX = "max"
        private const val INSTANCE_FINISHED_STROKE_COLOR = "finished_stroke_color"
        private const val INSTANCE_UNFINISHED_STROKE_COLOR = "unfinished_stroke_color"
        private const val INSTANCE_ARC_ANGLE = "arc_angle"
        private const val INSTANCE_SUFFIX = "suffix"
    }

    init {
        //初始化默认属性
        default_text_size = 18.ptFloat
        min_size = 100.pt
        default_text_size = 40.ptFloat
        default_suffix_text_size = 15.ptFloat
        default_suffix_padding = 0.ptFloat
        default_bottom_text_size = 10.ptFloat
        default_stroke_width = 4.ptFloat
        //初始化配置属性
        val attributes = context.obtainStyledAttributes(attrs, R.styleable.ArcProgress)
        finishedStrokeColor = attributes.getColor(R.styleable.ArcProgress_arc_finished_color, default_finished_color)
        unfinishedStrokeColor = attributes.getColor(R.styleable.ArcProgress_arc_unfinished_color, default_unfinished_color)
        textColor = attributes.getColor(R.styleable.ArcProgress_arc_text_color, default_text_color)
        textSize = attributes.getDimension(R.styleable.ArcProgress_arc_text_size, default_text_size)
        arcAngle = attributes.getFloat(R.styleable.ArcProgress_arc_angle, default_arc_angle)
        setMax(attributes.getInt(R.styleable.ArcProgress_arc_max, default_max))
        setProgress(attributes.getInt(R.styleable.ArcProgress_arc_progress, 0))
        strokeWidth = attributes.getDimension(R.styleable.ArcProgress_arc_stroke_width, default_stroke_width)
        suffixTextSize = attributes.getDimension(R.styleable.ArcProgress_arc_suffix_text_size, default_suffix_text_size)
        suffixText = attributes.getString(R.styleable.ArcProgress_arc_suffix_text).let { if (it.isNullOrEmpty()) default_suffix_text else it }
        suffixTextPadding = attributes.getDimension(R.styleable.ArcProgress_arc_suffix_text_padding, default_suffix_padding)
        bottomTextSize = attributes.getDimension(R.styleable.ArcProgress_arc_bottom_text_size, default_bottom_text_size)
        bottomText = attributes.getString(R.styleable.ArcProgress_arc_bottom_text).orEmpty()
        attributes.recycle()
        //初始化外侧及文字画笔
        initPainters()
    }

    override fun invalidate() {
        initPainters()
        super.invalidate()
    }

    private fun initPainters() {
        textPaint.setColor(textColor)
        textPaint.textSize = textSize
        textPaint.isAntiAlias = true
        paint.color = default_unfinished_color
        paint.isAntiAlias = true
        paint.strokeWidth = strokeWidth
        paint.style = Paint.Style.STROKE
        paint.strokeCap = Paint.Cap.ROUND
    }

    override fun getSuggestedMinimumHeight(): Int {
        return min_size
    }

    override fun getSuggestedMinimumWidth(): Int {
        return min_size
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(widthMeasureSpec, heightMeasureSpec)
        val width = MeasureSpec.getSize(widthMeasureSpec)
        rectF[strokeWidth / 2f, strokeWidth / 2f, width - strokeWidth / 2f] = MeasureSpec.getSize(heightMeasureSpec) - strokeWidth / 2f
        val radius = width / 2f
        val angle = (360 - arcAngle) / 2f
        arcBottomHeight = radius * (1 - cos(angle / 180 * Math.PI)).toFloat()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val startAngle = 270 - arcAngle / 2f
        val finishedSweepAngle = progress / getMax().toFloat() * arcAngle
        var finishedStartAngle = startAngle
        if (progress == 0) finishedStartAngle = 0.01f
        paint.color = unfinishedStrokeColor
        canvas.drawArc(rectF, startAngle, arcAngle, false, paint)
        paint.color = finishedStrokeColor
        canvas.drawArc(rectF, finishedStartAngle, finishedSweepAngle, false, paint)
        val text = getProgress().toString()
        if (text.isNotEmpty()) {
            textPaint.color = textColor
            textPaint.textSize = textSize
            val textHeight = textPaint.descent() + textPaint.ascent()
            val textBaseline = (height - textHeight) / 2.0f - 5.ptFloat
            canvas.drawText(text, (width - textPaint.measureText(text)) / 2.0f - 5.ptFloat, textBaseline, textPaint)
            textPaint.textSize = suffixTextSize
            val suffixHeight = textPaint.descent() + textPaint.ascent()
            canvas.drawText(suffixText, width / 2.0f + textPaint.measureText(text) / 2.0f - 4.ptFloat, textBaseline + textHeight - suffixHeight + 2.ptFloat, textPaint) //注释掉边距
        }
        if (arcBottomHeight == 0f) {
            val radius = width / 2f
            val angle = (360 - arcAngle) / 2f
            arcBottomHeight = radius * (1 - cos(angle / 180 * Math.PI)).toFloat()
        }
        if (getBottomText().isNotEmpty()) {
            textPaint.textSize = bottomTextSize
            textPaint.color = color(R.color.textSecondary)
            val bottomTextBaseline = height - arcBottomHeight - (textPaint.descent() + textPaint.ascent()) / 2 - 5.ptFloat
            canvas.drawText(getBottomText(), (width - textPaint.measureText(getBottomText())) / 2.0f, bottomTextBaseline, textPaint)
        }
    }

    override fun onSaveInstanceState(): Parcelable {
        val bundle = Bundle()
        bundle.putParcelable(INSTANCE_STATE, super.onSaveInstanceState())
        bundle.putFloat(INSTANCE_STROKE_WIDTH, getStrokeWidth())
        bundle.putFloat(INSTANCE_SUFFIX_TEXT_SIZE, getSuffixTextSize())
        bundle.putFloat(INSTANCE_SUFFIX_TEXT_PADDING, getSuffixTextPadding())
        bundle.putFloat(INSTANCE_BOTTOM_TEXT_SIZE, getBottomTextSize())
        bundle.putString(INSTANCE_BOTTOM_TEXT, getBottomText())
        bundle.putFloat(INSTANCE_TEXT_SIZE, getTextSize())
        bundle.putInt(INSTANCE_TEXT_COLOR, getTextColor())
        bundle.putInt(INSTANCE_PROGRESS, getProgress())
        bundle.putInt(INSTANCE_MAX, getMax())
        bundle.putInt(INSTANCE_FINISHED_STROKE_COLOR, getFinishedStrokeColor())
        bundle.putInt(INSTANCE_UNFINISHED_STROKE_COLOR, getUnfinishedStrokeColor())
        bundle.putFloat(INSTANCE_ARC_ANGLE, getArcAngle())
        bundle.putString(INSTANCE_SUFFIX, getSuffixText())
        return bundle
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state is Bundle) {
            strokeWidth = state.getFloat(INSTANCE_STROKE_WIDTH)
            suffixTextSize = state.getFloat(INSTANCE_SUFFIX_TEXT_SIZE)
            suffixTextPadding = state.getFloat(INSTANCE_SUFFIX_TEXT_PADDING)
            bottomTextSize = state.getFloat(INSTANCE_BOTTOM_TEXT_SIZE)
            bottomText = state.getString(INSTANCE_BOTTOM_TEXT)!!
            textSize = state.getFloat(INSTANCE_TEXT_SIZE)
            textColor = state.getInt(INSTANCE_TEXT_COLOR)
            setMax(state.getInt(INSTANCE_MAX))
            setProgress(state.getInt(INSTANCE_PROGRESS))
            finishedStrokeColor = state.getInt(INSTANCE_FINISHED_STROKE_COLOR)
            unfinishedStrokeColor = state.getInt(INSTANCE_UNFINISHED_STROKE_COLOR)
            suffixText = state.getString(INSTANCE_SUFFIX)!!
            initPainters()
            super.onRestoreInstanceState(state.getParcelable(INSTANCE_STATE))
            return
        }
        super.onRestoreInstanceState(state)
    }

    fun getStrokeWidth(): Float {
        return strokeWidth
    }

    fun setStrokeWidth(strokeWidth: Float) {
        this.strokeWidth = strokeWidth
        this.invalidate()
    }

    fun getSuffixTextSize(): Float {
        return suffixTextSize
    }

    fun setSuffixTextSize(suffixTextSize: Float) {
        this.suffixTextSize = suffixTextSize
        this.invalidate()
    }

    fun getBottomText(): String {
        return bottomText
    }

    fun setBottomText(bottomText: String?) {
        this.bottomText = bottomText!!
        this.invalidate()
    }

    fun getProgress(): Int {
        return progress
    }

    fun setProgress(progress: Int) {
        this.progress = progress
        if (this.progress > getMax()) {
            this.progress %= getMax()
        }
        invalidate()
    }

    fun getMax(): Int {
        return max
    }

    fun setMax(max: Int) {
        if (max > 0) {
            this.max = max
            invalidate()
        }
    }

    fun getBottomTextSize(): Float {
        return bottomTextSize
    }

    fun setBottomTextSize(bottomTextSize: Float) {
        this.bottomTextSize = bottomTextSize
        this.invalidate()
    }

    fun getTextSize(): Float {
        return textSize
    }

    fun setTextSize(textSize: Float) {
        this.textSize = textSize
        this.invalidate()
    }

    fun getTextColor(): Int {
        return textColor
    }

    fun setTextColor(textColor: Int) {
        this.textColor = textColor
        this.invalidate()
    }

    fun getFinishedStrokeColor(): Int {
        return finishedStrokeColor
    }

    fun setFinishedStrokeColor(finishedStrokeColor: Int) {
        this.finishedStrokeColor = finishedStrokeColor
        this.invalidate()
    }

    fun getUnfinishedStrokeColor(): Int {
        return unfinishedStrokeColor
    }

    fun setUnfinishedStrokeColor(unfinishedStrokeColor: Int) {
        this.unfinishedStrokeColor = unfinishedStrokeColor
        this.invalidate()
    }

    fun getArcAngle(): Float {
        return arcAngle
    }

    fun setArcAngle(arcAngle: Float) {
        this.arcAngle = arcAngle
        this.invalidate()
    }

    fun getSuffixText(): String {
        return suffixText
    }

    fun setSuffixText(suffixText: String?) {
        this.suffixText = suffixText!!
        this.invalidate()
    }

    fun getSuffixTextPadding(): Float {
        return suffixTextPadding
    }

    fun setSuffixTextPadding(suffixTextPadding: Float) {
        this.suffixTextPadding = suffixTextPadding
        this.invalidate()
    }

}