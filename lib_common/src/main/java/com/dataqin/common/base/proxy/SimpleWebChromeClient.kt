package com.dataqin.common.base.proxy

import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.ProgressBar
import com.tencent.smtt.export.external.interfaces.JsResult
import com.tencent.smtt.sdk.WebChromeClient
import com.tencent.smtt.sdk.WebView

/**
 * 通用加载事件
 */
class SimpleWebChromeClient(private var pbWeb: ProgressBar) : WebChromeClient() {

    override fun onJsAlert(p0: WebView?, p1: String?, p2: String?, p3: JsResult?): Boolean {
        return super.onJsAlert(p0, p1, p2, p3)
    }

    override fun onReceivedTitle(p0: WebView?, p1: String?) {
        super.onReceivedTitle(p0, p1)
    }

    override fun onProgressChanged(p0: WebView?, p1: Int) {
        super.onProgressChanged(p0, p1)
        if (p1 == 100) {
            pbWeb.visibility = GONE//加载完网页进度条消失
        } else {
            pbWeb.visibility = VISIBLE//开始加载网页时显示进度条
            pbWeb.progress = p1 //设置进度值
        }
    }

}