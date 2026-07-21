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
import androidx.annotation.ColorInt
import com.example.common.utils.function.color
import com.example.common.utils.function.pt
import com.example.common.utils.function.ptFloat
import com.example.mvvm.R
import kotlin.math.cos
import androidx.core.content.withStyledAttributes

/**
 * 进度条
 * <com.example.evidence.widget.ArcProgress
 *   android:id="@+id/arc_progress"
 *   android:layout_width="66pt"
 *   android:layout_height="66pt"
 *   android:layout_marginBottom="2pt"
 *   app:arc_bottom_text="已上传进度"
 *   app:arc_bottom_text_size="@dimen/textSize8"
 *   app:arc_finished_color="@color/textBlue"
 *   app:arc_max="100"
 *   app:arc_progress="0"
 *   app:arc_text_color="@color/textBlue"
 *   app:arc_text_size="@dimen/textSize20"
 *   app:arc_unfinished_color="@color/textHint" />
 */
class ArcProgress @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : View(context, attrs, defStyleAttr) {
    private var max = 0
    private var progress = 0
    private var textColor = 0
    private var finishedStrokeColor = 0
    private var unfinishedStrokeColor = 0
    private var strokeWidth = 0f
    private var suffixTextSize = 0f
    private var bottomTextSize = 0f
    private var arcAngle = 0f
    private var textSize = 0f
    private var suffixTextPadding = 0f
    private var arcBottomHeight = 0f
    private var suffixText = "%"
    private var bottomText = ""
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

        private var min_size = 100.pt
        private var default_text_size = 40.ptFloat
        private var default_suffix_padding = 0.ptFloat
        private var default_bottom_text_size = 10.ptFloat
        private var default_stroke_width = 4.ptFloat
        private var default_suffix_text_size = 15.ptFloat
        private val default_max = 100
        private val default_arc_angle = 360 * 0.7f // 180° 240° 270° -> 由设计图决定
        private val default_suffix_text = "%"
        private val default_finished_color = Color.WHITE
        private val default_unfinished_color = Color.rgb(72, 106, 176)
        private val default_text_color = Color.rgb(66, 145, 241)
    }

    /**
     * 初始化块：从 XML 属性中读取自定义样式并设置默认值，
     * 完成所有绘制属性的初始化后调用 initPainters() 配置画笔。
     */
    init {
        // 初始化配置属性
        context.withStyledAttributes(attrs, R.styleable.ArcProgress) {
            finishedStrokeColor = getColor(R.styleable.ArcProgress_arc_finished_color, default_finished_color)
            unfinishedStrokeColor = getColor(R.styleable.ArcProgress_arc_unfinished_color, default_unfinished_color)
            textColor = getColor(R.styleable.ArcProgress_arc_text_color, default_text_color)
            textSize = getDimension(R.styleable.ArcProgress_arc_text_size, default_text_size)
            arcAngle = getFloat(R.styleable.ArcProgress_arc_angle, default_arc_angle)
            setMax(getInt(R.styleable.ArcProgress_arc_max, default_max))
            setProgress(getInt(R.styleable.ArcProgress_arc_progress, 0))
            strokeWidth = getDimension(R.styleable.ArcProgress_arc_stroke_width, default_stroke_width)
            suffixTextSize = getDimension(R.styleable.ArcProgress_arc_suffix_text_size, default_suffix_text_size)
            suffixText = getString(R.styleable.ArcProgress_arc_suffix_text).let { if (it.isNullOrEmpty()) default_suffix_text else it }
            suffixTextPadding = getDimension(R.styleable.ArcProgress_arc_suffix_text_padding, default_suffix_padding)
            bottomTextSize = getDimension(R.styleable.ArcProgress_arc_bottom_text_size, default_bottom_text_size)
            bottomText = getString(R.styleable.ArcProgress_arc_bottom_text).orEmpty()
        }
        // 初始化外侧及文字画笔
        initPainters()
    }

