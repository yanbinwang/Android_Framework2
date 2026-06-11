package com.example.common.utils.function

import android.annotation.SuppressLint
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
import androidx.annotation.RequiresApi
import androidx.core.net.toUri
import com.example.framework.utils.function.value.toBoolean
import com.example.framework.utils.function.view.fade
import com.example.framework.utils.function.view.visible
import com.example.framework.utils.logE
import java.lang.ref.WeakReference

/**
 * 标记网页是否加载完成，借助 View.tag 存储状态
 */
var WebView?.isLoadFinished: Boolean
    set(value) {
        this?.tag = value
    }
    get() {
        return this?.tag.toBoolean(false)
    }

/**
 * 加载网页
 * @param url 网页地址
 * @param appendHeader 是否追加当前网页专属请求头
 */
fun WebView?.load(url: String, appendHeader: Boolean = false) {
    if (this == null) return
    isLoadFinished = false
    if (appendHeader) {
        loadUrl(url, getWebHeader())
    } else {
        loadUrl(url)
    }
}

/**
 * 添加请求头
 */
private fun getWebHeader(): Map<String, String> {
    val map = mutableMapOf<String, String>()
//    if (UserDataUtil.isLogin()) {
//        UserDataUtil.getUserData()?.token?.let { map["token"] = it }
//    }
    map["Content-Type"] = "application/json"
    return map
}

/**
 * 刷新当前网页
 */
fun WebView?.refresh() {
    if (this == null) return
    isLoadFinished = false
    reload()
}

/**
 * 清空 WebView 缓存、历史、Cookie 及本地存储
 */
fun WebView?.clearWebData() {
    if (this == null) return
    // 历史记录 & 表单
    clearHistory()
    clearFormData()
    // 内存+磁盘缓存
    clearCache(true)
    // 清空 DOM 存储
    WebStorage.getInstance().deleteAllData()
    // 清空 Cookie 并强制刷盘兜底
    val cookieManager = CookieManager.getInstance()
    cookieManager.removeAllCookies {
        cookieManager.flush()
    }
    cookieManager.flush()
}

/**
 * 执行 JS 脚本并回调结果
 * JS 返回空字符串 / "null" 统一转为 Kotlin null
 * @param script JS 脚本内容
 * @param listener 执行结果回调
 * 1) 检测 JS 方法是否存在
 * // 检测全局方法 jsCallNative 是否存在
 * val checkScript = "typeof jsCallNative === 'function' ? 'ok' : ''"
 * webView?.runJsScript(checkScript) { result ->
 *     if (result != null) {
 *         // JS 方法存在
 *     } else {
 *         // JS 方法不存在
 *     }
 * }
 * 2) 调用无参 JS 方法
 * // 调用 jsAlert()
 * webView?.runJsScript("jsAlert()") { result ->
 *     // 接收 JS 返回值
 * }
 * 3) 调用带参数的 JS 方法
 * // 调用 jsReceiveData("来自原生的数据")
 * val data = "来自原生的数据"
 * val script = "jsReceiveData('$data')"
 * webView?.runJsScript(script) { result ->
 *     // 处理返回结果
 * }
 * 4) 读取 JS 全局变量
 * // 读取全局变量 appVersion
 * webView?.runJsScript("appVersion") { version ->
 *     version?.let {
 *         // 拿到 JS 变量值
 *     }
 * }
 * 5) 执行单纯逻辑脚本（无需返回值）
 * // 执行一段 JS 业务逻辑
 * val script = """
 *     console.log('原生调用JS');
 *     initPage();
 * """.trimIndent()
 * webView?.runJsScript(script) { }
 */
fun WebView?.runJsScript(script: String, listener: (String?) -> Unit) {
    if (this == null) {
        listener(null)
        return
    }
    evaluateJavascript(script) {
        listener(if (it.isNullOrEmpty() || it == "null") null else it)
    }
}

/**
 * 移除延迟任务
 */
@SuppressLint("WebViewApiAvailability")
fun WebView?.clearWebClientTask() {
    if (this == null) return
    // 直接移除当前WebView上所有Runnable
    removeCallbacks(null)
    // API26+ 额外精准清理（可选，不加也够用）
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        (webChromeClient as? WebChromeClientImpl)?.clearTask(this)
    }
}

/**
 * 配置 WebView 客户端与监听
 */
fun WebView?.setupWebClient(loading: ProgressBar? = null, onPageStarted: () -> Unit = {}, onPageFinished: () -> Unit = {}, webChangedListener: OnWebChangedListener? = null) {
    if (this == null) return
    webChromeClient = WebChromeClientImpl(WeakReference(loading), webChangedListener)
    webViewClient = WebViewClientImpl(onPageStarted, onPageFinished)
}

private class WebChromeClientImpl(private val loading: WeakReference<ProgressBar>, private val listener: OnWebChangedListener?) : WebChromeClient() {
    private val runnable = Runnable {
        loading.get()?.fade(200)
        loading.get()?.progress = 0
    }

    /**
     * 监听网页加载进度的变化（0-100），常用于更新进度条。
     */
    override fun onProgressChanged(view: WebView, newProgress: Int) {
        listener?.onProgressChanged(newProgress)
        if (!view.isLoadFinished) {
            loading.get().visible()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                loading.get()?.setProgress(newProgress, true)
            } else {
                loading.get()?.progress = newProgress
            }
            // 加载完网页进度条消失
            if (newProgress >= 100) {
                view.isLoadFinished = true
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
        return true
//        return super.onJsAlert(view, url, message, result)
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

    /**
     * 移除延迟任务
     */
    fun clearTask(view: WebView) {
        view.removeCallbacks(runnable)
    }
}

private class WebViewClientImpl(private val onPageStarted: () -> Unit, private val onPageFinished: () -> Unit) : WebViewClient() {

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
            host.isNullOrEmpty() -> {
                if (scheme.isHttp) {
                    false
                } else {
                    uri.jumpToOtherApp(view.context)
                    true
                }
//            host.endsWith(routerLink) || host.endsWith(routerLink) -> {
//                currentActivity()?.let { RouterUtil.jump(it, url) }
//                true
//            }
            }
            scheme.isHttp -> {
                false
            }
            else -> {
                uri.jumpToOtherApp(view.context)
                true
            }
        }
    }

    private val String?.isHttp: Boolean
        get() = equals("http", true) || equals("https", true)

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

interface OnWebChangedListener {

    fun onShowCustomView(view: View?, callback: WebChromeClient.CustomViewCallback?)

    fun onHideCustomView()

    fun onProgressChanged(progress: Int)

}