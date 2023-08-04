package com.github.fujianlian.klinechart

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.ProgressBar
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.core.content.ContextCompat
import com.example.framework.utils.function.value.orFalse
import com.example.framework.utils.function.value.orZero
import com.example.framework.utils.function.view.gone
import com.github.fujianlian.klinechart.base.IChartDraw
import com.github.fujianlian.klinechart.draw.KDJDraw
import com.github.fujianlian.klinechart.draw.MACDDraw
import com.github.fujianlian.klinechart.draw.MainDraw
import com.github.fujianlian.klinechart.draw.RSIDraw
import com.github.fujianlian.klinechart.draw.VolumeDraw
import com.github.fujianlian.klinechart.draw.WRDraw
import com.github.fujianlian.klinechart.utils.ViewUtil.toDp
import kotlin.math.abs

/**
 * k线图
 * Created by tian on 2016/5/20.
 */
class KLineChartView @JvmOverloads constructor(context: Context?, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : BaseKLineChartView(context, attrs, defStyleAttr) {
    var mProgressBar: ProgressBar? = null
    private var isRefreshing = false
    private var isLoadMoreEnd = false
    private var mLastScrollEnable = false
    private var mLastScaleEnable = false
    private var mLoadMoreListener: (() -> Unit)? = null
    private var mMACDDraw: MACDDraw? = null
    private var mRSIDraw: RSIDraw? = null
    private var mMainDraw: MainDraw? = null
    private var mKDJDraw: KDJDraw? = null
    private var mWRDraw: WRDraw? = null
    private var mVolumeDraw: VolumeDraw? = null
    private var startX = 0
    private var startY = 0
    private fun initView() {
        mProgressBar = ProgressBar(context)
        val layoutParams = LayoutParams(50.pt, 50.pt)
        layoutParams.addRule(CENTER_IN_PARENT)
        addView(mProgressBar, layoutParams)
        mProgressBar.gone()
        mVolumeDraw = VolumeDraw(this)
        mMACDDraw = MACDDraw(this)
        mWRDraw = WRDraw(this)
        mKDJDraw = KDJDraw(this)
        mRSIDraw = RSIDraw(this)
        mMainDraw = MainDraw(this)
        addChildDraw(mMACDDraw as IChartDraw<Any>)
        addChildDraw(mKDJDraw as IChartDraw<Any>)
        addChildDraw(mRSIDraw as IChartDraw<Any>)
        addChildDraw(mWRDraw as IChartDraw<Any>)
        volDraw = mVolumeDraw as IChartDraw<Any>
        setMainDraw(mMainDraw as IChartDraw<Any>)
    }

    private fun initAttrs(attrs: AttributeSet?) {
        if (attrs == null) return
        val array = context.obtainStyledAttributes(attrs, R.styleable.KLineChartView)
        try {
            //public
            setPointWidth(array.getDimension(R.styleable.KLineChartView_kc_point_width, getDimension(R.dimen.chart_point_width)))
            textSize =
                array.getDimension(R.styleable.KLineChartView_kc_text_size, getDimension(R.dimen.chart_text_size))
            setTextColor(array.getColor(R.styleable.KLineChartView_kc_text_color, getColor(R.color.chart_text)))
            setMTextSize(array.getDimension(R.styleable.KLineChartView_kc_text_size, getDimension(R.dimen.chart_text_size)))
            setMTextColor(array.getColor(R.styleable.KLineChartView_kc_text_color, getColor(R.color.chart_white)))
            lineWidth =
                array.getDimension(R.styleable.KLineChartView_kc_line_width, getDimension(R.dimen.ma_line))
            setBackgroundColor(array.getColor(R.styleable.KLineChartView_kc_background_color, getColor(R.color.chart_bac)))
            setSelectPointColor(array.getColor(R.styleable.KLineChartView_kc_background_color, getColor(R.color.chart_point_bac)))
            setLastLinePaintColor(array.getColor(R.styleable.KLineChartView_kc_setLastLinePaintColor, getColor(R.color.chart_last_line)))
            setLastTextPaintColor(array.getColor(R.styleable.KLineChartView_kc_setLastTextPaintColor, getColor(R.color.chart_last_text)))

            // 设置十字选中线的宽高
            setSelectedXLineWidth(getDimension(R.dimen.chart_line_width).toDp())
            setSelectedYLineWidth(getDimension(R.dimen.chart_line_width).toDp())
            // 设置十字选中线的颜色
            setSelectedXLineColor(resources.getColor(R.color.chart_selected_x_line))
            setSelectedYLineColor(resources.getColor(R.color.chart_selected_y_line))
            setGridLineWidth(array.getDimension(R.styleable.KLineChartView_kc_grid_line_width, getDimension(R.dimen.chart_grid_line_width).toDp()))
            setGridLineColor(array.getColor(R.styleable.KLineChartView_kc_grid_line_color, getColor(R.color.chart_grid_line)))
            setBorderLineColor(array.getColor(R.styleable.KLineChartView_kc_grid_line_color, getColor(R.color.chart_border_line)))
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
            setmSelectorBackgroundPaintStrokeColor(array.getColor(R.styleable.KLineChartView_kc_selector_background_stroke_color, getColor(R.color.chart_selector_stroke)))
            setSelectorTextSize(array.getDimension(R.styleable.KLineChartView_kc_selector_text_size, getDimension(R.dimen.chart_selector_text_size)))

            setOpenPaintBgColor(getColor(R.color.appWindowBackground))
            setSlTpBgColor(getColor(R.color.appWindowBackground))

            setRedGreen(getColor(R.color.chartRed), getColor(R.color.chartGreen))
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

    /**
     * 设置MACD的宽度
     *
     * @param MACDWidth
     */
    fun setMACDWidth(MACDWidth: Float) {
        mMACDDraw?.setMACDWidth(MACDWidth)
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
     * 设置选择器背景
     *
     * @param color
     */
    fun setSelectorBackgroundColor(color: Int) {
        mMainDraw?.setSelectorBackgroundColor(color)
    }

    /**
     * 设置选择器背景描边
     *
     * @param color
     */
    fun setmSelectorBackgroundPaintStrokeColor(color: Int) {
        mMainDraw?.setmSelectorBackgroundPaintStrokeColor(color)
    }

    /**
     * 设置选择器文字大小
     *
     * @param textSize
     */
    fun setSelectorTextSize(textSize: Float) {
        mMainDraw?.setSelectorTextSize(textSize)
    }

    private var isLoadEnd = false
    override fun onLeftSide() {
        // 暂时关闭左滑加载更多功能
        //        showLoading();
        if (isLoadEnd) {
            mLoadMoreListener?.invoke()
            // 加载的时候置为false 然后加载更多数据回来之后置为true
            isLoadEnd = false
        }
    }

    fun setLoadSuccess(isLoadEnd: Boolean) {
        this.isLoadEnd = isLoadEnd
    }

    override fun onRightSide() {}

    override var isScrollEnable: Boolean
        get() = super.isScrollEnable
        set(value) {
            check(!isRefreshing) { "Do not set data while scrolling!" }
            super.isScrollEnable = value
        }

    override var isScaleEnable: Boolean
        get() = super.isScaleEnable
        set(value) {
            check(!isRefreshing) { "Do not set data while scrolling!" }
            super.isScaleEnable = value
        }

    fun showLoading() {
        if (!isLoadMoreEnd && !isRefreshing) {
            isRefreshing = true
            mProgressBar?.visibility = VISIBLE
            mLoadMoreListener?.invoke()
            mLastScaleEnable = isScaleEnable
            mLastScrollEnable = isScrollEnable
            super.isScrollEnable = false
            super.isScaleEnable = false
        }
    }

    fun justShowLoading() {
        if (!isRefreshing) {
            isLongPress = false
            isRefreshing = true
            mProgressBar?.visibility = VISIBLE
            mLoadMoreListener?.invoke()
            mLastScaleEnable = isScaleEnable
            mLastScrollEnable = isScrollEnable
            super.isScrollEnable = false
            super.isScaleEnable = false
        }
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

    private fun hideLoading() {
        mProgressBar?.visibility = GONE
        super.isScrollEnable = mLastScrollEnable
        super.isScaleEnable = mLastScaleEnable
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
     * 设置刷新监听
     */
    fun setLoadMoreListener(listener: (() -> Unit)?) {
        mLoadMoreListener = listener
    }

    fun setMainDrawLine(isLine: Boolean) {
        if (isLine == isMainDrawLine()) return
        mMainDraw?.setLine(isLine)
        invalidate()
    }

    fun isMainDrawLine(): Boolean {
        return mMainDraw?.isLine().orFalse
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        when (ev.action) {
            MotionEvent.ACTION_DOWN -> {
                startX = ev.x.toInt()
                startY = ev.y.toInt()
            }
            MotionEvent.ACTION_MOVE -> {
                val dX = (ev.x - startX).toInt()
                val dY = (ev.y - startX).toInt()
                return abs(dX) > abs(dY)
            }
            MotionEvent.ACTION_UP -> {
            }
            else -> {
            }
        }
        return super.onInterceptTouchEvent(ev)
    }

    override fun onLongPress(e: MotionEvent) {
        val count = adapter?.count.orZero
        if (!isRefreshing && count != 0) {
            super.onLongPress(e)
        }
    }

    override fun setTextColor(color: Int) {
        super.setTextColor(color)
        mMainDraw?.setSelectorTextColor(color)
    }

    override var textSize: Float
        get() = super.textSize
        set(textSize) {
            super.textSize = textSize
            mMainDraw?.setTextSize(textSize)
            mRSIDraw?.setTextSize(textSize)
            mMACDDraw?.setTextSize(textSize)
            mKDJDraw?.setTextSize(textSize)
            mWRDraw?.setTextSize(textSize)
            mVolumeDraw?.setTextSize(textSize)
        }
    override var lineWidth: Float
        get() = super.lineWidth
        set(lineWidth) {
            super.lineWidth = lineWidth
            mMainDraw?.setLineWidth(lineWidth)
            mRSIDraw?.setLineWidth(lineWidth)
            mMACDDraw?.setLineWidth(lineWidth)
            mKDJDraw?.setLineWidth(lineWidth)
            mWRDraw?.setLineWidth(lineWidth)
            mVolumeDraw?.setLineWidth(lineWidth)
        }

    init {
        // 取消硬件加速
        setLayerType(LAYER_TYPE_SOFTWARE, null)
        initView()
        initAttrs(attrs)
    }
}