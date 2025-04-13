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
import com.example.common.utils.function.orNoData
import com.example.common.utils.function.ptFloat
import com.example.common.widget.textview.edittext.ClearEditText
import com.example.common.widget.xrecyclerview.XRecyclerView
import com.example.framework.utils.AnimationUtil.Companion.elasticityEnter
import com.example.framework.utils.function.value.orFalse
import com.example.framework.utils.function.value.orTrue
import com.example.framework.utils.function.value.toSafeFloat
import com.example.framework.utils.function.value.toSafeInt
import com.example.framework.utils.function.view.adapter
import com.example.framework.utils.function.view.background
import com.example.framework.utils.function.view.backgroundCorner
import com.example.framework.utils.function.view.backgroundOval
import com.example.framework.utils.function.view.charBlackList
import com.example.framework.utils.function.view.charLimit
import com.example.framework.utils.function.view.decimalFilter
import com.example.framework.utils.function.view.emojiLimit
import com.example.framework.utils.function.view.generateTagKey
import com.example.framework.utils.function.view.linearGradient
import com.example.framework.utils.function.view.margin
import com.example.framework.utils.function.view.padding
import com.example.framework.utils.function.view.spaceLimit
import com.example.framework.utils.function.view.textColor

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
     * corner_color 颜色字符 -> "#cf111111"
     * corner_radius 圆角 -> 传入X.ptFloat,代码添加一个对应圆角的背景
     */
    @JvmStatic
    @BindingAdapter(value = ["corner_color", "corner_radius"], requireAll = false)
    fun bindingBackgroundCorner(view: View, cornerColor: String?, cornerRadius: Int?) {
        view.backgroundCorner(cornerColor.orEmpty(), cornerRadius.toSafeFloat(5f).ptFloat)
    }

    /**
     * oval_color 颜色字符 -> "#cf111111"
     */
    @JvmStatic
    @BindingAdapter(value = ["oval_color"], requireAll = false)
    fun bindingBackgroundOval(view: View, ovalColor: String?) {
        view.backgroundOval(ovalColor.orEmpty())
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
        pager.startAnimation(pager.context.elasticityEnter())
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
     * 首先是几组xml里结合mvvm的写法
     *  android:text="@{bean.nickText??@string/unitNoData}"
     *  android:textColor="@{bean!=null?bean.getAuthColorRes():@color/textPrimary}"
     *  android:background="@{bean.getAuthBgRes(context)??@drawable/shape_default}"
     *  *android:src="@{bean.getAvatarRes(context)??@drawable/shape_default}"
     *  android:visibility="@{bean!=null?bean.authVisible:View.GONE}"
     * 由此可知，系统默认的属性即可完成需求，但如果是一个频繁需要set数据的textview，仅仅提供绑定是不够的，我们需要给每个值set一个tag，尽可能的扩展一些系统自带的属性
     * 重复的set去return，减少内存的调度
     *
     * 例子：
     * fun View?.backgroundTag(@DrawableRes bg: Int) {
     * if (this == null) return
     *  val tag = getTag(id)
     * if (tag != null && tag is Int && tag == bg) {
     * return
     * }
     * this.setBackgroundResource(bg)
     * setTag(id, bg)
     * }
     *
     * 特殊文本显示文本
     * text:文本文案
     * text_color:文本颜色
     * background:文本背景
     *
     * textview.setSpanAll(text, keyText, keyColor.toSafeInt(R.color.textOrange))
     * textview.setSpanFirst(text, keyText, keyColor.toSafeInt(R.color.textOrange))
     * textview.setMatchText()
     */
    @JvmStatic
    @BindingAdapter(value = ["text", "text_color", "background"], requireAll = false)
    fun bindingTextViewTheme(textview: TextView, text: String?, textColor: Int?, background: Int?) {
        //处理文本设置(文本是必须要加载出来的)
        text.let { newText ->
            if (!newText.isNullOrEmpty()) {
                val textKey = textview.generateTagKey("text")
                val oldText = textview.getTag(textKey) as? String
                if (oldText != newText) {
                    textview.text = newText
                    textview.setTag(textKey, newText)
                }
            } else {
                textview.text = text.orNoData()
            }
        }
        //处理文本颜色设置
        textColor?.let { newTextColor ->
            val textColorKey = textview.generateTagKey("textColor")
            val oldTextColor = textview.getTag(textColorKey) as? Int
            if (oldTextColor != newTextColor) {
                textview.textColor(newTextColor)
                textview.setTag(textColorKey, newTextColor)
            }
        }
        //处理背景设置
        background?.let { newBackground ->
            val backgroundKey = textview.generateTagKey("background")
            val oldBackground = textview.getTag(backgroundKey) as? Int
            if (oldBackground != newBackground) {
                textview.background(newBackground)
                textview.setTag(backgroundKey, newBackground)
            }
        }
    }

    /**
     * 高亮文本
     * 利用TextSpan()类构建一个spannable
     */
    @JvmStatic
    @BindingAdapter(value = ["span_text"], requireAll = false)
    fun bindingTextViewSpan(textview: TextView, span: Spannable?) {
        val newText = span ?: NO_DATA
        if (textview.text != newText) {
            textview.text = newText
        }
    }

    /**
     * 文案渐变
     * tvTitle.linearGradient("#FFB818", "#8E00FE")
     */
    @JvmStatic
    @BindingAdapter(value = ["start_color", "end_color"], requireAll = false)
    fun bindingTextViewGradient(textview: TextView, startColor: String?, endColor: String?) {
        textview.linearGradient(startColor, endColor)
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