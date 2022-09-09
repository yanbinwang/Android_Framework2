package com.example.common.base.binding

import android.annotation.SuppressLint
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import androidx.fragment.app.FragmentPagerAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.widget.ViewPager2
import com.example.base.utils.function.orFalse
import com.example.base.utils.function.toSafeInt
import com.example.base.utils.function.view.adapter
import com.example.common.imageloader.ImageLoader
import com.example.common.widget.XWebView
import com.example.common.widget.xrecyclerview.XRecyclerView

/**
 * Created by WangYanBin on 2020/6/10.
 * 全局通用工具类
 * 复用性高的代码，统一放在common中
 * 比如列表页都需要设置适配器属性，富文本加载网页
 * bindingAdapters不遵循默认值,生成的类使用Java，
 * requireAll设置是否需要全部设置，true了就和设定属性layout_width和layout_height一样，不写就报错
 * 如果requireAll设置为false，则未通过编程设置的所有内容都将为null，false（对于布尔值）或0（对于数字）
 */
object BaseBindingAdapter {

    /**
     * 适配器
     * requireAll设置是否需要全部设置，true了就和设定属性layout_width和layout_height一样，不写就报错
     */
    @JvmStatic
    @BindingAdapter(value = ["adapter", "spanCount", "horizontalSpace", "verticalSpace", "hasHorizontalEdge", "hasVerticalEdge"], requireAll = false)
    fun <T : BaseQuickAdapter<*, *>> bindingRecyclerViewAdapter(rec: XRecyclerView, adapter: T, spanCount: Int?, horizontalSpace: Int?, verticalSpace: Int?, hasHorizontalEdge: Boolean?, hasVerticalEdge: Boolean?) {
        rec.setAdapter(adapter, spanCount.toSafeInt(1), horizontalSpace.toSafeInt(0), verticalSpace.toSafeInt(0), hasHorizontalEdge.orFalse, hasVerticalEdge.orFalse)
    }

    @JvmStatic
    @BindingAdapter(value = ["adapter"])
    fun <T : FragmentPagerAdapter> bindingViewPageAdapter(pager: ViewPager, adapter: T) {
        pager.adapter = adapter
        pager.offscreenPageLimit = adapter.count - 1
    }

    @JvmStatic
    @BindingAdapter(value = ["adapter", "isUserInput"], requireAll = false)
    fun <T : RecyclerView.Adapter<*>> bindingViewPage2Adapter(flipper: ViewPager2, adapter: T, isUserInput: Boolean?) {
        flipper.adapter(adapter, isUserInput.orFalse)
    }

    /**
     * 加载网页
     */
    @JvmStatic
    @SuppressLint("SetJavaScriptEnabled", "JavascriptInterface", "AddJavascriptInterface")
    @BindingAdapter(value = ["loadUrl"])
    fun bindingWebViewLoadUrl(webView: XWebView, loadPageUrl: String) {
        webView.loadUrl(loadPageUrl)
    }

    /**
     * 加载本地网页
     */
    @JvmStatic
    @SuppressLint("SetJavaScriptEnabled", "JavascriptInterface", "AddJavascriptInterface")
    @BindingAdapter(value = ["loadAssetUrl"])
    fun bindingWebViewLoadAssetUrl(webView: XWebView, assetPath: String) {
        webView.loadUrl("file:///android_asset/$assetPath")
    }

    /**
     * 加载图片
     */
    @JvmStatic
    @BindingAdapter(value = ["display"])
    fun bindingDisplay(view: ImageView, url: String) {
        ImageLoader.instance.display(view, url)
    }

    /**
     * 加载图片（比例缩放）
     */
    @JvmStatic
    @BindingAdapter(value = ["displayZoom"])
    fun bindingDisplayZoom(view: ImageView, url: String) {
        ImageLoader.instance.displayZoom(view, url)
    }

    /**
     * 加载图片（带圆角）
     */
    @JvmStatic
    @BindingAdapter(value = ["displayRound", "roundingRadius"], requireAll = false)
    fun bindingDisplayRound(view: ImageView, url: String, roundingRadius: Int?) {
        ImageLoader.instance.displayRound(view, url, roundingRadius.toSafeInt(5))
    }

    /**
     * 加载图片（圆形）
     */
    @JvmStatic
    @BindingAdapter(value = ["displayCircle"])
    fun bindingDisplayCircle(view: ImageView, url: String) {
        ImageLoader.instance.displayCircle(view, url)
    }

}