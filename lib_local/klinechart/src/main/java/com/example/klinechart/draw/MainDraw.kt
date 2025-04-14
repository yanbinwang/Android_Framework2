package com.example.klinechart.draw

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import androidx.core.content.ContextCompat
import com.example.framework.utils.function.value.orZero
import com.example.framework.utils.function.value.toSafeFloat
import com.example.klinechart.BaseKLineChartView
import com.example.klinechart.KLineChartView
import com.example.klinechart.R
import com.example.klinechart.base.IChartDraw
import com.example.klinechart.base.IValueFormatter
import com.example.klinechart.entity.ICandle
import com.example.klinechart.entity.IKLine
import com.example.klinechart.formatter.ValueFormatter
import com.example.klinechart.utils.ViewUtil
import kotlin.math.max

/**
 * 主图的实现类
 * Created by tifezh on 2016/6/14.
 */
class MainDraw(private val view: BaseKLineChartView) : IChartDraw<ICandle> {
    private var status = Status.MA
    // 是否分时
    private var isLine = false
    private var mCandleSolid = true
    private var mCandleWidth = 0f
    private var mCandleLineWidth = 0f
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mLinePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mRedPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mGreenPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val ma5Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val ma10Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val ma30Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mSelectorTextPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mSelectorBackgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mContext get() = view.context
    private val kChartView get() = view as? KLineChartView
    
    init {
        mRedPaint.color = ContextCompat.getColor(mContext, R.color.chart_red)
        mGreenPaint.color = ContextCompat.getColor(mContext, R.color.chart_green)
        mLinePaint.color = ContextCompat.getColor(mContext, R.color.chart_line)
        paint.color = ContextCompat.getColor(mContext, R.color.chart_line_background)
    }

    override fun drawTranslated(
        lastPoint: ICandle,
        curPoint: ICandle,
        lastX: Float,
        curX: Float,
        canvas: Canvas,
        view: BaseKLineChartView,
        position: Int
    ) {
        if (isLine) {
            view.drawMainLine(
                canvas,
                mLinePaint,
                lastX,
                lastPoint.getClosePrice(),
                curX,
                curPoint.getClosePrice()
            )
            view.drawMainMinuteLine(
                canvas,
                paint,
                lastX,
                lastPoint.getClosePrice(),
                curX,
                curPoint.getClosePrice()
            )
            if (status == Status.MA) {
                //画ma60
                if (lastPoint.getMA60Price() != 0f) {
                    view.drawMainLine(
                        canvas,
                        ma10Paint,
                        lastX,
                        lastPoint.getMA60Price(),
                        curX,
                        curPoint.getMA60Price()
                    )
                }
            } else if (status == Status.BOLL) {
                //画boll
                if (lastPoint.getMb() != 0f) {
                    view.drawMainLine(
                        canvas,
                        ma10Paint,
                        lastX,
                        lastPoint.getMb(),
                        curX,
                        curPoint.getMb()
                    )
                }
            }
        } else {
            drawCandle(
                view,
                canvas,
                curX,
                curPoint.getHighPrice(),
                curPoint.getLowPrice(),
                curPoint.getOpenPrice(),
                curPoint.getClosePrice()
            )
            if (status == Status.MA) {
                //画ma5
                if (lastPoint.getMA5Price() != 0f) {
                    view.drawMainLine(
                        canvas,
                        ma5Paint,
                        lastX,
                        lastPoint.getMA5Price(),
                        curX,
                        curPoint.getMA5Price()
                    )
                }
                //画ma10
                if (lastPoint.getMA10Price() != 0f) {
                    view.drawMainLine(
                        canvas,
                        ma10Paint,
                        lastX,
                        lastPoint.getMA10Price(),
                        curX,
                        curPoint.getMA10Price()
                    )
                }
                //画ma30
                if (lastPoint.getMA30Price() != 0f) {
                    view.drawMainLine(
                        canvas,
                        ma30Paint,
                        lastX,
                        lastPoint.getMA30Price(),
                        curX,
                        curPoint.getMA30Price()
                    )
                }
            } else if (status == Status.BOLL) {
                //画boll
                if (lastPoint.getUp() != 0f) {
                    view.drawMainLine(
                        canvas,
                        ma5Paint,
                        lastX,
                        lastPoint.getUp(),
                        curX,
                        curPoint.getUp()
                    )
                }
                if (lastPoint.getMb() != 0f) {
                    view.drawMainLine(
                        canvas,
                        ma10Paint,
                        lastX,
                        lastPoint.getMb(),
                        curX,
                        curPoint.getMb()
                    )
                }
                if (lastPoint.getDn() != 0f) {
                    view.drawMainLine(
                        canvas,
                        ma30Paint,
                        lastX,
                        lastPoint.getDn(),
                        curX,
                        curPoint.getDn()
                    )
                }
            }
        }
    }

