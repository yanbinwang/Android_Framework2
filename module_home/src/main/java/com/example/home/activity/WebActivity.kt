package com.example.home.activity

import android.view.KeyEvent
import androidx.fragment.app.FragmentActivity
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.common.base.BaseActivity
import com.example.common.config.ARouterPath
import com.example.home.databinding.ActivityWebBinding
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

    override fun getActivity() = this

    override fun getToKolPage() {
    }

    override fun getBack(value: String?) {
    }

}