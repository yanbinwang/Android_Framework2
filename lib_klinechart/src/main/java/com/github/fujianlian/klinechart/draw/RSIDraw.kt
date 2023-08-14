package com.github.fujianlian.klinechart.draw

import android.graphics.Canvas
import android.graphics.Paint
import com.example.framework.utils.function.value.orZero
import com.github.fujianlian.klinechart.BaseKLineChartView
import com.github.fujianlian.klinechart.base.IChartDraw
import com.github.fujianlian.klinechart.base.IValueFormatter
import com.github.fujianlian.klinechart.entity.IRSI
import com.github.fujianlian.klinechart.formatter.ValueFormatter

/**
 * RSI实现类
 * Created by tifezh on 2016/6/19.
 */
class RSIDraw(view: BaseKLineChartView) : IChartDraw<IRSI> {
    private val mRSI1Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mRSI2Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mRSI3Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    override fun drawTranslated(lastPoint: IRSI?, curPoint: IRSI, lastX: Float, curX: Float, canvas: Canvas, view: BaseKLineChartView, position: Int) {
        if (lastPoint?.rsi != 0f) {
            view.drawChildLine(canvas, mRSI1Paint, lastX, lastPoint?.rsi.orZero, curX, curPoint.rsi)
        }
    }

    override fun drawText(canvas: Canvas, view: BaseKLineChartView, position: Int, x: Float, y: Float) {
        val point = view.getItem(position) as? IRSI
        if (point?.rsi != 0f) {
            var valueX = x
            var text: String? = "RSI(14)  "
            canvas.drawText(text.orEmpty(), valueX, y, view.getTextPaint())
            valueX += view.getTextPaint().measureText(text)
            text = view.formatValue(point?.rsi.orZero)
            canvas.drawText(text, valueX, y, mRSI1Paint)
        }
    }

    override fun getValueFormatter(): IValueFormatter {
        return ValueFormatter()
    }

    override fun getMinValue(point: IRSI): Float {
        return point.rsi
    }

    override fun getMaxValue(point: IRSI): Float {
        return point.rsi
    }

    fun setRSI1Color(color: Int) {
        mRSI1Paint.color = color
    }

    fun setRSI2Color(color: Int) {
        mRSI2Paint.color = color
    }

    fun setRSI3Color(color: Int) {
        mRSI3Paint.color = color
    }

    /**
     * 设置曲线宽度
     */
    fun setLineWidth(width: Float) {
        mRSI1Paint.strokeWidth = width
        mRSI2Paint.strokeWidth = width
        mRSI3Paint.strokeWidth = width
    }

    /**
     * 设置文字大小
     */
    fun setTextSize(textSize: Float) {
        mRSI2Paint.textSize = textSize
        mRSI3Paint.textSize = textSize
        mRSI1Paint.textSize = textSize
    }

}