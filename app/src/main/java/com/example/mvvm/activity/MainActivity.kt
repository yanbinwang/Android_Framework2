package com.example.mvvm.activity

import com.alibaba.android.arouter.facade.annotation.Route
import com.example.base.utils.function.view.click
import com.example.common.base.BaseTitleActivity
import com.example.common.constant.ARouterPath
import com.example.mvvm.databinding.ActivityMainBinding
import com.example.mvvm.utils.PermissionHelper

@Route(path = ARouterPath.MainActivity)
class MainActivity : BaseTitleActivity<ActivityMainBinding>() {

    override fun initView() {
        super.initView()
        titleBuilder.setTitle("10086").getDefault()
    }

    override fun initEvent() {
        super.initEvent()
        binding.btnLogin.click {
            PermissionHelper.with(this).onRequest({
                if(it) {
                    navigation(ARouterPath.LoginActivity)
                }
            }).requestPermissions()
        }
    }


}