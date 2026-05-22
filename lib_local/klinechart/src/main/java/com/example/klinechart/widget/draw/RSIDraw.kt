package com.example.klinechart.widget.draw

import android.graphics.Canvas
import android.graphics.Paint
import androidx.annotation.ColorInt
import com.example.framework.utils.function.value.orZero
import com.example.klinechart.bean.IRSI
import com.example.klinechart.utils.formatter.IValueFormatter
import com.example.klinechart.utils.formatter.ValueFormatter
import com.example.klinechart.widget.BaseKLineChartView

/**
 * RSI实现类
 * 1) 核心公式
 *  上涨均值 MA_UP = N日内收盘价上涨幅度平均值
 *  下跌均值 MA_DOWN = N日内收盘价下跌幅度平均值
 *  RS = MA_UP / MA_DOWN
 *  RSI = 100 - 100 / (1 + RS)
 */
class RSIDraw : IChartDraw<IRSI> {
    private val mRSI1Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mRSI2Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mRSI3Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    override fun drawTranslated(lastPoint: IRSI?, curPoint: IRSI?, lastX: Float, curX: Float, canvas: Canvas, view: BaseKLineChartView, position: Int) {
        if (lastPoint?.getRsi() != 0f) {
            view.drawChildLine(canvas, mRSI1Paint, lastX, lastPoint?.getRsi().orZero, curX, curPoint?.getRsi().orZero)
        }
    }

    override fun drawText(canvas: Canvas?, view: BaseKLineChartView, position: Int, x: Float, y: Float) {
        var mX = x
        val point = view.getItem(position) as? IRSI
        if (point?.getRsi() != 0f) {
            var text = "RSI(14)\u0020\u0020"
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

    fun setRSI1Color(@ColorInt color: Int) {
        mRSI1Paint.color = color
    }

    fun setRSI2Color(@ColorInt color: Int) {
        mRSI2Paint.color = color
    }

    fun setRSI3Color(@ColorInt color: Int) {
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