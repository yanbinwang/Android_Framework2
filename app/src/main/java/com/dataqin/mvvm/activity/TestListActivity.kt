package com.dataqin.mvvm.activity

import com.alibaba.android.arouter.facade.annotation.Route
import com.dataqin.common.base.BaseActivity
import com.dataqin.common.constant.ARouterPath
import com.dataqin.mvvm.BR
import com.dataqin.mvvm.R
import com.dataqin.mvvm.adapter.TestListAdapter
import com.dataqin.mvvm.bridge.TestListViewModel
import com.dataqin.mvvm.databinding.ActivityTestListBinding

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

        binding.adapter?.setOnItemClickListener { _, _, position -> showToast("整体点击：$position") }
        binding.adapter?.setOnItemChildClickListener { _, view, position ->
            when (view.id) {
                R.id.iv_img -> showToast("图片点击：$position")
                R.id.tv_title -> showToast("标题点击：$position")
            }
        }

        viewModel.dataListData.observe(this, {
            binding.adapter?.setList(it)
        })
    }

    override fun initData() {
        super.initData()
        viewModel.getListData()
    }

}