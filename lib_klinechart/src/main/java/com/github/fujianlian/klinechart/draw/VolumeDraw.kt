package com.github.fujianlian.klinechart.draw

import android.graphics.Canvas
import android.graphics.Paint
import androidx.core.content.ContextCompat
import com.example.framework.utils.function.value.orZero
import com.github.fujianlian.klinechart.BaseKLineChartView
import com.github.fujianlian.klinechart.R
import com.github.fujianlian.klinechart.base.IChartDraw
import com.github.fujianlian.klinechart.base.IValueFormatter
import com.github.fujianlian.klinechart.entity.IVolume
import com.github.fujianlian.klinechart.formatter.BigValueFormatter
import com.github.fujianlian.klinechart.utils.ViewUtil.Dp2Px

/**
 * Created by hjm on 2017/11/14 17:49.
 */
class VolumeDraw(view: BaseKLineChartView) : IChartDraw<IVolume> {
    private val mRedPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mGreenPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val ma5Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val ma10Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var pillarWidth = 0

    init {
        val context = view.context
        mRedPaint.color = ContextCompat.getColor(context, R.color.chart_red)
        mGreenPaint.color = ContextCompat.getColor(context, R.color.chart_green)
        pillarWidth = Dp2Px(context, 4f)
    }

    override fun drawTranslated(lastPoint: IVolume?, curPoint: IVolume, lastX: Float, curX: Float, canvas: Canvas, view: BaseKLineChartView, position: Int) {
        drawHistogram(canvas, curPoint, lastPoint, curX, view, position)
        if (lastPoint?.getMA5Volume() != 0f) {
            view.drawVolLine(canvas, ma5Paint, lastX, lastPoint?.getMA5Volume().orZero, curX, curPoint.getMA5Volume())
        }
        if (lastPoint?.getMA10Volume() != 0f) {
            view.drawVolLine(canvas, ma10Paint, lastX, lastPoint?.getMA10Volume().orZero, curX, curPoint.getMA10Volume())
        }
    }

    private fun drawHistogram(canvas: Canvas, curPoint: IVolume?, lastPoint: IVolume?, curX: Float, view: BaseKLineChartView, position: Int) {
        val r = (pillarWidth / 2).toFloat()
        val top = view.getVolY(curPoint?.getVolume().orZero)
        val bottom = view.volRect.bottom
        if (curPoint?.getClosePrice().orZero >= curPoint?.getOpenPrice().orZero) { //涨
            canvas.drawRect(curX - r, top, curX + r, bottom.toFloat(), mRedPaint)
        } else {
            canvas.drawRect(curX - r, top, curX + r, bottom.toFloat(), mGreenPaint)
        }
    }

    override fun drawText(canvas: Canvas, view: BaseKLineChartView, position: Int, x: Float, y: Float) {
        var valueX = x
        val point = view.getItem(position) as IVolume
        var text = "VOL:" + getValueFormatter().format(point.getVolume()) + "  "
        canvas.drawText(text, valueX, y, view.textPaint)
        valueX += view.textPaint.measureText(text)
        text = "MA5:" + getValueFormatter().format(point.getMA5Volume()) + "  "
        canvas.drawText(text, valueX, y, ma5Paint)
        valueX += ma5Paint.measureText(text)
        text = "MA10:" + getValueFormatter().format(point.getMA10Volume())
        canvas.drawText(text, valueX, y, ma10Paint)
    }

    override fun getValueFormatter(): IValueFormatter {
        return BigValueFormatter()
    }

    override fun getMinValue(point: IVolume): Float {
        return point.getVolume().coerceAtMost(point.getMA5Volume().coerceAtMost(point.getMA10Volume()))
    }

    override fun getMaxValue(point: IVolume): Float {
        return point.getVolume().coerceAtLeast(point.getMA5Volume().coerceAtLeast(point.getMA10Volume()))
    }

    /**
     * 设置 MA5 线的颜色
     * @param color
     */
    fun setMa5Color(color: Int) {
        ma5Paint.color = color
    }

    /**
     * 设置 MA10 线的颜色
     * @param color
     */
    fun setMa10Color(color: Int) {
        ma10Paint.color = color
    }

    fun setLineWidth(width: Float) {
        ma5Paint.strokeWidth = width
        ma10Paint.strokeWidth = width
    }

    /**
     * 设置文字大小
     * @param textSize
     */
    fun setTextSize(textSize: Float) {
        ma5Paint.textSize = textSize
        ma10Paint.textSize = textSize
    }

}