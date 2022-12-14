package com.example.common.utils.file

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import com.example.framework.utils.function.value.toSafeFloat
import com.example.framework.utils.function.value.toSafeInt

/**
 * @description
 * 画笔默认取中心点坐标，所以要除2
 * 只有继承了当前画笔接口的类才能使用以下方法
 * @author yan
 */
interface PaintImpl {

    /**
     * x:距左距离
     * y:距上距离
     * 以左侧为基准点绘制对应文字
     */
    fun Paint.drawTextLeft(x: Number?, y: Number?, text: String, canvas: Canvas) {
        val measureHeight = measureSize(text).second
        canvas.drawText(text, x.toSafeFloat(), (y.toSafeFloat() + measureHeight / 2), this)
    }

    /**
     * x:距左距离
     * y:距上距离
     * 以中心为基准点绘制对应文字
     */
    fun Paint.drawTextCenter(x: Number?, y: Number?, text: String, canvas: Canvas) {
        val size = measureSize(text)
        canvas.drawText(text, (x.toSafeFloat() - size.first / 2), (y.toSafeFloat() + size.second / 2), this)
    }

    /**
     * 测绘绘制文字宽高
     * first-》宽
     * second-》高
     */
    fun Paint.measureSize(text: String): Pair<Float, Float> {
        val measureWidth = measureText(text)
        val measureHeight = fontMetrics.bottom - fontMetrics.top
        return measureWidth to measureHeight
    }

    /**
     * text本身默认绘制是一行的，不会自动换行，使用此方法传入指定宽度换行
     */
    fun TextPaint.drawTextStatic(maxTextWidth: Number?, text: String, canvas: Canvas, dx: Number? = 0, dy: Number? = 0, spacingmult: Number? = 1f) {
        //spacingmult 是行间距的倍数，通常情况下填 1 就好；
        //spacingadd 是行间距的额外增加值，通常情况下填 0 就好
        val layout = StaticLayout(text, this, maxTextWidth.toSafeInt(), Layout.Alignment.ALIGN_NORMAL, spacingmult.toSafeFloat(), 0f, false)
        canvas.save()
        //StaticLayout默认画在Canvas的(0,0)点，如果需要调整位置只能在draw之前移Canvas的起始坐标
        canvas.translate(dx.toSafeFloat(), dy.toSafeFloat())
        layout.draw(canvas)
    }

    /**
     * 获取一个预设的文字画笔
     */
    fun getTextPaint(textSize: Float, color: Int = Color.WHITE, typeface: Typeface = Typeface.DEFAULT): TextPaint {
        val paint = TextPaint()
        paint.isAntiAlias = true
        paint.textSize = textSize
        paint.color = color
        paint.typeface = typeface
//        paint.typeface = ResourcesCompat.getFont(BaseApplication.instance, fontId)
        return paint
    }

}