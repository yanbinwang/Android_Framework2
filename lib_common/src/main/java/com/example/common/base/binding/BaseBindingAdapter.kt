package com.example.common.base.binding

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.Guideline
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.base.utils.function.inAnimation
import com.example.base.utils.function.value.orFalse
import com.example.base.utils.function.value.toSafeInt
import com.example.base.utils.function.view.*
import com.example.common.R
import com.example.common.constant.Constants
import com.example.common.imageloader.ImageLoader
import com.example.common.imageloader.glide.callback.GlideRequestListener
import com.example.common.utils.builder.StatusBarBuilder
import com.example.common.utils.setSpan
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
     * 约束布局等高线设置
     */
    @JvmStatic
    @BindingAdapter(value = ["statusBar_margin"])
    fun bindingGuidelineStatusBar(guideline: Guideline, statusBarMargin: Boolean?) {
        if (statusBarMargin.orFalse) guideline.setGuidelineBegin(if (StatusBarBuilder.statusBarCheckVersion()) Constants.STATUS_BAR_HEIGHT else 0)
    }

    /**
     * ConcatAdapter为recyclerview支持的多适配器拼接的适配器，可用于绘制复杂界面拼接
     */
    @JvmStatic
    @BindingAdapter(value = ["concat_adapter"])
    fun bindingRecyclerViewConcatAdapter(rec: RecyclerView, adapter: ConcatAdapter) {
        rec.layoutManager = LinearLayoutManager(rec.context)
        rec.adapter = adapter
    }

    /**
     * 适配器
     * requireAll设置是否需要全部设置，true了就和设定属性layout_width和layout_height一样，不写就报错
     */
    @JvmStatic
    @BindingAdapter(
        value = ["adapter", "span_count", "horizontal_space", "vertical_space", "has_horizontal_edge", "has_vertical_edge"],
        requireAll = false
    )
    fun <T : BaseQuickAdapter<*, *>> bindingXRecyclerViewAdapter(rec: XRecyclerView, adapter: T, spanCount: Int?, horizontalSpace: Int?, verticalSpace: Int?, hasHorizontalEdge: Boolean?, hasVerticalEdge: Boolean?) {
        rec.setAdapter(adapter, spanCount.toSafeInt(1), horizontalSpace.toSafeInt(), verticalSpace.toSafeInt(), hasHorizontalEdge.orFalse, hasVerticalEdge.orFalse)
    }

    /**
     * 尽量替换为viewpager2，viewpager也支持绑定
     */
    @JvmStatic
    @BindingAdapter(value = ["adapter"])
    fun <T : PagerAdapter> bindingScaleViewPagerAdapter(pager: ViewPager, adapter: T) {
        pager.adapter = adapter
        pager.offscreenPageLimit = adapter.count - 1
        pager.currentItem = 0
        pager.startAnimation(pager.context.inAnimation())
    }

    /**
     * 绑定一个viewpager2的适配器
     */
    @JvmStatic
    @BindingAdapter(value = ["adapter"])
    fun <T : FragmentStateAdapter> bindingViewPageAdapter(pager: ViewPager2, adapter: T) {
        pager.adapter = adapter
        pager.offscreenPageLimit = adapter.itemCount - 1
    }

    /**
     * 不和tablayout或者其他view关联的数据加载可以直接在xml中绑定
     */
    @JvmStatic
    @BindingAdapter(value = ["adapter", "orientation", "is_user_input"], requireAll = false)
    fun <T : RecyclerView.Adapter<*>> bindingViewPage2Adapter(flipper: ViewPager2, adapter: T, orientation: Int?, isUserInput: Boolean?) {
        flipper.adapter(adapter, orientation.toSafeInt(ViewPager2.ORIENTATION_HORIZONTAL), isUserInput.orFalse)
    }

    /**
     * 加载网页
     */
    @JvmStatic
    @SuppressLint("SetJavaScriptEnabled", "JavascriptInterface", "AddJavascriptInterface")
    @BindingAdapter(value = ["load_url"])
    fun bindingWebViewLoadUrl(webView: XWebView, loadPageUrl: String) {
        webView.loadUrl(loadPageUrl)
    }

    /**
     * 加载本地网页
     */
    @JvmStatic
    @SuppressLint("SetJavaScriptEnabled", "JavascriptInterface", "AddJavascriptInterface")
    @BindingAdapter(value = ["load_asset_url"])
    fun bindingWebViewLoadAssetUrl(webView: XWebView, assetPath: String) {
        webView.loadUrl("file:///android_asset/$assetPath")
    }

    /**
     * 搜索高亮文本
     */
    @JvmStatic
    @BindingAdapter(value = ["text", "key_text", "key_color", "is_match_text"], requireAll = false)
    fun bindingTextViewKey(textview: TextView, text: String?, keyText: String?, keyColor: Int?, isMatchText: Boolean?) {
        if (!keyText.isNullOrEmpty()) textview.setSpan(text.orEmpty(), keyText, keyColor.toSafeInt(R.color.blue_3d81f2)) else { if (!text.isNullOrEmpty()) textview.text = text }
        if (isMatchText.orFalse) textview.setMatchText()
    }

    /**
     * 设置小数点
     */
    @JvmStatic
    @BindingAdapter(value = ["decimal_point"])
    fun bindingEditTextDecimal(editText: EditText, decimalPoint: Int?) {
        editText.decimalFilter(decimalPoint.toSafeInt())
    }

    /**
     * 加载图片
     */
    @JvmStatic
    @BindingAdapter(value = ["display", "placeholder_id"], requireAll = false)
    fun bindingDisplay(view: ImageView, url: String, placeholderId: Int?) {
        ImageLoader.instance.display(view, url, placeholderId.toSafeInt(R.drawable.shape_album_loading), listener = object : GlideRequestListener<Drawable?>() {
            override fun onStart() {
                view.disable()
            }

            override fun onComplete(resource: Drawable?) {
                view.enable()
            }
        })
    }

    /**
     * 加载图片（比例缩放）
     */
    @JvmStatic
    @BindingAdapter(value = ["display_zoom"])
    fun bindingDisplayZoom(view: ImageView, url: String) {
        ImageLoader.instance.displayZoom(view, url, listener = object : GlideRequestListener<Bitmap?>() {
            override fun onStart() {
                view.disable()
            }

            override fun onComplete(resource: Bitmap?) {
                view.enable()
            }
        })
    }

    /**
     * 加载图片（带圆角）
     */
    @JvmStatic
    @BindingAdapter(value = ["display_round", "rounding_radius"], requireAll = false)
    fun bindingDisplayRound(view: ImageView, url: String, roundingRadius: Int?) {
        ImageLoader.instance.displayRound(view, url, roundingRadius.toSafeInt(5))
    }

    /**
     * 加载图片（圆形）
     */
    @JvmStatic
    @BindingAdapter(value = ["display_circle"])
    fun bindingDisplayCircle(view: ImageView, url: String) {
        ImageLoader.instance.displayCircle(view, url)
    }

}