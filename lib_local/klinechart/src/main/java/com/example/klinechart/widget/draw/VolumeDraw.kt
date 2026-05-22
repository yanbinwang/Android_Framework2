package com.example.klinechart.widget.draw

import android.graphics.Canvas
import android.graphics.Paint
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import com.example.framework.utils.function.value.orZero
import com.example.framework.utils.function.value.toSafeFloat
import com.example.klinechart.R
import com.example.klinechart.bean.IVolume
import com.example.klinechart.utils.formatter.BigValueFormatter
import com.example.klinechart.utils.formatter.IValueFormatter
import com.example.klinechart.utils.ViewUtil
import com.example.klinechart.widget.BaseKLineChartView

/**
 * 成交量
 * 1) 参数释义
 *  VOL：单根K线当日成交总量
 *  MA5_VOL = 5日成交量算术平均值
 *  MA10_VOL = 10日成交量算术平均值
 */
class VolumeDraw(private val view: BaseKLineChartView) : IChartDraw<IVolume> {
    private var mPillarWidth = 0
    private val mRedPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mGreenPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val ma5Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val ma10Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mContext get() = view.context

    init {
        mRedPaint.color = ContextCompat.getColor(mContext, R.color.chart_red)
        mGreenPaint.color = ContextCompat.getColor(mContext, R.color.chart_green)
        mPillarWidth = ViewUtil.dp2px(mContext, 4f)
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
        val r = (mPillarWidth / 2).toFloat()
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
        var text = "VOL:${getValueFormatter().format(point?.getVolume().orZero)}\u0020\u0020"
        canvas?.drawText(text, mX, y, view.getTextPaint())
        mX += view.getTextPaint().measureText(text)
        text = "MA5:${getValueFormatter().format(point?.getMA5Volume().orZero)}\u0020\u0020"
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
     */
    fun setMa5Color(@ColorInt color: Int) {
        ma5Paint.color = color
    }

    /**
     * 设置 MA10 线的颜色
     */
    fun setMa10Color(@ColorInt color: Int) {
        ma10Paint.color = color
    }

    fun setLineWidth(width: Float) {
        ma5Paint.strokeWidth = width
        ma10Paint.strokeWidth = width
    }

    /**
     * 设置文字大小
     */
    fun setTextSize(textSize: Float) {
        ma5Paint.textSize = textSize
        ma10Paint.textSize = textSize
    }

}