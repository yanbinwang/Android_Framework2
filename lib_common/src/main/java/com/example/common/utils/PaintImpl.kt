package com.example.common.utils

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import com.example.base.utils.function.value.toSafeFloat

/**
 * @description 画笔默认取中心点坐标，所以要除2
 * @author yan
 */
interface PaintImpl {

    /**
     * x:距左距离
     * y:距上距离
     * 以左侧为基准点绘制对应文字
     */
    fun Paint.drawTextLeft(x: Number?, y: Number?, text: String, canvas: Canvas) {
        canvas.drawText(text, x.toSafeFloat(), (y.toSafeFloat() + measureSize(text).second / 2), this)
    }

    /**
     * x:距左距离
     * y:距上距离
     * 以中心为基准点绘制对应文字
     */
    fun Paint.drawTextCenter(x: Number?, y: Number?, text: String, canvas: Canvas) {
        canvas.drawText(text, (x.toSafeFloat() - measureSize(text).first / 2), (y.toSafeFloat() + measureSize(text).second / 2), this)
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
     * 获取一个预设的文字画笔
     */
    fun getTextPaint(textSize: Float, color: Int = Color.WHITE, typeface: Typeface = Typeface.DEFAULT): Paint {
        val paint = Paint()
        paint.isAntiAlias = true
        paint.textSize = textSize
        paint.color = color
        paint.typeface = typeface
//        paint.typeface = ResourcesCompat.getFont(BaseApplication.instance, fontId)
        return paint
    }

}