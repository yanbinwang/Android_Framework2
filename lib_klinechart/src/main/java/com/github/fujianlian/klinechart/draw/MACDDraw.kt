package com.github.fujianlian.klinechart.draw

import android.graphics.Canvas
import android.graphics.Paint
import androidx.core.content.ContextCompat
import com.github.fujianlian.klinechart.BaseKLineChartView
import com.github.fujianlian.klinechart.KLineHelper
import com.github.fujianlian.klinechart.R
import com.github.fujianlian.klinechart.base.IChartDraw
import com.github.fujianlian.klinechart.base.IValueFormatter
import com.github.fujianlian.klinechart.entity.ICandle
import com.github.fujianlian.klinechart.entity.IKLine
import com.github.fujianlian.klinechart.entity.IMACD
import com.github.fujianlian.klinechart.formatter.ValueFormatter
import kotlin.math.max
import kotlin.math.min

/**
 * macd实现类
 * Created by tifezh on 2016/6/19.
 */
class MACDDraw(view: BaseKLineChartView) : IChartDraw<IMACD> {
    private val mRedPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mGreenPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mDIFPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mDEAPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mMACDPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    /**
     * macd 中柱子的宽度
     */
    private var mMACDWidth = 0f
    override fun drawTranslated(lastPoint: IMACD?, curPoint: IMACD?, lastX: Float, curX: Float, canvas: Canvas, view: BaseKLineChartView, position: Int) {
        if (lastPoint == null || curPoint == null) return
        drawMACD(canvas, view, curX, curPoint.macd)
        view.drawChildLine(canvas, mDIFPaint, lastX, lastPoint.dea, curX, curPoint.dea)
        view.drawChildLine(canvas, mDEAPaint, lastX, lastPoint.dif, curX, curPoint.dif)
    }

    override fun drawText(canvas: Canvas, view: BaseKLineChartView, position: Int, x: Float, y: Float) {
        var newX = x
        val point = view.getItem(position) as? IMACD?:return
        val point1: ICandle = view.getItem(position) as? IKLine?:return
        // 位数
        val digits = point1.digits
        var text = "MACD(12,26,9)  "
        canvas.drawText(text, newX, y, view.textPaint)
        newX += view.textPaint.measureText(text)
        text = "MACD:" + view.formatValue(point.macd, digits) + "  "
        canvas.drawText(text, newX, y, mMACDPaint)
        newX += mMACDPaint.measureText(text)
        text = "DIF:" + view.formatValue(point.dif, digits) + "  "
        canvas.drawText(text, newX, y, mDEAPaint)
        newX += mDIFPaint.measureText(text)
        text = "DEA:" + view.formatValue(point.dea, digits)
        canvas.drawText(text, newX, y, mDIFPaint)
    }

    override fun getMaxValue(point: IMACD): Float {
        return max(point.macd, max(point.dea, point.dif))
    }

    override fun getMinValue(point: IMACD): Float {
        return min(point.macd, min(point.dea, point.dif))
    }

    override val valueFormatter: IValueFormatter
        get() = ValueFormatter()

    /**
     * 画macd
     *
     * @param canvas
     * @param x
     * @param macd
     */
    private fun drawMACD(canvas: Canvas, view: BaseKLineChartView, x: Float, macd: Float) {
        val macdy = view.getChildY(macd)
        val r = mMACDWidth / 2
        val zeroy = view.getChildY(0f)
        if (macd > 0) {
            //               left   top   right  bottom
            canvas.drawRect(x - r, macdy, x + r, zeroy, mRedPaint)
        } else {
            canvas.drawRect(x - r, zeroy, x + r, macdy, mGreenPaint)
        }
    }

    /**
     * 设置DIF颜色
     */
    fun setDIFColor(color: Int) {
        mDIFPaint.color = color
    }

    /**
     * 设置DEA颜色
     */
    fun setDEAColor(color: Int) {
        mDEAPaint.color = color
    }

    /**
     * 设置MACD颜色
     */
    fun setMACDColor(color: Int) {
        mMACDPaint.color = color
    }

    /**
     * 设置MACD的宽度
     *
     * @param MACDWidth
     */
    fun setMACDWidth(MACDWidth: Float) {
        mMACDWidth = MACDWidth
    }

    /**
     * 设置曲线宽度
     */
    fun setLineWidth(width: Float) {
        mDEAPaint.strokeWidth = width
        mDIFPaint.strokeWidth = width
        mMACDPaint.strokeWidth = width
    }

    /**
     * 设置文字大小
     */
    fun setTextSize(textSize: Float) {
        mDEAPaint.textSize = textSize
        mDIFPaint.textSize = textSize
        mMACDPaint.textSize = textSize
    }

    init {
        val context = view.context
        // 0：红涨绿跌  1：绿涨红跌
        if (KLineHelper.isRiseRed()) {
            mRedPaint.color = ContextCompat.getColor(context, R.color.chartRed)
            mGreenPaint.color = ContextCompat.getColor(context, R.color.chartGreen)
        } else {
            mRedPaint.color = ContextCompat.getColor(context, R.color.chartGreen)
            mGreenPaint.color = ContextCompat.getColor(context, R.color.chartRed)
        }
    }
}