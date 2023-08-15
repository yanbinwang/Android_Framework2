package com.github.fujianlian.klinechart

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.ProgressBar
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.core.content.ContextCompat
import com.github.fujianlian.klinechart.base.IChartDraw
import com.github.fujianlian.klinechart.draw.KDJDraw
import com.github.fujianlian.klinechart.draw.MACDDraw
import com.github.fujianlian.klinechart.draw.MainDraw
import com.github.fujianlian.klinechart.draw.RSIDraw
import com.github.fujianlian.klinechart.draw.VolumeDraw
import com.github.fujianlian.klinechart.draw.WRDraw
import kotlin.math.abs

/**
 * k线图
 * Created by tian on 2016/5/20.
 */
class KLineChartView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : BaseKLineChartView(context, attrs, defStyleAttr) {
    private var isRefreshing = false
    private var isLoadMoreEnd = false
    private var mLastScrollEnable = false
    private var mLastScaleEnable = false
    private var startX = 0
    private var startY = 0
    private var mProgressBar: ProgressBar? = null
    private var mMACDDraw: MACDDraw? = null
    private var mRSIDraw: RSIDraw? = null
    private var mMainDraw: MainDraw? = null
    private var mKDJDraw: KDJDraw? = null
    private var mWRDraw: WRDraw? = null
    private var mVolumeDraw: VolumeDraw? = null
    private var mRefreshListener: KChartRefreshListener? = null

    init {
        mProgressBar = ProgressBar(getContext())
        val layoutParams = LayoutParams(dp2px(50f), dp2px(50f))
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

        val array = getContext().obtainStyledAttributes(attrs, R.styleable.KLineChartView)
        try {
            //public
            setPointWidth(array.getDimension(R.styleable.KLineChartView_kc_point_width, getDimension(R.dimen.chart_point_width)))
            setTextSize(array.getDimension(R.styleable.KLineChartView_kc_text_size, getDimension(R.dimen.chart_text_size)))
            setTextColor(array.getColor(R.styleable.KLineChartView_kc_text_color, getColor(R.color.chart_text)))
            setMTextSize(array.getDimension(R.styleable.KLineChartView_kc_text_size, getDimension(R.dimen.chart_text_size)))
            setMTextColor(array.getColor(R.styleable.KLineChartView_kc_text_color, getColor(R.color.chart_white)))
            setLineWidth(array.getDimension(R.styleable.KLineChartView_kc_line_width, getDimension(R.dimen.chart_line_width)))
            setBackgroundColor(array.getColor(R.styleable.KLineChartView_kc_background_color, getColor(R.color.chart_bac)))
            setSelectPointColor(array.getColor(R.styleable.KLineChartView_kc_background_color, getColor(R.color.chart_point_bac)))
            setSelectedXLineColor(Color.WHITE)
            setSelectedXLineWidth(getDimension(R.dimen.chart_line_width))
            setSelectedYLineColor(Color.parseColor("#8040424D"))
            setSelectedYLineWidth(getDimension(R.dimen.chart_point_width))
            setGridLineWidth(array.getDimension(R.styleable.KLineChartView_kc_grid_line_width, getDimension(R.dimen.chart_grid_line_width)))
            setGridLineColor(array.getColor(R.styleable.KLineChartView_kc_grid_line_color, getColor(R.color.chart_grid_line)))
            //macd
            setMACDWidth(array.getDimension(R.styleable.KLineChartView_kc_macd_width, getDimension(R.dimen.chart_candle_width)))
            setDIFColor(array.getColor(R.styleable.KLineChartView_kc_dif_color, getColor(R.color.chart_ma5)))
            setDEAColor(array.getColor(R.styleable.KLineChartView_kc_dea_color, getColor(R.color.chart_ma10)))
            setMACDColor(array.getColor(R.styleable.KLineChartView_kc_macd_color, getColor(R.color.chart_ma30)))
            //kdj
            setKColor(array.getColor(R.styleable.KLineChartView_kc_dif_color, getColor(R.color.chart_ma5)))
            setDColor(array.getColor(R.styleable.KLineChartView_kc_dea_color, getColor(R.color.chart_ma10)))
            setJColor(array.getColor(R.styleable.KLineChartView_kc_macd_color, getColor(R.color.chart_ma30)))
            //wr
            setRColor(array.getColor(R.styleable.KLineChartView_kc_dif_color, getColor(R.color.chart_ma5)))
            //rsi
            setRSI1Color(array.getColor(R.styleable.KLineChartView_kc_dif_color, getColor(R.color.chart_ma5)))
            setRSI2Color(array.getColor(R.styleable.KLineChartView_kc_dea_color, getColor(R.color.chart_ma10)))
            setRSI3Color(array.getColor(R.styleable.KLineChartView_kc_macd_color, getColor(R.color.chart_ma30)))
            //main
            setMa5Color(array.getColor(R.styleable.KLineChartView_kc_dif_color, getColor(R.color.chart_ma5)))
            setMa10Color(array.getColor(R.styleable.KLineChartView_kc_dea_color, getColor(R.color.chart_ma10)))
            setMa30Color(array.getColor(R.styleable.KLineChartView_kc_macd_color, getColor(R.color.chart_ma30)))
            setCandleWidth(array.getDimension(R.styleable.KLineChartView_kc_candle_width, getDimension(R.dimen.chart_candle_width)))
            setCandleLineWidth(array.getDimension(R.styleable.KLineChartView_kc_candle_line_width, getDimension(R.dimen.chart_candle_line_width)))
            setSelectorBackgroundColor(array.getColor(R.styleable.KLineChartView_kc_selector_background_color, getColor(R.color.chart_selector)))
            setSelectorTextSize(array.getDimension(R.styleable.KLineChartView_kc_selector_text_size, getDimension(R.dimen.chart_selector_text_size)))
            setCandleSolid(array.getBoolean(R.styleable.KLineChartView_kc_candle_solid, true))
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            array.recycle()
        }
    }

