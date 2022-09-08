package com.example.common.base.binding

import android.annotation.SuppressLint
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.example.common.imageloader.ImageLoader
import com.example.common.widget.XWebView
import com.example.common.widget.xrecyclerview.XRecyclerView

/**
 * Created by WangYanBin on 2020/6/10.
 * 全局通用工具类
 * 复用性高的代码，统一放在common中
 * 比如列表页都需要设置适配器属性，富文本加载网页
 */
object BaseBindingAdapter {

    /**
     * 适配器
     * requireAll设置是否需要全部设置，true了就和设定属性layout_width和layout_height一样，不写就报错
     */
    @JvmStatic
    @BindingAdapter(value = ["adapter", "spanCount", "horizontalSpace", "verticalSpace", "hasHorizontalEdge", "hasVerticalEdge"], requireAll = false)
    fun <T : BaseQuickAdapter<*, *>> bindingAdapter(rec: XRecyclerView, adapter: T, spanCount: Int = 1, horizontalSpace: Int = 0, verticalSpace: Int = 0, hasHorizontalEdge: Boolean = false, hasVerticalEdge: Boolean = false) {
        rec.setAdapter(adapter, spanCount, horizontalSpace, verticalSpace, hasHorizontalEdge, hasVerticalEdge)
    }

    /**
     * 加载网页
     */
    @JvmStatic
    @SuppressLint("SetJavaScriptEnabled", "JavascriptInterface", "AddJavascriptInterface")
    @BindingAdapter(value = ["loadUrl"])
    fun bindingLoadUrl(webView: XWebView, loadPageUrl: String) {
        webView.loadUrl(loadPageUrl)
    }

    /**
     * 加载本地网页
     */
    @JvmStatic
    @SuppressLint("SetJavaScriptEnabled", "JavascriptInterface", "AddJavascriptInterface")
    @BindingAdapter(value = ["loadAssetUrl"])
    fun bindingLoadAssetsUrl(webView: XWebView, assetPath: String) {
        webView.loadUrl("file:///android_asset/$assetPath")
    }

    /**
     * 加载图片
     */
    @JvmStatic
    @BindingAdapter(value = ["display"])
    fun bindingDisplay(view: ImageView, url: String) {
        ImageLoader.instance.displayImage(view, url)
    }

    /**
     * 加载图片（比例缩放）
     */
    @JvmStatic
    @BindingAdapter(value = ["displayZoom"])
    fun bindingDisplayZoom(view: ImageView, url: String) {
        ImageLoader.instance.displayZoomImage(view, url)
    }

    /**
     * 加载图片（带圆角）
     */
    @JvmStatic
    @BindingAdapter(value = ["displayRound", "roundingRadius"], requireAll = false)
    fun setDisplayRound(view: ImageView, url: String, roundingRadius: Int = 5) {
        ImageLoader.instance.displayRoundImage(view, url, roundingRadius)
    }

    /**
     * 加载图片（圆形）
     */
    @JvmStatic
    @BindingAdapter(value = ["displayCircle"])
    fun bindingDisplayCircle(view: ImageView, url: String) {
        ImageLoader.instance.displayCircleImage(view, url)
    }

}