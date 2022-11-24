package com.example.mvvm.activity

import com.alibaba.android.arouter.facade.annotation.Route
import com.example.base.utils.function.value.toSafeFloat
import com.example.common.base.BaseActivity
import com.example.common.constant.ARouterPath
import com.example.common.utils.builder.shortToast
import com.example.mvvm.databinding.ActivityMainBinding
import com.scwang.smart.refresh.layout.api.RefreshHeader
import com.scwang.smart.refresh.layout.api.RefreshLayout
import com.scwang.smart.refresh.layout.constant.RefreshState
import com.scwang.smart.refresh.layout.listener.OnRefreshListener
import com.scwang.smart.refresh.layout.simple.SimpleMultiListener

@Route(path = ARouterPath.MainActivity)
class MainActivity : BaseActivity<ActivityMainBinding>(), OnRefreshListener {

    override fun initView() {
        super.initView()
        binding.xRefresh.setOnRefreshListener(this)
        binding.xRefresh.setOnMultiListener(object : SimpleMultiListener() {
            override fun onRefresh(refreshLayout: RefreshLayout) {
                super.onRefresh(refreshLayout)
                "触发刷新事件".shortToast()
//                refreshLayout.finishRefresh(2000)
            }

            override fun onLoadMore(refreshLayout: RefreshLayout) {
                super.onLoadMore(refreshLayout)
//                refreshLayout.finishLoadMore(2000)
            }

            override fun onHeaderMoving(header: RefreshHeader?, isDragging: Boolean, percent: Float, offset: Int, headerHeight: Int, maxDragHeight: Int) {
                super.onHeaderMoving(header, isDragging, percent, offset, headerHeight, maxDragHeight)
                binding.secondFloor.translationY = (offset - binding.secondFloor.height).coerceAtMost(binding.xRefresh.layout.height - binding.secondFloor.height).toSafeFloat()
                if (offset >= binding.secondFloor.height) navigation(ARouterPath.PullActivity)
            }
        })

    }

    override fun onRefresh(refreshLayout: RefreshLayout) {
        "触发刷新".shortToast()
        binding.xRefresh.finishRefresh()
    }

    override fun onStop() {
        super.onStop()
        if (binding.xRefresh.state == RefreshState.TwoLevel) {
            binding.xRefresh.closeHeaderOrFooter()
        }
    }

}