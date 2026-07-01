package com.example.framework.utils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.style.AbsoluteSizeSpan
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.ReplacementSpan
import android.text.style.StyleSpan
import android.view.View
import androidx.annotation.ColorInt
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
 * 字体样式生成库 (在同一个 TextView 中混合使用不同大小的字体（例如单位是 14sp，金额是 18sp），Android 默认的渲染行为是 Baseline Align（基线对齐）)
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
 * 自定义颜色字符串
 * TextSpan()
 * .add(txt, ColorSpan(context.color(R.color.blue)))
 * .build()
 */
class ColorSpan(@field:ColorInt private val color: Int) : SpanType {
    override fun setSpan(spannable: Spannable, start: Int, end: Int) {
        spannable.setSpan(ForegroundColorSpan(color), start, end, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
    }
}

/**
 * 自定义大小字符串
 * 使用 dimen(res) 引用的本地 dimes.xml 下的单位
 * TextSpan()
 * .add(txt, SizeSpan(dimen(R.dimen.textSize10))
 * .build()
 */
class SizeSpan(private val textSizePx: Float) : SpanType {
    override fun setSpan(spannable: Spannable, start: Int, end: Int) {
        spannable.setSpan(AbsoluteSizeSpan(textSizePx.toInt(), false), start, end, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
    }
}

/**
 * 自定义大小字符串 (垂直居中)
 * 解决不同字号混排时默认基线对齐导致的视觉不居中问题
 */
class CenterSizeSpan(private val textSizePx: Float) : SpanType {
    override fun setSpan(spannable: Spannable, start: Int, end: Int) {
        spannable.setSpan(CenterSizeReplacementSpan(textSizePx), start, end, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
    }
}

private class CenterSizeReplacementSpan(private val textSizePx: Float) : ReplacementSpan() {
    override fun getSize(paint: Paint, text: CharSequence?, start: Int, end: Int, fm: Paint.FontMetricsInt?): Int {
        val originalTextSize = paint.textSize
        paint.textSize = textSizePx
        val width = paint.measureText(text, start, end).toInt()
        if (fm != null) {
            val metrics = paint.fontMetricsInt
            fm.ascent = metrics.ascent
            fm.descent = metrics.descent
            fm.top = metrics.top
            fm.bottom = metrics.bottom
        }
        paint.textSize = originalTextSize
        return width
    }

    override fun draw(canvas: Canvas, text: CharSequence?, start: Int, end: Int, x: Float, top: Int, y: Int, bottom: Int, paint: Paint) {
        val originalTextSize = paint.textSize
        paint.textSize = textSizePx
        val lineHeight = bottom - top
        val fontHeight = paint.descent() - paint.ascent()
        val centerY = top + (lineHeight - fontHeight) / 2f - paint.ascent()
        canvas.drawText(text ?: return, start, end, x, centerY, paint)
        paint.textSize = originalTextSize
    }
}

/**
 * 自定样式字符串
 */
class TextStyleSpan(private val isBold: Boolean, private val isItalic: Boolean) : SpanType {
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
 * 链接样式字符串
 */
class ClickSpan(private val clickable: ClickableSpan) : SpanType {
    override fun setSpan(spannable: Spannable, start: Int, end: Int) {
        spannable.setSpan(clickable, start, end, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
    }
}

/**
 * 点击链接字符串
 * "我已阅读《用户协议》和《隐私政策》"
 *   .setSpanFirst("《用户协议》", LinkClickSpan(color(R.color.blue)) { "点击用户协议".logWTF })
 *   .setSpanFirst("《隐私政策》", LinkClickSpan(color(R.color.blue)) { "点击隐私政策".logWTF })
 * textView.movementMethod = LinkMovementMethod.getInstance()
 *  可以配合 setSpannable 扩展函数嵌套使用
 */
class LinkClickSpan(@field:ColorInt private val colorInt: Int, private val isUnderlineText: Boolean = false, private val timeMS: Long = 500L, private val listener: () -> Unit) : SpanType {
    override fun setSpan(spannable: Spannable, start: Int, end: Int) {
        spannable.setSpan(LinkClickableSpan(colorInt, isUnderlineText, timeMS, listener), start, end, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
    }
}

private class LinkClickableSpan(@field:ColorInt private val colorInt: Int, private val isUnderlineText: Boolean, private val timeMS: Long, private val listener: () -> Unit) : ClickableSpan() {
    private var lastClickTime: Long = 0L

    override fun onClick(widget: View) {
        val currentTimeNano = System.nanoTime() / 1000000L
        if (currentTimeNano - lastClickTime >= timeMS) {
            lastClickTime = currentTimeNano
            listener.invoke()
        }
    }

