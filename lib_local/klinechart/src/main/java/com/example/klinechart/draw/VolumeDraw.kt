package com.example.klinechart.draw

import android.graphics.Canvas
import android.graphics.Paint
import androidx.core.content.ContextCompat
import com.example.framework.utils.function.value.orZero
import com.example.klinechart.BaseKLineChartView
import com.example.klinechart.R
import com.example.klinechart.base.IChartDraw
import com.example.klinechart.base.IValueFormatter
import com.example.klinechart.entity.IVolume
import com.example.klinechart.formatter.BigValueFormatter
import com.example.klinechart.utils.ViewUtil

/**
 * Created by hjm on 2017/11/14 17:49.
 */
class VolumeDraw(private val view: BaseKLineChartView) : IChartDraw<IVolume> {
    private var pillarWidth = 0
    private val mRedPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mGreenPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val ma5Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val ma10Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mContext get() = view.context

    init {
        mRedPaint.color = ContextCompat.getColor(mContext, R.color.chart_red)
        mGreenPaint.color = ContextCompat.getColor(mContext, R.color.chart_green)
        pillarWidth = ViewUtil.Dp2Px(mContext, 4f)
    }

    override fun drawTranslated(lastPoint: IVolume, curPoint: IVolume, lastX: Float, curX: Float, canvas: Canvas, view: BaseKLineChartView, position: Int) {
        drawHistogram(canvas, curPoint, lastPoint, curX, view, position)
        if (lastPoint.getMA5Volume() != 0f) {
            view.drawVolLine(canvas, ma5Paint, lastX, lastPoint.getMA5Volume(), curX, curPoint.getMA5Volume())
        }
        if (lastPoint.getMA10Volume() != 0f) {
            view.drawVolLine(canvas, ma10Paint, lastX, lastPoint.getMA10Volume(), curX, curPoint.getMA10Volume())
        }
    }

    private fun drawHistogram(canvas: Canvas, curPoint: IVolume, lastPoint: IVolume, curX: Float, view: BaseKLineChartView, position: Int) {
        val r = (pillarWidth / 2).toFloat()
        val top = view.getVolY(curPoint.getVolume())
        val bottom = view.getVolRect().bottom
        if (curPoint.getClosePrice() >= curPoint.getOpenPrice()) { //涨
            canvas.drawRect(curX - r, top, curX + r, bottom.toFloat(), mRedPaint)
        } else {
            canvas.drawRect(curX - r, top, curX + r, bottom.toFloat(), mGreenPaint)
        }
    }

    override fun drawText(canvas: Canvas, view: BaseKLineChartView, position: Int, x: Float, y: Float) {
        var mX = x
        val point = view.getItem(position) as? IVolume
        var text = "VOL:${getValueFormatter().format(point?.getVolume().orZero)}  "
        canvas.drawText(text, mX, y, view.getTextPaint())
        mX += view.getTextPaint().measureText(text)
        text = "MA5:${getValueFormatter().format(point?.getMA5Volume().orZero)}  "
        canvas.drawText(text, mX, y, ma5Paint)
        mX += ma5Paint.measureText(text)
        text = "MA10:${getValueFormatter().format(point?.getMA10Volume().orZero)}"
        canvas.drawText(text, mX, y, ma10Paint)
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

}