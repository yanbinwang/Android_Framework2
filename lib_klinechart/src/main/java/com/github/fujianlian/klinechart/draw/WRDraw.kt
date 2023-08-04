package com.github.fujianlian.klinechart.draw

import android.graphics.Canvas
import android.graphics.Paint
import com.github.fujianlian.klinechart.BaseKLineChartView
import com.github.fujianlian.klinechart.base.IChartDraw
import com.github.fujianlian.klinechart.base.IValueFormatter
import com.github.fujianlian.klinechart.entity.ICandle
import com.github.fujianlian.klinechart.entity.IKLine
import com.github.fujianlian.klinechart.entity.IWR
import com.github.fujianlian.klinechart.formatter.ValueFormatter

/**
 * KDJ实现类
 * Created by tifezh on 2016/6/19.
 */
class WRDraw(view: BaseKLineChartView) : IChartDraw<IWR> {
    private val mRPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    override fun drawTranslated(lastPoint: IWR?, curPoint: IWR?, lastX: Float, curX: Float, canvas: Canvas, view: BaseKLineChartView, position: Int) {
        if (lastPoint == null || curPoint == null) return
        if (lastPoint.r != -10f) {
            view.drawChildLine(canvas, mRPaint, lastX, lastPoint.r, curX, curPoint.r)
        }
    }

    override fun drawText(canvas: Canvas, view: BaseKLineChartView, position: Int, x: Float, y: Float) {
        var newX = x
        val point = view.getItem(position) as? IWR ?: return
        val point1: ICandle = view.getItem(position) as? IKLine ?: return
        // 位数
        val digits = point1.digits
        if (point.r != -10f) {
            var text = "WR(14):"
            canvas.drawText(text, newX, y, view.textPaint)
            newX += view.textPaint.measureText(text)
            text = view.formatValue(point.r, digits) + " "
            canvas.drawText(text, newX, y, mRPaint)
        }
    }

    override fun getMaxValue(point: IWR): Float {
        return point.r
    }

    override fun getMinValue(point: IWR): Float {
        return point.r
    }

    override val valueFormatter: IValueFormatter
        get() = ValueFormatter()

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