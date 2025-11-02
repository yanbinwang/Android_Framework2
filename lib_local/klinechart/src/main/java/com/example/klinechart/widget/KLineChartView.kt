package com.example.klinechart.widget

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.ProgressBar
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.core.content.ContextCompat
import androidx.core.content.withStyledAttributes
import com.example.framework.utils.function.value.toSafeInt
import com.example.klinechart.R
import com.example.klinechart.draw.IChartDraw
import com.example.klinechart.draw.KDJDraw
import com.example.klinechart.draw.MACDDraw
import com.example.klinechart.draw.MainDraw
import com.example.klinechart.draw.RSIDraw
import com.example.klinechart.draw.VolumeDraw
import com.example.klinechart.draw.WRDraw
import com.example.klinechart.utils.ViewUtil
import kotlin.math.abs
import androidx.core.graphics.toColorInt

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
    private var mMACDDraw: MACDDraw? = null
    private var mRSIDraw: RSIDraw? = null
    private var mMainDraw: MainDraw? = null
    private var mKDJDraw: KDJDraw? = null
    private var mWRDraw: WRDraw? = null
    private var mVolumeDraw: VolumeDraw? = null
    private var mProgressBar: ProgressBar? = null
    private var mRefreshListener: KChartRefreshListener? = null

    init {
        mProgressBar = ProgressBar(getContext())
        val layoutParams = LayoutParams(ViewUtil.dp2px(getContext(), 50f), ViewUtil.dp2px(getContext(), 50f))
        layoutParams.addRule(CENTER_IN_PARENT)
        addView(mProgressBar, layoutParams)
        mProgressBar?.visibility = GONE
        mVolumeDraw = VolumeDraw(this)
        mMACDDraw = MACDDraw(this)
        mWRDraw = WRDraw(this)
        mKDJDraw = KDJDraw(this)
        mRSIDraw = RSIDraw(this)
        mMainDraw = MainDraw(this)
        addChildDraw(mMACDDraw as? IChartDraw<Any>)
        addChildDraw(mKDJDraw as? IChartDraw<Any>)
        addChildDraw(mRSIDraw as? IChartDraw<Any>)
        addChildDraw(mWRDraw as? IChartDraw<Any>)
        setVolDraw(mVolumeDraw as? IChartDraw<Any>)
        setMainDraw(mMainDraw as? IChartDraw<Any>)
        context.withStyledAttributes(attrs, R.styleable.KLineChartView) {
            try {
                // public
                setPointWidth(getDimension(R.styleable.KLineChartView_kc_point_width, getDimension(R.dimen.chart_point_width)))
                setTextSize(getDimension(R.styleable.KLineChartView_kc_text_size, getDimension(R.dimen.chart_text_size)))
                setTextColor(getColor(R.styleable.KLineChartView_kc_text_color, getColor(R.color.chart_text)))
                setMTextSize(getDimension(R.styleable.KLineChartView_kc_text_size, getDimension(R.dimen.chart_text_size)))
                setMTextColor(getColor(R.styleable.KLineChartView_kc_text_color, getColor(R.color.chart_white)))
                setLineWidth(getDimension(R.styleable.KLineChartView_kc_line_width, getDimension(R.dimen.chart_line_width)))
                setBackgroundColor(getColor(R.styleable.KLineChartView_kc_background_color, getColor(R.color.chart_bac)))
                setSelectPointColor(getColor(R.styleable.KLineChartView_kc_background_color, getColor(R.color.chart_point_bac)))
                setSelectedXLineColor(Color.WHITE)
                setSelectedXLineWidth(getDimension(R.dimen.chart_line_width))
                setSelectedYLineColor("#8040424D".toColorInt())
                setSelectedYLineWidth(getDimension(R.dimen.chart_point_width))
                setGridLineWidth(getDimension(R.styleable.KLineChartView_kc_grid_line_width, getDimension(R.dimen.chart_grid_line_width)))
                setGridLineColor(getColor(R.styleable.KLineChartView_kc_grid_line_color, getColor(R.color.chart_grid_line)))
                // macd
                setMACDWidth(getDimension(R.styleable.KLineChartView_kc_macd_width, getDimension(R.dimen.chart_candle_width)))
                setDIFColor(getColor(R.styleable.KLineChartView_kc_dif_color, getColor(R.color.chart_ma5)))
                setDEAColor(getColor(R.styleable.KLineChartView_kc_dea_color, getColor(R.color.chart_ma10)))
                setMACDColor(getColor(R.styleable.KLineChartView_kc_macd_color, getColor(R.color.chart_ma30)))
                // kdj
                setKColor(getColor(R.styleable.KLineChartView_kc_dif_color, getColor(R.color.chart_ma5)))
                setDColor(getColor(R.styleable.KLineChartView_kc_dea_color, getColor(R.color.chart_ma10)))
                setJColor(getColor(R.styleable.KLineChartView_kc_macd_color, getColor(R.color.chart_ma30)))
                // wr
                setRColor(getColor(R.styleable.KLineChartView_kc_dif_color, getColor(R.color.chart_ma5)))
                // rsi
                setRSI1Color(getColor(R.styleable.KLineChartView_kc_dif_color, getColor(R.color.chart_ma5)))
                setRSI2Color(getColor(R.styleable.KLineChartView_kc_dea_color, getColor(R.color.chart_ma10)))
                setRSI3Color(getColor(R.styleable.KLineChartView_kc_macd_color, getColor(R.color.chart_ma30)))
                // main
                setMa5Color(getColor(R.styleable.KLineChartView_kc_dif_color, getColor(R.color.chart_ma5)))
                setMa10Color(getColor(R.styleable.KLineChartView_kc_dea_color, getColor(R.color.chart_ma10)))
                setMa30Color(getColor(R.styleable.KLineChartView_kc_macd_color, getColor(R.color.chart_ma30)))
                setCandleWidth(getDimension(R.styleable.KLineChartView_kc_candle_width, getDimension(R.dimen.chart_candle_width)))
                setCandleLineWidth(getDimension(R.styleable.KLineChartView_kc_candle_line_width, getDimension(R.dimen.chart_candle_line_width)))
                setSelectorBackgroundColor(getColor(R.styleable.KLineChartView_kc_selector_background_color, getColor(R.color.chart_selector)))
                setSelectorTextSize(getDimension(R.styleable.KLineChartView_kc_selector_text_size, getDimension(R.dimen.chart_selector_text_size)))
                setCandleSolid(getBoolean(R.styleable.KLineChartView_kc_candle_solid, true))
            } catch (e: Exception) {
                e.printStackTrace()
            }
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

    override fun setTextColor(color: Int) {
        super.setTextColor(color)
        mMainDraw?.setSelectorTextColor(color)
    }

    override fun setTextSize(textSize: Float) {
        super.setTextSize(textSize)
        mMainDraw?.setTextSize(textSize)
        mRSIDraw?.setTextSize(textSize)
        mMACDDraw?.setTextSize(textSize)
        mKDJDraw?.setTextSize(textSize)
        mWRDraw?.setTextSize(textSize)
        mVolumeDraw?.setTextSize(textSize)
    }

    override fun setLineWidth(lineWidth: Float) {
        super.setLineWidth(lineWidth)
        mMainDraw?.setLineWidth(lineWidth)
        mRSIDraw?.setLineWidth(lineWidth)
        mMACDDraw?.setLineWidth(lineWidth)
        mKDJDraw?.setLineWidth(lineWidth)
        mWRDraw?.setLineWidth(lineWidth)
        mVolumeDraw?.setLineWidth(lineWidth)
    }

    private fun getDimension(@DimenRes resId: Int): Float {
        return resources.getDimension(resId)
    }

    private fun getColor(@ColorRes resId: Int): Int {
        return ContextCompat.getColor(context, resId)
    }

    fun showLoading() {
        if (!isLoadMoreEnd && !isRefreshing) {
            isRefreshing = true
            mProgressBar?.visibility = VISIBLE
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
            mProgressBar?.visibility = VISIBLE
            mRefreshListener?.onLoadMoreBegin(this)
            mLastScaleEnable = isScaleEnable()
            mLastScrollEnable = isScrollEnable()
            super.setScrollEnable(false)
            super.setScaleEnable(false)
        }
    }

    fun hideLoading() {
        mProgressBar?.visibility = GONE
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
    fun setDIFColor(color: Int) {
        mMACDDraw?.setDIFColor(color)
    }

    /**
     * 设置DEA颜色
     */
    fun setDEAColor(color: Int) {
        mMACDDraw?.setDEAColor(color)
    }

    /**
     * 设置MACD颜色
     */
    fun setMACDColor(color: Int) {
        mMACDDraw?.setMACDColor(color)
    }

    /**
     * 设置MACD的宽度
     *
     * @param MACDWidth
     */
    fun setMACDWidth(mACDWidth: Float) {
        mMACDDraw?.setMACDWidth(mACDWidth)
    }

    /**
     * 设置K颜色
     */
    fun setKColor(color: Int) {
        mKDJDraw?.setKColor(color)
    }

    /**
     * 设置D颜色
     */
    fun setDColor(color: Int) {
        mKDJDraw?.setDColor(color)
    }

    /**
     * 设置J颜色
     */
    fun setJColor(color: Int) {
        mKDJDraw?.setJColor(color)
    }

    /**
     * 设置R颜色
     */
    fun setRColor(color: Int) {
        mWRDraw?.setRColor(color)
    }

    /**
     * 设置ma5颜色
     *
     * @param color
     */
    fun setMa5Color(color: Int) {
        mMainDraw?.setMa5Color(color)
        mVolumeDraw?.setMa5Color(color)
    }

    /**
     * 设置ma10颜色
     *
     * @param color
     */
    fun setMa10Color(color: Int) {
        mMainDraw?.setMa10Color(color)
        mVolumeDraw?.setMa10Color(color)
    }

    /**
     * 设置ma20颜色
     *
     * @param color
     */
    fun setMa30Color(color: Int) {
        mMainDraw?.setMa30Color(color)
    }

    /**
     * 设置选择器文字大小
     *
     * @param textSize
     */
    fun setSelectorTextSize(textSize: Float) {
        mMainDraw?.setSelectorTextSize(textSize)
    }

    /**
     * 设置选择器背景
     *
     * @param color
     */
    fun setSelectorBackgroundColor(color: Int) {
        mMainDraw?.setSelectorBackgroundColor(color)
    }

    /**
     * 设置蜡烛宽度
     *
     * @param candleWidth
     */
    fun setCandleWidth(candleWidth: Float) {
        mMainDraw?.setCandleWidth(candleWidth)
    }

    /**
     * 设置蜡烛线宽度
     *
     * @param candleLineWidth
     */
    fun setCandleLineWidth(candleLineWidth: Float) {
        mMainDraw?.setCandleLineWidth(candleLineWidth)
    }

    /**
     * 蜡烛是否空心
     */
    fun setCandleSolid(candleSolid: Boolean) {
        mMainDraw?.setCandleSolid(candleSolid)
    }

    fun setRSI1Color(color: Int) {
        mRSIDraw?.setRSI1Color(color)
    }

    fun setRSI2Color(color: Int) {
        mRSIDraw?.setRSI2Color(color)
    }

    fun setRSI3Color(color: Int) {
        mRSIDraw?.setRSI3Color(color)
    }

    fun setMainDrawLine(isLine: Boolean) {
        mMainDraw?.setLine(isLine)
        invalidate()
    }

    /**
     * 设置刷新监听
     */
    fun setRefreshListener(refreshListener: KChartRefreshListener) {
        mRefreshListener = refreshListener
    }

    interface KChartRefreshListener {

        /**
         * 加载更多
         *
         * @param chart
         */
        fun onLoadMoreBegin(chart: KLineChartView)

    }

}