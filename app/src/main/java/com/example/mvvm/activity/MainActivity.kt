package com.example.mvvm.activity

import android.view.View
import android.view.View.OnClickListener
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.common.base.BaseActivity
import com.example.common.config.ARouterPath
import com.example.framework.utils.function.view.clicks
import com.example.mvvm.R
import com.example.mvvm.databinding.ActivityMainBinding

/**
 * 首页
 */
@Route(path = ARouterPath.MainActivity)
class MainActivity : BaseActivity<ActivityMainBinding>(), OnClickListener {

    override fun initEvent() {
        super.initEvent()
        clicks(mBinding?.btnSimple, mBinding?.btnAdvance)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btn_simple -> {}
            R.id.btn_advance -> {}
        }
    }

}