package com.example.klinechart.widget

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.ProgressBar
import androidx.annotation.ColorInt
import androidx.core.content.withStyledAttributes
import androidx.core.graphics.toColorInt
import com.example.common.utils.function.pt
import com.example.framework.utils.function.value.toSafeInt
import com.example.framework.utils.function.view.color
import com.example.framework.utils.function.view.dimen
import com.example.klinechart.R
import com.example.klinechart.widget.draw.IChartDraw
import com.example.klinechart.widget.draw.KDJDraw
import com.example.klinechart.widget.draw.MACDDraw
import com.example.klinechart.widget.draw.MainDraw
import com.example.klinechart.widget.draw.RSIDraw
import com.example.klinechart.widget.draw.VolumeDraw
import com.example.klinechart.widget.draw.WRDraw
import kotlin.math.abs

/**
 * k线图
 */
@Suppress("UNCHECKED_CAST")
class KLineChartView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : BaseKLineChartView(context, attrs, defStyleAttr) {
    private var startX = 0
    private var startY = 0
    private var isRefreshing = false
    private var isLoadMoreEnd = false
    private var mLastScrollEnable = false
    private var mLastScaleEnable = false
    private var mMACDDraw = MACDDraw(this)
    private var mKDJDraw = KDJDraw()
    private var mRSIDraw = RSIDraw()
    private var mWRDraw = WRDraw()
    private var mVolumeDraw = VolumeDraw(this)
    private var mMainDraw = MainDraw(this)
    private var mProgressBar = ProgressBar(context)
    private var mRefreshListener: KChartRefreshListener? = null

    init {
        val layoutParams = LayoutParams(50.pt, 50.pt)
        layoutParams.addRule(CENTER_IN_PARENT)
        addView(mProgressBar, layoutParams)
        mProgressBar.visibility = GONE
        addChildDraw(mMACDDraw as? IChartDraw<Any>)
        addChildDraw(mKDJDraw as? IChartDraw<Any>)
        addChildDraw(mRSIDraw as? IChartDraw<Any>)
        addChildDraw(mWRDraw as? IChartDraw<Any>)
        setVolDraw(mVolumeDraw as? IChartDraw<Any>)
        setMainDraw(mMainDraw as? IChartDraw<Any>)
        context.withStyledAttributes(attrs, R.styleable.KLineChartView) {
            // 公共方法赋值
            setPointWidth(getDimension(R.styleable.KLineChartView_kc_point_width, dimen(R.dimen.chart_point_width)))
            setTextSize(getDimension(R.styleable.KLineChartView_kc_text_size, dimen(R.dimen.chart_text_size)))
            setTextColor(getColor(R.styleable.KLineChartView_kc_text_color, color(R.color.chart_text)))
            setMTextSize(getDimension(R.styleable.KLineChartView_kc_text_size, dimen(R.dimen.chart_text_size)))
            setMTextColor(getColor(R.styleable.KLineChartView_kc_text_color, color(R.color.chart_white)))
            setLineWidth(getDimension(R.styleable.KLineChartView_kc_line_width, dimen(R.dimen.chart_line_width)))
            setBackgroundColor(getColor(R.styleable.KLineChartView_kc_background_color, color(R.color.chart_bac)))
            setSelectPointColor(getColor(R.styleable.KLineChartView_kc_background_color, color(R.color.chart_point_bac)))
            setSelectedXLineColor(Color.WHITE)
            setSelectedXLineWidth(dimen(R.dimen.chart_line_width))
            setSelectedYLineColor("#8040424D".toColorInt())
            setSelectedYLineWidth(dimen(R.dimen.chart_point_width))
            setGridLineWidth(getDimension(R.styleable.KLineChartView_kc_grid_line_width, dimen(R.dimen.chart_grid_line_width)))
            setGridLineColor(getColor(R.styleable.KLineChartView_kc_grid_line_color, color(R.color.chart_grid_line)))
            // MACD
            setMACDWidth(getDimension(R.styleable.KLineChartView_kc_macd_width, dimen(R.dimen.chart_candle_width)))
            setDIFColor(getColor(R.styleable.KLineChartView_kc_dif_color, color(R.color.chart_ma5)))
            setDEAColor(getColor(R.styleable.KLineChartView_kc_dea_color, color(R.color.chart_ma10)))
            setMACDColor(getColor(R.styleable.KLineChartView_kc_macd_color, color(R.color.chart_ma30)))
            // KDJ
            setKColor(getColor(R.styleable.KLineChartView_kc_dif_color, color(R.color.chart_ma5)))
            setDColor(getColor(R.styleable.KLineChartView_kc_dea_color, color(R.color.chart_ma10)))
            setJColor(getColor(R.styleable.KLineChartView_kc_macd_color, color(R.color.chart_ma30)))
            // WR
            setWRColor(getColor(R.styleable.KLineChartView_kc_dif_color, color(R.color.chart_ma5)))
            // RSI
            setRSI1Color(getColor(R.styleable.KLineChartView_kc_dif_color, color(R.color.chart_ma5)))
            setRSI2Color(getColor(R.styleable.KLineChartView_kc_dea_color, color(R.color.chart_ma10)))
            setRSI3Color(getColor(R.styleable.KLineChartView_kc_macd_color, color(R.color.chart_ma30)))
            // MAIN
            setMA5Color(getColor(R.styleable.KLineChartView_kc_dif_color, color(R.color.chart_ma5)))
            setMA10Color(getColor(R.styleable.KLineChartView_kc_dea_color, color(R.color.chart_ma10)))
            setMA30Color(getColor(R.styleable.KLineChartView_kc_macd_color, color(R.color.chart_ma30)))
            setCandleWidth(getDimension(R.styleable.KLineChartView_kc_candle_width, dimen(R.dimen.chart_candle_width)))
            setCandleLineWidth(getDimension(R.styleable.KLineChartView_kc_candle_line_width, dimen(R.dimen.chart_candle_line_width)))
            setSelectorBackgroundColor(getColor(R.styleable.KLineChartView_kc_selector_background_color, color(R.color.chart_selector)))
            setSelectorTextSize(getDimension(R.styleable.KLineChartView_kc_selector_text_size, dimen(R.dimen.chart_selector_text_size)))
            setCandleSolid(getBoolean(R.styleable.KLineChartView_kc_candle_solid, true))
        }
    }

