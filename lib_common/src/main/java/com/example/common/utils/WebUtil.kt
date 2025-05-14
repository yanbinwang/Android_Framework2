package com.example.common.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.ActivityInfo
import android.graphics.Color
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
    private var container: ViewGroup?
    private var lifecycleOwner: LifecycleOwner?
    private var mActivity: Activity? = null
    private var mXCustomView: View? = null
    private var mXCustomViewCallback: WebChromeClient.CustomViewCallback? = null
    var webView: WebView? = null
    var webSettings: WebSettings? = null

    constructor(activity: AppCompatActivity, container: ViewGroup?) {
        this.mActivity = activity
        this.lifecycleOwner = activity
        this.lifecycleOwner?.lifecycle?.addObserver(this)
        this.container = container
        init()
    }

    constructor(fragment: Fragment, container: ViewGroup?) {
        this.mActivity = fragment.activity
        this.lifecycleOwner = fragment
        this.lifecycleOwner?.lifecycle?.addObserver(this)
        this.container = container
        init()
    }

    fun init() {
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
                domStorageEnabled = true
                javaScriptEnabled = true
                savePassword = false
                allowFileAccess = true
                builtInZoomControls = false
                blockNetworkImage = false
                textZoom = 100
                cacheMode = WebSettings.LOAD_DEFAULT
                domStorageEnabled = true
                mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
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
            clearHistory()
            loadUrl("about:blank")
            onPause()
            destroyDrawingCache()
            removeAllViews()
            destroy()
            webView = null
        }
        mActivity = null
    }

}