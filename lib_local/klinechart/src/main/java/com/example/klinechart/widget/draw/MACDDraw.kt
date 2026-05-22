package com.example.klinechart.widget.draw

import android.graphics.Canvas
import android.graphics.Paint
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import com.example.framework.utils.function.value.orZero
import com.example.klinechart.R
import com.example.klinechart.bean.IMACD
import com.example.klinechart.utils.formatter.IValueFormatter
import com.example.klinechart.utils.formatter.ValueFormatter
import com.example.klinechart.widget.BaseKLineChartView

/**
 * MACD实现类
 * 1) 参数释义
 *  12：短期指数移动平均线 EMA12
 *  26：长期指数移动平均线 EMA26
 *  9：差值 DIF 的 9 周期平滑线 DEA
 * 2) 核心公式
 *  DIF = EMA12 - EMA26
 *  DEA = 9周期EMA(DIF)
 *  MACD柱 = (DIF - DEA) * 2
 */
class MACDDraw(private val view: BaseKLineChartView) : IChartDraw<IMACD> {
    private var mMACDWidth = 0f // 柱子宽度
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

    override fun drawTranslated(lastPoint: IMACD?, curPoint: IMACD?, lastX: Float, curX: Float, canvas: Canvas, view: BaseKLineChartView, position: Int) {
        drawMACD(canvas, view, curX, curPoint?.getMacd().orZero)
        view.drawChildLine(canvas, mDIFPaint, lastX, lastPoint?.getDea().orZero, curX, curPoint?.getDea().orZero)
        view.drawChildLine(canvas, mDEAPaint, lastX, lastPoint?.getDif().orZero, curX, curPoint?.getDif().orZero)
    }

    override fun drawText(canvas: Canvas?, view: BaseKLineChartView, position: Int, x: Float, y: Float) {
        var mX = x
        val point = view.getItem(position) as? IMACD
        var text = "MACD(12,26,9)\u0020\u0020"
        canvas?.drawText(text, mX, y, view.getTextPaint())
        mX += view.getTextPaint().measureText(text)
        text = "MACD:${view.formatValue(point?.getMacd().orZero)}\u0020\u0020"
        canvas?.drawText(text, mX, y, mMACDPaint)
        mX += mMACDPaint.measureText(text)
        text = "DIF:${view.formatValue(point?.getDif().orZero)}\u0020\u0020"
        canvas?.drawText(text, mX, y, mDEAPaint)
        mX += mDIFPaint.measureText(text)
        text = "DEA:" + view.formatValue(point?.getDea().orZero)
        canvas?.drawText(text, mX, y, mDIFPaint)
    }

    override fun getMaxValue(point: IMACD?): Float {
        val value = point?.getDea().orZero.coerceAtLeast(point?.getDif().orZero)
        return point?.getMacd().orZero.coerceAtLeast(value)
    }

    override fun getMinValue(point: IMACD?): Float {
        val value = point?.getDea().orZero.coerceAtMost(point?.getDif().orZero)
        return point?.getMacd().orZero.coerceAtMost(value)
    }

    override fun getValueFormatter(): IValueFormatter {
        return ValueFormatter()
    }

    /**
     * 画 MACD
     */
    private fun drawMACD(canvas: Canvas, view: BaseKLineChartView, x: Float, macd: Float) {
        val macdy = view.getChildY(macd)
        val r = mMACDWidth / 2
        val zeroy = view.getChildY(0f)
        if (macd > 0) {
            // left  top  right  bottom
            canvas.drawRect(x - r, macdy, x + r, zeroy, mRedPaint)
        } else {
            canvas.drawRect(x - r, zeroy, x + r, macdy, mGreenPaint)
        }
    }

    /**
     * 设置DIF颜色
     */
    fun setDIFColor(@ColorInt color: Int) {
        mDIFPaint.color = color
    }

    /**
     * 设置DEA颜色
     */
    fun setDEAColor(@ColorInt color: Int) {
        mDEAPaint.color = color
    }

    /**
     * 设置MACD颜色
     */
    fun setMACDColor(@ColorInt color: Int) {
        mMACDPaint.color = color
    }

    /**
     * 设置MACD的宽度
     */
    fun setMACDWidth(macdWidth: Float) {
        mMACDWidth = macdWidth
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