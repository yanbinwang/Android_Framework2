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
        super.onCreate(savedInstanceState)
        overridePendingTransition(R.anim.two_level_slide_in, R.anim.two_level_alpha_out)
    }

    override fun initEvent() {
        super.initEvent()
        binding.btnFinish.click { finish() }
//        binding.root.click { finish() }
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(0, R.anim.two_level_slide_out)
//        overridePendingTransition(0, 0)
    }
}