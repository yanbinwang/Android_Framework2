package com.example.klinechart.draw

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import androidx.core.content.ContextCompat
import com.example.framework.utils.function.value.orZero
import com.example.framework.utils.function.value.toSafeFloat
import com.example.klinechart.R
import com.example.klinechart.entity.ICandle
import com.example.klinechart.formatter.IValueFormatter
import com.example.klinechart.formatter.ValueFormatter
import com.example.klinechart.utils.ViewUtil
import com.example.klinechart.widget.BaseKLineChartView
import com.example.klinechart.widget.KLineChartView
import kotlin.math.max

/**
 * 主图的实现类
 */
class MainDraw(view: BaseKLineChartView) : IChartDraw<ICandle> {
    private var mCandleWidth = 0f
    private var mCandleLineWidth = 0f
    private var isLine = false // 是否分时
    private var mCandleSolid = true
    private var status = Status.MA
    private var mContext: Context? = null
    private var kChartView: KLineChartView? = null
    private val paint by lazy { Paint(Paint.ANTI_ALIAS_FLAG) }
    private val mLinePaint by lazy { Paint(Paint.ANTI_ALIAS_FLAG) }
    private val mRedPaint by lazy { Paint(Paint.ANTI_ALIAS_FLAG) }
    private val mGreenPaint by lazy { Paint(Paint.ANTI_ALIAS_FLAG) }
    private val ma5Paint by lazy { Paint(Paint.ANTI_ALIAS_FLAG) }
    private val ma10Paint by lazy { Paint(Paint.ANTI_ALIAS_FLAG) }
    private val ma30Paint by lazy { Paint(Paint.ANTI_ALIAS_FLAG) }
    private val mSelectorTextPaint by lazy { Paint(Paint.ANTI_ALIAS_FLAG) }
    private val mSelectorBackgroundPaint by lazy { Paint(Paint.ANTI_ALIAS_FLAG) }

    init {
        val context = view.context
        kChartView = view as? KLineChartView
        mContext = context
        mRedPaint.setColor(ContextCompat.getColor(context, R.color.chart_red))
        mGreenPaint.setColor(ContextCompat.getColor(context, R.color.chart_green))
        mLinePaint.setColor(ContextCompat.getColor(context, R.color.chart_line))
        paint.setColor(ContextCompat.getColor(context, R.color.chart_line_background))
    }

    override fun drawTranslated(lastPoint: ICandle?, curPoint: ICandle?, lastX: Float, curX: Float, canvas: Canvas, view: BaseKLineChartView, position: Int) {
        if (isLine) {
            view.drawMainLine(canvas, mLinePaint, lastX, lastPoint?.getClosePrice().orZero, curX, curPoint?.getClosePrice().orZero)
            view.drawMainMinuteLine(canvas, paint, lastX, lastPoint?.getClosePrice().orZero, curX, curPoint?.getClosePrice().orZero)
            if (status == Status.MA) {
                // 画ma60
                if (lastPoint?.getMA60Price() != 0f) {
                    view.drawMainLine(canvas, ma10Paint, lastX, lastPoint?.getMA60Price().orZero, curX, curPoint?.getMA60Price().orZero)
                }
            } else if (status == Status.BOLL) {
                // 画boll
                if (lastPoint?.getMb() != 0f) {
                    view.drawMainLine(canvas, ma10Paint, lastX, lastPoint?.getMb().orZero, curX, curPoint?.getMb().orZero)
                }
            }
        } else {
            drawCandle(view, canvas, curX, curPoint?.getHighPrice().orZero, curPoint?.getLowPrice().orZero, curPoint?.getOpenPrice().orZero, curPoint?.getClosePrice().orZero)
            if (status == Status.MA) {
                // 画ma5
                if (lastPoint?.getMA5Price() != 0f) {
                    view.drawMainLine(canvas, ma5Paint, lastX, lastPoint?.getMA5Price().orZero, curX, curPoint?.getMA5Price().orZero)
                }
                // 画ma10
                if (lastPoint?.getMA10Price() != 0f) {
                    view.drawMainLine(canvas, ma10Paint, lastX, lastPoint?.getMA10Price().orZero, curX, curPoint?.getMA10Price().orZero)
                }
                // 画ma30
                if (lastPoint?.getMA30Price() != 0f) {
                    view.drawMainLine(canvas, ma30Paint, lastX, lastPoint?.getMA30Price().orZero, curX, curPoint?.getMA30Price().orZero)
                }
            } else if (status == Status.BOLL) {
                //画boll
                if (lastPoint?.getUp() != 0f) {
                    view.drawMainLine(canvas, ma5Paint, lastX, lastPoint?.getUp().orZero, curX, curPoint?.getUp().orZero)
                }
                if (lastPoint?.getMb() != 0f) {
                    view.drawMainLine(canvas, ma10Paint, lastX, lastPoint?.getMb().orZero, curX, curPoint?.getMb().orZero)
                }
                if (lastPoint?.getDn() != 0f) {
                    view.drawMainLine(canvas, ma30Paint, lastX, lastPoint?.getDn().orZero, curX, curPoint?.getDn().orZero)
                }
            }
        }
    }

