package com.example.framework.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.os.Parcel
import android.text.*
import android.text.style.*
import android.text.style.StyleSpan
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import com.example.framework.utils.function.drawable
import com.example.framework.utils.function.value.orZero
import com.example.framework.utils.function.value.toSafeFloat
import com.example.framework.utils.function.value.toSafeInt

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
        spannable.setSpan(AbsoluteSizeSpan(textSize.toInt()), start, end, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
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
 * 加入一段链接样式的字符串
 */
class ClickSpan(private val clickable: ClickableSpan) : SpanType {
    override fun setSpan(spannable: Spannable, start: Int, end: Int) {
        spannable.setSpan(clickable, start, end, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
    }
}

/**
 * 加入一段图片样式的字符串
 * TextSpan()
 * .add("測試標籤", ImageSpan(ImageSpanBean(this, R.drawable.shape_test_bg, dimen(R.dimen.textSize14), color(R.color.white))))
 * .add("文本內容")
 * .build()
 */
class ImageSpan(private val bean: ImageSpanBean) : SpanType {
    override fun setSpan(spannable: Spannable, start: Int, end: Int) {
        spannable.setSpan(BackgroundImage(bean), start, end, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
    }
}

class BackgroundImage(private val bean: ImageSpanBean) : ReplacementSpan(), ParcelableSpan {
    private var mWidth = -1

    override fun draw(canvas: Canvas, text: CharSequence?, start: Int, end: Int, x: Float, top: Int, y: Int, bottom: Int, paint: Paint) {
//        paint.textSize = bean.size
//        paint.color = bean.color
//        canvas.save()
//        canvas.translate(x, top.toFloat())
//        //繪製背景
//        bean.mDrawable?.setBounds(0, 0, mWidth, bottom - top)
//        bean.mDrawable?.draw(canvas)
//        canvas.drawText(text.toString(), start, end, x, y.toSafeFloat(), paint)

        paint.textSize = bean.size
        paint.color = bean.color
        canvas.save()
        //将画布的原点（0，0）坐标移动到指定位置
        canvas.translate(x, top.toFloat())

        val measureWidth = paint.measureText("測試標籤")
        val measureHeight = paint.fontMetrics.bottom - paint.fontMetrics.top
        "绘制的宽:${mWidth}\n绘制的高:${bottom - top}\n重测的宽:${measureWidth}\n重测的高:${measureHeight}".logWTF

        //top和bottom是字体xy的坐标，需要减去多出来的差值
        val difference = bottom - top - measureHeight


        //繪製背景
        bean.mDrawable?.setBounds(0,
            0,
            (measureWidth +bean.start.orZero + bean.end.orZero).toSafeInt(),
            ((bottom + difference/2) - (top - difference /2)).toSafeInt())
        bean.mDrawable?.draw(canvas)
        canvas.drawText(text.toString(), start, end, x, y.toSafeFloat(), paint)
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

    override fun writeToParcel(dest: Parcel, flags: Int) {
    }

    override fun describeContents() = 0

}

data class ImageSpanBean(
    val mDrawable: Drawable?,//资源id
    val size: Float,//文字大小（pt转换）
    val color: Int,//文字颜色(color转换)
    val start: Int? = null,
    val top: Int? = null,
    val end: Int? = null,
    val bottom: Int? = null
)