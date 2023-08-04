package com.github.fujianlian.klinechart.draw

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import com.example.common.utils.function.ptFloat
import com.example.framework.utils.function.value.toFixed
import com.example.framework.utils.function.value.toSafeInt
import com.github.fujianlian.klinechart.BaseKLineChartView
import com.github.fujianlian.klinechart.KLineHelper
import com.github.fujianlian.klinechart.R
import com.github.fujianlian.klinechart.base.IChartDraw
import com.github.fujianlian.klinechart.base.IValueFormatter
import com.github.fujianlian.klinechart.entity.ICandle
import com.github.fujianlian.klinechart.entity.IKLine
import com.github.fujianlian.klinechart.formatter.ValueFormatter
import java.util.*
import kotlin.math.max
import kotlin.math.min

/**
 * 主图的实现类
 * Created by tifezh on 2016/6/14.
 */
class MainDraw(view: BaseKLineChartView) : IChartDraw<ICandle> {
    private var mCandleWidth = 0f
    private var mCandleLineWidth = 0f
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mLinePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mRedPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mGreenPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val ma5Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val ma10Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val ma30Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mSelectorTextPaint =
        Paint(Paint.ANTI_ALIAS_FLAG).apply { typeface = Typeface.MONOSPACE }
    private val mSelectorBackgroundPaintStroke = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mSelectorBackgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mContext: Context

    // 是否分时
    private var isLine = false
    var status = Status.MA
    override fun drawTranslated(lastPoint: ICandle?, curPoint: ICandle?, lastX: Float, curX: Float, canvas: Canvas, view: BaseKLineChartView, position: Int) {
        if (lastPoint == null || curPoint == null) return
        if (isLine) {
            view.drawMainLine(canvas, mLinePaint, lastX, lastPoint.closePrice, curX, curPoint.closePrice)
            view.drawMainMinuteLine(canvas, paint, lastX, lastPoint.closePrice, curX, curPoint.closePrice)
            //            if (status == Status.MA) {
            //                //画ma60
            //                if (lastPoint.mA60Price != 0f) {
            //                    view.drawMainLine(canvas, ma10Paint, lastX, lastPoint.mA60Price, curX, curPoint.mA60Price)
            //                }
            //            } else if (status == Status.BOLL) {
            //                //画boll
            //                if (lastPoint.mb != 0f) {
            //                    view.drawMainLine(canvas, ma10Paint, lastX, lastPoint.mb, curX, curPoint.mb)
            //                }
            //            }
        } else {
            drawCandle(view, canvas, curX, curPoint.highPrice, curPoint.lowPrice, curPoint.openPrice, curPoint.closePrice)
            if (status == Status.MA) {
                //画ma5
                if (lastPoint.mA5Price != 0f) {
                    view.drawMainLine(canvas, ma5Paint, lastX, lastPoint.mA5Price, curX, curPoint.mA5Price)
                }
                //画ma10
                if (lastPoint.mA10Price != 0f) {
                    view.drawMainLine(canvas, ma10Paint, lastX, lastPoint.mA10Price, curX, curPoint.mA10Price)
                }
                //画ma30
                if (lastPoint.mA30Price != 0f) {
                    view.drawMainLine(canvas, ma30Paint, lastX, lastPoint.mA30Price, curX, curPoint.mA30Price)
                }
            } else if (status == Status.BOLL) {
                //画boll
                if (lastPoint.up != 0f) {
                    view.drawMainLine(canvas, ma5Paint, lastX, lastPoint.up, curX, curPoint.up)
                }
                if (lastPoint.mb != 0f) {
                    view.drawMainLine(canvas, ma10Paint, lastX, lastPoint.mb, curX, curPoint.mb)
                }
                if (lastPoint.dn != 0f) {
                    view.drawMainLine(canvas, ma30Paint, lastX, lastPoint.dn, curX, curPoint.dn)
                }
            }
        }
    }

