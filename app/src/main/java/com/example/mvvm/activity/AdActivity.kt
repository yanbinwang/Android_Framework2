package com.example.mvvm.activity

import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.common.base.BaseActivity
import com.example.common.base.bridge.viewModels
import com.example.common.config.ARouterPath
import com.example.common.utils.function.getStatusBarHeight
import com.example.common.utils.function.pt
import com.example.common.widget.xrecyclerview.refresh.setHeaderDragListener
import com.example.common.widget.xrecyclerview.refresh.setHeaderDragRate
import com.example.common.widget.xrecyclerview.refresh.setProgressTint
import com.example.framework.utils.function.value.orZero
import com.example.framework.utils.function.value.toSafeFloat
import com.example.framework.utils.function.view.margin
import com.example.framework.utils.function.view.size
import com.example.home.R
import com.example.mvvm.databinding.ActivityAdBinding
import com.example.mvvm.viewmodel.AdViewModel
import com.scwang.smart.refresh.layout.api.RefreshLayout
import com.scwang.smart.refresh.layout.listener.OnRefreshListener

/**
 * 1.获取的广告取顶部1像素的颜色值
 * 2.根据该颜色值的色差,取电池深浅
 */
@Route(path = ARouterPath.AdActivity)
class AdActivity : BaseActivity<ActivityAdBinding>(), OnRefreshListener {
    private var statusBarDark = true//默认黑色导航栏
    private val viewModel: AdViewModel by viewModels()

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        viewModel.setExtraView(mBinding?.refresh)
        // 刷新控件初始化
        val statusBarHeight = getStatusBarHeight()
        mBinding?.viewCover.size(height = 330.pt + statusBarHeight)
        mBinding?.avBanner.margin(top = statusBarHeight)
        mBinding?.refresh?.setProgressTint(R.color.bgBlack)
        mBinding?.refresh?.setHeaderDragRate()
        mBinding?.refresh?.setHeaderDragListener { _: Boolean, _: Float, offset: Int, _: Int, _: Int ->
            val imgBgHeight = mBinding?.avBanner?.measuredHeight.orZero
            if (imgBgHeight <= 0) return@setHeaderDragListener
            mBinding?.viewCover?.pivotY = 0f
            mBinding?.viewCover?.scaleY = offset.toSafeFloat() / imgBgHeight.toSafeFloat() + 1f
        }
    }

    override fun initEvent() {
        super.initEvent()
        mBinding?.refresh?.setOnRefreshListener(this)
        viewModel.data.observe {
            mBinding?.avBanner?.setConfiguration(localAsset = true, barList = this.second)
            mBinding?.avBanner?.start(this.first)
        }
        mBinding?.avBanner?.setAdvertisingListener(onPageScrolled = {
            val windowBarStatus = it.first
            val coverBg = it.second
            if (windowBarStatus != statusBarDark) {
                statusBarDark = windowBarStatus
                initImmersionBar(statusBarDark)
                mBinding?.refresh?.setProgressTint(if (statusBarDark) R.color.bgBlack else R.color.bgWhite)
            }
            mBinding?.viewCover?.setBackgroundColor(coverBg)
        })
    }

    override fun initData() {
        super.initData()
        viewModel.refresh(true)
    }

    override fun onRefresh(refreshLayout: RefreshLayout) {
        viewModel.refresh(true)
    }

}