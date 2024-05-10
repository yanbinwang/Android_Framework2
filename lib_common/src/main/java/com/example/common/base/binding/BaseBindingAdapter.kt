package com.example.common.base.binding

import android.annotation.SuppressLint
import android.text.InputType
import android.text.Spannable
import android.view.View
import android.webkit.WebView
import android.widget.EditText
import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.widget.ViewPager2
import com.example.common.base.binding.adapter.BaseQuickAdapter
import com.example.common.config.Constants.NO_DATA
import com.example.common.utils.function.getStatusBarHeight
import com.example.common.utils.function.load
import com.example.common.utils.function.ptFloat
import com.example.common.utils.function.setSpanFirst
import com.example.common.widget.textview.edittext.ClearEditText
import com.example.common.widget.xrecyclerview.XRecyclerView
import com.example.framework.utils.enterAnimation
import com.example.framework.utils.function.value.orFalse
import com.example.framework.utils.function.value.orTrue
import com.example.framework.utils.function.value.toSafeFloat
import com.example.framework.utils.function.value.toSafeInt
import com.example.framework.utils.function.view.adapter
import com.example.framework.utils.function.view.background
import com.example.framework.utils.function.view.charBlackList
import com.example.framework.utils.function.view.charLimit
import com.example.framework.utils.function.view.decimalFilter
import com.example.framework.utils.function.view.emojiLimit
import com.example.framework.utils.function.view.margin
import com.example.framework.utils.function.view.padding
import com.example.framework.utils.function.view.setMatchText
import com.example.framework.utils.function.view.spaceLimit

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
    fun bindingStatusBarMargin(view: View, statusBarMargin: Boolean?) {
        if (statusBarMargin.orFalse) view.margin(top = getStatusBarHeight())
    }

    @JvmStatic
    @BindingAdapter(value = ["statusBar_padding"])
    fun bindingStatusBarPadding(view: View, statusBarPadding: Boolean?) {
        if (statusBarPadding.orFalse) view.padding(top = getStatusBarHeight())
    }

    /**
     * gradient_color 颜色字符 -> "#cf111111"
     * gradient_radius 圆角 -> 传入X.ptFloat,代码添加一个对应圆角的背景
     */
    @JvmStatic
    @BindingAdapter(value = ["gradient_color", "gradient_radius"], requireAll = false)
    fun bindingGradientBackground(view: View, gradientColor: String?, gradientRadius: Int?) {
        view.background(gradientColor.orEmpty(), gradientRadius.toSafeFloat(4.ptFloat))
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="viewpager+recyclerview绑定方法">
    /**
     * 尽量替换为viewpager2，viewpager也支持绑定
     */
    @JvmStatic
    @BindingAdapter(value = ["pager_adapter"])
    fun <T : PagerAdapter> bindingScaleViewPagerAdapter(pager: ViewPager, pagerAdapter: T) {
        pager.adapter = pagerAdapter
        pager.offscreenPageLimit = pagerAdapter.count - 1
        pager.currentItem = 0
        pager.startAnimation(pager.context.enterAnimation())
    }

    /**
     * 不和tablayout或者其他view关联的数据加载可以直接在xml中绑定
     */
    @JvmStatic
    @BindingAdapter(value = ["pager2_adapter", "orientation", "user_input_enabled", "page_limit"], requireAll = false)
    fun <T : RecyclerView.Adapter<*>> bindingViewPage2Adapter(flipper: ViewPager2, pager2Adapter: T, orientation: Int?, userInputEnabled: Boolean?, pageLimit: Boolean?) {
        flipper.adapter(pager2Adapter, orientation.toSafeInt(ViewPager2.ORIENTATION_HORIZONTAL), userInputEnabled.orTrue, pageLimit.orFalse)
    }

    /**
     * 适配器
     * requireAll设置是否需要全部设置，true了就和设定属性layout_width和layout_height一样，不写就报错
     */
    @JvmStatic
    @BindingAdapter(value = ["quick_adapter", "span_count", "horizontal_space", "vertical_space", "has_horizontal_edge", "has_vertical_edge"], requireAll = false)
    fun <T : BaseQuickAdapter<*, *>> bindingXRecyclerViewAdapter(rec: XRecyclerView, quickAdapter: T, spanCount: Int?, horizontalSpace: Int?, verticalSpace: Int?, hasHorizontalEdge: Boolean?, hasVerticalEdge: Boolean?) {
        rec.setAdapter(quickAdapter, spanCount.toSafeInt(1), horizontalSpace.toSafeInt(), verticalSpace.toSafeInt(), hasHorizontalEdge.orFalse, hasVerticalEdge.orFalse)
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
     * 特殊文本显示文本
     * text:文本
     * key_text:高亮文字
     */
    @JvmStatic
    @BindingAdapter(value = ["text", "key_text", "is_match"], requireAll = false)
    fun bindingTextViewText(textview: TextView, text: String?, keyText: String?, isMatch: Boolean?) {
        if (!keyText.isNullOrEmpty()) {
            textview.setSpanFirst((text ?: NO_DATA), keyText)
        } else {
            textview.text = text ?: NO_DATA
        }
        if (isMatch.orFalse) textview.setMatchText()
    }

//    /**
//     * 高亮文本
//     * text:文本
//     * key_text：高亮文本
//     * key_color：高亮文本颜色
//     * is_match：文字是否撑满宽度（textview本身有一定的padding且会根据内容自动换行）
//     */
//    @JvmStatic
//    @BindingAdapter(value = ["span_text", "key_text", "key_color", "is_all", "is_match"], requireAll = false)
//    fun bindingTextViewSpan(textview: TextView, text: String?, keyText: String?, keyColor: Int?, isAll: Boolean?, isMatch: Boolean?) {
//        if (!text.isNullOrEmpty() && !keyText.isNullOrEmpty()) {
//            if (isAll.orFalse) {
//                textview.setSpanAll(text, keyText, keyColor.toSafeInt(R.color.textOrange))
//            } else {
//                textview.setSpanFirst(text, keyText, keyColor.toSafeInt(R.color.textOrange))
//            }
//        }
//        if (isMatch.orFalse) textview.setMatchText()
//    }

    /**
     * 高亮文本
     */
    @JvmStatic
    @BindingAdapter(value = ["span_text", "is_match"], requireAll = false)
    fun bindingTextViewSpan(textview: TextView, span: Spannable?, isMatch: Boolean?) {
        textview.text = span ?: NO_DATA
        if (isMatch.orFalse) textview.setMatchText()
    }

    /**
     * 设置小数点
     */
    @JvmStatic
    @BindingAdapter(value = ["decimal_point"])
    fun bindingEditTextDecimal(editText: EditText, decimalPoint: Int?) {
        editText.decimalFilter(decimalPoint.toSafeInt())
    }

    @JvmStatic
    @BindingAdapter(value = ["decimal_point"])
    fun bindingEditTextDecimal(editText: ClearEditText, decimalPoint: Int?) {
        editText.editText.decimalFilter(decimalPoint.toSafeInt())
    }

    /**
     * 限制输入内容为非目标值
     */
    @JvmStatic
    @BindingAdapter(value = ["character_allowed"])
    fun bindingEditTextCharBlackList(editText: EditText, characterAllowed: CharArray?) {
        if (characterAllowed == null) return
        editText.charBlackList(characterAllowed)
    }

    @JvmStatic
    @BindingAdapter(value = ["character_allowed"])
    fun bindingEditTextCharBlackList(editText: ClearEditText, characterAllowed: CharArray?) {
        if (characterAllowed == null) return
        editText.editText.charBlackList(characterAllowed)
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

    @JvmStatic
    @BindingAdapter(value = ["char_limit"])
    fun bindingEditTextCharLimit(editText: ClearEditText, charLimit: CharArray?) {
        if (charLimit == null) return
        editText.editText.charLimit(charLimit)
    }

    /**
     * 是否禁止edittext输入emoji
     */
    @JvmStatic
    @BindingAdapter(value = ["emoji_limit"])
    fun bindingEditTextEmojiLimit(editText: EditText, emojiLimit: Boolean?) {
        if (emojiLimit.orFalse) editText.emojiLimit()
    }

    /**
     * 是否禁止输入空格
     */
    @JvmStatic
    @BindingAdapter(value = ["space_limit"])
    fun bindingEditTextSpaceLimit(editText: EditText, spaceLimit: Boolean?) {
        if (spaceLimit.orFalse) editText.spaceLimit()
    }

    @JvmStatic
    @BindingAdapter(value = ["space_limit"])
    fun bindingEditTextSpaceLimit(editText: ClearEditText, spaceLimit: Boolean?) {
        if (spaceLimit.orFalse) editText.editText.spaceLimit()
    }

    /**
     * 限制输入内容为正負號小數或整數
     */
    @JvmStatic
    @BindingAdapter(value = ["number_decimal"])
    fun bindingEditTextNumberDecimal(editText: EditText, numberDecimal: Boolean?) {
        if(numberDecimal.orFalse) editText.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL or InputType.TYPE_NUMBER_FLAG_SIGNED
    }

    @JvmStatic
    @BindingAdapter(value = ["number_decimal"])
    fun bindingEditTextNumberDecimal(editText: ClearEditText, numberDecimal: Boolean?) {
        if(numberDecimal.orFalse) editText.editText.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL or InputType.TYPE_NUMBER_FLAG_SIGNED
    }
    // </editor-fold>

}