    override fun onLeftSide() {
        showLoading()
    }

    override fun onRightSide() {
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        when (ev?.action) {
            MotionEvent.ACTION_DOWN -> {
                startX = ev.x.toSafeInt()
                startY = ev.y.toSafeInt()
            }
            MotionEvent.ACTION_MOVE -> {
                val dX = (ev.x - startX).toSafeInt()
                val dY = (ev.y - startX).toSafeInt()
                return if (abs(dX) > abs(dY)) {
                    // 左右滑动
                    true
                } else {
                    // 上下滑动
                    false
                }
            }
            MotionEvent.ACTION_UP -> {}
            else -> {}
        }
        return super.onInterceptTouchEvent(ev)
    }

    override fun onLongPress(e: MotionEvent) {
        if (!isRefreshing) {
            super.onLongPress(e)
        }
    }

    override fun setScrollEnable(scrollEnable: Boolean) {
        check(!isRefreshing) { "请勿在刷新状态设置属性" }
        super.setScrollEnable(scrollEnable)
    }

    override fun setScaleEnable(scaleEnable: Boolean) {
        check(!isRefreshing) { "请勿在刷新状态设置属性" }
        super.setScaleEnable(scaleEnable)
    }

    override fun setTextColor(@ColorInt color: Int) {
        super.setTextColor(color)
        mMainDraw.setSelectorTextColor(color)
    }

    override fun setTextSize(textSize: Float) {
        super.setTextSize(textSize)
        mMainDraw.setTextSize(textSize)
        mRSIDraw.setTextSize(textSize)
        mMACDDraw.setTextSize(textSize)
        mKDJDraw.setTextSize(textSize)
        mWRDraw.setTextSize(textSize)
        mVolumeDraw.setTextSize(textSize)
    }

    override fun setLineWidth(lineWidth: Float) {
        super.setLineWidth(lineWidth)
        mMainDraw.setLineWidth(lineWidth)
        mRSIDraw.setLineWidth(lineWidth)
        mMACDDraw.setLineWidth(lineWidth)
        mKDJDraw.setLineWidth(lineWidth)
        mWRDraw.setLineWidth(lineWidth)
        mVolumeDraw.setLineWidth(lineWidth)
    }

    fun showLoading() {
        if (!isLoadMoreEnd && !isRefreshing) {
            isRefreshing = true
            mProgressBar.visibility = VISIBLE
            mRefreshListener?.onLoadMoreBegin(this)
            mLastScaleEnable = isScaleEnable()
            mLastScrollEnable = isScrollEnable()
            super.setScrollEnable(false)
            super.setScaleEnable(false)
        }
    }

    fun justShowLoading() {
        if (!isRefreshing) {
            mIsLongPress = false
            isRefreshing = true
            mProgressBar.visibility = VISIBLE
            mRefreshListener?.onLoadMoreBegin(this)
            mLastScaleEnable = isScaleEnable()
            mLastScrollEnable = isScrollEnable()
            super.setScrollEnable(false)
            super.setScaleEnable(false)
        }
    }

