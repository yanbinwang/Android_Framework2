package com.github.fujianlian.klinechart.draw

import android.graphics.Canvas
import android.graphics.Paint
import com.github.fujianlian.klinechart.BaseKLineChartView
import com.github.fujianlian.klinechart.base.IChartDraw
import com.github.fujianlian.klinechart.base.IValueFormatter
import com.github.fujianlian.klinechart.entity.ICandle
import com.github.fujianlian.klinechart.entity.IKDJ
import com.github.fujianlian.klinechart.entity.IKLine
import com.github.fujianlian.klinechart.formatter.ValueFormatter
import kotlin.math.max
import kotlin.math.min

/**
 * KDJ实现类
 * Created by tifezh on 2016/6/19.
 */
class KDJDraw(view: BaseKLineChartView) : IChartDraw<IKDJ> {
    private val mKPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mDPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mJPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    override fun drawTranslated(lastPoint: IKDJ?, curPoint: IKDJ?, lastX: Float, curX: Float, canvas: Canvas, view: BaseKLineChartView, position: Int) {
        if (lastPoint == null || curPoint == null) return
        if (lastPoint.k != 0f) {
            view.drawChildLine(canvas, mKPaint, lastX, lastPoint.k, curX, curPoint.k)
        }
        if (lastPoint.d != 0f) {
            view.drawChildLine(canvas, mDPaint, lastX, lastPoint.d, curX, curPoint.d)
        }
        if (lastPoint.j != 0f) {
            view.drawChildLine(canvas, mJPaint, lastX, lastPoint.j, curX, curPoint.j)
        }
    }

    override fun drawText(canvas: Canvas, view: BaseKLineChartView, position: Int, x: Float, y: Float) {
        var newX = x
        val point = view.getItem(position) as? IKDJ ?: return
        val point1: ICandle = view.getItem(position) as? IKLine ?: return
        // 位数
        val digits = point1.digits
        if (point.k != 0f) {
            var text = "KDJ(14,1,3)  "
            canvas.drawText(text, newX, y, view.textPaint)
            newX += view.textPaint.measureText(text)
            text = "K:" + view.formatValue(point.k, digits) + " "
            canvas.drawText(text, newX, y, mKPaint)
            newX += mKPaint.measureText(text)
            if (point.d != 0f) {
                text = "D:" + view.formatValue(point.d, digits) + " "
                canvas.drawText(text, newX, y, mDPaint)
                newX += mDPaint.measureText(text)
                text = "J:" + view.formatValue(point.j, digits) + " "
                canvas.drawText(text, newX, y, mJPaint)
            }
        }
    }

    override fun getMaxValue(point: IKDJ): Float {
        return max(point.k, max(point.d, point.j))
    }

    override fun getMinValue(point: IKDJ): Float {
        return min(point.k, min(point.d, point.j))
    }

    override val valueFormatter: IValueFormatter
        get() = ValueFormatter()

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