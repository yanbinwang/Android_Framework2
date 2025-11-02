package com.example.mvvm.activity

import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.common.BaseApplication.Companion.needOpenHome
import com.example.common.base.BaseActivity
import com.example.common.config.ARouterPath
import com.example.common.widget.textview.edittext.EditTextImpl
import com.example.framework.utils.function.view.click
import com.example.mvvm.databinding.ActivityMainBinding

/**
 * 首页
 */
@Route(path = ARouterPath.MainActivity)
class MainActivity : BaseActivity<ActivityMainBinding>(), EditTextImpl {

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        mBinding?.tvKline.click {
            navigation(ARouterPath.KLineActivity)
        }
    }

    override fun onResume() {
        if (needOpenHome.get()) needOpenHome.set(false)
        super.onResume()
    }

}