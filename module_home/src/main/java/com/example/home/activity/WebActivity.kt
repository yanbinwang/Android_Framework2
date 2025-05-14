package com.example.home.activity

import android.os.Bundle
import android.view.KeyEvent
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.common.base.BaseTitleActivity
import com.example.common.base.page.Extra
import com.example.common.bean.WebBundle
import com.example.common.config.ARouterPath
import com.example.common.utils.builder.TitleBuilder.Companion.RIGHT_ICON
import com.example.common.utils.builder.TitleBuilder.Companion.TITLE_TEXT
import com.example.common.utils.function.orNoData
import com.example.framework.utils.function.intentSerializable
import com.example.framework.utils.function.value.orTrue
import com.example.home.R
import com.example.home.databinding.ActivityWebBinding
import com.example.home.utils.WebHelper
import com.example.home.utils.WebImpl

@Route(path = ARouterPath.WebActivity)
class WebActivity : BaseTitleActivity<ActivityWebBinding>(), WebImpl {
    private val bundle by lazy { intentSerializable<WebBundle>(Extra.BUNDLE_BEAN) }
    private val helper by lazy { WebHelper(this, mBinding).apply { setBundle(bundle, this@WebActivity) } }
    private val isTitleRequired get() = bundle?.getTitleRequired().orTrue

    override fun isImmersionBarEnabled() = false

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        initImmersionBar(bundle?.getLight().orTrue)
        //需要标题头并且值已经传输过来了则设置标题
        titleBuilder.apply {
            if (isTitleRequired) {
                setTitle(bundle?.getTitle().orNoData())
            } else {
                hideTitle()
            }
        }
    }

    override fun initEvent() {
        super.initEvent()
        helper.setClientListener({}, { title ->
            if (isTitleRequired) {
                //当传输的title为空时，取一次网页自带的标题并且刷新按钮浮现
                titleBuilder.apply {
                    if (nonNull(TITLE_TEXT, RIGHT_ICON)) return@apply
                    if (bundle?.getTitle().isNullOrEmpty()) {
                        setTitle(title.orNoData())
                    }
                    setRight(R.mipmap.ic_refresh, R.color.bgBlack) {
                        helper.refresh()
                    }
                }
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