package com.example.klinechart.draw

import android.graphics.Canvas
import android.graphics.Paint
import com.example.framework.utils.function.value.orZero
import com.example.klinechart.BaseKLineChartView
import com.example.klinechart.base.IChartDraw
import com.example.klinechart.base.IValueFormatter
import com.example.klinechart.entity.IWR
import com.example.klinechart.formatter.ValueFormatter

/**
 * WR实现类
 * Created by tifezh on 2016/6/19.
 */
class WRDraw(private val view: BaseKLineChartView) : IChartDraw<IWR> {
    private val mRPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mContext get() = view.context

    override fun drawTranslated(lastPoint: IWR, curPoint: IWR, lastX: Float, curX: Float, canvas: Canvas, view: BaseKLineChartView, position: Int) {
        if (lastPoint.getR() != -10f) {
            view.drawChildLine(canvas, mRPaint, lastX, lastPoint.getR(), curX, curPoint.getR())
        }
    }

    override fun drawText(canvas: Canvas, view: BaseKLineChartView, position: Int, x: Float, y: Float) {
        val point = view.getItem(position) as? IWR
        if (point?.getR() != -10f) {
            var mX = x
            var text = "WR(14):"
            canvas.drawText(text, mX, y, view.getTextPaint())
            mX += view.getTextPaint().measureText(text)
            text = "${view.formatValue(point?.getR().orZero)} "
            canvas.drawText(text, mX, y, mRPaint)
        }
    }

    override fun getValueFormatter(): IValueFormatter {
        return ValueFormatter()
    }

    override fun getMinValue(point: IWR): Float {
        return point.getR()
    }

    override fun getMaxValue(point: IWR): Float {
        return point.getR()
    }

    /**
     * 设置%R颜色
     */
    fun setRColor(color: Int) {
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