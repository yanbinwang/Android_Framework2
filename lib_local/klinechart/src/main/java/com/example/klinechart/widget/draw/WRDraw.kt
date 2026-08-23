package com.example.klinechart.widget.draw

import android.graphics.Canvas
import android.graphics.Paint
import androidx.annotation.ColorInt
import com.example.framework.utils.function.value.orZero
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

    override fun drawTranslated(lastPoint: IWR?, curPoint: IWR?, lastX: Float, curX: Float, canvas: Canvas, view: BaseKLineChartView, position: Int) {
        if (lastPoint?.wr != -10f) {
            view.drawChildLine(canvas, mRPaint, lastX, lastPoint?.wr.orZero, curX, curPoint?.wr.orZero)
        }
    }

    override fun drawText(canvas: Canvas, view: BaseKLineChartView, position: Int, x: Float, y: Float) {
        var mX = x
        val point = view.getItem(position) as? IWR
        if (point?.wr != -10f) {
            var text = "WR(14):"
            canvas.drawText(text, mX, y, view.getTextPaint())
            mX += view.getTextPaint().measureText(text)
            text = "${view.formatValue(point?.wr.orZero)}\u0020"
            canvas.drawText(text, mX, y, mRPaint)
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