package com.example.home.activity

import android.content.Context
import android.view.KeyEvent
import androidx.lifecycle.LifecycleOwner
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.home.databinding.ActivityWebBinding
import com.example.common.base.BaseActivity
import com.example.common.config.ARouterPath
import com.example.home.utils.WebHelper
import com.example.home.utils.WebImpl

@Route(path = ARouterPath.WebActivity)
class WebActivity : BaseActivity<ActivityWebBinding>(), WebImpl {
    private val webHelper by lazy { WebHelper(this) }

    override fun initData() {
        super.initData()
        webHelper.load()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            webHelper.onKeyDown()
        }
        return true
    }

    override fun getContext(): Context {
        return this
    }

    override fun getLifecycleOwner(): LifecycleOwner {
        return this
    }

    override fun getToKolPage() {
    }

    override fun getBack(result: String?) {
    }

}