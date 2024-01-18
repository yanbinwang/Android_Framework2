package com.example.mvvm.activity

import android.view.ViewGroup
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.common.base.BaseActivity
import com.example.common.config.ARouterPath
import com.example.common.utils.function.getStatusBarHeight
import com.example.common.utils.function.pt
import com.example.common.widget.NativeIndicator
import com.example.common.widget.textview.edittext.EditTextImpl
import com.example.common.widget.xrecyclerview.refresh.setHeaderDragListener
import com.example.common.widget.xrecyclerview.refresh.setHeaderMaxDragRate
import com.example.framework.utils.function.value.orZero
import com.example.framework.utils.function.value.toSafeFloat
import com.example.framework.utils.function.view.actionCancel
import com.example.framework.utils.function.view.appear
import com.example.framework.utils.function.view.disable
import com.example.framework.utils.function.view.doOnceAfterLayout
import com.example.framework.utils.function.view.enable
import com.example.framework.utils.function.view.fade
import com.example.framework.utils.function.view.gone
import com.example.framework.utils.function.view.padding
import com.example.framework.utils.function.view.size
import com.example.mvvm.BR
import com.example.mvvm.adapter.DealAdapter
import com.example.mvvm.databinding.ActivityMainBinding
import com.google.android.material.appbar.AppBarLayout
import com.scwang.smart.refresh.layout.api.RefreshLayout
import com.scwang.smart.refresh.layout.listener.OnRefreshLoadMoreListener
import kotlin.math.abs

@Route(path = ARouterPath.MainActivity)
class MainActivity : BaseActivity<ActivityMainBinding>(), EditTextImpl {
    private val indicator by lazy { NativeIndicator(mBinding?.tbMenu) }

    override fun initView() {
        super.initView()
        indicator.bind(listOf("全部", "付款中", "確認中", "凍結中"))
        mBinding?.setVariable(BR.adapter, DealAdapter())
        //通过代码动态设置一下顶部的高度
        val statusBarHeight = getStatusBarHeight()
        mBinding?.ivHomeBg.size(height = 163.pt + statusBarHeight)
        mBinding?.clContainer.padding(top = statusBarHeight)
        mBinding?.clContainer?.clipToPadding = false
        //刷新控件初始化
        mBinding?.refresh.setHeaderMaxDragRate()
        mBinding?.recList?.refresh.disable()
        mBinding?.recList?.refresh?.setHeaderMaxDragRate(2.5f)
        mBinding?.alTop.doOnceAfterLayout {
            //2-2.5
            val translationY = (mBinding?.alTop?.measuredHeight.orZero - mBinding?.clMenu?.measuredHeight.orZero) / 2.5f
            mBinding?.recList?.empty?.translationY = -translationY
        }
    }

    override fun initEvent() {
        super.initEvent()
        //设置外层滑动拉伸背景
        mBinding?.refresh.setHeaderDragListener { _: Boolean, _: Float, offset: Int, _: Int, _: Int ->
            changeBgHeight(offset)
        }
        mBinding?.refresh?.setOnRefreshListener {
            onRefresh()
        }
        mBinding?.alTop?.addOnOffsetChangedListener(object : AppBarLayout.OnOffsetChangedListener {
            var isHide = false
            override fun onOffsetChanged(appBarLayout: AppBarLayout?, verticalOffset: Int) {
                mBinding?.ivHomeBg?.translationY = verticalOffset.toSafeFloat()
                val needHide = abs(verticalOffset) + 10.pt < appBarLayout?.totalScrollRange.orZero
                if (needHide != isHide) {
                    isHide = needHide
                    if (needHide) {
                        mBinding?.refresh.enable()
                        mBinding?.recList?.refresh.disable()
                        mBinding?.recList?.recycler?.isNestedScrollingEnabled = true
                        mBinding?.viewCover.fade(100)
                        mBinding?.llMenuBtn.fade(100)
                    } else {
                        mBinding?.refresh.disable()
                        mBinding?.recList?.refresh.enable()
                        mBinding?.recList?.recycler?.isNestedScrollingEnabled = false
                        mBinding?.viewCover.appear(100)
                        mBinding?.llMenuBtn.appear(100)
                    }
                }
                //2-2.5
                mBinding?.recList?.empty?.translationY = -((mBinding?.alTop?.measuredHeight.orZero - mBinding?.clMenu?.measuredHeight.orZero) + verticalOffset) / 2.5f
            }
        })
        mBinding?.recList?.setHeaderDragListener { _: Boolean, percent: Float, _: Int, _: Int, _: Int ->
            if (percent >= 1.8f) {
                mBinding?.refresh.enable()
                (mBinding?.recList?.refresh?.parent as? ViewGroup)?.actionCancel()
                mBinding?.recList?.refresh?.finishRefresh(100)
                mBinding?.recList?.refresh.disable()
                mBinding?.recList?.recycler?.isNestedScrollingEnabled = true
                mBinding?.alTop?.setExpanded(true, true)
            }
        }
        mBinding?.recList?.setOnRefreshListener(object : OnRefreshLoadMoreListener {
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
        val imgBgHeight = mBinding?.ivHomeBg?.measuredHeight.orZero
        if (imgBgHeight <= 0) return
        //设置视图围绕其旋转和缩放的点的 y 位置。默认情况下，枢轴点以对象为中心。设置此属性会禁用此行为并导致视图仅使用显式设置的 pivotX 和 pivotY 值。
        mBinding?.ivHomeBg?.pivotY = 0f
        //设置视图围绕轴心点在 Y 轴上缩放的量，作为视图未缩放宽度的比例。值为 1 表示不应用缩放。
        mBinding?.ivHomeBg?.scaleY = offset.toSafeFloat() / imgBgHeight.toSafeFloat() + 1f
    }

    override fun initData() {
        super.initData()
        val list = ArrayList<String>()
        for(index in 0 until 40){
            list.add("")
        }
        mBinding?.adapter?.refresh(list)
        mBinding?.recList?.empty.gone()
    }

    private fun onRefresh() {
        mBinding?.refresh?.finishRefresh()
        mBinding?.recList?.refresh?.finishRefresh()
        mBinding?.recList?.refresh?.finishLoadMore()
    }

    private fun onLoadMore() {
        mBinding?.refresh?.finishRefresh()
        mBinding?.recList?.refresh?.finishRefresh()
        mBinding?.recList?.refresh?.finishLoadMore()
    }

}