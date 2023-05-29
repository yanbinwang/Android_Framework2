package com.example.framework.utils

import android.content.Context
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.style.AbsoluteSizeSpan
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.View
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat

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

abstract class XClickableSpan(private val context: Context, private val colorRes: Int, var click: (v: View) -> Unit = {}) : ClickableSpan() {
    override fun onClick(widget: View) {
        click.invoke(widget)
    }

    override fun updateDrawState(ds: TextPaint) {
        super.updateDrawState(ds)
        ds.color = ContextCompat.getColor(context, colorRes)
        ds.isUnderlineText = false
    }
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