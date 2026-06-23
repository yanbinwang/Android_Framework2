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
import android.webkit.JsPromptResult
import android.webkit.JsResult
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebStorage
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import androidx.core.net.toUri
import com.example.common.config.ServerConfig
import com.example.framework.utils.function.value.orZero
import com.example.framework.utils.function.value.toBoolean
import com.example.framework.utils.function.view.fade
import com.example.framework.utils.function.view.visible
import com.example.framework.utils.logE
import java.lang.ref.WeakReference

/**
 * 对应的拼接区分本地和测试
 */
val Int?.toServerUrl: String
    get() = string(this.orZero).toServerUrl

val String?.toServerUrl: String
    get() = "${ServerConfig.serverUrl()}${this}"

/**
 * 是否为 http 地址
 */
val String?.isHttpUrl: Boolean
    get() = !this.isNullOrBlank() && run {
        val s = trimStart().lowercase()
        s.startsWith("http://") || s.startsWith("https://") || s == "http" || s == "https"
    }

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
fun WebView?.loadWebUrl(url: String, appendHeader: Boolean = false) {
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
fun WebView?.reloadWebUrl() {
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
    // 全局单例操作，部分厂商 ROM 在 destroy 期间可能异常
    try {
        // 清空 DOM 存储
        WebStorage.getInstance().deleteAllData()
        // 清空 Cookie 并强制刷盘兜底
        val cookieManager = CookieManager.getInstance()
        cookieManager.removeAllCookies {
            cookieManager.flush()
        }
        cookieManager.flush()
    } catch (e: Exception) {
        e.logE
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
    // API26+ 额外精准清理
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        (webChromeClient as? WebChromeClientImpl)?.clearTask(this)
    }
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
fun WebView?.runWebJsScript(script: String, listener: (String?) -> Unit) {
    if (this == null) {
        listener(null)
        return
    }
    evaluateJavascript(script) {
        listener(if (it.isNullOrEmpty() || it == "null") null else it)
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
     * 若需要自定义弹窗（如用原生 Dialog 替代），可在这里实现，最后调用 result?.confirm() 通知 JS 弹窗已处理
     * @message JS alert 里的弹窗文本内容
     * @result JS 结果回调对象，核心对象，用来和网页 JS 通信、控制弹窗行为：
     * 1) result.cancel()：告知 JS 用户取消 / 关闭弹窗
     * 2) result.confirm()：告知 JS 用户点击确定
     *  true 拦截系统默认弹窗，由你原生代码全权处理本次 alert。
     *  false / return super.onJsAlert(...) 放行，使用 WebView 系统自带的 JS 弹窗。
     */
    override fun onJsAlert(view: WebView?, url: String?, message: String?, result: JsResult?): Boolean {
        result?.cancel()
        return true
//        return super.onJsAlert(view, url, message, result)
    }

    /**
     * 拦截 confirm 弹窗
     */
    override fun onJsConfirm(view: WebView?, url: String?, message: String?, result: JsResult?): Boolean {
        result?.cancel()
        return true
    }

    /**
     * 拦截 prompt 输入弹窗
     */
    override fun onJsPrompt(view: WebView?, url: String?, message: String?, defaultValue: String?, result: JsPromptResult?): Boolean {
        result?.cancel()
        return true
    }

    /**
     * 处理网页中的 “自定义视图” 请求（通常用于全屏显示，如视频播放全屏）
     * @view 网页请求全屏时，WebView 提供的「全屏承载视图」，类型一般是 VideoView/SurfaceView 或 容器布局，核心场景：H5 视频点击全屏播放
     * @callback WebView 和系统通信、管控全屏视图生命周期
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

interface OnWebChangedListener {

    fun onShowCustomView(view: View?, callback: WebChromeClient.CustomViewCallback?)

    fun onHideCustomView()

    fun onProgressChanged(progress: Int)

}

private class WebViewClientImpl(private val onPageStarted: () -> Unit, private val onPageFinished: () -> Unit) : WebViewClient() {

    /**
     * 网页要跳转新链接、点击 a 标签、JS 跳转时优先执行
     * true：拦截本次跳转，WebView 不再加载该链接（自己处理）
     * false：不拦截，交给 WebView 正常加载网页
     */
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

    /**
     * 统一处理链接跳转拦截逻辑
     * @view 当前WebView实例
     * @uri 解析后的链接Uri对象
     * @url 原始链接字符串
     */
    private fun shouldOverrideUrlLoading(view: WebView, uri: Uri, url: String): Boolean {
        // 打印当前跳转的完整链接到日志
        url.logE
        // 取出链接的协议头：http / https / tel / weixin 等
        val scheme = uri.scheme
        // 取出链接的域名/主机地址：如 www.baidu.com
        val host = uri.host
        // 多分支条件判断，根据链接特征决定是否拦截跳转
        return when {
            // 协议头为空（无 http/tel 等前缀）
            scheme.isNullOrEmpty() -> {
                false
            }
            // 有协议头，但没有域名（典型：tel:、mailto:、weixin:// 这类App协议）
            host.isNullOrEmpty() -> {
                if (scheme.isHttpUrl) {
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
            // 协议是 http / https 标准网页
            scheme.isHttpUrl -> {
                false
            }
            // 其余所有未知/自定义协议
            else -> {
                // 唤起外部App
                uri.jumpToOtherApp(view.context)
                // 拦截WebView跳转
                true
            }
        }
    }

    private fun Uri.jumpToOtherApp(context: Context) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, this)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            context.applicationContext.startActivity(intent)
        } catch (e: Exception) {
            // 防止没有安装的情况
            e.logE
//            "當前App尚未安裝，請安裝後再試".shortToast()
        }
    }

    /**
     * 网页开始加载（发起网络请求、渲染前）
     * @favicon 网页小图标（标签页图标）
     */
    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)
        onPageStarted()
    }

    /**
     * 网页全部加载完成（html、css、图片、静态资源加载完毕）
     */
    override fun onPageFinished(view: WebView, url: String) {
        super.onPageFinished(view, url)
        onPageFinished()
    }
}