    fun hideLoading() {
        mProgressBar.visibility = GONE
        super.setScrollEnable(mLastScrollEnable)
        super.setScaleEnable(mLastScaleEnable)
    }

    /**
     * 隐藏选择器内容
     */
    fun hideSelectData() {
        mIsLongPress = false
        invalidate()
    }

    /**
     * 刷新完成
     */
    fun refreshComplete() {
        isRefreshing = false
        hideLoading()
    }

    /**
     * 刷新完成，没有数据
     */
    fun refreshEnd() {
        isLoadMoreEnd = true
        isRefreshing = false
        hideLoading()
    }

    /**
     * 重置加载更多
     */
    fun resetLoadMoreEnd() {
        isLoadMoreEnd = false
    }

    fun setLoadMoreEnd() {
        isLoadMoreEnd = true
    }

    /**
     * 设置DIF颜色
     */
    fun setDIFColor(@ColorInt color: Int) {
        mMACDDraw.setDIFColor(color)
    }

    /**
     * 设置DEA颜色
     */
    fun setDEAColor(@ColorInt color: Int) {
        mMACDDraw.setDEAColor(color)
    }

    /**
     * 设置MACD颜色
     */
    fun setMACDColor(@ColorInt color: Int) {
        mMACDDraw.setMACDColor(color)
    }

    /**
     * 设置MACD的宽度
     */
    fun setMACDWidth(macdWidth: Float) {
        mMACDDraw.setMACDWidth(macdWidth)
    }

    /**
     * 设置K颜色
     */
    fun setKColor(@ColorInt color: Int) {
        mKDJDraw.setKColor(color)
    }

    /**
     * 设置D颜色
     */
    fun setDColor(@ColorInt color: Int) {
        mKDJDraw.setDColor(color)
    }

    /**
     * 设置J颜色
     */
    fun setJColor(@ColorInt color: Int) {
        mKDJDraw.setJColor(color)
    }

    /**
     * 设置WR颜色
     */
    fun setWRColor(@ColorInt color: Int) {
        mWRDraw.setWRColor(color)
    }

    /**
     * 设置ma5颜色
     */
    fun setMA5Color(@ColorInt color: Int) {
        mMainDraw.setMA5Color(color)
        mVolumeDraw.setMa5Color(color)
    }

    /**
     * 设置ma10颜色
     */
    fun setMA10Color(@ColorInt color: Int) {
        mMainDraw.setMA10Color(color)
        mVolumeDraw.setMa10Color(color)
    }

    /**
     * 设置ma20颜色
     */
    fun setMA30Color(@ColorInt color: Int) {
        mMainDraw.setMA30Color(color)
    }

    /**
     * 设置选择器画笔文字大小
     */
    fun setSelectorTextSize(textSize: Float) {
        mMainDraw.setSelectorTextSize(textSize)
    }

    /**
     * 设置选择器背景
     */
    fun setSelectorBackgroundColor(@ColorInt color: Int) {
        mMainDraw.setSelectorBackgroundColor(color)
    }

    /**
     * 设置蜡烛宽度
     *
     * @param candleWidth
     */
    fun setCandleWidth(candleWidth: Float) {
        mMainDraw.setCandleWidth(candleWidth)
    }

    /**
     * 设置蜡烛线宽度
     *
     * @param candleLineWidth
     */
    fun setCandleLineWidth(candleLineWidth: Float) {
        mMainDraw.setCandleLineWidth(candleLineWidth)
    }

    /**
     * 蜡烛是否空心
     */
    fun setCandleSolid(candleSolid: Boolean) {
        mMainDraw.setCandleSolid(candleSolid)
    }

    fun setRSI1Color(@ColorInt color: Int) {
        mRSIDraw.setRSI1Color(color)
    }

    fun setRSI2Color(@ColorInt color: Int) {
        mRSIDraw.setRSI2Color(color)
    }

    fun setRSI3Color(@ColorInt color: Int) {
        mRSIDraw.setRSI3Color(color)
    }

    fun setMainDrawLine(isLine: Boolean) {
        mMainDraw.setLine(isLine)
        invalidate()
    }

    /**
     * 设置刷新监听
     */
    fun setRefreshListener(listener: KChartRefreshListener) {
        mRefreshListener = listener
    }

    interface KChartRefreshListener {
        /**
         * 加载更多
         */
        fun onLoadMoreBegin(chart: KLineChartView)
    }

}