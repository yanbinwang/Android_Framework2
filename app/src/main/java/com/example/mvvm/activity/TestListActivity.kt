package com.example.mvvm.activity

import com.alibaba.android.arouter.facade.annotation.Route
import com.example.base.utils.TimerHelper
import com.example.common.base.BaseActivity
import com.example.common.constant.ARouterPath
import com.example.mvvm.BR
import com.example.mvvm.R
import com.example.mvvm.adapter.TestListAdapter
import com.example.mvvm.bridge.TestListViewModel
import com.example.mvvm.databinding.ActivityTestListBinding
import com.lcodecore.tkrefreshlayout.RefreshListenerAdapter
import com.lcodecore.tkrefreshlayout.TwinklingRefreshLayout

/**
 * Created by WangYanBin on 2020/6/4.
 */
@Route(path = ARouterPath.TestListActivity)
class TestListActivity : BaseActivity<ActivityTestListBinding>() {
    private val viewModel by lazy { createViewModel(TestListViewModel::class.java) }

    override fun initView() {
        super.initView()
        //绑定适配器,监听
        binding.setVariable(BR.adapter, TestListAdapter())
    }

    override fun initEvent() {
        super.initEvent()
        binding.btnTest.setOnClickListener { viewModel.getListData() }

        binding.adapter!!.onItemClick = {
            showToast("整体点击：$it")
        }

        viewModel.dataListData.observe(this) {
            binding.adapter?.data = it
        }

        binding.recTest.setOnRefreshListener(object : RefreshListenerAdapter(){
            override fun onRefresh(refreshLayout: TwinklingRefreshLayout?) {
                super.onRefresh(refreshLayout)
                TimerHelper.schedule({
                    binding.recTest.finishRefresh()
                },2000)
            }

            override fun onLoadMore(refreshLayout: TwinklingRefreshLayout?) {
                super.onLoadMore(refreshLayout)
                TimerHelper.schedule({
                    binding.recTest.finishRefresh()
                },2000)
            }
        })
    }

    override fun initData() {
        super.initData()
        viewModel.getListData()
    }

}