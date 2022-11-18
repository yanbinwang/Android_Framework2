package com.example.mvvm.activity

import android.annotation.SuppressLint
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.base.utils.function.view.click
import com.example.common.base.BaseActivity
import com.example.common.constant.ARouterPath
import com.example.common.utils.ExtraNumber.ptFloat
import com.example.common.utils.builder.shortToast
import com.example.mvvm.databinding.ActivityMainBinding
import com.example.mvvm.widget.PullRefreshHeader


/**
 * object是单例，适合做一些重复性的操作
 */
@Route(path = ARouterPath.MainActivity)
class MainActivity : BaseActivity<ActivityMainBinding>() {
    private val header by lazy { PullRefreshHeader(this) }

    override fun initView() {
        super.initView()
        binding.xRefresh.setRefreshHeader(header)
        binding.xRefresh.setHeaderHeight(40.ptFloat())//Header标准高度
        header.onMoving = {
            if (it > 0.8) {
                navigation(ARouterPath.PullActivity)
                binding.xRefresh.finishRefresh()
            }
        }
    }

    @SuppressLint("SetTextI18n")
    override fun initEvent() {
        super.initEvent()
        binding.btnCopy.click {
            navigation(ARouterPath.PullActivity)
        }
    }

}