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
    fun setAdapter(xRec: XRecyclerView, adapter: BaseQuickAdapter<*, *>) {
        xRec.apply {
            recyclerView.layoutManager = GridLayoutManager(context, 1)
            recyclerView.adapter = adapter
            addItemDecoration(1, 0, true, false)
        }
    }

//    @JvmStatic
//    @BindingAdapter(value = ["app:adapter2"])
//    fun setAdapter2(xRec: XRecyclerView, adapter: BaseQuickAdapter<*, *>) {
//        xRec.apply {
//            recyclerView.layoutManager = GridLayoutManager(context, 1)
//            recyclerView.adapter = adapter
//            addItemDecoration(0, 0, false, false)
//        }
//    }

    @JvmStatic
    @SuppressLint("SetJavaScriptEnabled", "JavascriptInterface", "AddJavascriptInterface")
    @BindingAdapter(value = ["app:loadPageUrl"])
    fun setLoadPage(webView: XWebView, loadPageUrl: String) {
        webView.loadUrl(loadPageUrl)
    }

    @JvmStatic
    @SuppressLint("SetJavaScriptEnabled", "JavascriptInterface", "AddJavascriptInterface")
    @BindingAdapter(value = ["app:loadAssetPath"])
    fun setLoadAssetsPage(webView: XWebView, assetPath: String) {
        webView.loadUrl("file:///android_asset/$assetPath")
    }

}