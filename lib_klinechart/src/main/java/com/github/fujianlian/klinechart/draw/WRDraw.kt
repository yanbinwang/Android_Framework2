package com.github.fujianlian.klinechart.draw

import android.graphics.Canvas
import android.graphics.Paint
import com.example.framework.utils.function.value.orZero
import com.github.fujianlian.klinechart.BaseKLineChartView
import com.github.fujianlian.klinechart.base.IChartDraw
import com.github.fujianlian.klinechart.base.IValueFormatter
import com.github.fujianlian.klinechart.entity.IWR
import com.github.fujianlian.klinechart.formatter.ValueFormatter

/**
 * KDJ实现类
 * Created by tifezh on 2016/6/19.
 */
class WRDraw(view: BaseKLineChartView) : IChartDraw<IWR> {
    private val mRPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    override fun drawTranslated(lastPoint: IWR?, curPoint: IWR, lastX: Float, curX: Float, canvas: Canvas, view: BaseKLineChartView, position: Int) {
        if (lastPoint?.getR() != -10f) {
            view.drawChildLine(canvas, mRPaint, lastX, lastPoint?.getR().orZero, curX, curPoint.getR())
        }
    }

    override fun drawText(canvas: Canvas, view: BaseKLineChartView, position: Int, x: Float, y: Float) {
        var valueX = x
        val point = view.getItem(position) as? IWR
        if (point?.getR() != -10f) {
            var text = "WR(14):"
            canvas.drawText(text, valueX, y, view.getTextPaint())
            valueX += view.getTextPaint().measureText(text)
            text = "${view.formatValue(point?.getR().orZero)} "
            canvas.drawText(text, valueX, y, mRPaint)
        }
    }

    override fun getValueFormatter(): IValueFormatter {
        return ValueFormatter()
    }

    override fun getMinValue(point: IWR): Float {
        return point.getR()
    }

    override fun getMaxValue(point: IWR): Float {
        return point.getR()
    }

    /**
     * 设置%R颜色
     */
    fun setRColor(color: Int) {
        mRPaint.color = color
    }

    /**
     * 设置曲线宽度
     */
    fun setLineWidth(width: Float) {
        mRPaint.strokeWidth = width
    }

    /**
     * 设置文字大小
     */
    fun setTextSize(textSize: Float) {
        mRPaint.textSize = textSize
    }

}