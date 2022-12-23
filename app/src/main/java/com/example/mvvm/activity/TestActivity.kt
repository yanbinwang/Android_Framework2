package com.example.mvvm.activity

import com.alibaba.android.arouter.facade.annotation.Route
import com.example.common.base.BaseActivity
import com.example.common.config.ARouterPath
import com.example.common.config.RequestCode.REQUEST_PHOTO
import com.example.framework.utils.logWTF
import com.example.mvvm.databinding.ActivityTestBinding

@Route(path = ARouterPath.TestActivity)
class TestActivity : BaseActivity<ActivityTestBinding>() {

    override fun initView() {
        super.initView()
        "打开test".logWTF
        setResult(REQUEST_PHOTO)
        finish()
    }

}