//    /**
//     * 重写 invalidate：每次视图需要重绘时重新初始化画笔参数，
//     * 确保画笔状态与当前属性值保持同步，然后调用父类刷新。
//     */
//    override fun invalidate() {
//        initPainters()
//        super.invalidate()
//    }

    /**
     * 初始化画笔：配置文字画笔（颜色、字号、抗锯齿）和
     * 圆弧画笔（颜色、线宽、描边样式、圆头端点）。
     */
    private fun initPainters() {
        textPaint.color = textColor
        textPaint.textSize = textSize
        textPaint.isAntiAlias = true
        paint.color = unfinishedStrokeColor
        paint.isAntiAlias = true
        paint.strokeWidth = strokeWidth
        paint.style = Paint.Style.STROKE
        paint.strokeCap = Paint.Cap.ROUND
    }

    /**
     * 返回建议的最小高度，保证控件在 wrap_content 时不会过小。
     */
    override fun getSuggestedMinimumHeight(): Int {
        return min_size
    }

    /**
     * 返回建议的最小宽度，保证控件在 wrap_content 时不会过小。
     */
    override fun getSuggestedMinimumWidth(): Int {
        return min_size
    }

    /**
     * 测量控件尺寸：根据 MeasureSpec 确定最终宽高，
     * 计算圆弧绘制区域 rectF、半径以及底部文字基线偏移量 arcBottomHeight。
     */
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(widthMeasureSpec, heightMeasureSpec)
        val width = MeasureSpec.getSize(widthMeasureSpec)
        rectF[strokeWidth / 2f, strokeWidth / 2f, width - strokeWidth / 2f] = MeasureSpec.getSize(heightMeasureSpec) - strokeWidth / 2f
        val radius = width / 2f
        val angle = (360 - arcAngle) / 2f
        arcBottomHeight = radius * (1 - cos(angle / 180 * Math.PI)).toFloat()
    }

    /**
     * 绘制控件：依次绘制未完成圆弧、已完成圆弧、进度数字、
     * 后缀文本（如 "%"）以及底部描述文字。
     */
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
        // 硬编码的魔法数字偏移量 -> -5.ptFloat、-4.ptFloat + 2.ptFloat 是为了视觉对齐的手动补偿
        if (text.isNotEmpty()) {
            textPaint.color = textColor
            textPaint.textSize = textSize
            val textHeight = textPaint.descent() + textPaint.ascent()
            val textBaseline = (height - textHeight) / 2.0f - 5.ptFloat
            canvas.drawText(text, (width - textPaint.measureText(text)) / 2.0f - 5.ptFloat, textBaseline, textPaint)
            textPaint.textSize = suffixTextSize
            val suffixHeight = textPaint.descent() + textPaint.ascent()
            canvas.drawText(suffixText, width / 2.0f + textPaint.measureText(text) / 2.0f - 4.ptFloat, textBaseline + textHeight - suffixHeight + 2.ptFloat, textPaint)
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

    /**
     * 保存实例状态：将当前所有可恢复属性打包到 Bundle 中，
     * 并将父类的 Parcelable 状态嵌套保存，用于配置变更或进程重建时恢复。
     */
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

    /**
     * 恢复实例状态：从 Bundle 中还原所有属性值并重新初始化画笔；
     * 若传入的不是 Bundle 则直接委托给父类处理。
     */
    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state is Bundle) {
            strokeWidth = state.getFloat(INSTANCE_STROKE_WIDTH)
            suffixTextSize = state.getFloat(INSTANCE_SUFFIX_TEXT_SIZE)
            suffixTextPadding = state.getFloat(INSTANCE_SUFFIX_TEXT_PADDING)
            bottomTextSize = state.getFloat(INSTANCE_BOTTOM_TEXT_SIZE)
            bottomText = state.getString(INSTANCE_BOTTOM_TEXT).orEmpty()
            textSize = state.getFloat(INSTANCE_TEXT_SIZE)
            textColor = state.getInt(INSTANCE_TEXT_COLOR)
            setProgress(state.getInt(INSTANCE_PROGRESS))
            setMax(state.getInt(INSTANCE_MAX))
            finishedStrokeColor = state.getInt(INSTANCE_FINISHED_STROKE_COLOR)
            unfinishedStrokeColor = state.getInt(INSTANCE_UNFINISHED_STROKE_COLOR)
            arcAngle = state.getFloat(INSTANCE_ARC_ANGLE)
            suffixText = state.getString(INSTANCE_SUFFIX).orEmpty()
            initPainters()
            super.onRestoreInstanceState(state.getParcelable(INSTANCE_STATE))
            return
        }
        super.onRestoreInstanceState(state)
    }

    /**
     * 获取当前圆弧描边宽度/设置圆弧描边宽度并触发重绘
     */
    fun getStrokeWidth(): Float {
        return strokeWidth
    }

    fun setStrokeWidth(strokeWidth: Float) {
        this.strokeWidth = strokeWidth
        this.invalidate()
    }

    /**
     * 获取后缀文本（如 "%"）的字号/设置后缀文本字号并触发重绘
     */
    fun getSuffixTextSize(): Float {
        return suffixTextSize
    }

    fun setSuffixTextSize(suffixTextSize: Float) {
        this.suffixTextSize = suffixTextSize
        this.invalidate()
    }

    /**
     * 获取底部描述文字内容/设置底部描述文字并触发重绘
     */
    fun getBottomText(): String {
        return bottomText
    }

    fun setBottomText(bottomText: String) {
        this.bottomText = bottomText
        this.invalidate()
    }

    /**
     * 获取当前进度值/设置当前进度值；若超过最大值则对最大值取模,设置完成后触发重绘。
     */
    fun getProgress(): Int {
        return progress
    }

    fun setProgress(progress: Int) {
        // 钳制到 [0, max] 区间：小于0归零，大于max封顶
        this.progress = progress.coerceIn(0, getMax())
        invalidate()
    }

    /**
     * 获取进度最大值/设置进度最大值（仅当大于 0 时生效）并触发重绘。
     */
    fun getMax(): Int {
        return max
    }

    fun setMax(max: Int) {
        if (max > 0) {
            this.max = max
            invalidate()
        }
    }

    /**
     * 获取底部描述文字的字号/设置底部描述文字字号并触发重绘
     */
    fun getBottomTextSize(): Float {
        return bottomTextSize
    }

    fun setBottomTextSize(bottomTextSize: Float) {
        this.bottomTextSize = bottomTextSize
        this.invalidate()
    }

    /**
     * 获取进度数字的字号设置进度数字字号并触发重绘
     */
    fun getTextSize(): Float {
        return textSize
    }

    fun setTextSize(textSize: Float) {
        this.textSize = textSize
        this.invalidate()
    }

    /**
     * 获取进度文字颜色/设置进度文字颜色并触发重绘
     */
    @ColorInt
    fun getTextColor(): Int {
        return textColor
    }

    fun setTextColor(@ColorInt textColor: Int) {
        this.textColor = textColor
        this.invalidate()
    }

    /**
     * 获取已完成圆弧的颜色/设置已完成圆弧颜色并触发重绘
     */
    @ColorInt
    fun getFinishedStrokeColor(): Int {
        return finishedStrokeColor
    }

    fun setFinishedStrokeColor(@ColorInt finishedStrokeColor: Int) {
        this.finishedStrokeColor = finishedStrokeColor
        this.invalidate()
    }

    /**
     * 获取未完成圆弧的颜色/设置未完成圆弧颜色并触发重绘
     */
    @ColorInt
    fun getUnfinishedStrokeColor(): Int {
        return unfinishedStrokeColor
    }

    fun setUnfinishedStrokeColor(@ColorInt unfinishedStrokeColor: Int) {
        this.unfinishedStrokeColor = unfinishedStrokeColor
        this.invalidate()
    }

    /**
     * 获取圆弧扫过的角度（度数）/设置圆弧扫过角度并触发重绘
     */
    fun getArcAngle(): Float {
        return arcAngle
    }

    fun setArcAngle(arcAngle: Float) {
        this.arcAngle = arcAngle
        this.invalidate()
    }

    /**
     * 获取后缀文本内容（如 "%"）/设置后缀文本内容并触发重绘。
     */
    fun getSuffixText(): String {
        return suffixText
    }

    fun setSuffixText(suffixText: String) {
        this.suffixText = suffixText
        this.invalidate()
    }

    /**
     * 获取后缀文本与进度数字之间的间距/设置后缀文本间距并触发重绘。
     */
    fun getSuffixTextPadding(): Float {
        return suffixTextPadding
    }

    fun setSuffixTextPadding(suffixTextPadding: Float) {
        this.suffixTextPadding = suffixTextPadding
        this.invalidate()
    }

}