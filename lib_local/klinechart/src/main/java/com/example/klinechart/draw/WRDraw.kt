package com.example.klinechart.draw

import android.graphics.Canvas
import android.graphics.Paint
import com.example.framework.utils.function.value.orZero
import com.example.klinechart.entity.IWR
import com.example.klinechart.formatter.IValueFormatter
import com.example.klinechart.formatter.ValueFormatter
import com.example.klinechart.widget.BaseKLineChartView

/**
 * KDJ实现类
 */
class WRDraw(view: BaseKLineChartView) : IChartDraw<IWR> {
    private val mRPaint by lazy { Paint(Paint.ANTI_ALIAS_FLAG) }

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
            text = "${view.formatValue(point?.getR().orZero)} "
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
    fun setRColor(color: Int) {
        mRPaint.setColor(color)
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