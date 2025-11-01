package com.example.klinechart.draw

import android.graphics.Canvas
import android.graphics.Paint
import com.example.framework.utils.function.value.orZero
import com.example.klinechart.entity.IKDJ
import com.example.klinechart.formatter.IValueFormatter
import com.example.klinechart.formatter.ValueFormatter
import com.example.klinechart.widget.BaseKLineChartView

/**
 * KDJ实现类
 */
class KDJDraw(view: BaseKLineChartView) : IChartDraw<IKDJ> {
    private val mKPaint by lazy { Paint(Paint.ANTI_ALIAS_FLAG) }
    private val mDPaint by lazy { Paint(Paint.ANTI_ALIAS_FLAG) }
    private val mJPaint by lazy { Paint(Paint.ANTI_ALIAS_FLAG) }

    override fun drawTranslated(lastPoint: IKDJ?, curPoint: IKDJ?, lastX: Float, curX: Float, canvas: Canvas, view: BaseKLineChartView, position: Int) {
        if (lastPoint?.getK() != 0f) {
            view.drawChildLine(canvas, mKPaint, lastX, lastPoint?.getK().orZero, curX, curPoint?.getK().orZero)
        }
        if (lastPoint?.getD() != 0f) {
            view.drawChildLine(canvas, mDPaint, lastX, lastPoint?.getD().orZero, curX, curPoint?.getD().orZero)
        }
        if (lastPoint?.getJ() != 0f) {
            view.drawChildLine(canvas, mJPaint, lastX, lastPoint?.getJ().orZero, curX, curPoint?.getJ().orZero)
        }
    }

    override fun drawText(canvas: Canvas?, view: BaseKLineChartView, position: Int, x: Float, y: Float) {
        var mX = x
        val point = view.getItem(position) as? IKDJ
        if (point?.getK() != 0f) {
            var text = "KDJ(14,1,3)  "
            canvas?.drawText(text, mX, y, view.getTextPaint())
            mX += view.getTextPaint().measureText(text)
            text = "K:${view.formatValue(point?.getK().orZero)} "
            canvas?.drawText(text, mX, y, mKPaint)
            mX += mKPaint.measureText(text)
            if (point?.getD() != 0f) {
                text = "D:${view.formatValue(point?.getD().orZero)} "
                canvas?.drawText(text, mX, y, mDPaint)
                mX += mDPaint.measureText(text)
                text = "J:${view.formatValue(point?.getJ().orZero)} "
                canvas?.drawText(text, mX, y, mJPaint)
            }
        }
    }

    override fun getMaxValue(point: IKDJ?): Float {
        val value = point?.getD().orZero.coerceAtLeast(point?.getJ().orZero)
        return point?.getK().orZero.coerceAtLeast(value)
    }

    override fun getMinValue(point: IKDJ?): Float {
        val value = point?.getD().orZero.coerceAtMost(point?.getJ().orZero)
        return point?.getK().orZero.coerceAtMost(value)
    }

    override fun getValueFormatter(): IValueFormatter {
        return ValueFormatter()
    }

    /**
     * 设置K颜色
     */
    fun setKColor(color: Int) {
        mKPaint.setColor(color)
    }

    /**
     * 设置D颜色
     */
    fun setDColor(color: Int) {
        mDPaint.setColor(color)
    }

    /**
     * 设置J颜色
     */
    fun setJColor(color: Int) {
        mJPaint.setColor(color)
    }

    /**
     * 设置曲线宽度
     */
    fun setLineWidth(width: Float) {
        mKPaint.strokeWidth = width
        mDPaint.strokeWidth = width
        mJPaint.strokeWidth = width
    }

    /**
     * 设置文字大小
     */
    fun setTextSize(textSize: Float) {
        mKPaint.textSize = textSize
        mDPaint.textSize = textSize
        mJPaint.textSize = textSize
    }

}