    override fun updateDrawState(ds: TextPaint) {
        super.updateDrawState(ds)
        ds.color = colorInt
        ds.isUnderlineText = isUnderlineText
    }
}

/**
 * 圆角字符串 (角标文字需要设置的比textview本身文字小，不然textview撑不开)
 * @param colorInt  背景颜色
 * @param radius 圆角半径
 * @param difference 差值，如果居中的话，文字的一段高度是无法代码计算的，需要传入对应pt微调
 * RadiusSpan(color(R.color.blue_aac6f4),5, 3.pt)
 * https://blog.csdn.net/industriously/article/details/53493392/
 */
class RadiusSpan(@field:ColorInt private val colorInt: Int, private val radius: Int, private val difference: Int = 0) : SpanType {
    override fun setSpan(spannable: Spannable, start: Int, end: Int) {
        spannable.setSpan(RadiusReplacementSpan(colorInt, radius, difference), start, end, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
    }
}

private class RadiusReplacementSpan(private val colorInt: Int, private val radius: Int, private val difference: Int) : ReplacementSpan() {
    private var mSize = 0

    override fun getSize(paint: Paint, text: CharSequence?, start: Int, end: Int, fm: Paint.FontMetricsInt?): Int {
        // 宽度 = 文本宽度 + 左右圆角半径（作为水平内边距）
        mSize = (paint.measureText(text, start, end) + 2 * radius).toSafeInt()
        return mSize
    }

    override fun draw(canvas: Canvas, text: CharSequence?, start: Int, end: Int, x: Float, top: Int, y: Int, bottom: Int, paint: Paint) {
        val originalColor = paint.color
        val originalAntiAlias = paint.isAntiAlias
        // 计算物理行高与纯字体高度
        val lineHeight = bottom - top
        val fm = paint.fontMetricsInt
        val fontHeight = fm.descent - fm.ascent
        // 背景矩形与文字在行高内绝对几何居中
        val centerY = top + (lineHeight - fontHeight) / 2f
        // 绘制圆角背景
        paint.color = colorInt
        paint.isAntiAlias = true
        val oval = RectF(x, centerY, x + mSize, centerY + fontHeight)
        canvas.drawRoundRect(oval, radius.toSafeFloat(), radius.toSafeFloat(), paint)
        // 绘制文字（核心修复：使用 ascent 将基线转换为顶部对齐 + difference 微调）
        paint.color = originalColor
        // fm.ascent 是负值，所以这里是加上它的绝对值来定位基线
        val baselineY = centerY - fm.ascent + difference
        canvas.drawText(text ?: "", start, end, (x + radius).toSafeFloat(), baselineY, paint)
        // 恢复画笔状态
        paint.color = originalColor
        paint.isAntiAlias = originalAntiAlias
    }
}

/**
 * 文案替换图片字符串
 * @param bitmap      替换的图片
 * @param imageWidth  图片宽度，传入pt
 * @param imageHeight 图片高度，传入pt
 * @param difference  差值，如果居中，文字的一段高度是无法代码计算的，需要传入对应pt微调
 * binding.tvTest.text = TextSpan()
 *  .add("在Cheezeebit交易，訂單賺取高達", SizeSpan(dimen(R.dimen.textSize14)))
 *  .add(" 0.5% ", SizeSpan(dimen(R.dimen.textSize14)), ColorSpan(color(R.color.grey_cccccc)))
 *  .add("的訂單獎勵", SizeSpan(dimen(R.dimen.textSize14)))
 *  .add("★", BitmapSpan(drawable(R.mipmap.ic_rank)?.toBitmapOrNull(), 18.pt))
 *  .build()
 */
class BitmapSpan(private val bitmap: Bitmap?, private val imageWidth: Int, private val imageHeight: Int = imageWidth, private val difference: Int = 0) : SpanType {
    override fun setSpan(spannable: Spannable, start: Int, end: Int) {
        spannable.setSpan(BitmapReplacementSpan(bitmap, imageWidth, imageHeight, difference), start, end, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
    }
}

private class BitmapReplacementSpan(private val bitmap: Bitmap?, private val imageWidth: Int, private val imageHeight: Int, private val difference: Int) : ReplacementSpan() {
    private var mSize = 0

    override fun getSize(paint: Paint, text: CharSequence?, start: Int, end: Int, fm: Paint.FontMetricsInt?): Int {
        // 当前字体环境下，占位符有多宽，Span 就有多宽
        mSize = paint.measureText(text, start, end).toInt()
        return mSize
    }

    override fun draw(canvas: Canvas, text: CharSequence?, start: Int, end: Int, x: Float, top: Int, y: Int, bottom: Int, paint: Paint) {
        if (bitmap == null) return
        val originalAntiAlias = paint.isAntiAlias
        paint.isAntiAlias = true
        val lineHeight = bottom - top
        // 垂直居中只依赖 imageHeight，与宽度彻底解耦
        val differenceY = (lineHeight - imageHeight) / 2f
        // RectF 宽高独立控制，非正方形图片不再被拉伸变形
        val oval = RectF(x, differenceY, x + imageWidth, differenceY + imageHeight + difference)
        canvas.drawBitmap(bitmap, null, oval, paint)
        paint.isAntiAlias = originalAntiAlias
    }
}

/**
 * Span 扩展
 */
interface SpanType {
    fun setSpan(spannable: Spannable, start: Int, end: Int)
}