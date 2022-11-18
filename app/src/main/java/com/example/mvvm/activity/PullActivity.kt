package com.example.mvvm.activity

import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.base.utils.function.view.click
import com.example.common.base.BaseActivity
import com.example.common.constant.ARouterPath
import com.example.mvvm.R
import com.example.mvvm.databinding.ActivityPullBinding

/**
 * @description
 * @author
 */
@Route(path = ARouterPath.PullActivity)
class PullActivity : BaseActivity<ActivityPullBinding>() {

    override fun onCreate(savedInstanceState: Bundle?) {
        statusBarBuilder.statusBarFullScreen()
        super.onCreate(savedInstanceState)
        overridePendingTransition(R.anim.slide_in, R.anim.alpha_out)
//        if (StatusBarBuilder.statusBarCheckVersion()) window.navigationBarColor = color(R.color.white)
    }

    override fun initEvent() {
        super.initEvent()
        binding.root.click { finish() }
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(0, R.anim.slide_out)
    }
}