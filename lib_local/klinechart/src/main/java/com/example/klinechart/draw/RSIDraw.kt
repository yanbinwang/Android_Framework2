package com.example.klinechart.draw

import android.graphics.Canvas
import android.graphics.Paint
import com.example.framework.utils.function.value.orZero
import com.example.klinechart.BaseKLineChartView
import com.example.klinechart.base.IChartDraw
import com.example.klinechart.base.IValueFormatter
import com.example.klinechart.entity.IRSI
import com.example.klinechart.formatter.ValueFormatter

/**
 * RSI实现类
 * Created by tifezh on 2016/6/19.
 */
class RSIDraw(private val view: BaseKLineChartView) : IChartDraw<IRSI> {
    private val mRSI1Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mRSI2Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mRSI3Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mContext get() = view.context

    override fun drawTranslated(lastPoint: IRSI, curPoint: IRSI, lastX: Float, curX: Float, canvas: Canvas, view: BaseKLineChartView, position: Int) {
        if (lastPoint.getRsi() != 0f) {
            view.drawChildLine(canvas, mRSI1Paint, lastX, lastPoint.getRsi(), curX, curPoint.getRsi())
        }
    }

    override fun drawText(canvas: Canvas, view: BaseKLineChartView, position: Int, x: Float, y: Float) {
        val point = view.getItem(position) as? IRSI
        if (point?.getRsi() != 0f) {
            var mX = x
            var text = "RSI(14)  "
            canvas.drawText(text, mX, y, view.getTextPaint())
            mX += view.getTextPaint().measureText(text)
            text = view.formatValue(point?.getRsi().orZero)
            canvas.drawText(text, mX, y, mRSI1Paint)
        }
    }

    override fun getValueFormatter(): IValueFormatter {
        return ValueFormatter()
    }

    override fun getMinValue(point: IRSI): Float {
        return point.getRsi()
    }

    override fun getMaxValue(point: IRSI): Float {
        return point.getRsi()
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

    fun setRSI1Color(color: Int) {
        mRSI1Paint.color = color
    }

    fun setRSI2Color(color: Int) {
        mRSI2Paint.color = color
    }

    fun setRSI3Color(color: Int) {
        mRSI3Paint.color = color
    }

}