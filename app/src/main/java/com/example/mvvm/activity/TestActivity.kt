package com.example.mvvm.activity

import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.common.base.BaseActivity
import com.example.common.base.page.RequestCode.REQUEST_PHOTO
import com.example.common.config.ARouterPath
import com.example.framework.utils.logWTF
import com.example.mvvm.databinding.ActivityTestBinding

/**
 * https://blog.csdn.net/YllP_1230/article/details/130317459
 */
@Route(path = ARouterPath.TestActivity)
class TestActivity : BaseActivity<ActivityTestBinding>() {

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        "打开test".logWTF
        setResult(REQUEST_PHOTO)
        finish()
    }

}