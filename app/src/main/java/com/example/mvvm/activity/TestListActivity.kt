package com.example.mvvm.activity

import com.alibaba.android.arouter.facade.annotation.Route
import com.example.common.base.BaseActivity
import com.example.common.constant.ARouterPath
import com.example.mvvm.BR
import com.example.mvvm.R
import com.example.mvvm.adapter.TestListAdapter
import com.example.mvvm.bridge.TestListViewModel
import com.example.mvvm.databinding.ActivityTestListBinding

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