    override fun drawText(canvas: Canvas?, view: BaseKLineChartView, position: Int, x: Float, y: Float) {
        var mX = x
        var mY = y
        val point = view.getItem(position) as? ICandle
        mY -= 5
        if (isLine) {
            if (status == Status.MA) {
                if (point?.getMA60Price() != 0f) {
                    val text = "MA60:${view.formatValue(point?.getMA60Price().orZero)}  "
                    canvas?.drawText(text, mX, mY, ma10Paint)
                }
            } else if (status == Status.BOLL) {
                if (point?.getMb() != 0f) {
                    val text = "BOLL:${view.formatValue(point?.getMb().orZero)}  "
                    canvas?.drawText(text, mX, mY, ma10Paint)
                }
            }
        } else {
            if (status == Status.MA) {
                var text: String?
                if (point?.getMA5Price() != 0f) {
                    text = "MA5:${view.formatValue(point?.getMA5Price().orZero)}  "
                    canvas?.drawText(text, mX, mY, ma5Paint)
                    mX += ma5Paint.measureText(text)
                }
                if (point?.getMA10Price() != 0f) {
                    text = "MA10:${view.formatValue(point?.getMA10Price().orZero)}  "
                    canvas?.drawText(text, mX, mY, ma10Paint)
                    mX += ma10Paint.measureText(text)
                }
                if (point?.getMA20Price() != 0f) {
                    text = "MA30:${view.formatValue(point?.getMA30Price().orZero)}"
                    canvas?.drawText(text, mX, mY, ma30Paint)
                }
            } else if (status == Status.BOLL) {
                if (point?.getMb() != 0f) {
                    var text = "BOLL:${view.formatValue(point?.getMb().orZero)}  "
                    canvas?.drawText(text, mX, mY, ma10Paint)
                    mX += ma5Paint.measureText(text)
                    text = "UB:${view.formatValue(point?.getUp().orZero)}  "
                    canvas?.drawText(text, mX, mY, ma5Paint)
                    mX += ma10Paint.measureText(text)
                    text = "LB:${view.formatValue(point?.getDn().orZero)}"
                    canvas?.drawText(text, mX, mY, ma30Paint)
                }
            }
        }
        if (view.isLongPress()) {
            drawSelector(view, canvas)
        }
    }

    override fun getMaxValue(point: ICandle?): Float {
        return if (status == Status.BOLL) {
            if (java.lang.Float.isNaN(point?.getUp().orZero)) {
                if (point?.getMb() == 0f) {
                    point.getHighPrice().orZero
                } else {
                    point?.getMb().orZero
                }
            } else if (point?.getUp() == 0f) {
                point.getHighPrice()
            } else {
                point?.getUp().orZero
            }
        } else {
            point?.getHighPrice().orZero.coerceAtLeast(point?.getMA30Price().orZero)
        }
    }

    override fun getMinValue(point: ICandle?): Float {
        return if (status == Status.BOLL) {
            if (point?.getDn() == 0f) {
                point.getLowPrice()
            } else {
                point?.getDn().orZero
            }
        } else {
            if (point?.getMA30Price() == 0f) {
                point.getLowPrice()
            } else {
                point?.getMA30Price().orZero.coerceAtMost(point?.getLowPrice().orZero)
            }
        }
    }

    override fun getValueFormatter(): IValueFormatter {
        return ValueFormatter()
    }

    fun setStatus(status: Status) {
        this.status = status
    }

    fun getStatus(): Status {
        return status
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
     *
     * @param view
     * @param canvas
     */
    private fun drawSelector(view: BaseKLineChartView, canvas: Canvas?) {
        val metrics = mSelectorTextPaint.getFontMetrics()
        val textHeight = metrics.descent - metrics.ascent
        val index = view.getSelectedIndex()
        val padding = ViewUtil.Dp2Px(mContext, 5f)
        val margin = ViewUtil.Dp2Px(mContext, 5f)
        var width = 0f
        val left: Float
        val top = margin + view.getTopPadding()
        val height = padding * 8 + textHeight * 5
        val point = view.getItem(index) as ICandle
        val strings: MutableList<String> = ArrayList()
        strings.add(view.getAdapter()?.getDate(index).orEmpty())
        strings.add("高:${point.getHighPrice()}")
        strings.add("低:${point.getLowPrice()}")
        strings.add("开:${point.getOpenPrice()}")
        strings.add("收:${point.getClosePrice()}")
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
        this.ma5Paint.setColor(color)
    }

    /**
     * 设置ma10颜色
     *
     * @param color
     */
    fun setMa10Color(color: Int) {
        this.ma10Paint.setColor(color)
    }

    /**
     * 设置ma30颜色
     *
     * @param color
     */
    fun setMa30Color(color: Int) {
        this.ma30Paint.setColor(color)
    }

    /**
     * 设置选择器文字颜色
     *
     * @param color
     */
    fun setSelectorTextColor(color: Int) {
        mSelectorTextPaint.setColor(color)
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
        mSelectorBackgroundPaint.setColor(color)
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
                kChartView?.setCandleWidth(kChartView?.dp2px(7f).toSafeFloat())
            } else {
                kChartView?.setCandleWidth(kChartView?.dp2px(6f).toSafeFloat())
            }
        }
    }

    fun isLine(): Boolean {
        return isLine
    }

}