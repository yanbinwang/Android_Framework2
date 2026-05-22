package com.example.klinechart.widget.draw

import android.graphics.Canvas
import android.graphics.Paint
import androidx.annotation.ColorInt
import com.example.framework.utils.function.value.orZero
import com.example.klinechart.bean.IKDJ
import com.example.klinechart.utils.formatter.IValueFormatter
import com.example.klinechart.utils.formatter.ValueFormatter
import com.example.klinechart.widget.BaseKLineChartView

/**
 * KDJ实现类
 * 1) 参数含义
 *  K=9：统计 9 根 K 线内最高价、最低价、收盘价，计算原始随机值
 *  D=3：对 K 值做 3 周期平滑均线过滤，弱化杂波
 *  J=3：基于 K、D 再做偏移计算，敏感度最高
 * 2) 计算公式
 *  未成熟随机值 RSV = (当日收盘价 - 9 周期最低价)÷(9 周期最高价 - 9 周期最低价)×100
 *  K 值 = 前一日 K×2/3 + 当日 RSV×1/3
 *  D 值 = 前一日 D×2/3 + 当日 K×1/3
 *  J 值 = 3×K - 2×D
 */
class KDJDraw : IChartDraw<IKDJ> {
    private val mKPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mDPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mJPaint = Paint(Paint.ANTI_ALIAS_FLAG)

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
            var text = "KDJ(14,1,3)\u0020\u0020"
            canvas?.drawText(text, mX, y, view.getTextPaint())
            mX += view.getTextPaint().measureText(text)
            text = "K:${view.formatValue(point?.getK().orZero)}\u0020"
            canvas?.drawText(text, mX, y, mKPaint)
            mX += mKPaint.measureText(text)
            if (point?.getD() != 0f) {
                text = "D:${view.formatValue(point?.getD().orZero)}\u0020"
                canvas?.drawText(text, mX, y, mDPaint)
                mX += mDPaint.measureText(text)
                text = "J:${view.formatValue(point?.getJ().orZero)}\u0020"
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
    fun setKColor(@ColorInt color: Int) {
        mKPaint.color = color
    }

    /**
     * 设置D颜色
     */
    fun setDColor(@ColorInt color: Int) {
        mDPaint.color = color
    }

    /**
     * 设置J颜色
     */
    fun setJColor(@ColorInt color: Int) {
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