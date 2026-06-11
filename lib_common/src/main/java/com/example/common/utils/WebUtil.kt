package com.example.common.utils

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.example.common.utils.function.clearWebClientTask
import com.example.common.utils.function.clearWebData
import com.example.framework.utils.function.view.gone
import com.example.framework.utils.function.view.invisible
import com.example.framework.utils.function.view.visible
import com.example.framework.utils.logE

/**
 * WebView 工具类
 * 帮助处理 WebView 的内存泄漏问题的类，传入一个将用来装填 WebView 的 ViewGroup
 */
@SuppressLint("SetJavaScriptEnabled", "SourceLockedOrientationActivity")
class WebUtil(host: Any, private val container: ViewGroup?) : DefaultLifecycleObserver {
    private val mActivity = when (host) {
        // Activity（兼容所有现代 Activity）
        is FragmentActivity -> host
        // AndroidX Fragment
        is Fragment -> host.requireActivity()
        // 旧系统Fragment
        is android.app.Fragment -> throw RuntimeException("android.app.Fragment is deprecated and not supported!")
        // 不认识的类型
        else -> throw IllegalArgumentException("Unsupported host type: ${host::class.java.name}")
    }
    private var mWebView: WebView? = null
    private var mWebSettings: WebSettings? = null
    private var mCustomView: View? = null
    private var mCustomViewCallback: WebChromeClient.CustomViewCallback? = null

    init {
        mActivity.lifecycle.addObserver(this)
        initWebView()
    }

    private fun initWebView() {
        if (mActivity.isFinishing || mActivity.isDestroyed) return
        try {
            // 使用 Activity 上下文创建，规避全局上下文渲染/泄漏问题
            mWebView = WebView(mActivity)
        } catch (_: RuntimeException) {
            // 这里捕捉一个webview不存在的bug
            mActivity.finish()
            return
        }
        val webView = mWebView ?: return
        container?.addView(webView)
        webView.apply {
            setBackgroundColor(Color.TRANSPARENT)
            layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            overScrollMode = View.OVER_SCROLL_NEVER
            isHorizontalScrollBarEnabled = false
            isVerticalScrollBarEnabled = false
            isFocusable = true
            isFocusableInTouchMode = true
            mWebSettings = settings
            mWebSettings?.apply {
                // 启用 DOM 存储（LocalStorage 和 SessionStorage），让网页可以在本地存储数据，很多现代网页依赖此功能正常运行。
                domStorageEnabled = true
                // 启用 JavaScript 执行，绝大多数交互性强的网页都需要 JavaScript 支持，禁用后会导致很多功能失效
                javaScriptEnabled = true
                // 禁用 WebView 自动保存密码的功能，增强安全性，避免敏感信息被存储 API 26+ 废弃，低版本保留，高版本直接移除
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                    savePassword = false
                }
                // 允许 WebView 访问本地文件系统（通过file://协议），如果网页需要加载本地文件（如图片、JS 等）则需要开启
                allowFileAccess = true
                //------------------------------------保留file跨域配置（适配本地HTML/PDF）------------------------------------
                // (allowFileAccessFromFileURLs，allowUniversalAccessFromFileURLs) 注意：API 30+ 系统已强制屏蔽这两个属性，仅 API23~29 生效
                // 允许通过file://协议的网页访问其他本地文件资源，进一步放宽本地文件访问限制（有安全风险，谨慎使用）
                allowFileAccessFromFileURLs = true
                // 允许通过file://协议的网页访问任何来源的资源（包括网络资源），安全性较低，通常用于特殊需求场景
                allowUniversalAccessFromFileURLs = true
                // 禁用安全浏览功能，安全浏览会自动检测并阻止恶意网站，禁用后可能增加安全风险
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    safeBrowsingEnabled = false
                }
                // 允许网页同时加载安全（HTTPS）和非安全（HTTP）资源，默认情况下会阻止非安全资源加载，此设置会降低安全性
                mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                //-------------------------------------------------------------------------------------------------------
                // 禁止网页自动播放音视频
                mediaPlaybackRequiresUserGesture = true
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
                // 设置网页文本的缩放比例，100 表示默认大小，可根据需求调整（如 120 表示放大 20%）
                textZoom = 100
                // 设置缓存模式为默认：有缓存时使用缓存，无缓存时从网络加载，同时会根据缓存有效期更新缓存
                cacheMode = WebSettings.LOAD_DEFAULT
            }
        }
    }

    fun onShowCustomView(view: View?, callback: WebChromeClient.CustomViewCallback?) {
        mActivity.apply {
            // 屏幕强制横屏
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            // 把原 WebView 隐藏
            mWebView.invisible()
            // 已经存在全屏视图 → 先关闭旧全屏，直接返回
            if (mCustomView != null) {
                callback?.onCustomViewHidden()
                return
            }
            // 保存系统传过来的视频View 和 回调对象
            mCustomView = view
            mCustomViewCallback = callback
            // 把视频View 添加到 Activity 顶层 DecorView（全局最上层）
            val decor = window.decorView as? FrameLayout
            decor?.addView(view)
            mCustomView.visible()
        }
    }

    fun onHideCustomView() {
        mActivity.apply {
            // 如果已经是竖屏，直接返回（防重复执行）
            if (requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) return
            // 切回竖屏
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            mCustomView.gone()
            // 从顶层 DecorView 移除全屏视频View
            val decor = window.decorView as? FrameLayout
            try {
                decor?.removeView(mCustomView)
            } catch (e: Exception) {
                e.logE
            }
            // 通知 WebView「全屏已关闭」
            mCustomViewCallback?.onCustomViewHidden()
            // 清空全局缓存、恢复原WebView显示
            mCustomView = null
            mCustomViewCallback = null
            mWebView.visible()
        }
    }

    fun getWebView(): WebView? {
        return mWebView
    }

    fun getWebSettings(): WebSettings? {
        return mWebSettings
    }

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)
        mWebView?.onResume()
        mWebView?.findFocus()
        mWebView?.resumeTimers()
    }

    override fun onPause(owner: LifecycleOwner) {
        super.onPause(owner)
        mWebView?.onPause()
    }

    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        // 先移除父容器并销毁自身，再执行清理操作
        mActivity.lifecycle.removeObserver(this)
        // 清理全屏自定义视图
        mCustomView?.let {
            val decor = mActivity.window.decorView as? FrameLayout
            try {
                decor?.removeView(it)
            } catch (e: Exception) {
                e.logE
            }
        }
        mCustomView = null
        mCustomViewCallback = null
        // 销毁 webview
        mWebView?.let { web ->
            // 移除进度条延迟任务
            web.clearWebClientTask()
            // 停止加载，清空页面
            web.stopLoading()
            web.loadUrl("about:blank")
            web.clearWebData()
            // 生命周期暂停
            web.onPause()
            // 清理视图缓存
            web.destroyDrawingCache()
            web.removeAllViews()
            // 先从父容器移除，再 destroy，大幅降低内存泄漏
            (web.parent as? ViewGroup)?.removeView(web)
            // destroy() 会终止 WebView 所有操作，若 clear() 在 destroy() 之后，可能清理不彻底
            web.destroy()
        }
        container?.removeAllViews()
        mWebView = null
        mWebSettings = null
    }

}