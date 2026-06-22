package com.example.mvvm.widget

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Shader
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.withStyledAttributes
import androidx.core.graphics.createBitmap
import com.example.common.utils.function.ptFloat
import com.example.common.utils.function.safeRecycle
import com.example.mvvm.R
import kotlin.math.ceil
import kotlin.math.roundToInt

/**
 * 自定义评星控件
 * app:starCount="8"
 * app:starDistance="5dp"
 * app:starEmpty="@drawable/star_empty"
 * app:starFill="@drawable/star_full"
 * app:starSize="30dp"
 * app:integerOnly="true"
 */
@SuppressLint("ClickableViewAccessibility")
class RatingBar @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : View(context, attrs, defStyleAttr) {
    // 星星配置
    private var starDistance = 0 // 绘制的星星间距
    private var starSize = 0 // 星星高度大小，星星一般正方形，宽度等于高度
    private var starCount = 5 // 绘制的星星个数（默认5星）
    private var integerOnly = false // 是否显示的是整数的星星
    private var rating = 0.0f // 显示的星星的数量（用于获取）
    private var starFillBitmap: Bitmap? = null // 全星
    private var starEmptyDrawable: Drawable? = null // 空星
    // 绘制 & 事件
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG) // 绘制星星画笔
    private val touchBlocker = OnTouchListener { _: View?, _: MotionEvent? -> true } // 屏蔽星星的触摸事件
    private var onRatingChangedListener: OnRatingChangedListener? = null // 监听星星变化接口

    companion object {
        private fun drawableToBitmap(drawable: Drawable?, starSize: Int): Bitmap? {
            drawable ?: return null
            drawable.mutate()
            val bitmap = createBitmap(starSize, starSize)
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, starSize, starSize)
            drawable.draw(canvas)
            return bitmap
        }
    }

    init {
        context.withStyledAttributes(attrs, R.styleable.RatingBar) {
            starDistance = getDimension(R.styleable.RatingBar_starDistance, 0f).toInt()
            starSize = getDimension(R.styleable.RatingBar_starSize, 20.ptFloat).toInt()
            starCount = getInteger(R.styleable.RatingBar_starCount, 5)
            integerOnly = getBoolean(R.styleable.RatingBar_integerOnly, false)
            starEmptyDrawable = getDrawable(R.styleable.RatingBar_starEmpty)?.mutate()
            starFillBitmap = drawableToBitmap(getDrawable(R.styleable.RatingBar_starFill), starSize)
        }
        isClickable = true
        starFillBitmap?.let {
            paint.shader = BitmapShader(it, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        starFillBitmap.safeRecycle()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        setMeasuredDimension((starSize * starCount + starDistance * (starCount - 1)), starSize)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val emptyDrawable = starEmptyDrawable ?: return
        // 绘制空心星星
        for (i in 0 until starCount) {
            val left = i * (starDistance + starSize)
            emptyDrawable.setBounds(left, 0, left + starSize, starSize)
            emptyDrawable.draw(canvas)
        }
        // 绘制实心星星
        if (rating > 1) {
            canvas.drawRect(0f, 0f, starSize.toFloat(), starSize.toFloat(), paint)
            if (rating - rating.toInt() == 0f) {
                var i = 1
                while (i < rating) {
                    canvas.translate((starDistance + starSize).toFloat(), 0f)
                    canvas.drawRect(0f, 0f, starSize.toFloat(), starSize.toFloat(), paint)
                    i++
                }
            } else {
                var i = 1
                while (i < rating - 1) {
                    canvas.translate((starDistance + starSize).toFloat(), 0f)
                    canvas.drawRect(0f, 0f, starSize.toFloat(), starSize.toFloat(), paint)
                    i++
                }
                canvas.translate((starDistance + starSize).toFloat(), 0f)
                val fraction = ((rating - rating.toInt()) * 10).roundToInt() * 0.1f
                canvas.drawRect(0f, 0f, starSize * fraction, starSize.toFloat(), paint)
            }
        } else {
            canvas.drawRect(0f, 0f, starSize * rating, starSize.toFloat(), paint)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x.coerceIn(0f, measuredWidth.toFloat())
        val singleStarWidth = measuredWidth / starCount.toFloat()
        val newRating = x / singleStarWidth
        when (event.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                setRating(newRating)
            }
        }
        invalidate()
        return super.onTouchEvent(event)
    }

    /**
     * 获取显示星星的数目
     */
    fun getRating(): Float {
        return rating
    }

    /**
     * 设置评分（星星数量）
     */
    fun setRating(score: Float) {
        rating = if (integerOnly) {
            ceil(score.toDouble()).toInt().toFloat()
        } else {
            (score * 10).roundToInt() * 0.1f
        }
        onRatingChangedListener?.onChanged(rating)
        invalidate()
    }

    /**
     * 是否能够操作移动星星
     */
    fun setIndicator(enabled: Boolean) {
        setOnTouchListener(if (enabled) touchBlocker else null)
    }

    /**
     * 设置评分变化监听
     */
    fun setOnRatingChangedListener(listener: OnRatingChangedListener) {
        onRatingChangedListener = listener
    }

    /**
     * 评分变化回调接口
     */
    interface OnRatingChangedListener {
        fun onChanged(rating: Float)
    }

}