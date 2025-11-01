package com.example.klinechart.draw

import android.graphics.Canvas
import android.graphics.Paint
import com.example.framework.utils.function.value.orZero
import com.example.klinechart.entity.IRSI
import com.example.klinechart.formatter.IValueFormatter
import com.example.klinechart.formatter.ValueFormatter
import com.example.klinechart.widget.BaseKLineChartView

/**
 * RSI实现类
 */
class RSIDraw(view: BaseKLineChartView) : IChartDraw<IRSI> {
    private val mRSI1Paint by lazy { Paint(Paint.ANTI_ALIAS_FLAG) }
    private val mRSI2Paint by lazy { Paint(Paint.ANTI_ALIAS_FLAG) }
    private val mRSI3Paint by lazy { Paint(Paint.ANTI_ALIAS_FLAG) }

    override fun drawTranslated(lastPoint: IRSI?, curPoint: IRSI?, lastX: Float, curX: Float, canvas: Canvas, view: BaseKLineChartView, position: Int) {
        if (lastPoint?.getRsi() != 0f) {
            view.drawChildLine(canvas, mRSI1Paint, lastX, lastPoint?.getRsi().orZero, curX, curPoint?.getRsi().orZero)
        }
    }

    override fun drawText(canvas: Canvas?, view: BaseKLineChartView, position: Int, x: Float, y: Float) {
        var mX = x
        val point = view.getItem(position) as? IRSI
        if (point?.getRsi() != 0f) {
            var text = "RSI(14)  "
            canvas?.drawText(text, mX, y, view.getTextPaint())
            mX += view.getTextPaint().measureText(text)
            text = view.formatValue(point?.getRsi().orZero)
            canvas?.drawText(text, mX, y, mRSI1Paint)
        }
    }

    override fun getMaxValue(point: IRSI?): Float {
        return point?.getRsi().orZero
    }

    override fun getMinValue(point: IRSI?): Float {
        return point?.getRsi().orZero
    }

    override fun getValueFormatter(): IValueFormatter {
        return ValueFormatter()
    }

    fun setRSI1Color(color: Int) {
        mRSI1Paint.setColor(color)
    }

    fun setRSI2Color(color: Int) {
        mRSI2Paint.setColor(color)
    }

    fun setRSI3Color(color: Int) {
        mRSI3Paint.setColor(color)
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