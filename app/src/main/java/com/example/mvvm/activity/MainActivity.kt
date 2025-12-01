package com.example.mvvm.activity

import android.os.Bundle
import com.example.common.BaseApplication.Companion.needOpenHome
import com.example.common.base.BaseActivity
import com.example.common.config.RouterPath
import com.example.common.widget.textview.edittext.EditTextImpl
import com.example.framework.utils.function.view.click
import com.example.mvvm.databinding.ActivityMainBinding
import com.therouter.router.Route

/**
 * 首页
 */
@Route(path = RouterPath.MainActivity)
class MainActivity : BaseActivity<ActivityMainBinding>(), EditTextImpl {

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        mBinding?.tvKline.click {
            navigation(RouterPath.KLineActivity)
        }
    }

    override fun onResume() {
        if (needOpenHome.get()) needOpenHome.set(false)
        super.onResume()
    }

}