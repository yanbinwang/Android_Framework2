package com.example.common.base.proxy

import android.widget.ProgressBar
import com.example.base.utils.function.view.gone
import com.example.base.utils.function.view.visible
import com.example.common.widget.xrecyclerview.refresh.finishRefreshing
import com.scwang.smart.refresh.layout.SmartRefreshLayout
import com.tencent.smtt.export.external.interfaces.JsResult
import com.tencent.smtt.sdk.WebChromeClient
import com.tencent.smtt.sdk.WebView

/**
 * 通用加载事件
 */
class BaseWebChromeClient(private var pbWeb: ProgressBar, private var refresh: SmartRefreshLayout) : WebChromeClient() {

    override fun onJsAlert(view: WebView?, url: String?, message: String?, result: JsResult?): Boolean {
        return super.onJsAlert(view, url, message, result)
    }

    override fun onReceivedTitle(view: WebView?, title: String?) {
        super.onReceivedTitle(view, title)
    }

    override fun onProgressChanged(view: WebView?, newProgress: Int) {
        super.onProgressChanged(view, newProgress)
        if (newProgress == 100) {
            refresh.finishRefreshing()
            pbWeb.gone()//加载完网页进度条消失
        } else {
            pbWeb.visible()//开始加载网页时显示进度条
            pbWeb.progress = newProgress //设置进度值
        }
    }

}