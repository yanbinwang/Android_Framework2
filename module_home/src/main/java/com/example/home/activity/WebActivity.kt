package com.example.home.activity

import android.view.KeyEvent
import android.view.View
import android.webkit.WebChromeClient
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.common.base.BaseTitleActivity
import com.example.common.base.page.Extra
import com.example.common.bean.WebBundle
import com.example.common.config.ARouterPath
import com.example.common.utils.function.OnWebChangedListener
import com.example.common.utils.function.setClient
import com.example.framework.utils.function.intentSerializable
import com.example.framework.utils.function.value.orFalse
import com.example.framework.utils.function.value.orTrue
import com.example.home.databinding.ActivityWebBinding
import com.example.home.utils.WebHelper
import com.example.home.utils.WebImpl
import com.example.home.utils.WebJavaScriptObject
import java.lang.ref.WeakReference

@Route(path = ARouterPath.WebActivity)
class WebActivity : BaseTitleActivity<ActivityWebBinding>(), WebImpl {
    private val bean by lazy { intentSerializable<WebBundle>(Extra.BUNDLE_BEAN) }
    private val webHelper by lazy { WebHelper(this, binding.flWebRoot, bean?.getUrl()) }

    override fun initView() {
        super.initView()
        //需要标题头并且值已经传输过来了则设置标题
        bean?.let {
            if (!it.getLight().orTrue) initImmersionBar(false)
            if (it.getTitleRequired().orTrue) {
                titleBuilder.setTitle(it.getTitle())
            } else {
                titleBuilder.hideTitle()
            }
        }
    }

    override fun initEvent() {
        super.initEvent()
        //WebView与JS交互
        webHelper.apply {
            webView?.addJavascriptInterface(WebJavaScriptObject(WeakReference(this@WebActivity)), "JSCallAndroid")
            setClient(binding.pbWeb, onPageFinished = { bean?.let { if (it.getTitleRequired().orFalse && it.getTitle().isEmpty()) titleBuilder.setTitle(webView?.title?.trim().orEmpty()) } })
        }
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