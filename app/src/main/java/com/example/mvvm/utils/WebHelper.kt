package com.example.mvvm.utils

import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.example.common.bean.WebBundle
import com.example.common.config.Extras
import com.example.common.utils.FormActivityUtil
import com.example.common.utils.WebViewUtil
import com.example.common.utils.builder.TitleBuilder
import com.example.common.utils.function.OnWebChangedListener
import com.example.common.utils.function.load
import com.example.common.utils.function.refresh
import com.example.common.utils.function.setClient
import com.example.framework.utils.function.intentSerializable
import com.example.framework.utils.function.value.orFalse
import com.example.framework.utils.function.value.orTrue
import com.example.framework.utils.function.view.background
import com.example.framework.utils.function.view.byHardwareAccelerate
import com.example.mvvm.R
import com.example.mvvm.activity.WebActivity
import com.example.mvvm.databinding.ActivityWebBinding
import java.lang.ref.WeakReference

/**
 * 网页帮助类
 */
class WebHelper(private val activity: WebActivity) : LifecycleEventObserver {
    private val bean by lazy { activity.intentSerializable(Extras.BUNDLE_BEAN) as? WebBundle }
    private val binding by lazy { ActivityWebBinding.inflate(activity.layoutInflater) }
    private val titleBuilder by lazy { TitleBuilder(activity, binding.titleContainer) }
    private val webViewUtil by lazy { WebViewUtil(activity, binding.flWebRoot) }
    private var webView: WebView? = null

    init {
        activity.lifecycle.addObserver(this)
        if (!bean?.getLight().orTrue) activity.initImmersionBar(false)
        addWebView()
        FormActivityUtil.setAct(activity)
    }

    private fun addWebView() {
        //需要标题头并且值已经传输过来了则设置标题
        bean?.apply {
            if (getTitleRequired().orTrue) {
                titleBuilder.setTitle(getTitle()).getDefault()
            } else {
                titleBuilder.hideTitle()
            }
        }
        webView = webViewUtil.webView
        webView?.byHardwareAccelerate()
        webView?.background(R.color.white)
        webView?.settings?.useWideViewPort = true
        webView?.settings?.loadWithOverviewMode = true
        //WebView与JS交互
        webView?.addJavascriptInterface(WebJavaScriptObject(WeakReference(activity)), "JSCallAndroid")
        webView?.setClient(binding.pbWeb, {
            //开始加载页面的操作...
        }, {
            //加载完成后的操作...
            bean?.let { if (it.getTitleRequired().orFalse && it.getTitle().isEmpty()) titleBuilder.setTitle(webView?.title?.trim().orEmpty()).getDefault() }
//            val url = webView?.url.orEmpty()
        }, object : OnWebChangedListener {
            override fun onShowCustomView(view: View?, callback: WebChromeClient.CustomViewCallback?) {
                webViewUtil.onShowCustomView(view, callback)
            }

            override fun onHideCustomView() {
                webViewUtil.onHideCustomView()
            }

            override fun onProgressChanged(progress: Int) {
            }
        })
    }

    /**
     * 加载页面
     */
    fun load() = webView.load(bean?.getUrl().orEmpty(), true)

    /**
     * 刷新页面
     */
    fun refresh() = webView.refresh()

    /**
     * 返回点击
     */
    fun onKeyDown() {
        webView?.copyBackForwardList()
//        webView.evaluateJs("javascript:onBackPressed()") {
//            //请求结果不为true（请求拦截）时的处理
//            if (it?.lowercase(Locale.US) != "true") {
        if (webView?.canGoBack().orFalse) {
            webView?.goBack()
        } else {
            activity.finish()
        }
//            }
//        }
    }

    /**
     * 获取加载的url
     */
    fun getUrl() = bean?.getUrl()

    /**
     * 生命周期订阅
     */
    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            Lifecycle.Event.ON_DESTROY -> {
                webView?.removeJavascriptInterface("JSCallAndroid")
                webView = null
                activity.lifecycle.removeObserver(this)
            }
            else -> {}
        }

    }
}