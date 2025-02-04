package com.example.mvvm.activity

import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.common.base.BaseTitleActivity
import com.example.common.config.ARouterPath
import com.example.framework.utils.function.value.orZero
import com.example.mvvm.BR
import com.example.mvvm.databinding.ActivityCountryBinding
import com.example.mvvm.viewmodel.CountryViewModel
import com.example.mvvm.widget.sidebar.SideAdapter
import com.example.mvvm.widget.sidebar.SideBar

/**
 * 选择城市
 */
@Route(path = ARouterPath.CountryActivity)
class CountryActivity : BaseTitleActivity<ActivityCountryBinding>() {
    private val viewModel by lazy { CountryViewModel().create() }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        titleBuilder.setTitle("选择城市")
        mBinding?.setVariable(BR.adapter, SideAdapter())
        mBinding?.sideBar?.setTextView(mBinding?.tvDialog)
        viewModel?.setExtraView(mBinding?.xrvList)
    }

    override fun initEvent() {
        super.initEvent()
        //手势移动的时候listview跟着走动
        mBinding?.sideBar?.setOnTouchingLetterChangedListener(object : SideBar.OnTouchingLetterChangedListener {
            override fun onTouchingLetterChanged(s: String?) {
                val position = mBinding?.adapter?.getPositionForSection(s?.get(0)?.code.orZero).orZero
                if (position != -1) {
                    mBinding?.xrvList?.scrollToPosition(position)
                }
            }
        })
        viewModel?.setOnEmptyRefreshListener {
            viewModel?.getPageInfo()
        }
        viewModel?.list.observe {
            mBinding?.adapter?.refresh(this)
        }
    }

    override fun initData() {
        super.initData()
        viewModel?.getPageInfo()
    }

}