package com.example.common.base.binding

import android.annotation.SuppressLint
import android.text.InputType
import android.webkit.WebView
import android.widget.EditText
import android.widget.TextView
import androidx.constraintlayout.widget.Guideline
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.widget.ViewPager2
import com.example.common.R
import com.example.common.base.binding.adapter.BaseQuickAdapter
import com.example.common.utils.function.getStatusBarHeight
import com.example.common.utils.function.load
import com.example.common.utils.function.orNoData
import com.example.common.utils.function.setSpanFirst
import com.example.common.widget.textview.edit.ClearEditText
import com.example.common.widget.xrecyclerview.XRecyclerView
import com.example.framework.utils.function.value.orFalse
import com.example.framework.utils.function.value.orTrue
import com.example.framework.utils.function.value.removeEndZero
import com.example.framework.utils.function.value.thousandsFormat
import com.example.framework.utils.function.value.toSafeInt
import com.example.framework.utils.function.view.adapter
import com.example.framework.utils.function.view.cancelItemAnimator
import com.example.framework.utils.function.view.charBlackList
import com.example.framework.utils.function.view.charLimit
import com.example.framework.utils.function.view.decimalFilter
import com.example.framework.utils.function.view.emojiLimit
import com.example.framework.utils.function.view.inhibitSpace
import com.example.framework.utils.function.view.initGridHorizontal
import com.example.framework.utils.function.view.initGridVertical
import com.example.framework.utils.function.view.initLinearHorizontal
import com.example.framework.utils.function.view.initLinearVertical
import com.example.framework.utils.function.view.setMatchText
import com.example.framework.utils.scaleShown

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
    @BindingAdapter(value = ["statusBar_margin"])
    fun bindingGuidelineStatusBar(guideline: Guideline, statusBarMargin: Boolean?) {
        if (statusBarMargin.orFalse) guideline.setGuidelineBegin(getStatusBarHeight())
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="viewpager+recyclerview绑定方法">
    /**
     * 尽量替换为viewpager2，viewpager也支持绑定
     */
    @BindingAdapter(value = ["pager_adapter"])
    fun <T : PagerAdapter> bindingScaleViewPagerAdapter(pager: ViewPager, pagerAdapter: T) {
        pager.adapter = pagerAdapter
        pager.offscreenPageLimit = pagerAdapter.count - 1
        pager.currentItem = 0
        pager.startAnimation(pager.context.scaleShown())
    }

    /**
     * 不和tablayout或者其他view关联的数据加载可以直接在xml中绑定
     */
    @BindingAdapter(value = ["pager2_adapter", "orientation", "user_input_enabled", "page_limit"], requireAll = false)
    fun <T : RecyclerView.Adapter<*>> bindingViewPage2Adapter(flipper: ViewPager2, pager2Adapter: T, orientation: Int?, userInputEnabled: Boolean?, pageLimit: Boolean?) {
        flipper.adapter(pager2Adapter, orientation.toSafeInt(ViewPager2.ORIENTATION_HORIZONTAL), userInputEnabled.orTrue, pageLimit.orFalse)
    }

//    /**
//     * ConcatAdapter为recyclerview支持的多适配器拼接的适配器，可用于绘制复杂界面拼接
//     */
//    @BindingAdapter(value = ["concat_adapter"])
//    fun bindingRecyclerViewConcatAdapter(rec: RecyclerView, adapter: ConcatAdapter) {
//        rec.layoutManager = LinearLayoutManager(rec.context)
//        rec.adapter = adapter
//    }

    /**
     * 给recyclerview绑定一个适配器
     */
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
    @BindingAdapter(value = ["quick_adapter", "span_count", "horizontal_space", "vertical_space", "has_horizontal_edge", "has_vertical_edge"], requireAll = false)
    fun <T : BaseQuickAdapter<*, *>> bindingXRecyclerViewAdapter(rec: XRecyclerView, quickAdapter: T, spanCount: Int?, horizontalSpace: Int?, verticalSpace: Int?, hasHorizontalEdge: Boolean?, hasVerticalEdge: Boolean?) {
        rec.setAdapter(quickAdapter, spanCount.toSafeInt(1), horizontalSpace.toSafeInt(), verticalSpace.toSafeInt(), hasHorizontalEdge.orFalse, hasVerticalEdge.orFalse)
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="webview绑定方法">
    /**
     * 加载网页
     */
    @SuppressLint("SetJavaScriptEnabled", "JavascriptInterface", "AddJavascriptInterface")
    @BindingAdapter(value = ["load_url", "need_header"], requireAll = false)
    fun bindingWebViewLoadUrl(webView: WebView, loadPageUrl: String, needHeader: Boolean?) {
        webView.load(loadPageUrl, needHeader.orFalse)
    }

    /**
     * 加载本地网页
     */
    @SuppressLint("SetJavaScriptEnabled", "JavascriptInterface", "AddJavascriptInterface")
    @BindingAdapter(value = ["load_asset_url", "need_header"], requireAll = false)
    fun bindingWebViewLoadAssetUrl(webView: WebView, assetPath: String, needHeader: Boolean?) {
        webView.load("file:///android_asset/$assetPath", needHeader.orFalse)
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="textview绑定方法">
    /**
     * 特殊文本显示文本
     * text:文本
     * text_type：0：默认 1：数据空 2：金额空 3：%空
     */
    @BindingAdapter(value = ["text", "text_type"], requireAll = false)
    fun bindingTextViewText(textview: TextView, text: String?, textType: Int?) {
        val type = textType.toSafeInt()
        if (!text.isNullOrEmpty()) {
            textview.text = if(2 == type) text.removeEndZero().thousandsFormat() else text
        } else {
            textview.text = text.orNoData()
        }
    }

    /**
     * 高亮文本
     * text:文本
     * key_text：高亮文本
     * key_color：高亮文本颜色
     * is_match：文字是否撑满宽度（textview本身有一定的padding且会根据内容自动换行）
     */
    @BindingAdapter(value = ["span_text", "key_text", "key_color", "is_match"], requireAll = false)
    fun bindingTextViewSpanFirst(textview: TextView, text: String?, keyText: String?, keyColor: Int?, isMatch: Boolean?) {
        if (!text.isNullOrEmpty() && !keyText.isNullOrEmpty()) textview.setSpanFirst(text, keyText, keyColor.toSafeInt(R.color.appTheme))
        if (isMatch.orFalse) textview.setMatchText()
    }

    /**
     * 设置小数点
     */
    @BindingAdapter(value = ["decimal_point"])
    fun bindingEditTextDecimal(editText: EditText, decimalPoint: Int?) {
        editText.decimalFilter(decimalPoint.toSafeInt())
    }

    @BindingAdapter(value = ["decimal_point"])
    fun bindingEditTextDecimal(editText: ClearEditText, decimalPoint: Int?) {
        editText.editText.decimalFilter(decimalPoint.toSafeInt())
    }

    /**
     * 限制输入内容为非目标值
     */
    @BindingAdapter(value = ["character_allowed"])
    fun bindingEditTextCharBlackList(editText: EditText, characterAllowed: CharArray?) {
        if (characterAllowed == null) return
        editText.charBlackList(characterAllowed)
    }

    @BindingAdapter(value = ["character_allowed"])
    fun bindingEditTextCharBlackList(editText: ClearEditText, characterAllowed: CharArray?) {
        if (characterAllowed == null) return
        editText.editText.charBlackList(characterAllowed)
    }

    /**
     * 限制输入内容为目标值
     */
    @BindingAdapter(value = ["char_limit"])
    fun bindingEditTextCharLimit(editText: EditText, charLimit: CharArray?) {
        if (charLimit == null) return
        editText.charLimit(charLimit)
    }

    @BindingAdapter(value = ["char_limit"])
    fun bindingEditTextCharLimit(editText: ClearEditText, charLimit: CharArray?) {
        if (charLimit == null) return
        editText.editText.charLimit(charLimit)
    }

    /**
     * 是否禁止edittext输入emoji
     */
    @BindingAdapter(value = ["is_emoji"])
    fun bindingEditTextRejectEmoji(editText: EditText, isEmoji: Boolean?) {
        if (isEmoji.orFalse) editText.emojiLimit()
    }

    /**
     * 是否禁止输入空格
     */
    @BindingAdapter(value = ["is_inhibit_space"])
    fun bindingEditTextInhibitInputSpace(editText: EditText, isInhibitSpace: Boolean?) {
        if (isInhibitSpace.orFalse) editText.inhibitSpace()
    }

    @BindingAdapter(value = ["is_inhibit_space"])
    fun bindingEditTextInhibitInputSpace(editText: ClearEditText, isInhibitSpace: Boolean?) {
        if (isInhibitSpace.orFalse) editText.editText.inhibitSpace()
    }

    /**
     * 限制输入内容为正負號小數或整數
     */
    @BindingAdapter(value = ["is_number_decimal"])
    fun bindingEditTextNumberDecimal(editText: EditText, isNumberDecimal: Boolean?) {
        if(isNumberDecimal.orFalse) editText.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL or InputType.TYPE_NUMBER_FLAG_SIGNED
    }

    @BindingAdapter(value = ["is_number_decimal"])
    fun bindingEditTextNumberDecimal(editText: ClearEditText, isNumberDecimal: Boolean?) {
        if(isNumberDecimal.orFalse) editText.editText.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL or InputType.TYPE_NUMBER_FLAG_SIGNED
    }
    // </editor-fold>

}