package com.example.klinechart.widget.draw

import android.graphics.Canvas
import android.graphics.Paint
import androidx.annotation.ColorInt
import com.example.framework.utils.function.value.orZero
import com.example.klinechart.bean.IWR
import com.example.klinechart.utils.formatter.IValueFormatter
import com.example.klinechart.utils.formatter.ValueFormatter
import com.example.klinechart.widget.BaseKLineChartView

/**
 * WR实现类 (威廉指标)
 * 1) 核心公式
 *  WR = (N周期最高价 - 当日收盘价) / (N周期最高价 - N周期最低价) * 100
 */
class WRDraw : IChartDraw<IWR> {
    private val mRPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    override fun drawTranslated(lastPoint: IWR?, curPoint: IWR?, lastX: Float, curX: Float, canvas: Canvas, view: BaseKLineChartView, position: Int) {
        if (lastPoint?.getR() != -10f) {
            view.drawChildLine(canvas, mRPaint, lastX, lastPoint?.getR().orZero, curX, curPoint?.getR().orZero)
        }
    }

    override fun drawText(canvas: Canvas?, view: BaseKLineChartView, position: Int, x: Float, y: Float) {
        var mX = x
        val point = view.getItem(position) as? IWR
        if (point?.getR() != -10f) {
            var text = "WR(14):"
            canvas?.drawText(text, mX, y, view.getTextPaint())
            mX += view.getTextPaint().measureText(text)
            text = "${view.formatValue(point?.getR().orZero)}\u0020"
            canvas?.drawText(text, mX, y, mRPaint)
        }
    }

    override fun getMaxValue(point: IWR?): Float {
        return point?.getR().orZero
    }

    override fun getMinValue(point: IWR?): Float {
        return point?.getR().orZero
    }

    override fun getValueFormatter(): IValueFormatter {
        return ValueFormatter()
    }

    /**
     * 设置%R颜色
     */
    fun setRColor(@ColorInt color: Int) {
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