    private fun getDimension(@DimenRes resId: Int): Float {
        return resources.getDimension(resId)
    }

    private fun getColor(@ColorRes resId: Int): Int {
        return ContextCompat.getColor(context, resId)
    }

    override fun onLeftSide() {
        showLoading()
    }

    override fun onRightSide() {
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

    override fun setTextColor(color: Int) {
        super.setTextColor(color)
        mMainDraw?.setSelectorTextColor(color)
    }

    override fun setScaleEnable(scaleEnable: Boolean) {
        check(!isRefreshing) { "请勿在刷新状态设置属性" }
        super.setScaleEnable(scaleEnable)
    }

    override fun setScrollEnable(scrollEnable: Boolean) {
        check(!isRefreshing) { "请勿在刷新状态设置属性" }
        super.setScrollEnable(scrollEnable)
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        when (ev?.action) {
            MotionEvent.ACTION_DOWN -> {
                startX = ev.x.toInt()
                startY = ev.y.toInt()
            }
            MotionEvent.ACTION_MOVE -> {
                val dX = (ev.x - startX).toInt()
                val dY = (ev.y - startX).toInt()
                return abs(dX) > abs(dY)
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

    fun showLoading() {
        if (!isLoadMoreEnd && !isRefreshing) {
            isRefreshing = true
            if (mProgressBar != null) {
                mProgressBar?.visibility = VISIBLE
            }
            mRefreshListener?.onLoadMoreBegin(this)
            mLastScaleEnable = isScaleEnable()
            mLastScrollEnable = isScrollEnable()
            super.setScrollEnable(false)
            super.setScaleEnable(false)
        }
    }

    fun justShowLoading() {
        if (!isRefreshing) {
            isLongPress = false
            isRefreshing = true
            if (mProgressBar != null) {
                mProgressBar?.visibility = VISIBLE
            }
            mRefreshListener?.onLoadMoreBegin(this)
            mLastScaleEnable = isScaleEnable()
            mLastScrollEnable = isScrollEnable()
            super.setScrollEnable(false)
            super.setScaleEnable(false)
        }
    }

    private fun hideLoading() {
        if (mProgressBar != null) {
            mProgressBar?.visibility = GONE
        }
        super.setScrollEnable(mLastScrollEnable)
        super.setScaleEnable(mLastScaleEnable)
    }

    /**
     * 隐藏选择器内容
     */
    fun hideSelectData() {
        isLongPress = false
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
    fun setMACDWidth(MACDWidth: Float) {
        mMACDDraw?.setMACDWidth(MACDWidth)
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

    /**
     * 设置刷新监听
     */
    fun setRefreshListener(refreshListener: KChartRefreshListener) {
        mRefreshListener = refreshListener
    }

    fun setMainDrawLine(isLine: Boolean) {
        mMainDraw?.setLine(isLine)
        invalidate()
    }

    interface KChartRefreshListener {
        /**
         * 加载更多
         *
         * @param chart
         */
        fun onLoadMoreBegin(chart: KLineChartView?)
    }

}