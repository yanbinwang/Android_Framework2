package com.example.common.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.example.common.BaseApplication
import com.example.common.utils.function.clear
import com.example.framework.utils.function.view.gone
import com.example.framework.utils.function.view.invisible
import com.example.framework.utils.function.view.visible
import com.example.framework.utils.logE

/**
 * @description webview工具类
 * 帮助处理WebView的内存泄漏问题的类，传入一个将用来装填WebView的ViewGroup
 * @author yan
 */
@SuppressLint("SetJavaScriptEnabled", "SourceLockedOrientationActivity")
class WebUtil : DefaultLifecycleObserver {
    private var lifecycleOwner: LifecycleOwner?
    private var container: ViewGroup? = null
    private var webView: WebView? = null
    private var webSettings: WebSettings? = null
    private var mActivity: Activity? = null
    private var mXCustomView: View? = null
    private var mXCustomViewCallback: WebChromeClient.CustomViewCallback? = null

    constructor(activity: AppCompatActivity, container: ViewGroup?) {
        this.mActivity = activity
        this.lifecycleOwner = activity
        init(container)
    }

    constructor(fragment: Fragment, container: ViewGroup?) {
        this.mActivity = fragment.activity
        this.lifecycleOwner = fragment
        init(container)
    }

    private fun init(container: ViewGroup?) {
        this.lifecycleOwner?.lifecycle?.addObserver(this)
        this.container = container
        try {
            webView = WebView(BaseApplication.instance.applicationContext)
        } catch (e: RuntimeException) {
            //这里捕捉一个webview不存在的bug
            if (lifecycleOwner is Fragment) {
                (lifecycleOwner as? Fragment)?.activity?.finish()
            } else if (lifecycleOwner is Activity) {
                (lifecycleOwner as? Activity)?.finish()
            }
            return
        }
        container?.addView(webView)
        webView?.apply {
            overScrollMode = View.OVER_SCROLL_NEVER
            isFocusable = false
            setBackgroundColor(Color.TRANSPARENT)
            layoutParams.height = FrameLayout.LayoutParams.MATCH_PARENT
            layoutParams.width = FrameLayout.LayoutParams.MATCH_PARENT
            isHorizontalScrollBarEnabled = false
            isVerticalScrollBarEnabled = false
            isFocusable = true
            isFocusableInTouchMode = true
            webSettings = settings
            webSettings?.apply {
                // 启用 DOM 存储（LocalStorage 和 SessionStorage），让网页可以在本地存储数据，很多现代网页依赖此功能正常运行。
                domStorageEnabled = true
                // 启用 JavaScript 执行，绝大多数交互性强的网页都需要 JavaScript 支持，禁用后会导致很多功能失效
                javaScriptEnabled = true
                // 禁用 WebView 自动保存密码的功能，增强安全性，避免敏感信息被存储
                savePassword = false
                // 允许 WebView 访问本地文件系统（通过file://协议），如果网页需要加载本地文件（如图片、JS 等）则需要开启
                allowFileAccess = true
                // 允许通过file://协议的网页访问其他本地文件资源，进一步放宽本地文件访问限制（有安全风险，谨慎使用）
                allowFileAccessFromFileURLs = true  // *风险点
                // 允许通过file://协议的网页访问任何来源的资源（包括网络资源），安全性较低，通常用于特殊需求场景
                allowUniversalAccessFromFileURLs = true // *风险点
                // 禁用 WebView 内置的缩放控制按钮，若需要缩放功能可设为true
                builtInZoomControls = false
                // 强制隐藏内置缩放控件，即使 builtInZoomControls 开启也不会显示
                displayZoomControls = false
                // 允许 WebView 加载网络图片，设为true则会阻止所有网络图片加载
                blockNetworkImage = false
                // 允许 WebView 支持 “viewport” 标签（HTML 中控制页面缩放的元标签），让网页可以根据屏幕宽度自适应调整布局
                useWideViewPort = true
                // 结合useWideViewPort使用，使网页以概览模式加载（即缩小页面以适应屏幕宽度），避免横向滚动，提升移动端浏览体验
                loadWithOverviewMode = true
                // 禁用安全浏览功能，安全浏览会自动检测并阻止恶意网站，禁用后可能增加安全风险
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    safeBrowsingEnabled = false // *风险点
                }
                // 设置网页文本的缩放比例，100 表示默认大小，可根据需求调整（如 120 表示放大 20%）
                textZoom = 100
                // 设置缓存模式为默认：有缓存时使用缓存，无缓存时从网络加载，同时会根据缓存有效期更新缓存
                cacheMode = WebSettings.LOAD_DEFAULT
                // 允许网页同时加载安全（HTTPS）和非安全（HTTP）资源，默认情况下会阻止非安全资源加载，此设置会降低安全性
                mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW // *风险点
            }
        }
    }

    fun onShowCustomView(view: View?, callback: WebChromeClient.CustomViewCallback?) {
        mActivity?.apply {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            webView.invisible()
            //如果一个视图已经存在，那么立刻终止并新建一个
            if (mXCustomView != null) {
                callback?.onCustomViewHidden()
                return
            }
            mXCustomView = view
            mXCustomViewCallback = callback
            val decor = window.decorView as? FrameLayout
            decor?.addView(view)
            mXCustomView.visible()
        }
    }

    fun onHideCustomView() {
        mActivity?.apply {
            if (requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) return
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            mXCustomView.gone()
            val decor = window.decorView as? FrameLayout
            try {
                decor?.removeView(mXCustomView)
            } catch (e: Exception) {
                e.logE
            }
            mXCustomViewCallback?.onCustomViewHidden()
            mXCustomView = null
            mXCustomViewCallback = null
            webView.visible()
        }
    }

    fun getWebView(): WebView? {
        return webView
    }

    fun getWebSettings(): WebSettings? {
        return webSettings
    }

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)
        webView?.onResume()
        webView?.findFocus()
        webView?.resumeTimers()
    }

    override fun onPause(owner: LifecycleOwner) {
        super.onPause(owner)
        webView?.onPause()
    }

    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        // 先移除父容器并销毁自身，再执行清理操作
        lifecycleOwner?.lifecycle?.removeObserver(this)
        lifecycleOwner = null
        val decor = mActivity?.window?.decorView as? FrameLayout
        if (mXCustomView != null) try {
            decor?.removeView(mXCustomView)
        } catch (e: Exception) {
            e.logE
        }
        mXCustomView = null
        mXCustomViewCallback = null
        container?.removeAllViews()
        container = null
        webView?.apply {
            clear()
            loadUrl("about:blank")
            onPause()
            destroyDrawingCache()
            removeAllViews()
            // destroy() 会终止 WebView 所有操作，若 clear() 在 destroy() 之后，可能清理不彻底
            destroy()
            webView = null
        }
        mActivity = null
    }

}