    override fun drawText(canvas: Canvas, view: BaseKLineChartView, position: Int, x: Float, y: Float) {
        var newX = x
        var newY = y
        val point: ICandle = view.getItem(position) as? IKLine ?: return
        // 位数
        val digits = point.digits
        newY -= 5
        if (isLine) {
            //            if (status == Status.MA) {
            //                if (point.mA60Price != 0f) {
            //                    val text = "MA60:" + view.formatValue(point.mA60Price, digits) + "  "
            //                    canvas.drawText(text, newX, newY, ma10Paint)
            //                }
            //            } else if (status == Status.BOLL) {
            //                if (point.mb != 0f) {
            //                    val text = "BOLL:" + view.formatValue(point.mb, digits) + "  "
            //                    canvas.drawText(text, newX, newY, ma10Paint)
            //                }
            //            }
        } else {
            if (status == Status.MA) {
                var text: String
                if (point.mA5Price != 0f) {
                    text = "MA5:" + view.formatValue(point.mA5Price, digits) + "  "
                    canvas.drawText(text, newX, newY, ma5Paint)
                    newX += ma5Paint.measureText(text)
                }
                if (point.mA10Price != 0f) {
                    text = "MA10:" + view.formatValue(point.mA10Price, digits) + "  "
                    canvas.drawText(text, newX, newY, ma10Paint)
                    newX += ma10Paint.measureText(text)
                }
                if (point.mA20Price != 0f) {
                    text = "MA30:" + view.formatValue(point.mA30Price, digits)
                    canvas.drawText(text, newX, newY, ma30Paint)
                }
            } else if (status == Status.BOLL) {
                if (point.mb != 0f) {
                    var text = "BOLL:" + view.formatValue(point.mb, digits) + "  "
                    canvas.drawText(text, newX, newY, ma10Paint)
                    newX += ma5Paint.measureText(text)
                    text = "UB:" + view.formatValue(point.up, digits) + "  "
                    canvas.drawText(text, newX, newY, ma5Paint)
                    newX += ma10Paint.measureText(text)
                    text = "LB:" + view.formatValue(point.dn, digits)
                    canvas.drawText(text, newX, newY, ma30Paint)
                }
            }
        }
        if (view.isLongPress) {
            drawSelector(view, canvas)
        }
    }

    override fun getMaxValue(point: ICandle): Float {
        return if (status == Status.BOLL) {
            if (java.lang.Float.isNaN(point.up)) {
                if (point.mb == 0f) {
                    //                    return point.getHighPrice();
                    // BOLL指标时，最高最低点会出现超出K线布局的情况
                    // https://github.com/fujianlian/KLineChart/issues/20
                    Math.max(point.up, point.highPrice)
                } else {
                    point.mb
                }
            } else if (point.up == 0f) {
                point.highPrice
            } else {
                //                return point.getUp();
                Math.max(point.up, point.highPrice)
            }
        } else {
            Math.max(point.highPrice, point.mA30Price)
        }
    }

    override fun getMinValue(point: ICandle): Float {
        return if (status == Status.BOLL) {
            if (point.dn == 0f) {
                point.lowPrice
            } else {
                //                return point.getDn();
                min(point.dn, point.lowPrice)
            }
        } else {
            if (point.mA30Price == 0f) {
                point.lowPrice
            } else {
                min(point.mA30Price, point.lowPrice)
            }
        }
    }

    override val valueFormatter: IValueFormatter
        get() = ValueFormatter()

    /**
     * draw选择器
     *
     * @param view
     * @param canvas
     */
    private fun drawSelector(view: BaseKLineChartView, canvas: Canvas) {
        val metrics = mSelectorTextPaint.fontMetrics
        val textHeight = metrics.descent - metrics.ascent
        val index = view.selectedIndex
        val padding = 5.ptFloat
        val margin = 5.ptFloat
        var width = 0f
        val left: Float
        val top = margin + view.topPadding
        val height = padding * 8 + textHeight * 5
        val point = view.getItem(index) as? ICandle
        val strings: MutableList<String?> = ArrayList()
        strings.add(view.adapter?.getDateL(index))
        val fixed = point?.digits.toSafeInt(2)
        strings.add(KLineHelper.chartHigh + point?.highPrice.toFixed(fixed))
        strings.add(KLineHelper.chartLow + point?.lowPrice.toFixed(fixed))
        strings.add(KLineHelper.chartOpen + point?.openPrice.toFixed(fixed))
        strings.add(KLineHelper.chartClose + point?.closePrice.toFixed(fixed))
        for (s in strings) {
            width = max(width, mSelectorTextPaint.measureText(s))
        }
        width += padding * 2
        val x = view.translateXtoX(view.getX(index))
        left = if (x > view.chartWidth / 2) {
            margin
        } else {
            view.chartWidth - width - margin
        }
        val r = RectF(left, top, left + width, top + height)

        //为图片添加描边
        mSelectorBackgroundPaintStroke.style = Paint.Style.STROKE //设置填充样式为描边
        mSelectorBackgroundPaintStroke.strokeWidth = 2f //设置笔触宽度为2像素
        canvas.drawRoundRect(r, padding, padding, mSelectorBackgroundPaintStroke) //绘
        mSelectorBackgroundPaint.style = Paint.Style.FILL
        canvas.drawRoundRect(r, padding, padding, mSelectorBackgroundPaint)
        var y = top + padding * 2 + (textHeight - metrics.bottom - metrics.top) / 2
        for (s in strings) {
            canvas.drawText(s.orEmpty(), left + padding, y, mSelectorTextPaint)
            y += textHeight + padding
        }
    }

