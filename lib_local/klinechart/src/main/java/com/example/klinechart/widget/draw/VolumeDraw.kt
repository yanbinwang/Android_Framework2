package com.example.klinechart.widget.draw

import android.graphics.Canvas
import android.graphics.Paint
import androidx.annotation.ColorInt
import com.example.common.utils.function.pt
import com.example.framework.utils.function.color
import com.example.framework.utils.function.value.toSafeFloat
import com.example.klinechart.R
import com.example.klinechart.bean.IVolume
import com.example.klinechart.utils.formatter.value.BigValueFormatter
import com.example.klinechart.utils.formatter.value.IValueFormatter
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
        mRedPaint.color = mContext.color(R.color.chart_red)
        mGreenPaint.color = mContext.color(R.color.chart_green)
        mPillarWidth = 4.pt
    }

    override fun drawTranslated(canvas: Canvas, view: BaseKLineChartView, position: Int, lastPoint: IVolume?, curPoint: IVolume?, lastX: Float, curX: Float) {
        if (lastPoint == null || curPoint == null) return
        drawHistogram(canvas, curPoint, curX, view)
        if (lastPoint.ma5Volume != 0f) {
            view.drawVolLine(canvas, ma5Paint, lastX, lastPoint.ma5Volume, curX, curPoint.ma5Volume)
        }
        if (lastPoint.ma10Volume != 0f) {
            view.drawVolLine(canvas, ma10Paint, lastX, lastPoint.ma10Volume, curX, curPoint.ma10Volume)
        }
    }

    private fun drawHistogram(canvas: Canvas, curPoint: IVolume, curX: Float, view: BaseKLineChartView) {
        val r = (mPillarWidth / 2).toFloat()
        val top = view.getVolY(curPoint.volume)
        val bottom = view.getVolRect()?.bottom
        // 涨
        if (curPoint.closePrice >= curPoint.openPrice) {
            canvas.drawRect(curX - r, top, curX + r, bottom.toSafeFloat(), mRedPaint)
        } else {
            canvas.drawRect(curX - r, top, curX + r, bottom.toSafeFloat(), mGreenPaint)
        }
    }

    override fun drawText(canvas: Canvas, view: BaseKLineChartView, position: Int, x: Float, y: Float) {
        val point = view.getItem(position) as? IVolume ?: return
        var curX = x
        var text = "VOL:${getValueFormatter().format(point.volume)}\u0020\u0020"
        canvas.drawText(text, curX, y, view.getTextPaint())
        curX += view.getTextPaint().measureText(text)
        text = "MA5:${getValueFormatter().format(point.ma5Volume)}\u0020\u0020"
        canvas.drawText(text, curX, y, ma5Paint)
        curX += ma5Paint.measureText(text)
        text = "MA10:${getValueFormatter().format(point.ma10Volume)}"
        canvas.drawText(text, curX, y, ma10Paint)
    }

    override fun getMaxValue(point: IVolume?): Float {
        point ?: return 0f
        val value = point.ma5Volume.coerceAtLeast(point.ma10Volume)
        return point.volume.coerceAtLeast(value)
    }

    override fun getMinValue(point: IVolume?): Float {
        point ?: return 0f
        val value = point.ma5Volume.coerceAtMost(point.ma10Volume)
        return point.volume.coerceAtMost(value)
    }

    override fun getValueFormatter(): IValueFormatter {
        return BigValueFormatter()
    }

    /**
     * 设置 MA5 线的颜色
     */
    fun setMA5Color(@ColorInt color: Int) {
        ma5Paint.color = color
    }

    /**
     * 设置 MA10 线的颜色
     */
    fun setMA10Color(@ColorInt color: Int) {
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