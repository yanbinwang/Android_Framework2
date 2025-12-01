package com.example.mvvm.activity

import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.example.common.base.BaseTitleActivity
import com.example.common.base.bridge.viewModels
import com.example.common.config.RouterPath
import com.example.framework.utils.function.value.toArrayList
import com.example.framework.utils.function.view.clicks
import com.example.framework.utils.function.view.textColor
import com.example.klinechart.adapter.KLineChartAdapter
import com.example.klinechart.draw.Status
import com.example.klinechart.entity.KLineEntity
import com.example.klinechart.formatter.DateFormatter
import com.example.klinechart.utils.DataHelper
import com.example.mvvm.R
import com.example.mvvm.databinding.ActivityKlineBinding
import com.example.mvvm.viewmodel.KLineViewModel
import com.therouter.router.Route

@Route(path = RouterPath.KLineActivity)
class KLineActivity : BaseTitleActivity<ActivityKlineBinding>(), View.OnClickListener {
    private var datas = ArrayList<KLineEntity>()
    private val adapter by lazy { KLineChartAdapter() }
    // 主图指标下标
    private var mainIndex = 0
    // 副图指标下标
    private var subIndex = -1
    private val subTexts by lazy { arrayListOf(mBinding?.tvMacd, mBinding?.tvKdj, mBinding?.tvRsi, mBinding?.tvWr) }
    private val viewModel: KLineViewModel by viewModels()

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        titleRoot.setTitle("KLine")
        mBinding?.kline?.setAdapter(adapter)
        mBinding?.kline?.setDateTimeFormatter(DateFormatter())
        mBinding?.kline?.setGridRows(4)
        mBinding?.kline?.setGridColumns(4)
    }

    override fun initEvent() {
        super.initEvent()
        clicks(mBinding?.tvMa, mBinding?.tvBoll, mBinding?.tvMainHide, mBinding?.tvMacd, mBinding?.tvKdj, mBinding?.tvRsi, mBinding?.tvWr, mBinding?.tvSubHide, mBinding?.tvFen, mBinding?.tvK)
        viewModel.list.observe {
            datas = this?.subList(0, 500).orEmpty().toArrayList()
            DataHelper.calculate(datas)
            adapter.addFooterData(datas)
            adapter.notifyDataSetChanged()
        }
        viewModel.uiManage.observe {
            if (this) {
                mBinding?.kline?.justShowLoading()
            } else {
                mBinding?.kline?.startAnimation()
                mBinding?.kline?.refreshEnd()
            }
        }
    }

    override fun initData() {
        super.initData()
        viewModel.getAll()
    }

    private fun setSubTexts(v: View, index: Int) {
        val text = v as? TextView
        if (subIndex != index) {
            mBinding?.kline?.hideSelectData()
            if (subIndex != -1) {
                subTexts[subIndex].textColor(R.color.btnMainDisabled)
            }
            subIndex = index
            text.textColor(R.color.btnMainPressed)
            mBinding?.kline?.setChildDraw(subIndex)
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.tv_ma -> {
                if (mainIndex != 0) {
                    mBinding?.kline?.hideSelectData()
                    mainIndex = 0
                    mBinding?.tvMa.textColor(R.color.btnMainPressed)
                    mBinding?.tvBoll.textColor(R.color.btnMainDisabled)
                    mBinding?.kline?.changeMainDrawType(Status.MA)
                }
            }
            R.id.tv_boll -> {
                if (mainIndex != 1) {
                    mBinding?.kline?.hideSelectData()
                    mainIndex = 1
                    mBinding?.tvMa.textColor(R.color.btnMainDisabled)
                    mBinding?.tvBoll.textColor(R.color.btnMainPressed)
                    mBinding?.kline?.changeMainDrawType(Status.BOLL)
                }
            }
            R.id.tv_main_hide -> {
                if (mainIndex != -1) {
                    mBinding?.kline?.hideSelectData()
                    mainIndex = -1
                    mBinding?.tvMa.textColor(R.color.btnMainDisabled)
                    mBinding?.tvBoll.textColor(R.color.btnMainDisabled)
                    mBinding?.kline?.changeMainDrawType(Status.NONE)
                }
            }
            R.id.tv_macd -> {
                setSubTexts(v, 0)
            }
            R.id.tv_kdj -> {
                setSubTexts(v, 1)
            }
            R.id.tv_rsi -> {
                setSubTexts(v, 2)
            }
            R.id.tv_wr -> {
                setSubTexts(v, 3)
            }
            R.id.tv_sub_hide -> {
                if (subIndex != -1) {
                    mBinding?.kline?.hideSelectData()
                    subTexts[subIndex].textColor(R.color.btnMainDisabled)
                    subIndex = -1
                    mBinding?.kline?.hideChildDraw()
                }
            }
            R.id.tv_fen -> {
                mBinding?.kline?.hideSelectData()
                mBinding?.tvFen.textColor(R.color.btnMainPressed)
                mBinding?.tvK.textColor(R.color.btnMainDisabled)
                mBinding?.kline?.setMainDrawLine(true)
            }
            R.id.tv_k -> {
                mBinding?.kline?.hideSelectData()
                mBinding?.tvFen.textColor(R.color.btnMainDisabled)
                mBinding?.tvK.textColor(R.color.btnMainPressed)
                mBinding?.kline?.setMainDrawLine(false)
            }
        }
    }

}