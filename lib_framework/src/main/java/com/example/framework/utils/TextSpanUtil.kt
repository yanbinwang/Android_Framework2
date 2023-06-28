package com.example.framework.utils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.style.AbsoluteSizeSpan
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.ReplacementSpan
import android.text.style.StyleSpan
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
 * 角标文字需要设置的比textview本身文字小，不然textview撑不开
 */
class RadiusSpan(private val radiusBackground: RadiusBackgroundSpan) : SpanType {
    override fun setSpan(spannable: Spannable, start: Int, end: Int) {
        spannable.setSpan(radiusBackground, start, end, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
    }
}

/**
 * @description 圆角span
 * @param mColor  背景颜色
 * @param mRadius 圆角半径
 * @param mDifference 差值，如果居中的话，文字的一段高度是无法代码计算的，需要传入对应pt微调
 * RadiusSpan(RadiusBackgroundSpan(color(R.color.blue_aac6f4),5, 3.pt))
 * https://blog.csdn.net/industriously/article/details/53493392/
 * @author yan
 */
class RadiusBackgroundSpan(private val mColor: Int, private val mRadius: Int, private val mDifference: Int = 0) : ReplacementSpan() {
    private var mSize = 0

    override fun getSize(paint: Paint, text: CharSequence?, start: Int, end: Int, fm: Paint.FontMetricsInt?): Int {
        mSize = (paint.measureText(text, start, end) + 2 * mRadius).toSafeInt()
        //mSize就是span的宽度，span有多宽，开发者可以在这里随便定义规则
        //我的规则：这里text传入的是SpannableString，start，end对应setSpan方法相关参数
        //可以根据传入起始截至位置获得截取文字的宽度，最后加上左右两个圆角的半径得到span宽度
        return mSize
    }

    override fun draw(canvas: Canvas, text: CharSequence?, start: Int, end: Int, x: Float, top: Int, y: Int, bottom: Int, paint: Paint) {
        val color = paint.color//保存文字颜色
        paint.color = mColor//设置背景颜色
        paint.isAntiAlias = true// 设置画笔的锯齿效果
//        val oval = RectF(x, y + paint.ascent(), x + mSize, y + paint.descent())
//        //设置文字背景矩形，x为span其实左上角相对整个TextView的x值，y为span左上角相对整个View的y值。paint.ascent()获得文字上边缘，paint.descent()获得文字下边缘
//        canvas.drawRoundRect(oval, mRadius.toSafeFloat(), mRadius.toSafeFloat(), paint)//绘制圆角矩形，第二个参数是x半径，第三个参数是y半径
//        paint.color = color//恢复画笔的文字颜色
//        canvas.drawText(text ?: return, start, end, (x + mRadius).toSafeFloat(), y.toSafeFloat(), paint)//绘制文字
        //居中显示
        val lineHeight = bottom - top
        val fontHeight = paint.descent() - paint.ascent()
        val differenceY = (lineHeight - fontHeight) / 2f
        val oval = RectF(x, differenceY, x + mSize, differenceY + fontHeight)
        canvas.drawRoundRect(oval, mRadius.toSafeFloat(), mRadius.toSafeFloat(), paint)
        paint.color = color
        canvas.drawText(text ?: return, start, end, (x + mRadius).toSafeFloat(), differenceY + fontHeight - mDifference, paint)
    }
}

/**
 * 文案替换图片
 *  binding.tvTest.text = TextSpan()
 *  .add("在Cheezeebit交易，訂單賺取高達", SizeSpan(dimen(R.dimen.textSize14)))
 *  .add(" 0.5% ", SizeSpan(dimen(R.dimen.textSize14)), ColorSpan(color(R.color.grey_cccccc)))
 *  .add("的訂單獎勵", SizeSpan(dimen(R.dimen.textSize14)))
 *  .add("★", BitmapSpan(ImageSpan(drawable(R.mipmap.ic_rank)?.toBitmapOrNull(), 18.pt, 18.pt)))
 *  .build().setRankSpan(18.pt)
 */
class BitmapSpan(private val imageSpan: ImageSpan) : SpanType {
    override fun setSpan(spannable: Spannable, start: Int, end: Int) {
        spannable.setSpan(imageSpan, start, end, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
    }
}

/**
 * @description 图片span
 * @param bitmap     替换的图片
 * @param mImageSize 图片的大小，传入pt
 * @param mDifference 差值，如果居中的话，文字的一段高度是无法代码计算的，需要传入对应pt微调
 * @author yan
 */
class ImageSpan(private val bitmap: Bitmap?, private val mImageSize: Int, private val mDifference: Int = 0) : ReplacementSpan() {
    private var mSize = 0
//    private val bitmap = ResourcesCompat.getDrawable(BaseApplication.instance.resources, R.mipmap.ic_rank, null)?.toBitmapOrNull(mImageSize, mImageSize)

    override fun getSize(paint: Paint, text: CharSequence?, start: Int, end: Int, fm: Paint.FontMetricsInt?): Int {
        mSize = paint.measureText(text, start, end).toInt()
        //mSize就是span的宽度，span有多宽，开发者可以在这里随便定义规则
        //可以根据传入起始截至位置获得截取文字的宽度，最后加上左右两个圆角的半径得到span宽度
        return mSize
    }

    override fun draw(canvas: Canvas, text: CharSequence?, start: Int, end: Int, x: Float, top: Int, y: Int, bottom: Int, paint: Paint) {
        if (bitmap == null) return
        paint.isAntiAlias = true //设置画笔的锯齿效果
        val lineHeight = bottom - top
        val differenceY = (lineHeight - mImageSize) / 2f
        val oval = RectF(x, differenceY, x + mSize, differenceY + mImageSize + mDifference)
        canvas.drawBitmap(bitmap, null, oval, paint)
    }
}