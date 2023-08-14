package com.github.fujianlian.klinechart.draw

import android.graphics.Canvas
import android.graphics.Paint
import com.example.framework.utils.function.value.orZero
import com.github.fujianlian.klinechart.BaseKLineChartView
import com.github.fujianlian.klinechart.base.IChartDraw
import com.github.fujianlian.klinechart.base.IValueFormatter
import com.github.fujianlian.klinechart.entity.IKDJ
import com.github.fujianlian.klinechart.formatter.ValueFormatter

/**
 * KDJ实现类
 * Created by tifezh on 2016/6/19.
 */
class KDJDraw(view: BaseKLineChartView) : IChartDraw<IKDJ> {
    private val mKPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mDPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mJPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    override fun drawTranslated(lastPoint: IKDJ?, curPoint: IKDJ, lastX: Float, curX: Float, canvas: Canvas, view: BaseKLineChartView, position: Int) {
        if (lastPoint?.getK() != 0f) {
            view.drawChildLine(canvas, mKPaint, lastX, lastPoint?.getK().orZero, curX, curPoint.getK())
        }
        if (lastPoint?.getD() != 0f) {
            view.drawChildLine(canvas, mDPaint, lastX, lastPoint?.getD().orZero, curX, curPoint.getD())
        }
        if (lastPoint?.getJ() != 0f) {
            view.drawChildLine(canvas, mJPaint, lastX, lastPoint?.getJ().orZero, curX, curPoint.getJ())
        }
    }

    override fun drawText(canvas: Canvas, view: BaseKLineChartView, position: Int, x: Float, y: Float) {
        var valueX = x
        val point = view.getItem(position) as? IKDJ
        if (point?.getK() != 0f) {
            var text = "KDJ(14,1,3)  "
            canvas.drawText(text, valueX, y, view.getTextPaint())
            valueX += view.getTextPaint().measureText(text)
            text = "K:${view.formatValue(point?.getK().orZero)} "
            canvas.drawText(text, valueX, y, mKPaint)
            valueX += mKPaint.measureText(text)
            if (point?.getD() != 0f) {
                text = "D:${view.formatValue(point?.getD().orZero)} "
                canvas.drawText(text, valueX, y, mDPaint)
                valueX += mDPaint.measureText(text)
                text = "J:${view.formatValue(point?.getJ().orZero)} "
                canvas.drawText(text, valueX, y, mJPaint)
            }
        }
    }

    override fun getValueFormatter(): IValueFormatter {
        return ValueFormatter()
    }

    override fun getMinValue(point: IKDJ): Float {
        return point.getK().coerceAtMost(point.getD().coerceAtMost(point.getJ()))
    }

    override fun getMaxValue(point: IKDJ): Float {
        return point.getK().coerceAtLeast(point.getD().coerceAtLeast(point.getJ()))
    }

    /**
     * 设置K颜色
     */
    fun setKColor(color: Int) {
        mKPaint.color = color
    }

    /**
     * 设置D颜色
     */
    fun setDColor(color: Int) {
        mDPaint.color = color
    }

    /**
     * 设置J颜色
     */
    fun setJColor(color: Int) {
        mJPaint.color = color
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