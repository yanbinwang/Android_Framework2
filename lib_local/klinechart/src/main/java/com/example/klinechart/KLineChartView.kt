package com.example.klinechart

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.widget.ProgressBar
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.core.content.ContextCompat
import androidx.core.content.withStyledAttributes
import com.example.klinechart.base.IChartDraw
import com.example.klinechart.draw.KDJDraw
import com.example.klinechart.draw.MACDDraw
import com.example.klinechart.draw.MainDraw
import com.example.klinechart.draw.RSIDraw
import com.example.klinechart.draw.VolumeDraw
import com.example.klinechart.draw.WRDraw

/**
 * k线图
 * Created by tian on 2016/5/20.
 */
class KLineChartView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : BaseKLineChartView(context, attrs, defStyleAttr) {
    private var isRefreshing = false
    private var isLoadMoreEnd = false
    private var mLastScrollEnable = false
    private var mLastScaleEnable = false
    private var mRefreshListener: KChartRefreshListener? = null
    private var mMACDDraw: MACDDraw? = null
    private var mRSIDraw: RSIDraw? = null
    private var mMainDraw: MainDraw? = null
    private var mKDJDraw: KDJDraw? = null
    private var mWRDraw: WRDraw? = null
    private var mVolumeDraw: VolumeDraw? = null
    var mProgressBar: ProgressBar? = null

