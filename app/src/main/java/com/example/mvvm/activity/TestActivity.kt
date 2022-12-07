package com.example.mvvm.activity

import com.alibaba.android.arouter.facade.annotation.Route
import com.example.base.utils.WeakHandler
import com.example.common.base.BaseActivity
import com.example.common.constant.ARouterPath
import com.example.common.constant.RequestCode.REQUEST_PHOTO
import com.example.mvvm.databinding.ActivityStartBinding

@Route(path = ARouterPath.TestActivity)
class TestActivity : BaseActivity<ActivityStartBinding>() {

    override fun initView() {
        super.initView()
        setResult(REQUEST_PHOTO)
        finish()
    }

}