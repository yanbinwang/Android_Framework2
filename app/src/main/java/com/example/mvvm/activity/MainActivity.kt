package com.example.mvvm.activity

import com.alibaba.android.arouter.facade.annotation.Route
import com.example.common.base.BaseActivity
import com.example.common.config.ARouterPath
import com.example.common.utils.function.getStatusBarHeight
import com.example.common.utils.function.pt
import com.example.common.widget.xrecyclerview.refresh.ProjectRefreshHeader
import com.example.common.widget.xrecyclerview.refresh.headerMaxDragRate
import com.example.framework.utils.function.value.toSafeFloat
import com.example.framework.utils.function.view.padding
import com.example.framework.utils.function.view.size
import com.example.mvvm.databinding.ActivityMainBinding

@Route(path = ARouterPath.MainActivity)
class MainActivity : BaseActivity<ActivityMainBinding>() {

    override fun initEvent() {
        super.initEvent()
        //通过代码动态重置一下顶部的高度
        val bgHeight = 328.pt + getStatusBarHeight()
        binding.ivFundsBg.size(height = bgHeight)
        binding.llFunds.apply {
            size(height = bgHeight)
            padding(top = getStatusBarHeight())
        }
        //全屏的刷新，顶部需要空出导航栏的距离
        binding.refresh.headerMaxDragRate()
        //设置头部的滑动监听
        (binding.refresh.refreshHeader as? ProjectRefreshHeader)?.apply {
            onDragListener = { isDragging: Boolean, percent: Float, offset: Int, height: Int, maxDragHeight: Int ->
                changeBgHeight(offset)
            }
        }
    }

    /**
     * 滑动时改变对应的图片高度
     */
    private fun changeBgHeight(offset: Int) {
        val imgBgHeight = binding.llFunds.measuredHeight
        if (imgBgHeight <= 0) return
        binding.ivFundsBg.pivotY = 0f
        binding.ivFundsBg.scaleY = offset.toSafeFloat() / imgBgHeight.toSafeFloat() + 1f
    }

}