    /**
     * 画Candle
     *
     * @param canvas
     * @param x      x轴坐标
     * @param high   最高价
     * @param low    最低价
     * @param open   开盘价
     * @param close  收盘价
     */
    private fun drawCandle(view: BaseKLineChartView, canvas: Canvas, x: Float, high: Float, low: Float, open: Float, close: Float) {
        var high = high
        var low = low
        var open = open
        var close = close
        high = view.getMainY(high)
        low = view.getMainY(low)
        open = view.getMainY(open)
        close = view.getMainY(close)
        val r = mCandleWidth / 2
        val lineR = mCandleLineWidth / 2
        if (open > close) {
            //实心
            canvas.drawRect(x - r, close, x + r, open, mRedPaint)
            canvas.drawRect(x - lineR, high, x + lineR, low, mRedPaint)
        } else if (open < close) {
            canvas.drawRect(x - r, open, x + r, close, mGreenPaint)
            canvas.drawRect(x - lineR, high, x + lineR, low, mGreenPaint)
        } else {
            canvas.drawRect(x - r, open, x + r, close + 1, mRedPaint)
            canvas.drawRect(x - lineR, high, x + lineR, low, mRedPaint)
        }
    }

    /**
     * 设置蜡烛宽度
     *
     * @param candleWidth
     */
    fun setCandleWidth(candleWidth: Float) {
        mCandleWidth = candleWidth
    }

    /**
     * 设置蜡烛线宽度
     *
     * @param candleLineWidth
     */
    fun setCandleLineWidth(candleLineWidth: Float) {
        mCandleLineWidth = candleLineWidth
    }

    /**
     * 设置ma5颜色
     *
     * @param color
     */
    fun setMa5Color(color: Int) {
        ma5Paint.color = color
    }

    /**
     * 设置ma10颜色
     *
     * @param color
     */
    fun setMa10Color(color: Int) {
        ma10Paint.color = color
    }

    /**
     * 设置ma30颜色
     *
     * @param color
     */
    fun setMa30Color(color: Int) {
        ma30Paint.color = color
    }

    /**
     * 设置选择器文字颜色
     *
     * @param color
     */
    fun setSelectorTextColor(color: Int) {
        mSelectorTextPaint.color = color
    }

    /**
     * 设置选择器文字大小
     *
     * @param textSize
     */
    fun setSelectorTextSize(textSize: Float) {
        mSelectorTextPaint.textSize = textSize
    }

    /**
     * 设置选择器背景
     *
     * @param color
     */
    fun setSelectorBackgroundColor(color: Int) {
        mSelectorBackgroundPaint.color = color
    }

    /**
     * 设置选择器背景描边
     *
     * @param color
     */
    fun setmSelectorBackgroundPaintStrokeColor(color: Int) {
        mSelectorBackgroundPaintStroke.color = color
    }

    /**
     * 设置曲线宽度
     */
    fun setLineWidth(width: Float) {
        ma30Paint.strokeWidth = width
        ma10Paint.strokeWidth = width
        ma5Paint.strokeWidth = width
        mLinePaint.strokeWidth = width
    }

    /**
     * 设置文字大小
     */
    fun setTextSize(textSize: Float) {
        ma30Paint.textSize = textSize
        ma10Paint.textSize = textSize
        ma5Paint.textSize = textSize
    }

    fun isLine(): Boolean {
        return isLine
    }

    fun setLine(line: Boolean) {
        if (isLine != line) {
            isLine = line
            if (isLine) {
                setCandleWidth(7.ptFloat)
            } else {
                setCandleWidth(6.ptFloat)
            }
        }
    }

    init {
        val context = view.context
        mContext = context
        // 0：红涨绿跌  1：绿涨红跌
        if (KLineHelper.isRiseRed()) {
            mRedPaint.color = ContextCompat.getColor(context, R.color.chartRed)
            mGreenPaint.color = ContextCompat.getColor(context, R.color.chartGreen)
        } else {
            mRedPaint.color = ContextCompat.getColor(context, R.color.chartGreen)
            mGreenPaint.color = ContextCompat.getColor(context, R.color.chartRed)
        }
        mLinePaint.color = ContextCompat.getColor(context, R.color.chart_line)
        paint.color =
            ContextCompat.getColor(context, R.color.chart_line_background)
    }

    fun setColor(@ColorInt green: Int, @ColorInt red: Int, @ColorInt line: Int) {
        mRedPaint.color = red
        mGreenPaint.color = green
        mLinePaint.color = line
    }
}