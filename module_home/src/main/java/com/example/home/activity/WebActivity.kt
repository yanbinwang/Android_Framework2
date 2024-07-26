package com.example.home.activity

import android.os.Bundle
import android.view.KeyEvent
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.common.base.BaseTitleActivity
import com.example.common.base.page.Extra
import com.example.common.bean.WebBundle
import com.example.common.config.ARouterPath
import com.example.common.utils.function.pt
import com.example.framework.utils.function.intentSerializable
import com.example.framework.utils.function.value.orFalse
import com.example.framework.utils.function.value.orTrue
import com.example.framework.utils.function.view.gone
import com.example.home.R
import com.example.home.databinding.ActivityWebBinding
import com.example.home.utils.WebHelper
import com.example.home.utils.WebImpl

@Route(path = ARouterPath.WebActivity)
class WebActivity : BaseTitleActivity<ActivityWebBinding>(), WebImpl {
    private val bean by lazy { intentSerializable<WebBundle>(Extra.BUNDLE_BEAN) }
    private val helper by lazy { WebHelper(this, mBinding).apply { setBundle(bean, this@WebActivity) } }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        if (!bean?.getLight().orTrue) initImmersionBar(false)
        //需要标题头并且值已经传输过来了则设置标题
        titleBuilder.apply {
            if (bean?.getTitleRequired().orTrue) {
                setTitle(bean?.getTitle().orEmpty())
            } else {
                hideTitle()
            }
        }
    }

    override fun initEvent() {
        super.initEvent()
        helper.setClientListener({
            titleBuilder.ivRight.gone()
        }, {
            if (bean?.isTitleRequired().orFalse) {
                titleBuilder
                    //当传输的title为空时，取一次网页自带的标题并且刷新按钮浮现
                    .setTitle(bean?.getTitle() ?: it.orEmpty())
                    .setRight(R.mipmap.ic_refresh, R.color.bgBlack, 60.pt, 60.pt) { helper.refresh() }
            }
        })
    }

    override fun initData() {
        super.initData()
        helper.load()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            helper.onKeyDown()
        }
        return true
    }

    override fun getActivity() = this

    override fun getGoBackJS(value: String?) {
    }

    override fun getToKolJS() {
    }

}