package com.example.mvvm.activity

import androidx.lifecycle.Observer
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.common.base.BaseActivity
import com.example.common.constant.ARouterPath
import com.example.mvvm.BR
import com.example.mvvm.R
import com.example.mvvm.adapter.TestListAdapter
import com.example.mvvm.bridge.TestListViewModel
import com.example.mvvm.bridge.event.TestListEvent
import com.example.mvvm.databinding.ActivityTestListBinding

/**
 * Created by WangYanBin on 2020/6/4.
 */
@Route(path = ARouterPath.TestListActivity)
open class TestListActivity : BaseActivity<ActivityTestListBinding>() {
    protected val viewModel: TestListViewModel by lazy {
        createViewModel(TestListViewModel::class.java)
    }

    override fun getLayoutResID(): Int {
        return R.layout.activity_test_list
    }

    override fun initView() {
        super.initView()
//        viewModel = createViewModel(TestListViewModel::class.java)
        //绑定适配器,监听
        binding?.setVariable(BR.adapter, TestListAdapter())
        binding?.setVariable(BR.event, TestListEvent())
    }

    override fun initEvent() {
        super.initEvent()
        viewModel?.dataList?.observe(this, Observer {
            binding?.adapter?.setList(it)
        })
    }

    override fun initData() {
        super.initData()
        viewModel?.getListData()
    }
}