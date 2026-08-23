package com.example.klinechart.widget.draw

import android.graphics.Canvas
import android.graphics.Paint
import androidx.annotation.ColorInt
import com.example.klinechart.bean.IWR
import com.example.klinechart.utils.formatter.value.IValueFormatter
import com.example.klinechart.utils.formatter.value.ValueFormatter
import com.example.klinechart.widget.BaseKLineChartView

/**
 * WR实现类 (威廉指标)
 * 1) 核心公式
 *  WR = (N周期最高价 - 当日收盘价) / (N周期最高价 - N周期最低价) * 100
 */
class WRDraw : IChartDraw<IWR> {
    private val mRPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    override fun drawTranslated(canvas: Canvas, view: BaseKLineChartView, position: Int, lastPoint: IWR?, curPoint: IWR?, lastX: Float, curX: Float) {
        if (lastPoint == null || curPoint == null) return
        if (lastPoint.wr != -10f) {
            view.drawChildLine(canvas, mRPaint, lastX, lastPoint.wr, curX, curPoint.wr)
        }
    }

    override fun drawText(canvas: Canvas, view: BaseKLineChartView, position: Int, x: Float, y: Float) {
        val point = view.getItem(position) as? IWR ?: return
        var curX = x
        if (point.wr != -10f) {
            var text = "WR(14):"
            canvas.drawText(text, curX, y, view.getTextPaint())
            curX += view.getTextPaint().measureText(text)
            text = "${view.formatValue(point.wr)}\u0020"
            canvas.drawText(text, curX, y, mRPaint)
        }
    }

    override fun getMaxValue(point: IWR?): Float {
        point ?: return 0f
        return point.wr
    }

    override fun getMinValue(point: IWR?): Float {
        point ?: return 0f
        return point.wr
    }

    override fun getValueFormatter(): IValueFormatter {
        return ValueFormatter()
    }

    /**
     * 设置%R颜色
     */
    fun setWRColor(@ColorInt color: Int) {
        mRPaint.color = color
    }

    /**
     * 设置曲线宽度
     */
    fun setLineWidth(width: Float) {
        mRPaint.strokeWidth = width
    }

    /**
     * 设置文字大小
     */
    fun setTextSize(textSize: Float) {
        mRPaint.textSize = textSize
    }

}