    init {
        mProgressBar = ProgressBar(getContext())
        val layoutParams = LayoutParams(dp2px(50f), dp2px(50f))
        layoutParams.addRule(CENTER_IN_PARENT)
        addView(mProgressBar, layoutParams)
        mProgressBar!!.visibility = GONE
        mVolumeDraw = VolumeDraw(this)
        mMACDDraw = MACDDraw(this)
        mWRDraw = WRDraw(this)
        mKDJDraw = KDJDraw(this)
        mRSIDraw = RSIDraw(this)
        mMainDraw = MainDraw(this)
        addChildDraw(mMACDDraw as? IChartDraw<Any?>)
        addChildDraw(mKDJDraw as? IChartDraw<Any?>)
        addChildDraw(mRSIDraw as? IChartDraw<Any?>)
        addChildDraw(mWRDraw as? IChartDraw<Any?>)
        setVolDraw(mVolumeDraw as? IChartDraw<Any?>)
        setMainDraw(mMainDraw as? IChartDraw<Any?>)
        context.withStyledAttributes(attrs, R.styleable.KLineChartView) {


            //public
            setPointWidth(
                array.getDimension(
                    R.styleable.KLineChartView_kc_point_width,
                    getDimension(R.dimen.chart_point_width)
                )
            )
            setTextSize(
                array.getDimension(
                    R.styleable.KLineChartView_kc_text_size,
                    getDimension(R.dimen.chart_text_size)
                )
            )
            setTextColor(
                array.getColor(
                    R.styleable.KLineChartView_kc_text_color,
                    getColor(R.color.chart_text)
                )
            )
            setMTextSize(
                array.getDimension(
                    R.styleable.KLineChartView_kc_text_size,
                    getDimension(R.dimen.chart_text_size)
                )
            )
            setMTextColor(
                array.getColor(
                    R.styleable.KLineChartView_kc_text_color,
                    getColor(R.color.chart_white)
                )
            )
            setLineWidth(
                array.getDimension(
                    R.styleable.KLineChartView_kc_line_width,
                    getDimension(R.dimen.chart_line_width)
                )
            )
            setBackgroundColor(
                array.getColor(
                    R.styleable.KLineChartView_kc_background_color,
                    getColor(R.color.chart_bac)
                )
            )
            setSelectPointColor(
                array.getColor(
                    R.styleable.KLineChartView_kc_background_color,
                    getColor(R.color.chart_point_bac)
                )
            )

            setSelectedXLineColor(Color.WHITE)
            setSelectedXLineWidth(getDimension(R.dimen.chart_line_width))

            setSelectedYLineColor(Color.parseColor("#8040424D"))
            setSelectedYLineWidth(getDimension(R.dimen.chart_point_width))

            setGridLineWidth(
                array.getDimension(
                    R.styleable.KLineChartView_kc_grid_line_width,
                    getDimension(R.dimen.chart_grid_line_width)
                )
            )
            setGridLineColor(
                array.getColor(
                    R.styleable.KLineChartView_kc_grid_line_color,
                    getColor(R.color.chart_grid_line)
                )
            )

            //macd
            setMACDWidth(
                array.getDimension(
                    R.styleable.KLineChartView_kc_macd_width,
                    getDimension(R.dimen.chart_candle_width)
                )
            )
            setDIFColor(
                array.getColor(
                    R.styleable.KLineChartView_kc_dif_color,
                    getColor(R.color.chart_ma5)
                )
            )
            setDEAColor(
                array.getColor(
                    R.styleable.KLineChartView_kc_dea_color,
                    getColor(R.color.chart_ma10)
                )
            )
            setMACDColor(
                array.getColor(
                    R.styleable.KLineChartView_kc_macd_color,
                    getColor(R.color.chart_ma30)
                )
            )

            //kdj
            setKColor(
                array.getColor(
                    R.styleable.KLineChartView_kc_dif_color,
                    getColor(R.color.chart_ma5)
                )
            )
            setDColor(
                array.getColor(
                    R.styleable.KLineChartView_kc_dea_color,
                    getColor(R.color.chart_ma10)
                )
            )
            setJColor(
                array.getColor(
                    R.styleable.KLineChartView_kc_macd_color,
                    getColor(R.color.chart_ma30)
                )
            )

            //wr
            setRColor(
                array.getColor(
                    R.styleable.KLineChartView_kc_dif_color,
                    getColor(R.color.chart_ma5)
                )
            )

            //rsi
            setRSI1Color(
                array.getColor(
                    R.styleable.KLineChartView_kc_dif_color,
                    getColor(R.color.chart_ma5)
                )
            )
            setRSI2Color(
                array.getColor(
                    R.styleable.KLineChartView_kc_dea_color,
                    getColor(R.color.chart_ma10)
                )
            )
            setRSI3Color(
                array.getColor(
                    R.styleable.KLineChartView_kc_macd_color,
                    getColor(R.color.chart_ma30)
                )
            )

            //main
            setMa5Color(
                array.getColor(
                    R.styleable.KLineChartView_kc_dif_color,
                    getColor(R.color.chart_ma5)
                )
            )
            setMa10Color(
                array.getColor(
                    R.styleable.KLineChartView_kc_dea_color,
                    getColor(R.color.chart_ma10)
                )
            )
            setMa30Color(
                array.getColor(
                    R.styleable.KLineChartView_kc_macd_color,
                    getColor(R.color.chart_ma30)
                )
            )
            setCandleWidth(
                array.getDimension(
                    R.styleable.KLineChartView_kc_candle_width,
                    getDimension(R.dimen.chart_candle_width)
                )
            )
            setCandleLineWidth(
                array.getDimension(
                    R.styleable.KLineChartView_kc_candle_line_width,
                    getDimension(R.dimen.chart_candle_line_width)
                )
            )
            setSelectorBackgroundColor(
                array.getColor(
                    R.styleable.KLineChartView_kc_selector_background_color,
                    getColor(R.color.chart_selector)
                )
            )
            setSelectorTextSize(
                array.getDimension(
                    R.styleable.KLineChartView_kc_selector_text_size,
                    getDimension(R.dimen.chart_selector_text_size)
                )
            )
            setCandleSolid(array.getBoolean(R.styleable.KLineChartView_kc_candle_solid, true))
        }
    }

    private fun getDimension(@DimenRes resId: Int): Float {
        return resources.getDimension(resId)
    }

    private fun getColor(@ColorRes resId: Int): Int {
        return ContextCompat.getColor(context, resId)
    }

    override fun onLeftSide() {
    }

    override fun onRightSide() {
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