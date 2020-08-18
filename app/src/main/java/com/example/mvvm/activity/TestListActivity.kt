package com.example.mvvm.activity

import android.view.View
import androidx.lifecycle.Observer
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
    private val viewModel: TestListViewModel by lazy {
        createViewModel(TestListViewModel::class.java)
    }

    override fun getLayoutResID(): Int {
        return R.layout.activity_test_list
    }

    override fun initView() {
        super.initView()
        //绑定适配器,监听
        binding?.setVariable(BR.adapter, TestListAdapter())
        binding?.setVariable(BR.event, TestListActivity())
    }

    override fun initEvent() {
        super.initEvent()
        binding?.btnTest?.setOnClickListener { View.OnClickListener { viewModel.getListData() } }

        binding?.adapter?.setOnItemClickListener { _, _, position -> showToast("整体点击：$position") }
        binding?.adapter?.setOnItemChildClickListener { _, view, position ->
            when (view.id) {
                R.id.iv_img -> showToast("图片点击：$position")
                R.id.tv_title -> showToast("标题点击：$position")
            }
        }

        viewModel.dataList.observe(this, Observer {
            binding?.adapter?.setList(it)
        })
    }

    override fun initData() {
        super.initData()
        viewModel.getListData()
    }

}