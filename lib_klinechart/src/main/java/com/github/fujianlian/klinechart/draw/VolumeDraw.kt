package com.github.fujianlian.klinechart.draw

import android.graphics.Canvas
import android.graphics.Paint
import androidx.core.content.ContextCompat
import com.example.common.utils.function.pt
import com.example.framework.utils.function.value.orZero
import com.github.fujianlian.klinechart.BaseKLineChartView
import com.github.fujianlian.klinechart.KLineHelper
import com.github.fujianlian.klinechart.R
import com.github.fujianlian.klinechart.base.IChartDraw
import com.github.fujianlian.klinechart.base.IValueFormatter
import com.github.fujianlian.klinechart.entity.ICandle
import com.github.fujianlian.klinechart.entity.IKLine
import com.github.fujianlian.klinechart.entity.IVolume
import com.github.fujianlian.klinechart.formatter.BigValueFormatter
import kotlin.math.max
import kotlin.math.min

/**
 * K线下方柱子
 */
class VolumeDraw(view: BaseKLineChartView) : IChartDraw<IVolume> {
    private val mRedPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mGreenPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val ma5Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val ma10Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val pillarWidth: Int
    override fun drawTranslated(
        lastPoint: IVolume?, curPoint: IVolume?, lastX: Float, curX: Float,
        canvas: Canvas, view: BaseKLineChartView, position: Int,
    ) {
        if (lastPoint == null || curPoint == null) return
        drawHistogram(canvas, curPoint, lastPoint, curX, view, position)
        if (lastPoint.mA5Volume != 0f) {
            view.drawVolLine(canvas, ma5Paint, lastX, lastPoint.mA5Volume, curX, curPoint.mA5Volume)
        }
        if (lastPoint.mA10Volume != 0f) {
            view.drawVolLine(canvas, ma10Paint, lastX, lastPoint.mA10Volume, curX, curPoint.mA10Volume)
        }
    }

    /**
     * 绘制柱形图
     */
    private fun drawHistogram(
        canvas: Canvas, curPoint: IVolume, lastPoint: IVolume?, curX: Float,
        view: BaseKLineChartView, position: Int,
    ) {
        val r = pillarWidth / 2.0f
        val top = view.getVolY(curPoint.volume)
        val bottom = view.volRect?.bottom.orZero
        if (curPoint.closePrice >= curPoint.openPrice) { //涨
            canvas.drawRect(curX - r, top, curX + r, bottom.toFloat(), mRedPaint)
        } else {
            canvas.drawRect(curX - r, top, curX + r, bottom.toFloat(), mGreenPaint)
        }
    }

    override fun drawText(
        canvas: Canvas, view: BaseKLineChartView, position: Int, x: Float, y: Float,
    ) {
        var newX = x
        val point = view.getItem(position) as? IVolume ?: return
        val point1: ICandle = view.getItem(position) as? IKLine ?: return
        // 位数
        val digits = point1.digits
        var text = "VOL:" + valueFormatter.format(point.volume, digits) + "  "
        canvas.drawText(text, newX, y, view.textPaint)
        newX += view.textPaint.measureText(text)
        text = "MA5:" + valueFormatter.format(point.mA5Volume, digits) + "  "
        canvas.drawText(text, newX, y, ma5Paint)
        newX += ma5Paint.measureText(text)
        text = "MA10:" + valueFormatter.format(point.mA10Volume, digits)
        canvas.drawText(text, newX, y, ma10Paint)
    }

    override fun getMaxValue(point: IVolume): Float {
        return max(point.volume, max(point.mA5Volume, point.mA10Volume))
    }

    override fun getMinValue(point: IVolume): Float {
        return min(point.volume, min(point.mA5Volume, point.mA10Volume))
    }

    override val valueFormatter: IValueFormatter
        get() = BigValueFormatter()

    /**
     * 设置 MA5 线的颜色
     *
     * @param color
     */
    fun setMa5Color(color: Int) {
        ma5Paint.color = color
    }

    /**
     * 设置 MA10 线的颜色
     *
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
     *
     * @param textSize
     */
    fun setTextSize(textSize: Float) {
        ma5Paint.textSize = textSize
        ma10Paint.textSize = textSize
    }

    init {
        val context = view.context
        if (KLineHelper.isRiseRed()) {
            mRedPaint.color = ContextCompat.getColor(context, R.color.chartRed)
            mGreenPaint.color = ContextCompat.getColor(context, R.color.chartGreen)
        } else {
            mRedPaint.color = ContextCompat.getColor(context, R.color.chartGreen)
            mGreenPaint.color = ContextCompat.getColor(context, R.color.chartRed)
        }
        pillarWidth = 4.pt
    }
}