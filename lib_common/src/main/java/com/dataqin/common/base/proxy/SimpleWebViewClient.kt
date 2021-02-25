package com.dataqin.common.base.proxy

import android.graphics.Bitmap
import com.tencent.smtt.export.external.interfaces.WebResourceRequest
import com.tencent.smtt.sdk.WebView
import com.tencent.smtt.sdk.WebViewClient

/**
 * 通用页面事件
 */
class SimpleWebViewClient : WebViewClient() {

    override fun onLoadResource(p0: WebView?, p1: String?) {
        super.onLoadResource(p0, p1)
    }

    override fun shouldOverrideUrlLoading(p0: WebView?, p1: WebResourceRequest?): Boolean {
        return super.shouldOverrideUrlLoading(p0, p1)
    }

    override fun onPageStarted(p0: WebView?, p1: String?, p2: Bitmap?) {
        super.onPageStarted(p0, p1, p2)
    }

    override fun onPageFinished(p0: WebView?, p1: String?) {
        super.onPageFinished(p0, p1)
    }

}