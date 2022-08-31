package com.example.common.widget

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import com.tencent.smtt.sdk.WebView

/**
 * @description
 * @author
 */
class XWebView(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : WebView(context, attrs, defStyleAttr) {

    init {
        initialize()
    }

    @SuppressLint("SetJavaScriptEnabled", "JavascriptInterface", "AddJavascriptInterface")
    private fun initialize() {
        this.settings.apply {
            //设置WebView属性，能够执行Javascript脚本
            javaScriptEnabled = true
            //自动打开窗口
            javaScriptCanOpenWindowsAutomatically = true
            //设置WebView 可以加载更多格式页面
            loadWithOverviewMode = true
            //设置webview页面自适应网页宽度
            useWideViewPort = true
            //启用或禁止WebView访问文件数据
            allowFileAccess = true
            //支持手势缩放
            builtInZoomControls = false
            domStorageEnabled = true
            //告诉webview不启用应用程序缓存api---退出activity时全部清空
            setAppCacheEnabled(false)
            defaultTextEncodingName = "utf-8"
            //兼容h5样式->亦可由前端实现
            textZoom = 100
            defaultFontSize = 16
        }
    }

}