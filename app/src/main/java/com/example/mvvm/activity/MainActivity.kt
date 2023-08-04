package com.example.mvvm.activity

import android.graphics.Color
import android.view.View
import android.view.View.OnClickListener
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.common.base.BaseActivity
import com.example.common.config.ARouterPath
import com.example.common.utils.function.getStatusBarHeight
import com.example.framework.utils.function.value.safeGet
import com.example.framework.utils.function.view.click
import com.example.framework.utils.function.view.clicks
import com.example.framework.utils.function.view.margin
import com.example.mvvm.R
import com.example.mvvm.databinding.ActivityMainBinding
import com.example.mvvm.viewmodel.MainViewModel
import com.github.fujianlian.klinechart.KLineChartAdapter
import com.github.fujianlian.klinechart.draw.Status
import com.github.fujianlian.klinechart.formatter.DateFormatter

@Route(path = ARouterPath.MainActivity)
class MainActivity : BaseActivity<ActivityMainBinding>(), OnClickListener {
    //主图指标下标
    private var mainIndex = 0
    //副图指标下标
    private var subIndex = -1
    private val adapter by lazy { KLineChartAdapter() }
    private val subTexts by lazy { arrayListOf(binding.macdText, binding.kdjText, binding.rsiText, binding.wrText) }
    private val viewModel by lazy { createViewModel(MainViewModel::class.java) }

    override fun initView() {
        super.initView()
        binding.title.margin(top = getStatusBarHeight())
        binding.kLineChartView.apply {
            adapter = this@MainActivity.adapter
            dateTimeFormatter = DateFormatter()
            setGridRows(4)
            setGridColumns(4)
            justShowLoading()
        }
    }

    override fun initData() {
        super.initData()
        viewModel.getPageData()
    }

    override fun initEvent() {
        super.initEvent()
        clicks(binding.maText, binding.bollText, binding.mainHide, binding.subHide, binding.fenText, binding.kText)
        for ((index, text) in subTexts.withIndex()) {
            text.click {
                if (subIndex != index) {
                    binding.kLineChartView.hideSelectData()
                    subTexts.safeGet(subIndex)?.setTextColor(Color.WHITE)
                    subIndex = index
                    text.setTextColor(Color.parseColor("#eeb350"))
                    binding.kLineChartView.setChildDraw(subIndex)
                }
            }
        }
        viewModel.kLineData.observe(this) {
            adapter.addFooterData(it)
            adapter.notifyDataSetChanged()
            binding.kLineChartView.startAnimation()
            binding.kLineChartView.refreshEnd()
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.maText -> {
                if (mainIndex != 0) {
                    binding.kLineChartView.hideSelectData()
                    mainIndex = 0
                    binding.maText.setTextColor(Color.parseColor("#eeb350"))
                    binding.bollText.setTextColor(Color.WHITE)
                    binding.kLineChartView.changeMainDrawType(Status.MA)
                }
            }
            R.id.bollText -> {
                if (mainIndex != 1) {
                    binding.kLineChartView.hideSelectData()
                    mainIndex = 1
                    binding.bollText.setTextColor(Color.parseColor("#eeb350"))
                    binding.maText.setTextColor(Color.WHITE)
                    binding.kLineChartView.changeMainDrawType(Status.BOLL)
                }
            }
            R.id.mainHide -> {
                if (mainIndex != -1) {
                    binding.kLineChartView.hideSelectData()
                    mainIndex = -1
                    binding.bollText.setTextColor(Color.WHITE)
                    binding.maText.setTextColor(Color.WHITE)
                    binding.kLineChartView.changeMainDrawType(Status.NONE)
                }
            }
            R.id.subHide -> {
                if (subIndex != -1) {
                    binding.kLineChartView.hideSelectData()
                    subTexts[subIndex].setTextColor(Color.WHITE)
                    subIndex = -1
                    binding.kLineChartView.hideChildDraw()
                }
            }
            R.id.fenText -> {
                binding.kLineChartView.hideSelectData()
                binding.fenText.setTextColor(Color.parseColor("#eeb350"))
                binding.kText.setTextColor(Color.WHITE)
                binding.kLineChartView.setMainDrawLine(true)
            }
            R.id.kText -> {
                binding.kLineChartView.hideSelectData()
                binding.kText.setTextColor(Color.parseColor("#eeb350"))
                binding.fenText.setTextColor(Color.WHITE)
                binding.kLineChartView.setMainDrawLine(false)
            }
        }
    }
}