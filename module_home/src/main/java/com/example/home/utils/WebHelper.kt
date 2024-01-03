package com.example.home.utils

import android.view.View
import android.webkit.WebChromeClient
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.example.common.bean.WebBundle
import com.example.common.utils.FormActivityUtil
import com.example.common.utils.WebUtil
import com.example.common.utils.function.OnWebChangedListener
import com.example.common.utils.function.clear
import com.example.common.utils.function.load
import com.example.common.utils.function.refresh
import com.example.common.utils.function.setClient
import com.example.framework.utils.function.value.orFalse
import com.example.framework.utils.function.view.background
import com.example.framework.utils.function.view.byHardwareAccelerate
import com.example.home.R
import com.example.home.activity.WebActivity
import com.example.home.databinding.ActivityWebBinding
import java.lang.ref.WeakReference

/**
 * 网页帮助类
 */
class WebHelper(private val mActivity: WebActivity, private val mBinding: ActivityWebBinding?) : LifecycleEventObserver {
    private val webUtil by lazy { WebUtil(mActivity, mBinding?.flWebRoot) }
    private val webView get() = webUtil.webView
    private var bean: WebBundle? = null
    private var onPageStarted: (() -> Unit)? = null
    private var onPageFinished: ((title: String?) -> Unit)? = null

    init {
        mActivity.lifecycle.addObserver(this)
        addWebView()
        FormActivityUtil.setAct(mActivity)
    }

    private fun addWebView() {
        webView?.byHardwareAccelerate()
        webView?.background(R.color.bgWhite)
        webView?.settings?.useWideViewPort = true
        webView?.settings?.loadWithOverviewMode = true
        //WebView与JS交互
        webView?.addJavascriptInterface(WebJavaScriptObject(WeakReference(mActivity)), "JSCallAndroid")
        webView?.setClient(mBinding?.pbWeb, {
            //开始加载页面的操作...
            onPageStarted?.invoke()
        }, {
            //加载完成后的操作...(不传标题则使用web加载的标题)
            onPageFinished?.invoke(webView?.title?.trim())
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
    fun load() = webView.load(bean?.getUrl().orEmpty(), true)

    /**
     * 刷新页面
     */
    fun refresh() = webView.refresh()

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
            mActivity.finish()
        }
//            }
//        }
    }

    /**
     * 设置加载参数
     */
    fun setBundle(bean: WebBundle?) {
        this.bean = bean
    }

    /**
     * 设置页面加载完毕后的监听
     */
    fun setClientListener(onPageStarted: (() -> Unit), onPageFinished: ((title: String?) -> Unit)) {
        this.onPageStarted = onPageStarted
        this.onPageFinished = onPageFinished
    }

    /**
     * 生命周期订阅
     */
    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            Lifecycle.Event.ON_DESTROY -> {
                webView?.removeJavascriptInterface("JSCallAndroid")
                webView?.clear()
//                webView = null
                mBinding?.unbind()
                mActivity.lifecycle.removeObserver(this)
            }
            else -> {}
        }
    }

}