package com.example.mvvm.utils

import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebView
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.example.framework.utils.function.value.orFalse
import com.example.framework.utils.function.value.orTrue
import com.example.framework.utils.function.view.background
import com.example.framework.utils.function.view.byHardwareAccelerate
import com.example.framework.utils.function.view.gone
import com.example.common.utils.FormActivityUtil
import com.example.common.utils.WebUtil
import com.example.common.utils.builder.TitleBuilder
import com.example.common.utils.function.OnWebChangedListener
import com.example.common.utils.function.evaluateJs
import com.example.common.utils.function.load
import com.example.common.utils.function.setClient
import com.example.mvvm.R
import com.example.mvvm.activity.WebActivity
import com.example.mvvm.activity.WebBundle
import com.example.mvvm.databinding.ActivityWebBinding
import java.util.*

/**
 * 网页帮助类
 */
class WebHelper(private val act: WebActivity, private val bean: WebBundle?) : LifecycleEventObserver {
    private val binding by lazy { ActivityWebBinding.inflate(act.layoutInflater) }
    private val titleBuilder by lazy { TitleBuilder(act, binding.titleContainer) }
    private val webUtil by lazy { WebUtil(act, binding.flWebRoot) }
    private var webView: WebView? = null

    init {
        act.lifecycle.addObserver(this)
        titleBuilder.getDefault()
        binding.titleContainer.ivLeft.gone()
        addWebView()
        FormActivityUtil.setAct(act)
    }

    private fun addWebView() {
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

    private fun setTitle(title: String) = titleBuilder.setTransparentTitle(title, light = bean?.isLight().orTrue, transparent = false)

    fun load() = webView.load(bean?.getWebUrl().orEmpty(), true)

    fun onKeyDown() {
        webView?.copyBackForwardList()
        webView.evaluateJs("javascript:onBackPressed()") {
            //请求结果不为true（请求拦截）时的处理
            if (it?.lowercase(Locale.US) != "true") {
                if (webView?.canGoBack().orFalse) {
                    webView?.goBack()
                } else {
                    act.finish()
                }
            }
        }
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            Lifecycle.Event.ON_DESTROY -> {
                //        webView?.removeJavascriptInterface("JSCallAndroid")
                webView = null
                act.lifecycle.removeObserver(this)
            }
            else -> {}
        }

    }
}