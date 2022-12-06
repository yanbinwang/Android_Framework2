package com.example.mvvm.activity

import android.view.KeyEvent
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.base.utils.function.intentSerializable
import com.example.common.base.BaseActivity
import com.example.common.constant.ARouterPath
import com.example.common.constant.Extras
import com.example.mvvm.databinding.ActivityWebBinding
import com.example.mvvm.utils.WebHelper
import java.io.Serializable

/**
 * @description
 * @author
 */
@Route(path = ARouterPath.WebActivity)
class WebActivity : BaseActivity<ActivityWebBinding>() {
    private val bean by lazy { intentSerializable(Extras.BUNDLE_BEAN) as? WebBundle }
    private val webHelper by lazy { WebHelper(this, binding, bean) }

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

}

abstract class WebBundle : Serializable {

    /**
     * 黑白电池
     */
    abstract fun isLight(): Boolean

    /**
     * 是否需要头
     */
    abstract fun isWebTitleRequired(): Boolean

    /**
     * 获取页面标题
     */
    abstract fun getWebTitle(): String

    /**
     *获取页面地址
     */
    abstract fun getWebUrl(): String

}