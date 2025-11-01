package com.example.klinechart.draw

import android.graphics.Canvas
import android.graphics.Paint
import androidx.core.content.ContextCompat
import com.example.framework.utils.function.value.orZero
import com.example.framework.utils.function.value.toSafeFloat
import com.example.klinechart.R
import com.example.klinechart.entity.IVolume
import com.example.klinechart.formatter.BigValueFormatter
import com.example.klinechart.formatter.IValueFormatter
import com.example.klinechart.utils.ViewUtil
import com.example.klinechart.widget.BaseKLineChartView

class VolumeDraw(view: BaseKLineChartView) : IChartDraw<IVolume> {
    private var pillarWidth = 0
    private val mRedPaint by lazy { Paint(Paint.ANTI_ALIAS_FLAG) }
    private val mGreenPaint by lazy { Paint(Paint.ANTI_ALIAS_FLAG) }
    private val ma5Paint by lazy { Paint(Paint.ANTI_ALIAS_FLAG) }
    private val ma10Paint by lazy { Paint(Paint.ANTI_ALIAS_FLAG) }

    init {
        val context = view.context
        mRedPaint.setColor(ContextCompat.getColor(context, R.color.chart_red))
        mGreenPaint.setColor(ContextCompat.getColor(context, R.color.chart_green))
        pillarWidth = ViewUtil.Dp2Px(context, 4f)
    }

    override fun drawTranslated(lastPoint: IVolume?, curPoint: IVolume?, lastX: Float, curX: Float, canvas: Canvas, view: BaseKLineChartView, position: Int) {
        drawHistogram(canvas, curPoint, curX, view)
        if (lastPoint?.getMA5Volume() != 0f) {
            view.drawVolLine(canvas, ma5Paint, lastX, lastPoint?.getMA5Volume().orZero, curX, curPoint?.getMA5Volume().orZero)
        }
        if (lastPoint?.getMA10Volume() != 0f) {
            view.drawVolLine(canvas, ma10Paint, lastX, lastPoint?.getMA10Volume().orZero, curX, curPoint?.getMA10Volume().orZero)
        }
    }

    private fun drawHistogram(canvas: Canvas, curPoint: IVolume?, curX: Float, view: BaseKLineChartView) {
        val r = (pillarWidth / 2).toFloat()
        val top = view.getVolY(curPoint?.getVolume().orZero)
        val bottom = view.getVolRect()?.bottom
        // 涨
        if (curPoint?.getClosePrice().orZero >= curPoint?.getOpenPrice().orZero) {
            canvas.drawRect(curX - r, top, curX + r, bottom.toSafeFloat(), mRedPaint)
        } else {
            canvas.drawRect(curX - r, top, curX + r, bottom.toSafeFloat(), mGreenPaint)
        }
    }

    override fun drawText(canvas: Canvas?, view: BaseKLineChartView, position: Int, x: Float, y: Float) {
        var mX = x
        val point = view.getItem(position) as? IVolume
        var text = "VOL:${getValueFormatter().format(point?.getVolume().orZero)}  "
        canvas?.drawText(text, mX, y, view.getTextPaint())
        mX += view.getTextPaint().measureText(text)
        text = "MA5:${getValueFormatter().format(point?.getMA5Volume().orZero)}  "
        canvas?.drawText(text, mX, y, ma5Paint)
        mX += ma5Paint.measureText(text)
        text = "MA10:${getValueFormatter().format(point?.getMA10Volume().orZero)}"
        canvas?.drawText(text, mX, y, ma10Paint)
    }

    override fun getMaxValue(point: IVolume?): Float {
        val value = point?.getMA5Volume().orZero.coerceAtLeast(point?.getMA10Volume().orZero)
        return point?.getVolume().orZero.coerceAtLeast(value)
    }

    override fun getMinValue(point: IVolume?): Float {
        val value = point?.getMA5Volume().orZero.coerceAtMost(point?.getMA10Volume().orZero)
        return point?.getVolume().orZero.coerceAtMost(value)
    }

    override fun getValueFormatter(): IValueFormatter {
        return BigValueFormatter()
    }

    /**
     * 设置 MA5 线的颜色
     *
     * @param color
     */
    fun setMa5Color(color: Int) {
        this.ma5Paint.setColor(color)
    }

    /**
     * 设置 MA10 线的颜色
     *
     * @param color
     */
    fun setMa10Color(color: Int) {
        this.ma10Paint.setColor(color)
    }

    fun setLineWidth(width: Float) {
        this.ma5Paint.strokeWidth = width
        this.ma10Paint.strokeWidth = width
    }

    /**
     * 设置文字大小
     *
     * @param textSize
     */
    fun setTextSize(textSize: Float) {
        this.ma5Paint.textSize = textSize
        this.ma10Paint.textSize = textSize
    }

}