    override fun drawText(
        canvas: Canvas,
        view: BaseKLineChartView,
        position: Int,
        x: Float,
        y: Float
    ) {
        val point: ICandle? = view.getItem(position) as? IKLine
        y = y - 5
        if (isLine) {
            if (status == Status.MA) {
                if (point?.getMA60Price() != 0f) {
                    val text = "MA60:" + view.formatValue(point?.getMA60Price().orZero) + "  "
                    canvas.drawText(text, x, y, ma10Paint)
                }
            } else if (status == Status.BOLL) {
                if (point?.getMb() != 0f) {
                    val text = "BOLL:" + view.formatValue(point?.getMb().orZero) + "  "
                    canvas.drawText(text, x, y, ma10Paint)
                }
            }
        } else {
            if (status == Status.MA) {
                var text: String
                if (point?.getMA5Price() != 0f) {
                    text = "MA5:" + view.formatValue(point?.getMA5Price().orZero) + "  "
                    canvas.drawText(text, x, y, ma5Paint)
                    x += ma5Paint.measureText(text)
                }
                if (point?.getMA10Price() != 0f) {
                    text = "MA10:" + view.formatValue(point?.getMA10Price().orZero) + "  "
                    canvas.drawText(text, x, y, ma10Paint)
                    x += ma10Paint.measureText(text)
                }
                if (point?.getMA20Price() != 0f) {
                    text = "MA30:" + view.formatValue(point?.getMA30Price().orZero)
                    canvas.drawText(text, x, y, ma30Paint)
                }
            } else if (status == Status.BOLL) {
                if (point?.getMb() != 0f) {
                    var text = "BOLL:" + view.formatValue(point?.getMb().orZero) + "  "
                    canvas.drawText(text, x, y, ma10Paint)
                    x += ma5Paint.measureText(text)
                    text = "UB:" + view.formatValue(point?.getUp().orZero) + "  "
                    canvas.drawText(text, x, y, ma5Paint)
                    x += ma10Paint.measureText(text)
                    text = "LB:" + view.formatValue(point?.getDn().orZero)
                    canvas.drawText(text, x, y, ma30Paint)
                }
            }
        }
        if (view.isLongPress()) {
            drawSelector(view, canvas)
        }
    }

    override fun getValueFormatter(): IValueFormatter {
        return ValueFormatter()
    }

    override fun getMinValue(point: ICandle): Float {
        return if (status == Status.BOLL) {
            if (point.getDn() == 0f) {
                point.getLowPrice()
            } else {
                point.getDn()
            }
        } else {
            if (point.getMA30Price() == 0f) {
                point.getLowPrice()
            } else {
                Math.min(point.getMA30Price(), point.getLowPrice())
            }
        }
    }

    override fun getMaxValue(point: ICandle): Float {
        return if (status == Status.BOLL) {
            if (java.lang.Float.isNaN(point.getUp())) {
                if (point.getMb() == 0f) {
                    point.getHighPrice()
                } else {
                    point.getMb()
                }
            } else if (point.getUp() == 0f) {
                point.getHighPrice()
            } else {
                point.getUp()
            }
        } else {
            Math.max(point.getHighPrice(), point.getMA30Price())
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
    private fun drawCandle(
        view: BaseKLineChartView,
        canvas: Canvas,
        x: Float,
        high: Float,
        low: Float,
        open: Float,
        close: Float
    ) {
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
    private fun drawSelector(view: BaseKLineChartView, canvas: Canvas) {
        val metrics = mSelectorTextPaint.fontMetrics
        val textHeight = metrics.descent - metrics.ascent

        val index = view.getSelectedIndex()
        val padding = ViewUtil.Dp2Px(mContext, 5f)
        val margin = ViewUtil.Dp2Px(mContext, 5f)
        var width = 0f
        val left: Float
        val top = margin + view.getTopPadding()
        val height = padding * 8 + textHeight * 5

        val point = view.getItem(index) as ICandle?
        val strings: MutableList<String> = ArrayList()
        strings.add(view.getAdapter().getDate(index))
        strings.add("高:" + point?.getHighPrice())
        strings.add("低:" + point?.getLowPrice())
        strings.add("开:" + point?.getOpenPrice())
        strings.add("收:" + point?.getClosePrice())

        for (s in strings) {
            width = max(width.toDouble(), mSelectorTextPaint.measureText(s).toDouble()).toFloat()
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
                kChartView.setCandleWidth(kChartView?.dp2px(7f))
            } else {
                kChartView.setCandleWidth(kChartView?.dp2px(6f))
            }
        }
    }

    fun isLine(): Boolean {
        return isLine
    }

    fun setStatus(status: Status?) {
        this.status = status?
    }

    fun getStatus(): Status {
        return status
    }
    
}