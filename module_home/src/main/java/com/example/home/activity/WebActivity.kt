package com.example.home.activity

import android.view.KeyEvent
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.common.base.BaseTitleActivity
import com.example.common.base.page.Extra
import com.example.common.bean.WebBundle
import com.example.common.config.ARouterPath
import com.example.framework.utils.function.intentSerializable
import com.example.framework.utils.function.value.orFalse
import com.example.framework.utils.function.value.orTrue
import com.example.home.R
import com.example.home.databinding.ActivityWebBinding
import com.example.home.utils.WebHelper
import com.example.home.utils.WebImpl

@Route(path = ARouterPath.WebActivity)
class WebActivity : BaseTitleActivity<ActivityWebBinding>(), WebImpl {
    private val bean by lazy { intentSerializable<WebBundle>(Extra.BUNDLE_BEAN) }
    private val webHelper by lazy { WebHelper(this).apply { setBundle(bean) } }

    override fun initView() {
        super.initView()
        if (!bean?.getLight().orTrue) initImmersionBar(false)
        //需要标题头并且值已经传输过来了则设置标题
        titleBuilder.apply {
            if (bean?.getTitleRequired().orTrue) {
                setTitle(bean?.getTitle().orEmpty())
                setRight(R.mipmap.ic_refresh) { webHelper.refresh() }
            } else {
                hideTitle()
            }
        }
    }

    override fun initEvent() {
        super.initEvent()
        webHelper.setClientListener({}, {
            if (bean?.isTitleRequired().orFalse && !it.isNullOrEmpty()) titleBuilder.setTitle(it)
        })
    }

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