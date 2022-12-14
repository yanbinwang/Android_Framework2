package com.example.mvvm.activity

import com.alibaba.android.arouter.facade.annotation.Route
import com.example.common.base.BaseActivity
import com.example.common.config.ARouterPath
import com.example.common.config.RequestCode.REQUEST_PHOTO
import com.example.mvvm.databinding.ActivityStartBinding

@Route(path = ARouterPath.TestActivity)
class TestActivity : BaseActivity<ActivityStartBinding>() {

    override fun initView() {
        super.initView()
        setResult(REQUEST_PHOTO)
        finish()
    }

}