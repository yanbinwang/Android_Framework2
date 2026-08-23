package com.example.klinechart.widget.draw

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import com.example.common.utils.function.pt
import com.example.common.utils.function.ptFloat
import com.example.framework.utils.function.value.orZero
import com.example.framework.utils.function.value.toSafeFloat
import com.example.klinechart.R
import com.example.klinechart.bean.ICandle
import com.example.klinechart.utils.formatter.value.IValueFormatter
import com.example.klinechart.utils.formatter.value.ValueFormatter
import com.example.klinechart.widget.BaseKLineChartView
import com.example.klinechart.widget.KLineChartView
import kotlin.math.max

/**
 * 主图的实现类
 */
class MainDraw(private val view: BaseKLineChartView) : IChartDraw<ICandle> {
    private var mCandleWidth = 0f
    private var mCandleLineWidth = 0f
    private var isLine = false // 是否分时
    private var mCandleSolid = true
    private var mStatus = Status.MA
    private val mPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mLinePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mRedPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mGreenPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val ma5Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val ma10Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val ma30Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mSelectorTextPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mSelectorBackgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mContext get() = view.context
    private val mKChartView get() = view as? KLineChartView

    /**
     * 主图状态
     */
    enum class Status {
        MA, BOLL, NONE
    }

    init {
        mRedPaint.color = ContextCompat.getColor(mContext, R.color.chart_red)
        mGreenPaint.color = ContextCompat.getColor(mContext, R.color.chart_green)
        mLinePaint.color = ContextCompat.getColor(mContext, R.color.chart_line)
        mPaint.color = ContextCompat.getColor(mContext, R.color.chart_line_background)
    }

    override fun drawTranslated(lastPoint: ICandle?, curPoint: ICandle?, lastX: Float, curX: Float, canvas: Canvas, view: BaseKLineChartView, position: Int) {
        if (isLine) {
            view.drawMainLine(canvas, mLinePaint, lastX, lastPoint?.closePrice.orZero, curX, curPoint?.closePrice.orZero)
            view.drawMainMinuteLine(canvas, mPaint, lastX, lastPoint?.closePrice.orZero, curX, curPoint?.closePrice.orZero)
            if (mStatus == Status.MA) {
                // 画ma60
                if (lastPoint?.ma60Price != 0f) {
                    view.drawMainLine(canvas, ma10Paint, lastX, lastPoint?.ma60Price.orZero, curX, curPoint?.ma60Price.orZero)
                }
            } else if (mStatus == Status.BOLL) {
                // 画boll
                if (lastPoint?.mb != 0f) {
                    view.drawMainLine(canvas, ma10Paint, lastX, lastPoint?.mb.orZero, curX, curPoint?.mb.orZero)
                }
            }
        } else {
            drawCandle(view, canvas, curX, curPoint?.highPrice.orZero, curPoint?.lowPrice.orZero, curPoint?.openPrice.orZero, curPoint?.closePrice.orZero)
            if (mStatus == Status.MA) {
                // 画ma5
                if (lastPoint?.ma5Price != 0f) {
                    view.drawMainLine(canvas, ma5Paint, lastX, lastPoint?.ma5Price.orZero, curX, curPoint?.ma5Price.orZero)
                }
                // 画ma10
                if (lastPoint?.ma10Price != 0f) {
                    view.drawMainLine(canvas, ma10Paint, lastX, lastPoint?.ma10Price.orZero, curX, curPoint?.ma10Price.orZero)
                }
                // 画ma30
                if (lastPoint?.ma30Price != 0f) {
                    view.drawMainLine(canvas, ma30Paint, lastX, lastPoint?.ma30Price.orZero, curX, curPoint?.ma30Price.orZero)
                }
            } else if (mStatus == Status.BOLL) {
                // 画boll
                if (lastPoint?.up != 0f) {
                    view.drawMainLine(canvas, ma5Paint, lastX, lastPoint?.up.orZero, curX, curPoint?.up.orZero)
                }
                if (lastPoint?.mb != 0f) {
                    view.drawMainLine(canvas, ma10Paint, lastX, lastPoint?.mb.orZero, curX, curPoint?.mb.orZero)
                }
                if (lastPoint?.dn != 0f) {
                    view.drawMainLine(canvas, ma30Paint, lastX, lastPoint?.dn.orZero, curX, curPoint?.dn.orZero)
                }
            }
        }
    }

