package com.example.common.base.binding

import android.annotation.SuppressLint
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.chad.library.adapter.base.BaseQuickAdapter
import com.example.common.imageloader.ImageLoader
import com.example.common.widget.XWebView
import com.example.common.widget.xrecyclerview.XRecyclerView

/**
 * Created by WangYanBin on 2020/6/10.
 * 全局通用工具类
 * 复用性高的代码，统一放在common中
 * 比如列表页都需要设置适配器属性，富文本加载网页
 */
object CommonBindingAdapter {

    /**
     * 适配器
     */
    @JvmStatic
    @BindingAdapter(value = ["adapter"])
    fun <T : BaseQuickAdapter<*, *>> setAdapter(rec: XRecyclerView, adapter: T) {
        rec.setAdapter(adapter)
    }

    /**
     * 加载网页
     */
    @JvmStatic
    @SuppressLint("SetJavaScriptEnabled", "JavascriptInterface", "AddJavascriptInterface")
    @BindingAdapter(value = ["loadUrl"])
    fun setLoadUrl(webView: XWebView, loadPageUrl: String) {
        webView.loadUrl(loadPageUrl)
    }

    /**
     * 加载本地网页
     */
    @JvmStatic
    @SuppressLint("SetJavaScriptEnabled", "JavascriptInterface", "AddJavascriptInterface")
    @BindingAdapter(value = ["loadAssetUrl"])
    fun setLoadAssetsUrl(webView: XWebView, assetPath: String) {
        webView.loadUrl("file:///android_asset/$assetPath")
    }

    /**
     * 加载图片
     */
    @JvmStatic
    @BindingAdapter(value = ["display"])
    fun setDisplay(view: ImageView, url: String) {
        ImageLoader.instance.displayImage(view, url)
    }

    /**
     * 加载图片（比例缩放）
     */
    @JvmStatic
    @BindingAdapter(value = ["displayZoom"])
    fun setDisplayZoom(view: ImageView, url: String) {
        ImageLoader.instance.displayZoomImage(view, url)
    }

    /**
     * 加载图片（带圆角）
     */
    @JvmStatic
    @BindingAdapter(value = ["displayRound"])
    fun setDisplayRound(view: ImageView, url: String) {
        ImageLoader.instance.displayRoundImage(view, url, 5)
    }

    /**
     * 加载图片（圆形）
     */
    @JvmStatic
    @BindingAdapter(value = ["displayCircle"])
    fun setDisplayCircle(view: ImageView, url: String) {
        ImageLoader.instance.displayCircleImage(view, url)
    }

}