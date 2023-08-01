package com.example.home.utils

import android.annotation.SuppressLint
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
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
import java.lang.ref.WeakReference

/**
 * 网页帮助类
 */
@SuppressLint("JavascriptInterface")
class WebHelper(private val activity: AppCompatActivity, container: ViewGroup? = null, private val url: String? = null) : LifecycleEventObserver {
    private val webUtil by lazy { WebUtil(activity, container) }
    private val webView: WebView?
        get() = webUtil.webView

    init {
        activity.lifecycle.addObserver(this)
        addWebView()
        FormActivityUtil.setAct(activity)
    }

    private fun addWebView() {
        webView?.byHardwareAccelerate()
        webView?.background(R.color.bgWhite)
        webView?.settings?.useWideViewPort = true
        webView?.settings?.loadWithOverviewMode = true
    }

    /**
     * 加载页面
     */
    fun load() = webView.load(url.orEmpty(), true)

    /**
     * 刷新页面
     */
    fun refresh() = webView.refresh()

    /**
     * js注入
     */
    fun addJavascriptInterface(obj: Any, name: String) {
        webView?.addJavascriptInterface(obj, name)
    }

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
     * 加载回调
     */
    fun setClient(loading: ProgressBar?, onPageStarted: () -> Unit = {}, onPageFinished: () -> Unit = {}) {
        webView?.setClient(loading, {
            //开始加载页面的操作...
            onPageStarted.invoke()
        }, {
            //加载完成后的操作...(不传标题则使用web加载的标题)
            onPageFinished.invoke()
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
     * 获取webview的title
     */
    fun getTitle() = webView?.title

    /**
     * 获取webview的url
     */
    fun getUrl() = webView?.url

    /**
     * 生命周期订阅
     */
    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            Lifecycle.Event.ON_DESTROY -> {
                webView?.removeJavascriptInterface("JSCallAndroid")
                webView?.clear()
                activity.lifecycle.removeObserver(this)
            }
            else -> {}
        }
    }

}