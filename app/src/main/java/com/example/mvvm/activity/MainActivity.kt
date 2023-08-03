package com.example.mvvm.activity

import android.graphics.Color
import android.widget.TextView
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.common.base.BaseActivity
import com.example.common.config.ARouterPath
import com.example.common.utils.function.getStatusBarHeight
import com.example.framework.utils.function.view.padding
import com.example.mvvm.databinding.ActivityMainBinding
import com.example.mvvm.utils.DataRequest
import com.github.fujianlian.klinechart.DataHelper
import com.github.fujianlian.klinechart.KLineChartAdapter
import com.github.fujianlian.klinechart.KLineEntity
import com.github.fujianlian.klinechart.draw.Status
import com.github.fujianlian.klinechart.formatter.DateFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.jessyan.autosize.internal.CancelAdapt

@Route(path = ARouterPath.MainActivity)
class MainActivity : BaseActivity<ActivityMainBinding>(), CancelAdapt {
    private lateinit var datas: List<KLineEntity>
    private val adapter by lazy { KLineChartAdapter() }
    private val subTexts: ArrayList<TextView> by lazy { arrayListOf(binding.macdText, binding.kdjText, binding.rsiText, binding.wrText) }
    // 主图指标下标
    private var mainIndex = 0
    // 副图指标下标
    private var subIndex = -1

    override fun initView() {
        super.initView()
        binding.title.padding(top = getStatusBarHeight())
        binding.kLineChartView.adapter = adapter
        binding.kLineChartView.dateTimeFormatter = DateFormatter()
        binding.kLineChartView.setGridRows(4)
        binding.kLineChartView.setGridColumns(4)
    }

    override fun initData() {
        super.initData()
        binding.kLineChartView.justShowLoading()
        launch (Dispatchers.IO){
            datas = DataRequest.getALL(this@MainActivity).subList(0, 500)
            DataHelper.calculate(datas)
            runOnUiThread {
                adapter.addFooterData(datas)
                adapter.notifyDataSetChanged()
                binding.kLineChartView.startAnimation()
                binding.kLineChartView.refreshEnd()
            }
        }
    }

    override fun initEvent() {
        super.initEvent()
        binding.maText.setOnClickListener {
            if (mainIndex != 0) {
                binding.kLineChartView.hideSelectData()
                mainIndex = 0
                binding.maText.setTextColor(Color.parseColor("#eeb350"))
                binding.bollText.setTextColor(Color.WHITE)
                binding.kLineChartView.changeMainDrawType(Status.MA)
            }
        }
        binding.bollText.setOnClickListener {
            if (mainIndex != 1) {
                binding.kLineChartView.hideSelectData()
                mainIndex = 1
                binding.bollText.setTextColor(Color.parseColor("#eeb350"))
                binding.maText.setTextColor(Color.WHITE)
                binding.kLineChartView.changeMainDrawType(Status.BOLL)
            }
        }
        binding.mainHide.setOnClickListener {
            if (mainIndex != -1) {
                binding.kLineChartView.hideSelectData()
                mainIndex = -1
                binding.bollText.setTextColor(Color.WHITE)
                binding.maText.setTextColor(Color.WHITE)
                binding.kLineChartView.changeMainDrawType(Status.NONE)
            }
        }
        for ((index, text) in subTexts.withIndex()) {
            text.setOnClickListener {
                if (subIndex != index) {
                    binding.kLineChartView.hideSelectData()
                    if (subIndex != -1) {
                        subTexts[subIndex].setTextColor(Color.WHITE)
                    }
                    subIndex = index
                    text.setTextColor(Color.parseColor("#eeb350"))
                    binding.kLineChartView.setChildDraw(subIndex)
                }
            }
        }
        binding.subHide.setOnClickListener {
            if (subIndex != -1) {
                binding.kLineChartView.hideSelectData()
                subTexts[subIndex].setTextColor(Color.WHITE)
                subIndex = -1
                binding.kLineChartView.hideChildDraw()
            }
        }
        binding.fenText.setOnClickListener {
            binding.kLineChartView.hideSelectData()
            binding.fenText.setTextColor(Color.parseColor("#eeb350"))
            binding.kText.setTextColor(Color.WHITE)
            binding.kLineChartView.setMainDrawLine(true)
        }
        binding.kText.setOnClickListener {
            binding.kLineChartView.hideSelectData()
            binding.kText.setTextColor(Color.parseColor("#eeb350"))
            binding.fenText.setTextColor(Color.WHITE)
            binding.kLineChartView.setMainDrawLine(false)
        }
    }
}