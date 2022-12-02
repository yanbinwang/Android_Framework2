package com.example.mvvm.activity

import android.view.KeyEvent
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebView
import androidx.core.view.isVisible
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.base.utils.function.intentSerializable
import com.example.base.utils.function.value.orFalse
import com.example.base.utils.function.value.orTrue
import com.example.base.utils.function.view.background
import com.example.base.utils.function.view.byHardwareAccelerate
import com.example.base.utils.function.view.gone
import com.example.common.base.BaseActivity
import com.example.common.constant.ARouterPath
import com.example.common.constant.Extras
import com.example.common.utils.FormActivityUtil
import com.example.common.utils.builder.TitleBuilder
import com.example.common.utils.web.*
import com.example.mvvm.R
import com.example.mvvm.databinding.ActivityWebBinding
import java.io.Serializable
import java.util.*

/**
 * @description
 * @author
 */
@Route(path = ARouterPath.WebActivity)
class WebActivity : BaseActivity<ActivityWebBinding>() {
    private val bean by lazy { intentSerializable(Extras.BUNDLE_BEAN) as? WebBundle }
    private val titleBuilder by lazy { TitleBuilder(this, binding.titleContainer) } //标题栏
    private val webUtil by lazy { WebUtil(this, binding.flWebRoot) }
    protected var webView: WebView? = null//继承的子类可以拿

    override fun initView() {
        super.initView()
        titleBuilder.getDefault()
        binding.titleContainer.ivLeft.gone()
        addWebView()
        FormActivityUtil.setAct(this)
    }

    private fun addWebView() {
        //不传string会去获取加载时候的图
        if (bean?.isWebTitleRequired().orTrue) {
            bean?.let { if (it.getWebTitle().isNotEmpty()) setTitle(it.getWebTitle()) }
        } else {
            binding.titleContainer.clContainer.gone()
        }
        webView = webUtil.webView
        webView?.byHardwareAccelerate()
        webView?.background(R.color.white)
        webView?.settings?.useWideViewPort = true
        webView?.settings?.loadWithOverviewMode = true
        //WebView与JS交互
//        webView?.addJavascriptInterface(JsInterface(WeakReference(this)), "JSCallAndroid")
        webView?.setClient(binding.pbWeb, {
//            toolBarUtil.hideRightBtn()
        }, {
            val webTitle = webView?.title?.trim()
            if (bean?.isWebTitleRequired().orFalse && !webTitle.isNullOrEmpty()) setTitle(webTitle)
//            val url = webView?.url.orEmpty()
//            if (url.contains("zendesk.com")) {
//                toolbar?.imgRight?.tint(R.color.textPrimary)
//                toolBarUtil.setRightImgBtn(R.mipmap.share_toolbar, 18.pt) {
//                    showShareDialog(shareTitle, webView?.url)
//                }
//            } else {
//                toolBarUtil.hideRightBtn()
//            }
        }, object : OnWebChangedListener {

            override fun onShowCustomView(view: View?, callback: WebChromeClient.CustomViewCallback?) {
                webUtil.onShowCustomView(view, callback)
            }

            override fun onHideCustomView() {
                webUtil.onHideCustomView()
            }

            override fun onProgressChanged(progress: Int) {
                binding.titleContainer.ivLeft.isVisible = webView?.canGoBack().orFalse
            }
        })
    }

    private fun setTitle(title: String) {
        titleBuilder.setTransparentTitle(title, light = bean?.isLight().orTrue, transparent = false)
    }

    override fun initData() {
        super.initData()
        webView.load(bean?.getWebUrl().orEmpty(), true)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            webView?.copyBackForwardList()
            webView.evaluateJs("javascript:onBackPressed()") {
                //请求结果不为true（请求拦截）时的处理
                if (it?.lowercase(Locale.US) != "true") {
                    if (webView?.canGoBack().orFalse) {
                        webView?.goBack()
                    } else {
                        finish()
                    }
                }
            }
        }
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
//        webView?.removeJavascriptInterface("JSCallAndroid")
        webView = null
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