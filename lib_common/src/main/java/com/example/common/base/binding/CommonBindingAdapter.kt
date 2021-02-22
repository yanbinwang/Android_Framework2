package com.example.common.base.binding

import android.annotation.SuppressLint
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.GridLayoutManager
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
    @BindingAdapter(value = ["app:adapter"])
    fun setAdapter(xRec: XRecyclerView, adapter: BaseQuickAdapter<*, *>?) {
        if (adapter != null) {
            xRec.recyclerView.layoutManager = GridLayoutManager(xRec.context, 1)
            xRec.recyclerView.adapter = adapter
            xRec.addItemDecoration(1, 0, true, false)//默认是1的行数，留有空行
        }
    }

    @JvmStatic
    @BindingAdapter(value = ["app:adapter2"])
    fun setAdapter2(xRec: XRecyclerView, adapter: BaseQuickAdapter<*, *>?) {
        if (adapter != null) {
            xRec.recyclerView.layoutManager = GridLayoutManager(xRec.context, 1)
            xRec.recyclerView.adapter = adapter
            xRec.addItemDecoration(0, 0, false, false)
        }
    }

    @JvmStatic
    @SuppressLint("SetJavaScriptEnabled", "JavascriptInterface", "AddJavascriptInterface")
    @BindingAdapter(value = ["app:loadPageUrl"])
    fun setLoadPage(webView: XWebView, loadPageUrl: String?) {
        webView.loadUrl(loadPageUrl)
    }

    @JvmStatic
    @SuppressLint("SetJavaScriptEnabled", "JavascriptInterface", "AddJavascriptInterface")
    @BindingAdapter(value = ["app:loadAssetPath"])
    fun setLoadAssetsPage(webView: XWebView, assetPath: String?) {
        val url = "file:///android_asset/$assetPath"
        webView.loadUrl(url)
    }

}