    override fun drawText(canvas: Canvas, view: BaseKLineChartView, position: Int, x: Float, y: Float) {
        var mX = x
        var mY = y
        val point = view.getItem(position) as? ICandle
        mY -= 5
        if (isLine) {
            if (mStatus == Status.MA) {
                if (point?.ma60Price != 0f) {
                    val text = "MA60:${view.formatValue(point?.ma60Price.orZero)}\u0020\u0020"
                    canvas.drawText(text, mX, mY, ma10Paint)
                }
            } else if (mStatus == Status.BOLL) {
                if (point?.mb != 0f) {
                    val text = "BOLL:${view.formatValue(point?.mb.orZero)}\u0020\u0020"
                    canvas.drawText(text, mX, mY, ma10Paint)
                }
            }
        } else {
            if (mStatus == Status.MA) {
                var text: String?
                if (point?.ma5Price != 0f) {
                    text = "MA5:${view.formatValue(point?.ma5Price.orZero)}\u0020\u0020"
                    canvas.drawText(text, mX, mY, ma5Paint)
                    mX += ma5Paint.measureText(text)
                }
                if (point?.ma10Price != 0f) {
                    text = "MA10:${view.formatValue(point?.ma10Price.orZero)}\u0020\u0020"
                    canvas.drawText(text, mX, mY, ma10Paint)
                    mX += ma10Paint.measureText(text)
                }
                if (point?.ma20Price != 0f) {
                    text = "MA30:${view.formatValue(point?.ma30Price.orZero)}"
                    canvas.drawText(text, mX, mY, ma30Paint)
                }
            } else if (mStatus == Status.BOLL) {
                if (point?.mb != 0f) {
                    var text = "BOLL:${view.formatValue(point?.mb.orZero)}\u0020\u0020"
                    canvas.drawText(text, mX, mY, ma10Paint)
                    mX += ma5Paint.measureText(text)
                    text = "UB:${view.formatValue(point?.up.orZero)}\u0020\u0020"
                    canvas.drawText(text, mX, mY, ma5Paint)
                    mX += ma10Paint.measureText(text)
                    text = "LB:${view.formatValue(point?.dn.orZero)}"
                    canvas.drawText(text, mX, mY, ma30Paint)
                }
            }
        }
        if (view.isLongPress()) {
            drawSelector(view, canvas)
        }
    }

    override fun getMaxValue(point: ICandle?): Float {
        point ?: return 0f
        return if (mStatus == Status.BOLL) {
            if (java.lang.Float.isNaN(point.up)) {
                if (point.mb == 0f) {
                    point.highPrice.orZero
                } else {
                    point.mb
                }
            } else if (point.up == 0f) {
                point.highPrice
            } else {
                point.up
            }
        } else {
            point.highPrice.coerceAtLeast(point.ma30Price)
        }
    }

    override fun getMinValue(point: ICandle?): Float {
        point ?: return 0f
        return if (mStatus == Status.BOLL) {
            if (point.dn == 0f) {
                point.lowPrice
            } else {
                point.dn
            }
        } else {
            if (point.ma30Price == 0f) {
                point.lowPrice
            } else {
                point.ma30Price.coerceAtMost(point.lowPrice)
            }
        }
    }

    override fun getValueFormatter(): IValueFormatter {
        return ValueFormatter()
    }

    fun setStatus(status: Status) {
        mStatus = status
    }

    fun getStatus(): Status {
        return mStatus
    }

