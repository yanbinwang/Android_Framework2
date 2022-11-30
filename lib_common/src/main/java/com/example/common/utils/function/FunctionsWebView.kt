package com.example.common.utils.function

import android.graphics.Bitmap
import android.widget.ProgressBar
import com.example.base.utils.function.value.toBoolean
import com.example.base.utils.function.view.gone
import com.example.base.utils.function.view.visible
import com.example.common.widget.XWebView
import com.example.common.widget.xrecyclerview.refresh.finishRefreshing
import com.scwang.smart.refresh.layout.SmartRefreshLayout
import com.tencent.smtt.export.external.interfaces.JsResult
import com.tencent.smtt.export.external.interfaces.WebResourceRequest
import com.tencent.smtt.sdk.WebChromeClient
import com.tencent.smtt.sdk.WebView
import com.tencent.smtt.sdk.WebViewClient
import java.lang.ref.WeakReference

var XWebView?.loadFinished: Boolean
    set(value) {
        this?.tag = value
    }
    get() {
        return this?.tag.toBoolean(false)
    }

fun XWebView?.load(url: String, needHeader: Boolean) {
    if (this == null) return
    loadFinished = false
    if (needHeader) {
        loadUrl(url, getHeader())
    } else {
        loadUrl(url)
    }
}

/**
 * 添加请求头
 */
private fun getHeader(): Map<String, String> {
    val map = HashMap<String, String>()
//        if (UserDataUtil.isLogin()) {
//            UserDataUtil.getUserData()?.token?.let { map["token"] = it }
//        }
    map["Content-Type"] = "application/json"
    return map
}

fun XWebView?.refresh() {
    if (this == null) return
    loadFinished = false
    reload()
}

fun XWebView?.setClient(progress: ProgressBar?, refresh: SmartRefreshLayout, onReceivedTitle: ((title: String?) -> Unit)? = null) {
    if (this == null) return
    webViewClient = XWebViewClient()
    webChromeClient = XWebChromeClient(WeakReference(progress), WeakReference(refresh), onReceivedTitle)
}


class XWebViewClient : WebViewClient() {

    override fun onLoadResource(view: WebView?, url: String?) {
        super.onLoadResource(view, url)
    }

    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        return super.shouldOverrideUrlLoading(view, request)
    }

    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)
    }

}

class XWebChromeClient(private var progress: WeakReference<ProgressBar?>?, private var refresh: WeakReference<SmartRefreshLayout?>?, private var onReceivedTitle: ((title: String?) -> Unit)? = null) : WebChromeClient() {

    override fun onJsAlert(view: WebView?, url: String?, message: String?, result: JsResult?): Boolean {
        return super.onJsAlert(view, url, message, result)
    }

    override fun onReceivedTitle(view: WebView?, title: String?) {
        super.onReceivedTitle(view, title)
        onReceivedTitle?.invoke(title)
    }

    override fun onProgressChanged(view: WebView?, newProgress: Int) {
        val web = view as? XWebView
        if (!web.loadFinished) {
            if (newProgress >= 100) {
                web.loadFinished = true
                refresh?.get().finishRefreshing()
                progress?.get().gone()//加载完网页进度条消失
            } else {
                progress?.get().visible()//开始加载网页时显示进度条
                progress?.get()?.progress = newProgress //设置进度值
            }
        }
        super.onProgressChanged(view, newProgress)
    }

}