package com.example.mvvm.activity

import com.alibaba.android.arouter.facade.annotation.Route
import com.example.common.base.BaseActivity
import com.example.common.constant.ARouterPath
import com.example.common.utils.builder.shortToast
import com.example.mvvm.databinding.ActivityMainBinding
import com.example.mvvm.widget.PullRefreshHeader
import com.scwang.smart.refresh.layout.api.RefreshLayout
import com.scwang.smart.refresh.layout.listener.OnRefreshListener

@Route(path = ARouterPath.MainActivity)
class MainActivity : BaseActivity<ActivityMainBinding>(), OnRefreshListener {
    private val header by lazy { PullRefreshHeader(this) }

    override fun initView() {
        super.initView()
        binding.xRefresh.setRefreshHeader(header)
        binding.xRefresh.setHeaderTriggerRate(0.8f)
        binding.xRefresh.setOnRefreshListener(this)
        header.onReleased = { navigation(ARouterPath.PullActivity) }
    }

    override fun onRefresh(refreshLayout: RefreshLayout) {
        "触发刷新".shortToast()
        binding.xRefresh.finishRefresh()
    }

}