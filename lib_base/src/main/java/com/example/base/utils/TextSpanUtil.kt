package com.example.base.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.os.Parcel
import android.text.*
import android.text.style.AbsoluteSizeSpan
import android.text.style.ForegroundColorSpan
import android.text.style.ReplacementSpan
import android.text.style.StyleSpan
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import com.example.base.utils.function.value.toSafeFloat

//------------------------------------字符串扩展函数类------------------------------------
/**
 * 处理字符串中部分位置的样式
 * "xxx".setSpan(0, index + txtPrice.length, ColorSpan(color(R.color.textPrimary)))
 */
fun CharSequence.setSpan(start: Int, end: Int, vararg type: SpanType): Spannable {
    val spannable = if (this is Spannable) this else SpannableString.valueOf(this)
    type.forEach { it.setSpan(spannable, start, end) }
    return spannable
}

/**
 * 处理字符串中特定字符的样式（满足条件的第一条会变，后面的不会）
 * "USDXXUSD".setSpanFirst(USD, ColorSpan(rootView.color(R.color.textSecondary)))
 * 第一个USD会变
 */
fun CharSequence.setSpanFirst(string: String, vararg type: SpanType): Spannable {
    val spannable = if (this is Spannable) this else SpannableString.valueOf(this)
    if (string.isEmpty()) return spannable
    val foundIndex = this.indexOf(string)
    if (foundIndex >= 0) {
        type.forEach { it.setSpan(spannable, foundIndex, foundIndex + string.length) }
    }
    return spannable
}

/**
 * 处理字符串中特定字符的样式
 * "USDXXUSD".setSpanAll(USD, ColorSpan(rootView.color(R.color.textSecondary)))
 * 所有的USB都会变
 */
fun CharSequence.setSpanAll(string: String, vararg type: SpanType): Spannable {
    val spannable = if (this is Spannable) this else SpannableString.valueOf(this)
    if (string.isEmpty()) return spannable
    var startIndex = 0
    while (true) {
        val foundIndex = this.indexOf(string, startIndex)
        if (foundIndex < 0) {
            break
        } else type.forEach {
            it.setSpan(spannable, foundIndex, foundIndex + string.length)
        }
        startIndex = foundIndex + string.length
    }
    return spannable
}

/**
 * TextSpan()
 * .add(string(R.string.refShareContent, ""))
 * .add("\n")
 * .add(inviteCode, ColorSpan(context.color(R.color.inviteCopy)))
 * .build()
 */
class TextSpan {
    private val list: ArrayList<Pair<String, Array<out SpanType>>> = arrayListOf()
    private val stringBuilder = StringBuilder()

    fun add(string: String, vararg types: SpanType): TextSpan {
        stringBuilder.append(string)
        list.add(string to types)
        return this
    }

    fun build(): Spannable {
        var startIndex = 0
        val spannable = SpannableString.valueOf(stringBuilder.toString())
        list.forEach {
            if (it.second.isNotEmpty()) {
                val start = startIndex
                val end = startIndex + it.first.length
                it.second.forEach { type ->
                    type.setSpan(spannable, start, end)
                }
            }
            startIndex += it.first.length
        }
        return spannable
    }
}

/**
 * span扩展
 */
interface SpanType {
    fun setSpan(spannable: Spannable, start: Int, end: Int)
}

/**
 * 加入一段带颜色的字符串
 * TextSpan()
 * .add(txt, ColorSpan(context.color(R.color.blue)))
 * .build()
 */
class ColorSpan(@ColorInt val color: Int) : SpanType {
    override fun setSpan(spannable: Spannable, start: Int, end: Int) {
        spannable.setSpan(ForegroundColorSpan(color), start, end, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
    }
}

/**
 * 加入一段自定大小的字符串
 * TextSpan()
 * .add(txt, SizeSpan(dimen(R.dimen.textSize10)
 * .build()
 */
class SizeSpan(val textSize: Float) : SpanType {
    override fun setSpan(spannable: Spannable, start: Int, end: Int) {
        spannable.setSpan(
            AbsoluteSizeSpan(textSize.toInt()), start, end,
            Spanned.SPAN_INCLUSIVE_EXCLUSIVE
        )
    }
}

/**
 * 加入一段自定样式的字符串
 */
class StyleSpan(val isBold: Boolean, val isItalic: Boolean) : SpanType {
    override fun setSpan(spannable: Spannable, start: Int, end: Int) {
        val type = when {
            isBold && isItalic -> Typeface.BOLD_ITALIC
            isBold -> Typeface.BOLD
            isItalic -> Typeface.ITALIC
            else -> Typeface.NORMAL
        }
        spannable.setSpan(StyleSpan(type), start, end, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
    }
}

/**
 * first->资源id
 * second->文字大小（pt转换）
 * third->文字颜色
 */
class BackgroundImageSpan(private val context: Context, private val triple: Triple<Int, Int, Int>) : ReplacementSpan(),
    ParcelableSpan {
    private var mWidth = -1

    override fun draw(canvas: Canvas, text: CharSequence?, start: Int, end: Int, x: Float, top: Int, y: Int, bottom: Int, paint: Paint) {
        paint.textSize = triple.second.toSafeFloat()
        paint.color = triple.third
        draw(canvas, mWidth, x, top, bottom)
        canvas.drawText(text.toString(), start, end, x, y.toSafeFloat(), paint)
    }

    private fun draw(canvas: Canvas, width: Int, x: Float, top: Int, bottom: Int) {
        val mDrawable = ContextCompat.getDrawable(context, triple.first)
        canvas.save()
        canvas.translate(x, top.toFloat())
        mDrawable?.setBounds(0, 0, width, bottom - top)
        mDrawable?.draw(canvas)
        canvas.restore()
    }

    override fun getSize(paint: Paint, text: CharSequence?, start: Int, end: Int, fm: Paint.FontMetricsInt?): Int {
        val size = paint.measureText(text, start, end)
        if (fm != null) paint.getFontMetricsInt(fm)
        mWidth = size.toInt()
        return mWidth
    }

    override fun updateDrawState(ds: TextPaint?) {
    }

    override fun getSpanTypeId() = 0

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.writeInt(triple.first)
    }

    override fun describeContents() = 0

}