package com.example.mvvm.activity

import android.os.Bundle
import android.view.View
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.base.utils.function.color
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
        window.navigationBarColor = color(R.color.white)
//        window.decorView.apply { systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION }
        super.onCreate(savedInstanceState)
        overridePendingTransition(R.anim.slide_in, R.anim.alpha_out)
    }

    override fun initEvent() {
        super.initEvent()
        binding.btnFinish.click { finish() }
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(0, R.anim.slide_out)
//        overridePendingTransition(0, 0)
    }
}