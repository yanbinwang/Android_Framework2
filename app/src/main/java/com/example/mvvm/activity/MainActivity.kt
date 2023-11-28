package com.example.mvvm.activity

import android.view.ViewGroup
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.common.base.BaseActivity
import com.example.common.config.ARouterPath
import com.example.common.utils.function.getStatusBarHeight
import com.example.common.utils.function.pt
import com.example.common.widget.textview.edittext.EditTextImpl
import com.example.common.widget.xrecyclerview.refresh.setHeaderDragListener
import com.example.common.widget.xrecyclerview.refresh.setHeaderMaxDragRate
import com.example.framework.utils.function.value.orZero
import com.example.framework.utils.function.value.toSafeFloat
import com.example.framework.utils.function.view.actionCancel
import com.example.framework.utils.function.view.disable
import com.example.framework.utils.function.view.enable
import com.example.framework.utils.function.view.padding
import com.example.framework.utils.function.view.size
import com.example.mvvm.databinding.ActivityMainBinding
import com.google.android.material.appbar.AppBarLayout
import com.scwang.smart.refresh.layout.api.RefreshLayout
import com.scwang.smart.refresh.layout.listener.OnRefreshLoadMoreListener
import kotlin.math.abs

@Route(path = ARouterPath.MainActivity)
class MainActivity : BaseActivity<ActivityMainBinding>(), EditTextImpl {

    override fun initView() {
        super.initView()
        //通过代码动态设置一下顶部的高度
        val statusBarHeight = getStatusBarHeight()
        binding.ivHomeBg.size(height = 163.pt + statusBarHeight)
        binding.clContainer.padding(top = statusBarHeight)
        binding.clContainer.clipToPadding = false
        //刷新控件初始化
        binding.refresh.setHeaderMaxDragRate()
        binding.recList.refresh.disable()
        binding.recList.refresh?.setHeaderMaxDragRate(2.5f)
    }

    override fun initEvent() {
        super.initEvent()
        //设置外层滑动拉伸背景
        binding.refresh.setHeaderDragListener { _: Boolean, _: Float, offset: Int, _: Int, _: Int ->
            changeBgHeight(offset)
        }
        binding.refresh.setOnRefreshListener {
            onRefresh()
        }
        binding.alTop.addOnOffsetChangedListener(object : AppBarLayout.OnOffsetChangedListener {
            var isHide = false
            override fun onOffsetChanged(appBarLayout: AppBarLayout?, verticalOffset: Int) {
                binding.ivHomeBg.translationY = verticalOffset.toSafeFloat()
                val needHide = abs(verticalOffset) + 10.pt < appBarLayout?.totalScrollRange.orZero
                if (needHide != isHide) {
                    isHide = needHide
                    if (needHide) {
                        binding.refresh.enable()
                        binding.recList.isNestedScrollingEnabled = true
                        binding.recList.refresh.disable()
//                        viewAppBarCover.fade(100)
//                        llMenu.fade(100)
//                        ivFilter.appear(100)
                    } else {
                        binding.refresh.disable()
                        binding.recList.isNestedScrollingEnabled = false
                        binding.recList.refresh.enable()
//                        viewAppBarCover.appear(100)
//                        llMenu.appear(100)
//                        ivFilter.fade(100)
                    }
                }
            }
        })
        binding.recList.refresh?.setHeaderDragListener { _: Boolean, percent: Float, _: Int, _: Int, _: Int ->
            if (percent >= 1.8f) {
                //给刷新父级传递一个取消事件
                (binding.recList.refresh?.parent as? ViewGroup)?.actionCancel()
                binding.recList.refresh?.finishRefresh(100)
                binding.recList.isNestedScrollingEnabled = true
                binding.recList.refresh.disable()
                binding.refresh.enable()
                binding.alTop.setExpanded(true, true)
            }
        }
        binding.recList.refresh?.setOnRefreshListener(object : OnRefreshLoadMoreListener {
            override fun onRefresh(refreshLayout: RefreshLayout) {
                onRefresh()
            }

            override fun onLoadMore(refreshLayout: RefreshLayout) {
                onLoadMore()
            }
        })
    }

    /**
     * 滑动时改变对应的图片高度
     */
    private fun changeBgHeight(offset: Int) {
        val imgBgHeight = binding.ivHomeBg.measuredHeight
        if (imgBgHeight <= 0) return
        //设置视图围绕其旋转和缩放的点的 y 位置。默认情况下，枢轴点以对象为中心。设置此属性会禁用此行为并导致视图仅使用显式设置的 pivotX 和 pivotY 值。
        binding.ivHomeBg.pivotY = 0f
        //设置视图围绕轴心点在 Y 轴上缩放的量，作为视图未缩放宽度的比例。值为 1 表示不应用缩放。
        binding.ivHomeBg.scaleY = offset.toSafeFloat() / imgBgHeight.toSafeFloat() + 1f
    }

    private fun onRefresh() {

    }

    private fun onLoadMore() {

    }

}