package com.example.common.base.binding

import android.annotation.SuppressLint
import android.content.Intent
import android.view.View
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.databinding.BindingAdapter
import com.example.common.BaseApplication
import com.example.common.widget.XWebView

/**
 * Create by wyb at 2020/3/13
 */
object WebViewBindingAdapter {

    @JvmStatic
    @SuppressLint("SetJavaScriptEnabled")
    @BindingAdapter(value = ["app:pageAssetPath"], requireAll = false)
    fun setLoadAssetsPage(webView: WebView, assetPath: String) {
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView,
                request: WebResourceRequest
            ): Boolean {
                val uri = request.url
                val intent = Intent(Intent.ACTION_VIEW, uri)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                BaseApplication.instance.startActivity(intent)
                return true
            }
        }
        webView.scrollBarStyle = View.SCROLLBARS_INSIDE_OVERLAY
        val webSettings = webView.settings
        webSettings.javaScriptEnabled = true
        webSettings.defaultTextEncodingName = "UTF-8"
        webSettings.setSupportZoom(true)
        webSettings.builtInZoomControls = true
        webSettings.displayZoomControls = false
        webSettings.useWideViewPort = true
        webSettings.loadWithOverviewMode = true
        val url = "file:///android_asset/$assetPath"
        webView.loadUrl(url)
    }

    @JvmStatic
    @SuppressLint("SetJavaScriptEnabled")
    @BindingAdapter(value = ["app:loadPage"], requireAll = false)
    fun setLoadPage(webView: WebView, loadPage: String?) {
        webView.webViewClient = WebViewClient()
        webView.scrollBarStyle = View.SCROLLBARS_INSIDE_OVERLAY
        val webSettings = webView.settings
        webSettings.javaScriptEnabled = true
        webSettings.defaultTextEncodingName = "UTF-8"
        webSettings.setSupportZoom(true)
        webSettings.builtInZoomControls = true
        webSettings.displayZoomControls = false
        webSettings.useWideViewPort = true
        webSettings.loadWithOverviewMode = true
        webView.loadUrl(loadPage)
    }

    @JvmStatic
    @SuppressLint("SetJavaScriptEnabled", "JavascriptInterface", "AddJavascriptInterface")
    @BindingAdapter(value = ["app:loadPageUrl"], requireAll = false)
    fun setLoadPage(webView: XWebView, loadPageUrl: String?) {
        webView.loadUrl(loadPageUrl)
    }

}