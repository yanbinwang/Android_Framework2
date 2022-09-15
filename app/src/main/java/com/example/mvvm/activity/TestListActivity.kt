package com.example.mvvm.activity

import com.alibaba.android.arouter.facade.annotation.Route
import com.example.common.base.BaseActivity
import com.example.common.constant.ARouterPath
import com.example.mvvm.adapter.TestListAdapter
import com.example.mvvm.bridge.TestListViewModel
import com.example.mvvm.databinding.ActivityTestListBinding
import com.example.mvvm.model.TestListModel


/**
 * Created by WangYanBin on 2020/6/4.
 */
@Route(path = ARouterPath.TestListActivity)
class TestListActivity : BaseActivity<ActivityTestListBinding>() {
    private val viewModel by lazy { createViewModel(TestListViewModel::class.java) }

    override fun initView() {
        super.initView()
//        //绑定适配器,监听
//        binding.setVariable(BR.adapter, TestListAdapter())
//        binding.setVariable(BR.mobile,intent.getParcelableExtra(Extras.MOBILE))
    }

    var mStrings = ArrayList<TestListModel>()

    override fun initEvent() {
        super.initEvent()


        for (i in 1..20) {
            mStrings.add(TestListModel("標題：${i}", "內容：${i}", i))
        }
//        binding.recTest.setFlatFlow(false)
//        binding.recTest.setIntervalRatio(0.8f)
        val myAdapter = TestListAdapter()
        myAdapter.data = mStrings
        binding.recTest.adapter = myAdapter


//        binding.btnTest.setOnClickListener { viewModel.getListData() }
//
//        binding.adapter!!.onItemClick = {
//            showToast("整体点击：$it")
//        }
//
//        viewModel.dataListData.observe(this) {
//            binding.adapter?.data = it
//        }

//        binding.recTest.setOnRefreshListener(object : RefreshListenerAdapter(){
//            override fun onRefresh(refreshLayout: TwinklingRefreshLayout?) {
//                super.onRefresh(refreshLayout)
//                TimerHelper.schedule({
//                    binding.recTest.finishRefresh()
//                },2000)
//            }
//
//            override fun onLoadMore(refreshLayout: TwinklingRefreshLayout?) {
//                super.onLoadMore(refreshLayout)
//                TimerHelper.schedule({
//                    binding.recTest.finishRefresh()
//                },2000)
//            }
//        })
//        binding.recTest.setOnRefreshListener(listener)
    }

//    override fun initData() {
//        super.initData()
//        viewModel.getListData()
////        listener.onRefresh(null)
//    }

//    val  listener = object : RefreshListenerAdapter(){
//        override fun onRefresh(refreshLayout: TwinklingRefreshLayout?) {
//            super.onRefresh(refreshLayout)
//            if(null == refreshLayout) {
//                showToast("主动调用")
//            } else {
//                TimerHelper.schedule({
//                    binding.recTest.finishRefresh()
//                },2000)
//            }
//        }
//
//        override fun onLoadMore(refreshLayout: TwinklingRefreshLayout?) {
//            super.onLoadMore(refreshLayout)
//            TimerHelper.schedule({
//                binding.recTest.finishRefresh()
//            },2000)
//        }
//    }

}