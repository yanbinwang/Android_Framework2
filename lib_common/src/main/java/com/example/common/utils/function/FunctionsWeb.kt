package com.example.common.utils.function

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.view.View
import android.webkit.ConsoleMessage
import android.webkit.JsResult
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebStorage
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import androidx.core.net.toUri
import com.example.framework.utils.function.value.toBoolean
import com.example.framework.utils.function.view.fade
import com.example.framework.utils.function.view.visible
import com.example.framework.utils.logE
import java.lang.ref.WeakReference

/**
 * 这里做是否加载完毕的处理，丢在tag中
 */
var WebView?.loadFinished: Boolean
    set(value) {
        this?.tag = value
    }
    get() {
        return this?.tag.toBoolean(false)
    }

/**
 * 加载网页
 */
fun WebView?.load(url: String, needHeader: Boolean) {
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
//    if (UserDataUtil.isLogin()) {
//        UserDataUtil.getUserData()?.token?.let { map["token"] = it }
//    }
    map["Content-Type"] = "application/json"
    return map
}

/**
 * 刷新网页
 */
fun WebView?.refresh() {
    if (this == null) return
    loadFinished = false
    reload()
}

/**
 * 网页緩存清除
 */
fun WebView?.clear() {
    if (this == null) return
    WebStorage.getInstance().deleteAllData() //清空WebView的localStorage
    clearHistory()
    context.deleteDatabase("webview.db")
    context.deleteDatabase("webviewCache.db")
}

/**
 * 是否具备对应js方法
 */
fun WebView?.evaluateJs(script: String, listener: (String?) -> Unit) {
    if (this == null) {
        listener(null)
        return
    }
    this.evaluateJavascript(script) {
        if (it.isNullOrEmpty() || it == "null") {
            listener(null)
        } else {
            listener(it)
        }
    }
}

/**
 * 设置client处理
 */
fun WebView?.setClient(loading: ProgressBar?, onPageStarted: () -> Unit, onPageFinished: () -> Unit, webChangedListener: OnWebChangedListener?) {
    if (this == null) return
    webChromeClient = XWebChromeClient(WeakReference(loading), webChangedListener)
    webViewClient = XWebViewClient(onPageStarted, onPageFinished)
}

private class XWebViewClient(val onPageStarted: () -> Unit, val onPageFinished: () -> Unit) : WebViewClient() {

    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
        return try {
            shouldOverrideUrlLoading(view, request.url, request.url.toString())
        } catch (e: Exception) {
            e.logE
            false
        }
    }

    @Deprecated("Deprecated in Java")
    override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
        return shouldOverrideUrlLoading(view, url.toUri(), url)
    }

    private fun shouldOverrideUrlLoading(view: WebView, uri: Uri, url: String): Boolean {
        url.logE
        val scheme = uri.scheme
        val host = uri.host
        return when {
            scheme.isNullOrEmpty() -> {
                false
            }
            host.isNullOrEmpty() -> if (scheme.isHttp) {
                false
            } else {
                uri.jumpToOtherApp(view.context)
                true
            }
//            host.endsWith(routerLink) || host.endsWith(routerLink) -> {
//                currentActivity()?.let { RouterUtil.jump(it, url) }
//                true
//            }
            scheme.isHttp -> {
                false
            }
            else -> {
                uri.jumpToOtherApp(view.context)
                true
            }
        }
    }

    private val String?.isHttp: Boolean get() = this == "http" || this == "https"

    private fun Uri.jumpToOtherApp(context: Context) {
        try {
            // 以下固定写法
            val intent = Intent(Intent.ACTION_VIEW, this)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            context.applicationContext.startActivity(intent)
        } catch (e: Exception) {
            // 防止没有安装的情况
            e.logE
//            "當前App尚未安裝，請安裝後再試".shortToast()
        }
    }

    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)
        onPageStarted()
    }

    override fun onPageFinished(view: WebView, url: String) {
        super.onPageFinished(view, url)
        onPageFinished()
    }

}

private class XWebChromeClient(private val loading: WeakReference<ProgressBar>, val listener: OnWebChangedListener?) : WebChromeClient() {
    private val runnable = Runnable {
        loading.get()?.fade(200)
        loading.get()?.progress = 0
    }

    override fun onProgressChanged(view: WebView, newProgress: Int) {
        listener?.onProgressChanged(newProgress)
        if (!view.loadFinished) {
            loading.get().visible()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                loading.get()?.setProgress(newProgress, true)
            } else {
                loading.get()?.progress = newProgress
            }
            //加载完网页进度条消失
            if (newProgress >= 100) {
                view.loadFinished = true
                view.postDelayed(runnable, 500)
            }
        }
        super.onProgressChanged(view, newProgress)
    }

    override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
        return super.onConsoleMessage(consoleMessage)
    }

    override fun onJsAlert(view: WebView?, url: String?, message: String?, result: JsResult?): Boolean {
        result?.cancel()
        return super.onJsAlert(view, url, message, result)
    }

    override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
        listener?.onShowCustomView(view, callback)
    }

    override fun onHideCustomView() {
        listener?.onHideCustomView()
    }
}

interface OnWebChangedListener {

    fun onShowCustomView(view: View?, callback: WebChromeClient.CustomViewCallback?)

    fun onHideCustomView()

    fun onProgressChanged(progress: Int)

}