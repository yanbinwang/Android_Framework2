package com.example.common.base.binding

import android.annotation.SuppressLint
import android.webkit.WebView
import android.widget.EditText
import android.widget.TextView
import androidx.constraintlayout.widget.Guideline
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.widget.ViewPager2
import com.example.common.R
import com.example.common.base.binding.adapter.BaseQuickAdapter
import com.example.common.utils.function.getStatusBarHeight
import com.example.common.utils.function.load
import com.example.common.utils.function.setSpanFirst
import com.example.common.widget.xrecyclerview.XRecyclerView
import com.example.framework.utils.function.value.orFalse
import com.example.framework.utils.function.value.orTrue
import com.example.framework.utils.function.value.toSafeInt
import com.example.framework.utils.function.view.*
import com.example.framework.utils.shownAnim

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

    // <editor-fold defaultstate="collapsed" desc="基础绑定方法">
    /**
     * 约束布局等高线设置
     */
    @JvmStatic
    @BindingAdapter(value = ["statusBar_margin"])
    fun bindingGuidelineStatusBar(guideline: Guideline, statusBarMargin: Boolean?) {
        if (statusBarMargin.orFalse) guideline.setGuidelineBegin(getStatusBarHeight())
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="viewpager+recyclerview绑定方法">
    /**
     * 尽量替换为viewpager2，viewpager也支持绑定
     */
    @JvmStatic
    @BindingAdapter(value = ["adapter"])
    fun <T : PagerAdapter> bindingScaleViewPagerAdapter(pager: ViewPager, adapter: T) {
        pager.adapter = adapter
        pager.offscreenPageLimit = adapter.count - 1
        pager.currentItem = 0
        pager.startAnimation(pager.context.shownAnim())
    }

    /**
     * 不和tablayout或者其他view关联的数据加载可以直接在xml中绑定
     */
    @JvmStatic
    @BindingAdapter(value = ["adapter", "orientation", "user_input_enabled", "page_limit"], requireAll = false)
    fun <T : RecyclerView.Adapter<*>> bindingViewPage2Adapter(flipper: ViewPager2, adapter: T, orientation: Int?, userInputEnabled: Boolean?, pageLimit: Boolean?) {
        flipper.adapter(adapter, orientation.toSafeInt(ViewPager2.ORIENTATION_HORIZONTAL), userInputEnabled.orTrue, pageLimit.orFalse)
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
     * 给recyclerview绑定一个适配器
     */
    @JvmStatic
    @BindingAdapter(value = ["linear_adapter", "linear_orientation"], requireAll = false)
    fun <T : RecyclerView.Adapter<*>> bindingRecyclerViewLinearLayoutManager(rec: RecyclerView, linearAdapter: T, @RecyclerView.Orientation linearOrientation: Int?) {
        rec.cancelItemAnimator()
        if (linearOrientation.toSafeInt(RecyclerView.VERTICAL) == RecyclerView.VERTICAL) {
            rec.initLinearVertical(linearAdapter)
        } else {
            rec.initLinearHorizontal(linearAdapter)
        }
    }

    /**
     * 给recyclerview绑定一个适配器
     */
    @JvmStatic
    @BindingAdapter(value = ["grid_adapter", "grid_orientation", "grid_columns"], requireAll = false)
    fun <T : RecyclerView.Adapter<*>> bindingRecyclerViewGridLayoutManager(rec: RecyclerView, gridAdapter: T, @RecyclerView.Orientation gridOrientation: Int?, gridColumns: Int?) {
        rec.cancelItemAnimator()
        if (gridOrientation.toSafeInt(RecyclerView.VERTICAL) == RecyclerView.VERTICAL) {
            rec.initGridVertical(gridAdapter, gridColumns.toSafeInt(1))
        } else {
            rec.initGridHorizontal(gridAdapter, gridColumns.toSafeInt(1))
        }
    }

    /**
     * 适配器
     * requireAll设置是否需要全部设置，true了就和设定属性layout_width和layout_height一样，不写就报错
     */
    @JvmStatic
    @BindingAdapter(value = ["adapter", "span_count", "horizontal_space", "vertical_space", "has_horizontal_edge", "has_vertical_edge"], requireAll = false)
    fun <T : BaseQuickAdapter<*, *>> bindingXRecyclerViewAdapter(rec: XRecyclerView, adapter: T, spanCount: Int?, horizontalSpace: Int?, verticalSpace: Int?, hasHorizontalEdge: Boolean?, hasVerticalEdge: Boolean?) {
        rec.setAdapter(adapter, spanCount.toSafeInt(1), horizontalSpace.toSafeInt(), verticalSpace.toSafeInt(), hasHorizontalEdge.orFalse, hasVerticalEdge.orFalse)
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="webview绑定方法">
    /**
     * 加载网页
     */
    @JvmStatic
    @SuppressLint("SetJavaScriptEnabled", "JavascriptInterface", "AddJavascriptInterface")
    @BindingAdapter(value = ["load_url", "need_header"], requireAll = false)
    fun bindingWebViewLoadUrl(webView: WebView, loadPageUrl: String, needHeader: Boolean?) {
        webView.load(loadPageUrl, needHeader.orFalse)
    }

    /**
     * 加载本地网页
     */
    @JvmStatic
    @SuppressLint("SetJavaScriptEnabled", "JavascriptInterface", "AddJavascriptInterface")
    @BindingAdapter(value = ["load_asset_url", "need_header"], requireAll = false)
    fun bindingWebViewLoadAssetUrl(webView: WebView, assetPath: String, needHeader: Boolean?) {
        webView.load("file:///android_asset/$assetPath", needHeader.orFalse)
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="textview绑定方法">
    /**
     * 高亮文本
     */
    @JvmStatic
    @BindingAdapter(value = ["text", "key_text", "key_color", "is_match_text"], requireAll = false)
    fun bindingTextViewArguments(textview: TextView, text: String?, keyText: String?, keyColor: Int?, isMatchText: Boolean?) {
        if (!keyText.isNullOrEmpty()) textview.setSpanFirst(text.orEmpty(), keyText, keyColor.toSafeInt(R.color.defaultTheme)) else textview.text = text.orEmpty()
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
     * 是否禁止edittext输入emoji
     */
    @JvmStatic
    @BindingAdapter(value = ["is_emoji_limit"])
    fun bindingEditTextRejectEmoji(editText: EditText, isReject: Boolean?) {
        if (isReject.orFalse) editText.emojiLimit()
    }

    /**
     * 是否禁止输入空格
     */
    @JvmStatic
    @BindingAdapter(value = ["is_inhibit_space"])
    fun bindingEditTextInhibitInputSpace(editText: EditText, isInhibitInputSpace: Boolean?) {
        if (isInhibitInputSpace.orFalse) editText.inhibitSpace()
    }

    /**
     * 限制输入内容为非目标值
     */
    @JvmStatic
    @BindingAdapter(value = ["char_black_limit"])
    fun bindingEditTextCharBlackList(editText: EditText, charBlackList: CharArray?) {
        if (charBlackList == null) return
        editText.charBlackList(charBlackList)
    }

    /**
     * 限制输入内容为目标值
     */
    @JvmStatic
    @BindingAdapter(value = ["char_limit"])
    fun bindingEditTextCharLimit(editText: EditText, charLimit: CharArray?) {
        if (charLimit == null) return
        editText.charLimit(charLimit)
    }
    // </editor-fold>

}