package com.example.common.base.binding

import android.annotation.SuppressLint
import androidx.databinding.BindingAdapter
import com.chad.library.adapter.base.BaseQuickAdapter
import com.example.common.widget.XWebView
import com.example.common.widget.xrecyclerview.XRecyclerView

/**
 * Created by WangYanBin on 2020/6/10.
 * 复用性高的代码，统一放在common中
 * 比如列表页都需要设置适配器属性，富文本加载网页
 */
object CommonBindingAdapter {

    @JvmStatic
    @BindingAdapter(value = ["adapter"])
    fun <T : BaseQuickAdapter<*, *>> setAdapter(rec: XRecyclerView, adapter: T) {
        rec.setAdapter(adapter)
    }

    @JvmStatic
    @SuppressLint("SetJavaScriptEnabled", "JavascriptInterface", "AddJavascriptInterface")
    @BindingAdapter(value = ["loadUrl"])
    fun setLoadUrl(webView: XWebView, loadPageUrl: String) {
        webView.loadUrl(loadPageUrl)
    }

    @JvmStatic
    @SuppressLint("SetJavaScriptEnabled", "JavascriptInterface", "AddJavascriptInterface")
    @BindingAdapter(value = ["loadAssetUrl"])
    fun setLoadAssetsUrl(webView: XWebView, assetPath: String) {
        webView.loadUrl("file:///android_asset/$assetPath")
    }

}