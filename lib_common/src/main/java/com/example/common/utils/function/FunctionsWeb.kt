package com.example.common.utils.function

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.view.View
import android.webkit.ConsoleMessage
import android.webkit.CookieManager
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
    // 清除 WebView 的 浏览历史记录（不涉及缓存文件）
    clearHistory()
    // WebView 会将网络请求的资源（如图片、JS、CSS 等）缓存到磁盘,参数 true 表示同时清除磁盘缓存，false 仅清除内存缓存
    clearCache(true)
    // 清除 WebView 的 LocalStorage 和 SessionStorage（属于 DOM 存储数据，非传统缓存）
    WebStorage.getInstance().deleteAllData()
    // 删除 WebView 存储表单数据、会话信息、应用缓存数据库等的 数据库文件（部分缓存相关数据）
    context.deleteDatabase("webview.db")
    context.deleteDatabase("webviewCache.db")
    context.deleteDatabase("webviewAppCache.db")
    // 清除 Cookie
    val cookieManager = CookieManager.getInstance()
    cookieManager.removeAllCookies { success ->
        // 清除完成的回调（可选）
        if (success) {
            cookieManager.flush() // 强制同步到磁盘
        }
    }
    // 清除表单自动填充数据
    clearFormData()
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

    /**
     * 监听网页加载进度的变化（0-100），常用于更新进度条。
     */
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

    /**
     * 监听网页中的 JavaScript 控制台输出（如 console.log()）
     * 返回 true 表示 “已处理，无需父类默认行为”
     */
    override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
        return super.onConsoleMessage(consoleMessage)
    }

    /**
     * 拦截网页中的 JavaScript 弹窗（alert()）
     * 调用 result?.cancel() 取消弹窗（阻止系统默认弹窗显示）。
     * 若需要自定义弹窗（如用原生 Dialog 替代），可在这里实现，最后调用 result?.confirm() 通知 JS 弹窗已处理。
     */
    override fun onJsAlert(view: WebView?, url: String?, message: String?, result: JsResult?): Boolean {
        result?.cancel()
        return super.onJsAlert(view, url, message, result)
    }

    /**
     * 处理网页中的 “自定义视图” 请求（通常用于全屏显示，如视频播放全屏）。
     */
    override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
        listener?.onShowCustomView(view, callback)
    }

    /**
     * 与 onShowCustomView 对应，通知退出自定义视图（如视频退出全屏）
     */
    override fun onHideCustomView() {
        listener?.onHideCustomView()
    }
}

interface OnWebChangedListener {

    fun onShowCustomView(view: View?, callback: WebChromeClient.CustomViewCallback?)

    fun onHideCustomView()

    fun onProgressChanged(progress: Int)

}