package com.example.home.utils

import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.example.common.base.page.Extra
import com.example.home.databinding.ActivityWebBinding
import com.example.common.bean.WebBundle
import com.example.common.utils.FormActivityUtil
import com.example.common.utils.WebUtil
import com.example.common.utils.builder.TitleBuilder
import com.example.common.utils.function.*
import com.example.framework.utils.function.intentSerializable
import com.example.framework.utils.function.value.orFalse
import com.example.framework.utils.function.value.orTrue
import com.example.framework.utils.function.view.background
import com.example.framework.utils.function.view.byHardwareAccelerate
import com.example.home.R
import com.example.home.activity.WebActivity
import java.lang.ref.WeakReference

/**
 * 网页帮助类
 */
class WebHelper(private val activity: WebActivity) : LifecycleEventObserver {
    //在此处获取跳转的值以及重新绑定对应的view
    private val bean by lazy { activity.intentSerializable(Extra.BUNDLE_BEAN) as? WebBundle }
    private val binding by lazy { ActivityWebBinding.inflate(activity.layoutInflater) }
    private val titleBuilder by lazy { TitleBuilder(activity, binding.titleContainer) }
    private val webUtil by lazy { WebUtil(activity, binding.flWebRoot) }
    private var webView: WebView? = null

    init {
        activity.lifecycle.addObserver(this)
        if (!bean?.getLight().orTrue) activity.initImmersionBar(false)
        addWebView()
        FormActivityUtil.setAct(activity)
    }

    private fun addWebView() {
        //需要标题头并且值已经传输过来了则设置标题
        bean?.let {
            if (it.getTitleRequired().orTrue) {
                titleBuilder.setTitle(it.getTitle()).getDefault()
            } else {
                titleBuilder.hideTitle()
            }
        }
        webView = webUtil.webView
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
                webUtil.onShowCustomView(view, callback)
            }

            override fun onHideCustomView() {
                webUtil.onHideCustomView()
            }

            override fun onProgressChanged(progress: Int) {
            }
        })
    }

    /**
     * 加载页面
     */
    fun load() = webView.load(getUrl(), true)

    /**
     * 刷新页面
     */
    fun refresh() = webView.refresh()

    /**
     * 获取加载的url
     */
    fun getUrl() = bean?.getUrl().orEmpty()

    /**
     * 返回点击
     */
    fun onKeyDown() {
//        webView?.copyBackForwardList()
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
     * 生命周期订阅
     */
    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            Lifecycle.Event.ON_DESTROY -> {
                webView?.removeJavascriptInterface("JSCallAndroid")
                webView?.clear()
                webView = null
                binding.unbind()
                activity.lifecycle.removeObserver(this)
            }
            else -> {}
        }
    }

}