    /**
     * 画Candle
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
            // 实心
            if (mCandleSolid) {
                canvas.drawRect(x - r, close, x + r, open, mRedPaint)
                canvas.drawRect(x - lineR, high, x + lineR, low, mRedPaint)
            } else {
                mRedPaint.strokeWidth = mCandleLineWidth
                canvas.drawLine(x, high, x, close, mRedPaint)
                canvas.drawLine(x, open, x, low, mRedPaint)
                canvas.drawLine(x - r + lineR, open, x - r + lineR, close, mRedPaint)
                canvas.drawLine(x + r - lineR, open, x + r - lineR, close, mRedPaint)
                mRedPaint.strokeWidth = mCandleLineWidth * view.scaleX
                canvas.drawLine(x - r, open, x + r, open, mRedPaint)
                canvas.drawLine(x - r, close, x + r, close, mRedPaint)
            }
        } else if (open < close) {
            canvas.drawRect(x - r, open, x + r, close, mGreenPaint)
            canvas.drawRect(x - lineR, high, x + lineR, low, mGreenPaint)
        } else {
            canvas.drawRect(x - r, open, x + r, close + 1, mRedPaint)
            canvas.drawRect(x - lineR, high, x + lineR, low, mRedPaint)
        }
    }

    /**
     * draw选择器
     */
    private fun drawSelector(view: BaseKLineChartView, canvas: Canvas?) {
        val metrics = mSelectorTextPaint.fontMetrics
        val textHeight = metrics.descent - metrics.ascent
        val index = view.getSelectedIndex()
        val padding = 5.pt
        val margin = 5.pt
        var width = 0f
        val left: Float
        val top = margin + view.getTopPadding()
        val height = padding * 8 + textHeight * 5
        val point = view.getItem(index) as ICandle
        val strings = ArrayList<String>()
        strings.add(view.getAdapter()?.getDate(index).orEmpty())
        strings.add("高:${point.highPrice}")
        strings.add("低:${point.lowPrice}")
        strings.add("开:${point.openPrice}")
        strings.add("收:${point.closePrice}")
        for (s in strings) {
            width = max(width, mSelectorTextPaint.measureText(s))
        }
        width += padding * 2
        val x = view.translateXtoX(view.getX(index))
        if (x > view.getChartWidth() / 2) {
            left = margin.toSafeFloat()
        } else {
            left = view.getChartWidth() - width - margin
        }
        val r = RectF(left, top, left + width, top + height)
        canvas?.drawRoundRect(r, padding.toSafeFloat(), padding.toSafeFloat(), mSelectorBackgroundPaint)
        var y = top + padding * 2 + (textHeight - metrics.bottom - metrics.top) / 2
        for (s in strings) {
            canvas?.drawText(s, left + padding, y, mSelectorTextPaint)
            y += textHeight + padding
        }
    }

    /**
     * 设置蜡烛宽度
     */
    fun setCandleWidth(candleWidth: Float) {
        mCandleWidth = candleWidth
    }

    /**
     * 设置蜡烛线宽度
     */
    fun setCandleLineWidth(candleLineWidth: Float) {
        mCandleLineWidth = candleLineWidth
    }

    /**
     * 设置ma5颜色
     */
    fun setMA5Color(@ColorInt color: Int) {
        ma5Paint.color = color
    }

    /**
     * 设置ma10颜色
     */
    fun setMA10Color(@ColorInt color: Int) {
        ma10Paint.color = color
    }

    /**
     * 设置ma30颜色
     */
    fun setMA30Color(@ColorInt color: Int) {
        ma30Paint.color = color
    }

    /**
     * 设置选择器文字颜色
     */
    fun setSelectorTextColor(@ColorInt color: Int) {
        mSelectorTextPaint.color = color
    }

    /**
     * 设置选择器文字大小 (只认 PX（像素）)
     */
    fun setSelectorTextSize(textSize: Float) {
        mSelectorTextPaint.textSize = textSize
    }

    /**
     * 设置选择器背景
     */
    fun setSelectorBackgroundColor(@ColorInt color: Int) {
        mSelectorBackgroundPaint.color = color
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

    /**
     * 蜡烛是否实心
     */
    fun setCandleSolid(candleSolid: Boolean) {
        mCandleSolid = candleSolid
    }

    fun setLine(line: Boolean) {
        if (isLine != line) {
            isLine = line
            if (isLine) {
                mKChartView?.setCandleWidth(7.ptFloat)
            } else {
                mKChartView?.setCandleWidth(6.ptFloat)
            }
        }
    }

    fun isLine(): Boolean {
        return isLine
    }

}