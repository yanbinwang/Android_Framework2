package com.example.klinechart.draw

import android.graphics.Canvas
import android.graphics.Paint
import androidx.core.content.ContextCompat
import com.example.framework.utils.function.value.orZero
import com.example.klinechart.BaseKLineChartView
import com.example.klinechart.R
import com.example.klinechart.base.IChartDraw
import com.example.klinechart.base.IValueFormatter
import com.example.klinechart.entity.IMACD
import com.example.klinechart.formatter.ValueFormatter

/**
 * macd实现类
 * Created by tifezh on 2016/6/19.
 */
class MACDDraw(private val view: BaseKLineChartView) : IChartDraw<IMACD> {
    /**
     * macd 中柱子的宽度
     */
    private var mMACDWidth = 0f
    private val mRedPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mGreenPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mDIFPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mDEAPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mMACDPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mContext get() = view.context

    init {
        mRedPaint.color = ContextCompat.getColor(mContext, R.color.chart_red)
        mGreenPaint.color = ContextCompat.getColor(mContext, R.color.chart_green)
    }

    override fun drawTranslated(lastPoint: IMACD, curPoint: IMACD, lastX: Float, curX: Float, canvas: Canvas, view: BaseKLineChartView, position: Int) {
        drawMACD(canvas, view, curX, curPoint.getMacd())
        view.drawChildLine(canvas, mDIFPaint, lastX, lastPoint.getDea(), curX, curPoint.getDea())
        view.drawChildLine(canvas, mDEAPaint, lastX, lastPoint.getDif(), curX, curPoint.getDif())
    }

    override fun drawText(canvas: Canvas, view: BaseKLineChartView, position: Int, x: Float, y: Float) {
        var mX = x
        val point = view.getItem(position) as? IMACD
        var text = "MACD(12,26,9)  "
        canvas.drawText(text, mX, y, view.getTextPaint())
        mX += view.getTextPaint().measureText(text)
        text = "MACD:${view.formatValue(point?.getMacd().orZero)}  "
        canvas.drawText(text, mX, y, mMACDPaint)
        mX += mMACDPaint.measureText(text)
        text = "DIF:${view.formatValue(point?.getDif().orZero)}  "
        canvas.drawText(text, mX, y, mDEAPaint)
        mX += mDIFPaint.measureText(text)
        text = "DEA:${view.formatValue(point?.getDea().orZero)}"
        canvas.drawText(text, mX, y, mDIFPaint)
    }

    override fun getValueFormatter(): IValueFormatter {
        return ValueFormatter()
    }

    override fun getMinValue(point: IMACD): Float {
        return point.getMacd().coerceAtMost(point.getDea().coerceAtMost(point.getDif()))
    }

    override fun getMaxValue(point: IMACD): Float {
        return point.getMacd().coerceAtLeast(point.getDea().coerceAtLeast(point.getDif()))
    }

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
}