package com.example.mvvm.activity

import android.os.Bundle
import com.example.common.base.BaseTitleActivity
import com.example.common.base.bridge.viewModels
import com.example.common.config.RouterPath
import com.example.framework.utils.function.value.orZero
import com.example.mvvm.BR
import com.example.mvvm.databinding.ActivityCountryBinding
import com.example.mvvm.viewmodel.CountryViewModel
import com.example.mvvm.widget.sidebar.SideAdapter
import com.example.mvvm.widget.sidebar.SideBar
import com.therouter.router.Route

/**
 * 选择城市
 */
@Route(path = RouterPath.CountryActivity)
class CountryActivity : BaseTitleActivity<ActivityCountryBinding>() {
    private val viewModel: CountryViewModel by viewModels()

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        titleRoot.setTitle("选择城市")
        mBinding?.setVariable(BR.adapter, SideAdapter())
        mBinding?.sideBar?.setTextView(mBinding?.tvDialog)
        viewModel.setExtraView(mBinding?.xrvList)
    }

    override fun initEvent() {
        super.initEvent()
        // 手势移动的时候listview跟着走动
        mBinding?.sideBar?.setOnTouchingLetterChangedListener(object : SideBar.OnTouchingLetterChangedListener {
            override fun onTouchingLetterChanged(s: String?) {
                val position = mBinding?.adapter?.getPositionForSection(s?.get(0)?.code.orZero).orZero
                mBinding?.xrvList?.scrollToPosition(position)
            }
        })
        viewModel.setOnEmptyRefreshListener {
            viewModel.getPageInfo()
        }
        viewModel.list.observe {
            mBinding?.adapter?.refresh(this)
        }
    }

    override fun initData() {
        super.initData()
        viewModel.getPageInfo()
    }

}