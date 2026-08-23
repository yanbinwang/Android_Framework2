package com.example.klinechart.widget.draw

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import com.example.common.utils.function.pt
import com.example.common.utils.function.ptFloat
import com.example.framework.utils.function.color
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
        mRedPaint.color = mContext.color(R.color.chart_red)
        mGreenPaint.color = mContext.color(R.color.chart_green)
        mLinePaint.color = mContext.color(R.color.chart_line)
        mPaint.color = mContext.color(R.color.chart_line_background)
    }

    override fun drawTranslated(canvas: Canvas, view: BaseKLineChartView, position: Int, lastPoint: ICandle?, curPoint: ICandle?, lastX: Float, curX: Float) {
        if (lastPoint == null || curPoint == null) return
        if (isLine) {
            view.drawMainLine(canvas, mLinePaint, lastX, lastPoint.closePrice, curX, curPoint.closePrice)
            view.drawMainMinuteLine(canvas, mPaint, lastX, lastPoint.closePrice, curX, curPoint.closePrice)
            if (mStatus == Status.MA) {
                // 画ma60
                if (lastPoint.ma60Price != 0f) {
                    view.drawMainLine(canvas, ma10Paint, lastX, lastPoint.ma60Price, curX, curPoint.ma60Price)
                }
            } else if (mStatus == Status.BOLL) {
                // 画boll
                if (lastPoint.mb != 0f) {
                    view.drawMainLine(canvas, ma10Paint, lastX, lastPoint.mb, curX, curPoint.mb)
                }
            }
        } else {
            drawCandle(view, canvas, curX, curPoint.highPrice, curPoint.lowPrice, curPoint.openPrice, curPoint.closePrice)
            if (mStatus == Status.MA) {
                // 画ma5
                if (lastPoint.ma5Price != 0f) {
                    view.drawMainLine(canvas, ma5Paint, lastX, lastPoint.ma5Price, curX, curPoint.ma5Price)
                }
                // 画ma10
                if (lastPoint.ma10Price != 0f) {
                    view.drawMainLine(canvas, ma10Paint, lastX, lastPoint.ma10Price, curX, curPoint.ma10Price)
                }
                // 画ma30
                if (lastPoint.ma30Price != 0f) {
                    view.drawMainLine(canvas, ma30Paint, lastX, lastPoint.ma30Price, curX, curPoint.ma30Price)
                }
            } else if (mStatus == Status.BOLL) {
                // 画boll
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
        val point = view.getItem(position) as? ICandle ?: return
        var curX = x
        var curY = y
        curY -= 5
        if (isLine) {
            if (mStatus == Status.MA) {
                if (point.ma60Price != 0f) {
                    val text = "MA60:${view.formatValue(point.ma60Price)}\u0020\u0020"
                    canvas.drawText(text, curX, curY, ma10Paint)
                }
            } else if (mStatus == Status.BOLL) {
                if (point.mb != 0f) {
                    val text = "BOLL:${view.formatValue(point.mb)}\u0020\u0020"
                    canvas.drawText(text, curX, curY, ma10Paint)
                }
            }
        } else {
            if (mStatus == Status.MA) {
                var text: String?
                if (point.ma5Price != 0f) {
                    text = "MA5:${view.formatValue(point.ma5Price)}\u0020\u0020"
                    canvas.drawText(text, curX, curY, ma5Paint)
                    curX += ma5Paint.measureText(text)
                }
                if (point.ma10Price != 0f) {
                    text = "MA10:${view.formatValue(point.ma10Price)}\u0020\u0020"
                    canvas.drawText(text, curX, curY, ma10Paint)
                    curX += ma10Paint.measureText(text)
                }
                if (point.ma20Price != 0f) {
                    text = "MA30:${view.formatValue(point.ma30Price)}"
                    canvas.drawText(text, curX, curY, ma30Paint)
                }
            } else if (mStatus == Status.BOLL) {
                if (point.mb != 0f) {
                    var text = "BOLL:${view.formatValue(point.mb)}\u0020\u0020"
                    canvas.drawText(text, curX, curY, ma10Paint)
                    curX += ma5Paint.measureText(text)
                    text = "UB:${view.formatValue(point.up)}\u0020\u0020"
                    canvas.drawText(text, curX, curY, ma5Paint)
                    curX += ma10Paint.measureText(text)
                    text = "LB:${view.formatValue(point.dn)}"
                    canvas.drawText(text, curX, curY, ma30Paint)
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
                    point.highPrice
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
    private fun drawSelector(view: BaseKLineChartView, canvas: Canvas) {
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
        left = if (x > view.getChartWidth() / 2) {
            margin.toSafeFloat()
        } else {
            view.getChartWidth() - width - margin
        }
        val r = RectF(left, top, left + width, top + height)
        canvas.drawRoundRect(r, padding.toSafeFloat(), padding.toSafeFloat(), mSelectorBackgroundPaint)
        var y = top + padding * 2 + (textHeight - metrics.bottom - metrics.top) / 2
        for (s in strings) {
            canvas.drawText(s, left + padding, y, mSelectorTextPaint)
            y += textHeight + padding
        }
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
     * 蜡烛是否实心
     */
    fun setCandleSolid(candleSolid: Boolean) {
        mCandleSolid = candleSolid
    }

    /**
     * 设置选择器背景
     */
    fun setSelectorBackgroundColor(@ColorInt color: Int) {
        mSelectorBackgroundPaint.color = color
    }

    /**
     * 设置选择器文字大小 (只认 PX（像素）)
     */
    fun setSelectorTextSize(textSize: Float) {
        mSelectorTextPaint.textSize = textSize
    }

    /**
     * 设置选择器文字颜色
     */
    fun setSelectorTextColor(@ColorInt color: Int) {
        mSelectorTextPaint.color = color
    }

    fun setMainDrawLine(line: Boolean) {
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

}