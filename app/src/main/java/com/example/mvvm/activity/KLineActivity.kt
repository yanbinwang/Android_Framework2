package com.example.mvvm.activity

import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.common.base.BaseTitleActivity
import com.example.common.config.ARouterPath
import com.example.mvvm.databinding.ActivityKlineBinding

@Route(path = ARouterPath.KLineActivity)
class KLineActivity : BaseTitleActivity<ActivityKlineBinding>() {

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        titleRoot.setTitle("KLine")
    }

}