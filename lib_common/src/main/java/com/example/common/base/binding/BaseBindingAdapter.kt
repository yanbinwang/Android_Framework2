package com.example.common.base.binding

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.text.InputType
import android.text.Spannable
import android.view.View
import android.webkit.WebView
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.ToggleButton
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.widget.ViewPager2
import com.example.common.R
import com.example.common.base.binding.adapter.BaseQuickAdapter
import com.example.common.config.Constants.NO_DATA
import com.example.common.utils.function.color
import com.example.common.utils.function.drawable
import com.example.common.utils.function.getStatusBarHeight
import com.example.common.utils.function.load
import com.example.common.utils.function.pt
import com.example.common.utils.function.ptFloat
import com.example.common.widget.advertising.Advertising
import com.example.common.widget.textview.edittext.ClearEditText
import com.example.common.widget.xrecyclerview.XRecyclerView
import com.example.framework.utils.PropertyAnimator.Companion.elasticityEnter
import com.example.framework.utils.function.value.createOvalDrawable
import com.example.framework.utils.function.value.createRectangleDrawable
import com.example.framework.utils.function.value.orFalse
import com.example.framework.utils.function.value.orTrue
import com.example.framework.utils.function.value.toSafeFloat
import com.example.framework.utils.function.value.toSafeInt
import com.example.framework.utils.function.view.adapter
import com.example.framework.utils.function.view.blackListLimit
import com.example.framework.utils.function.view.clearBackground
import com.example.framework.utils.function.view.clearHighlightColor
import com.example.framework.utils.function.view.decimalLimit
import com.example.framework.utils.function.view.emojiLimit
import com.example.framework.utils.function.view.initGridHorizontal
import com.example.framework.utils.function.view.initGridVertical
import com.example.framework.utils.function.view.initLinearHorizontal
import com.example.framework.utils.function.view.initLinearVertical
import com.example.framework.utils.function.view.linearGradient
import com.example.framework.utils.function.view.margin
import com.example.framework.utils.function.view.padding
import com.example.framework.utils.function.view.spaceLimit
import com.example.framework.utils.function.view.whiteListLimit

/**
 * Created by WangYanBin on 2020/6/10.
 * 全局通用工具类
 * 复用性高的代码，统一放在common中，比如列表页都需要设置适配器属性，富文本加载网页
 * bindingAdapters不遵循默认值,生成的类使用Java，需要额外注意，Data Binding 不会自动应用 Kotlin 方法的默认参数值！！
 * requireAll设置是否需要全部设置，true了就和设定属性layout_width和layout_height一样，不写就报错
 * 如果requireAll设置为false，则未通过编程设置的所有内容都将为null，false（对于布尔值）或0（对于数字）
 */
object BaseBindingAdapter {

    // <editor-fold defaultstate="collapsed" desc="View/Textview绑定方法">
    /**
     * 约束布局等高线设置
     */
    @JvmStatic
    @BindingAdapter(value = ["statusBar_margin"])
    fun bindingStatusBarMargin(view: View, statusBarMargin: Boolean?) {
        if (!statusBarMargin.orFalse) return
        view.margin(top = getStatusBarHeight())
    }

    @JvmStatic
    @BindingAdapter(value = ["statusBar_padding"])
    fun bindingStatusBarPadding(view: View, statusBarPadding: Boolean?) {
        if (!statusBarPadding.orFalse) return
        view.padding(top = getStatusBarHeight())
    }

    /**
     * corner_color 颜色字符 -> "#cf111111"
     * corner_radius 圆角 -> 传入X.ptFloat,代码添加一个对应圆角的背景
     */
    @JvmStatic
    @BindingAdapter(value = ["corner_color", "corner_radius"], requireAll = false)
    fun bindingBackgroundCorner(view: View, cornerColor: String?, cornerRadius: Int?) {
        view.background = createRectangleDrawable(cornerColor.orEmpty(), cornerRadius.toSafeFloat(5f).ptFloat)
    }

    /**
     * oval_color 颜色字符 -> "#cf111111"
     */
    @JvmStatic
    @BindingAdapter(value = ["oval_color"], requireAll = false)
    fun bindingBackgroundOval(view: View, ovalColor: String?) {
        view.background = createOvalDrawable(ovalColor.orEmpty())
    }

    /**
     * 几组xml里结合mvvm的写法
     *  android:text="@{bean.nickText??@string/unitNoData}"
     *  *android:text="@{bean.pointsText??`0`,default=`0`}"
     *  android:textColor="@{bean!=null?bean.getAuthColorRes():@color/textPrimary}"
     *  android:background="@{bean.getAuthBgRes(context)??@drawable/shape_default}"
     *  *android:src="@{bean.getAvatarRes(context)??@drawable/shape_default}"
     *  android:visibility="@{bean!=null?bean.authVisible:View.GONE}"
     * 系统默认的属性即可完成需求，但如果是一个频繁需要set数据的textview，仅仅提供绑定是不够的，我们需要给每个值set一个tag，尽可能的扩展一些系统自带的属性重复的set去return，减少内存的调度
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
     * 绑定「基础数据类型属性」且上游对象可能为 null → 必报错 : 绑定「引用类型属性」→ 不报错（仅显示空白 / 默认效果）
     * 一) 必报错（必须加非空判断！）
     * android:visibility -> int -> 错误：@{bean.visible}/正确：@{null!=bean?bean.visible : View.GONE}
     * android:alpha -> float -> 错误：@{bean.alpha}/正确：@{null!=bean?bean.alpha : 1.0f}
     * android:enabled -> boolean -> 错误：@{bean.isEnabled}/正确：@{null!=bean?bean.isEnabled : true}
     *
     * <data>
     *     <import type="androidx.databinding.Dimension" />
     * </data>
     * android:layout_width="@{bean != null ? bean.layoutWidth : Dimension.dpToPx(100)}"
     * android:layout_width -> int -> 错误：@{bean.layoutWidth}/正确：@{null!=bean?bean.layoutWidth : @dimen/width_100dp}（需转尺寸）
     *
     * android:maxLines -> int -> 错误：@{bean.maxLineCount}/正确：@{null!=bean?bean.maxLineCount : 2}
     * android:progress -> int -> 错误：@{bean.progress}/正确：@{null!=bean?bean.progress : 0}
     * android:rating -> float -> 错误：@{bean.rating}/正确：@{null!=bean?bean.rating : 3.5f}
     * android:textSize -> float/dimen -> 错误：@{bean.textSize}/正确：@{null!=bean?bean.textSize : @dimen/textSize14}
     * 基础类型的「包装类」（如 Integer、Float）也适用：比如 bean.count 是 Integer 类型，绑定 android:progress="@{bean.count}"，bean 为 null 时依然抛 NPE（因为最终要转成基础类型 int）；
     * 必须加非空保护（?. 或 bean != null ? ...），且兜底值要和类型匹配（如 int 用 View.GONE/0，float 用 1.0f）
     *
     * 二) 不报错（无需强制加非空判断，建议兜底优化体验）
     * android:text -> String -> 基础：@{bean.name}/优化：@{bean.name ,default= @string/unitNoData}
     * android:src/app:srcCompat -> Drawable -> 基础：@{bean.icon}/优化：@{bean.icon ,default= @drawable/default_icon}
     * android:background -> Drawable/Color -> 基础：@{bean.bgDrawable}/优化：@{bean.bgDrawable ,default= @color/bgDefault}
     * android:hint -> String -> 基础：@{bean.hintText}/优化：@{bean.hintText ,default= @string/hint_default}
     * android:tag -> Object -> @{bean.tagObj}
     * app:adapter（RecyclerView） -> Adapter -> @{bean.adapter}
     * android:contentDescription -> String -> 基础：@{bean.desc}/优化：@{bean.desc ,default= @string/default_desc}
     * 自定义属性（引用类型） -> 自定义 Bean -> @{bean.customObj}
     * 引用类型可以接收 null，所以不会抛 NPE，但可能显示「空白 / 透明 / 无数据」，建议用 ?? 加兜底（默认文本 / 图片 / 颜色）优化用户体验；
     * 若引用类型属性的「属性值本身是基础类型」（如 bean.iconResId 是 int 型资源 ID，绑定 app:srcCompat="@{@drawable/bean.iconResId}"），
     * 需先判断 bean 非空（bean != null ? @drawable/bean.iconResId : @drawable/default），否则 bean 为 null 时依然抛 NPE（本质是访问 null 的 int 属性）。
     *
     * 绑定关系建立时（页面初始化），没调用 setVariable 前，所有 @{bean.xxx} 表达式的解析结果都是 null；
     * 但系统绑定属性或自定义 BindingAdapter 对这些 null 参数做了 “容错处理”（跳过执行 / 默认值兜底），所以不会报错
     *
     * 特殊文本显示文本
     * @text:文本文案 -> "普通文本"
     * @spannable:高亮文本文案 -> TextSpan().add("高亮文本", ColorSpan(color(R.color.bgMain))).build()
     * @textColor:文本颜色 -> "文本颜色(@ColorRes):${R.color.bgBlack}"
     * @background:view背景 -> "背景:${R.drawable.shape_r20_grey}"
     * @visibility:view可见性 -> "可见性:${View.VISIBLE}"
     *
     * textview.setSpanAll(text, keyText, keyColor.toSafeInt(R.color.textOrange))
     * textview.setSpanFirst(text, keyText, keyColor.toSafeInt(R.color.textOrange))
     * textview.setMatchText()
     */
    @JvmStatic
    @BindingAdapter(value = ["text", "spannable", "textColor", "background", "visibility"], requireAll = false)
    fun bindingCompound(view: View, text: String?, spannable: Spannable?, @ColorRes textColor: Int?, @DrawableRes background: Int?, visibility: Int?) {
        if (view is TextView) {
            // 处理文本设置
            text?.let { newText ->
                val textKey = R.id.theme_text_tag
                val oldText = view.getTag(textKey) as? String
                if (oldText != newText) {
                    view.text = newText
                    view.setTag(textKey, newText)
                }
            }
            // 处理高亮文本
            spannable?.let { newSpannable ->
                val spannableKey = R.id.theme_spannable_tag
                val oldSpannable = view.getTag(spannableKey) as? Spannable
                if (oldSpannable != newSpannable) {
                    view.text = newSpannable
                    view.setTag(spannableKey, newSpannable)
                }
            }
            // 文本是必须要加载出来的
            if (text == null && spannable == null) {
                view.text = NO_DATA
            }
            // 处理文本颜色设置
            textColor?.let { newTextColor ->
                val textColorKey = R.id.theme_text_color_tag
                val oldTextColor = view.getTag(textColorKey) as? Int
                if (oldTextColor != newTextColor) {
                    view.setTextColor(color(newTextColor))
                    view.setTag(textColorKey, newTextColor)
                }
            }
        }
        // 处理背景设置
        background?.let { newBackground ->
            val backgroundKey = R.id.theme_background_tag
            val oldBackground = view.getTag(backgroundKey) as? Int
            if (oldBackground != newBackground) {
                view.setBackgroundResource(newBackground)
                view.setTag(backgroundKey, newBackground)
            }
        }
        // 处理可见性设置
        visibility?.let { newVisibility ->
            val visibilityKey = R.id.theme_visibility_tag
            val oldVisibility = view.getTag(visibilityKey) as? Int
            if (oldVisibility != newVisibility) {
                view.visibility = newVisibility
                view.setTag(visibilityKey, newVisibility)
            }
        }
    }

    /**
     * textview绘制图片
     * <TextView-->不需要配置自定义text值
     *  drawableHeight="@{80}"
     *  drawablePadding="@{5}"
     *  drawableTop="@{R.mipmap.ic_flash_on}"
     *  drawableWidth="@{80}"
     *  android:text="闪光灯"
     *  android:layout_width="wrap_content"
     *  android:layout_height="wrap_content"
     *  android:gravity="center"
     *  android:textColor="@color/textPrimary"
     *  android:textSize="@dimen/textSize14" />
     *
     * 可变点击按钮
     * <ToggleButton
     *   drawableHeight="@{80}"
     *   drawablePadding="@{5}"
     *   drawableTop="@{R.drawable.selector_flash}"
     *   drawableWidth="@{80}"
     *   text="@{`闪光灯`}"
     *   android:layout_width="wrap_content"
     *   android:layout_height="wrap_content"
     *   android:checked="false"
     *   android:gravity="center"
     *   android:textColor="@drawable/selector_flash_txt"
     *   android:textSize="@dimen/textSize14" />
     *
     *   private var isIntercepted = false // 标记是否拦截切换
     *   toggleButton.setOnCheckedChangeListener { _, isChecked ->
     *     if (isIntercepted) {
     *         // 如果需要拦截，恢复到之前的状态
     *         toggleButton.isChecked = !isChecked
     *     } else {
     *         // 在这里可以处理正常的状态改变逻辑
     *        // 例如根据 isChecked 的值执行不同的操作
     *     }
     *  }
     */
    data class TextViewCompoundDrawableConfig(
        val drawableText: String? = null,
        @param:DrawableRes val drawableStart: Int? = null,
        @param:DrawableRes val drawableTop: Int? = null,
        @param:DrawableRes val drawableEnd: Int? = null,
        @param:DrawableRes val drawableBottom: Int? = null,
        @param:ColorRes val drawableTintColor: Int? = null,
        val drawableWidth: Int? = null,
        val drawableHeight: Int? = null,
        val drawablePadding: Int? = null
    )

    @JvmStatic
    @BindingAdapter(value = ["drawableText", "drawableStart", "drawableTop", "drawableEnd", "drawableBottom", "drawableTintColor", "drawableWidth", "drawableHeight", "drawablePadding"], requireAll = false)
    fun bindingTextViewCompound(view: TextView, drawableText: String?, @DrawableRes drawableStart: Int?, @DrawableRes drawableTop: Int?, @DrawableRes drawableEnd: Int?, @DrawableRes drawableBottom: Int?, @ColorRes drawableTintColor: Int?, drawableWidth: Int?, drawableHeight: Int?, drawablePadding: Int?) {
        // 清除背景
        view.clearBackground()
        view.clearHighlightColor()
        // 构建新的属性对象
        val newConfig = TextViewCompoundDrawableConfig(drawableText, drawableStart, drawableTop, drawableEnd, drawableBottom, drawableTintColor, drawableWidth, drawableHeight, drawablePadding)
        val compoundDrawableKey = R.id.theme_compound_drawable_tag
        // 获取旧属性对象，判断是否需要更新（利用数据类equals）
        val oldConfig = view.getTag(compoundDrawableKey) as? TextViewCompoundDrawableConfig
        // 属性未变化，直接返回，避免无效刷新
        if (oldConfig == newConfig) return
        view.setTag(compoundDrawableKey, newConfig)
        // 处理ToggleButton文本
        if (view is ToggleButton) {
            val newText = newConfig.drawableText.orEmpty()
            // 避免空文本重复赋值，同时兼容null转空字符串
            if (view.text?.toString().orEmpty() != newText) {
                view.text = newText
                view.textOn = newText
                view.textOff = newText
            }
        }
        // 仅Drawable相关属性变化时，才执行Drawable加载/设置
        if (!isOnlyTextChanged(oldConfig, newConfig)) {
            val startDrawable = newConfig.drawableStart?.let { drawable(it) }
            val topDrawable = newConfig.drawableTop?.let { drawable(it) }
            val endDrawable = newConfig.drawableEnd?.let { drawable(it) }
            val bottomDrawable = newConfig.drawableBottom?.let { drawable(it) }
            // 存储 Drawable 的数组
            val drawables = arrayOf(startDrawable, topDrawable, endDrawable, bottomDrawable)
            // 设置 Drawable 大小
            applyDrawableBoundsAndTint(drawables, drawableTintColor, drawableWidth, drawableHeight)
            // 设置 TextView 的 CompoundDrawables
            view.setCompoundDrawables(startDrawable, topDrawable, endDrawable, bottomDrawable)
//        view.setCompoundDrawablesRelativeWithIntrinsicBounds(startDrawable, topDrawable, endDrawable, bottomDrawable)
            // 间距
            drawablePadding?.let { view.compoundDrawablePadding = it.pt }
        }
    }

    /**
     * 判断仅文本变化（Drawable相关属性无变化）
     * @param oldConfig 旧的配置对象
     * @param newConfig 新的配置对象
     * @return true = 仅文本变化（无需更新Drawable），false = Drawable相关属性变化（需要更新Drawable）
     */
    private fun isOnlyTextChanged(oldConfig: TextViewCompoundDrawableConfig?, newConfig: TextViewCompoundDrawableConfig): Boolean {
        // 旧属性为空 → Drawable必变化
        if (oldConfig == null) return false
        return oldConfig.drawableStart == newConfig.drawableStart &&
                oldConfig.drawableTop == newConfig.drawableTop &&
                oldConfig.drawableEnd == newConfig.drawableEnd &&
                oldConfig.drawableBottom == newConfig.drawableBottom &&
                oldConfig.drawableTintColor == newConfig.drawableTintColor &&
                oldConfig.drawableWidth == newConfig.drawableWidth &&
                oldConfig.drawableHeight == newConfig.drawableHeight &&
                oldConfig.drawablePadding == newConfig.drawablePadding
    }

    /**
     * 为Drawable设置边界（宽高）和着色
     * @param drawables 需要处理的Drawable数组
     * @param tintColor 着色颜色资源ID
     * @param width Drawable宽度（px）
     * @param height Drawable高度（px）
     */
    private fun applyDrawableBoundsAndTint(drawables: Array<Drawable?>, tintColor: Int?, width: Int?, height: Int?) {
        if (width != null && height != null) {
            for (drawable in drawables) {
                drawable?.let {
                    it.setBounds(0, 0, width.pt, height.pt)
                    if (null != tintColor) it.setTint(tintColor)
                }
            }
        }
    }

    /**
     * 文案渐变
     * tvTitle.linearGradient("#FFB818", "#8E00FE")
     */
    @JvmStatic
    @BindingAdapter(value = ["start_color", "end_color"], requireAll = false)
    fun bindingTextViewGradient(view: TextView, startColor: String?, endColor: String?) {
        view.linearGradient(startColor, endColor)
    }

    /**
     * 限制输入内容为目标值
     */
    @JvmStatic
    @BindingAdapter(value = ["allowed_limit"])
    fun bindingEditTextAllowedLimit(view: EditText, allowed: CharArray?) {
        if (allowed == null) return
        view.whiteListLimit(allowed)
    }

    @JvmStatic
    @BindingAdapter(value = ["allowed_limit"])
    fun bindingEditTextAllowedLimit(view: ClearEditText, allowed: CharArray?) {
        if (allowed == null) return
        view.editText.whiteListLimit(allowed)
    }

    /**
     * 限制输入内容排除指定字符（黑名单）
     */
    @JvmStatic
    @BindingAdapter(value = ["disallowed_limit"])
    fun bindingEditTextDisallowedList(view: EditText, disallowed: CharArray?) {
        if (disallowed == null) return
        view.blackListLimit(disallowed)
    }

    @JvmStatic
    @BindingAdapter(value = ["disallowed_limit"])
    fun bindingEditTextDisallowedList(view: ClearEditText, disallowed: CharArray?) {
        if (disallowed == null) return
        view.editText.blackListLimit(disallowed)
    }

    /**
     * 是否禁止edittext输入emoji
     */
    @JvmStatic
    @BindingAdapter(value = ["emoji_limit"])
    fun bindingEditTextEmojiLimit(view: EditText, emojiLimit: Boolean?) {
        if (!emojiLimit.orFalse) return
        view.emojiLimit()
    }

    /**
     * 设置小数点
     */
    @JvmStatic
    @BindingAdapter(value = ["decimal_limit"])
    fun bindingEditTextDecimal(view: EditText, decimalPoint: Int?) {
        view.decimalLimit(decimalPoint.toSafeInt())
    }

    @JvmStatic
    @BindingAdapter(value = ["decimal_limit"])
    fun bindingEditTextDecimal(view: ClearEditText, decimalPoint: Int?) {
        view.editText.decimalLimit(decimalPoint.toSafeInt())
    }

    /**
     * 限制输入内容为正負號小數或整數
     */
    @JvmStatic
    @BindingAdapter(value = ["number_decimal"])
    fun bindingEditTextNumberDecimal(view: EditText, numberDecimal: Boolean?) {
        if(!numberDecimal.orFalse) return
        view.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL or InputType.TYPE_NUMBER_FLAG_SIGNED
    }

    @JvmStatic
    @BindingAdapter(value = ["number_decimal"])
    fun bindingEditTextNumberDecimal(view: ClearEditText, numberDecimal: Boolean?) {
        if(!numberDecimal.orFalse) return
        view.editText.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL or InputType.TYPE_NUMBER_FLAG_SIGNED
    }

    /**
     * 是否禁止输入空格
     */
    @JvmStatic
    @BindingAdapter(value = ["space_limit"])
    fun bindingEditTextSpaceLimit(view: EditText, spaceLimit: Boolean?) {
        if (!spaceLimit.orFalse) return
        view.spaceLimit()
    }

    @JvmStatic
    @BindingAdapter(value = ["space_limit"])
    fun bindingEditTextSpaceLimit(view: ClearEditText, spaceLimit: Boolean?) {
        if (!spaceLimit.orFalse) return
        view.editText.spaceLimit()
    }

    /**
     * 广告加载时直接设置带弧形的背景
     */
    @JvmStatic
    @BindingAdapter(value = ["corner_radius", "corner_color"], requireAll = false)
    fun bindingAdvertisingCorner(view: Advertising, cornerRadius: Int?, cornerColor: String?) {
        view.background = createRectangleDrawable(cornerColor ?: "#F9FAFB", cornerRadius.ptFloat)
    }

    /**
     * 解决水平进度条频繁赋值带来的内存开销
     * xml内设置ProgressBar的style="?android:attr/progressBarStyleHorizontal"
     */
    @JvmStatic
    @BindingAdapter(value = ["progress", "progressMax", "progressDrawable"], requireAll = false)
    fun bindingProgressBarCompound(view: ProgressBar, progress: Int?, progressMax: Int?, @DrawableRes progressDrawable: Int?) {
        // 进度条值
        progress?.takeIf { it >= 0 }?.let { newProgress ->
            val progressKey = R.id.theme_progress_tag
            val oldProgress = view.getTag(progressKey) as? Int
            if (oldProgress != newProgress) {
                view.progress = newProgress
                view.setTag(progressKey, newProgress)
            }
        }
        // 进度条最大值
        progressMax?.takeIf { it >= 0 }?.let { newProgressMax ->
            val progressMaxKey = R.id.theme_progress_max_tag
            val oldProgressMax = view.getTag(progressMaxKey) as? Int
            if (oldProgressMax != newProgressMax) {
                view.max = newProgressMax
                view.setTag(progressMaxKey, newProgressMax)
            }
        }
        // 进度条背景
        progressDrawable?.let { newProgressDrawable ->
            val progressDrawableKey = R.id.theme_progress_drawable_tag
            val oldProgressDrawable = view.getTag(progressDrawableKey) as? Int
            if (oldProgressDrawable != newProgressDrawable) {
                view.progressDrawable = drawable(newProgressDrawable)
                view.setTag(progressDrawableKey, newProgressDrawable)
            }
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="ViewPager/ViewPager2/XRecyclerView绑定方法">
    /**
     * 尽量替换为viewpager2，viewpager也支持绑定
     */
    @JvmStatic
    @BindingAdapter(value = ["pager_adapter"])
    fun <T : PagerAdapter> bindingScaleViewPagerAdapter(view: ViewPager, pagerAdapter: T) {
        view.adapter = pagerAdapter
        view.offscreenPageLimit = pagerAdapter.count - 1
        view.currentItem = 0
        view.startAnimation(view.context.elasticityEnter())
    }

    /**
     * 不和TabLayout或者其他View关联的数据加载可以直接在xml中绑定
     */
    @JvmStatic
    @BindingAdapter(value = ["pager2_adapter", "orientation", "user_input_enabled", "page_limit"], requireAll = false)
    fun <T : RecyclerView.Adapter<*>> bindingViewPage2Adapter(view: ViewPager2, pager2Adapter: T, orientation: Int?, userInputEnabled: Boolean?, pageLimit: Boolean?) {
        view.adapter(pager2Adapter, orientation.toSafeInt(ViewPager2.ORIENTATION_HORIZONTAL), userInputEnabled.orTrue, pageLimit.orFalse)
    }

    /**
     * 传统适配器
     */
    @JvmStatic
    @BindingAdapter(value = ["adapter", "span_count", "layout_orientation"], requireAll = false)
    fun <T : RecyclerView.Adapter<*>> bindingRecyclerViewAdapter(view: RecyclerView, adapter: T, spanCount: Int?, @RecyclerView.Orientation orientation: Int?) {
        val validSpanCount = spanCount ?: 1
        val validOrientation = orientation ?: RecyclerView.VERTICAL
        when {
            validSpanCount <= 1 && validOrientation == RecyclerView.VERTICAL -> {
                view.initLinearVertical(adapter)
            }
            validSpanCount <= 1 && validOrientation == RecyclerView.HORIZONTAL -> {
                view.initLinearHorizontal(adapter)
            }
            validSpanCount > 1 && validOrientation == RecyclerView.VERTICAL -> {
                view.initGridVertical(adapter, validSpanCount)
            }
            validSpanCount > 1 && validOrientation == RecyclerView.HORIZONTAL -> {
                view.initGridHorizontal(adapter, validSpanCount)
            }
        }
    }

    /**
     * 自定义列表适配器
     * requireAll设置是否需要全部设置，true了就和设定属性layout_width和layout_height一样，不写就报错
     */
    @JvmStatic
    @BindingAdapter(value = ["adapter", "span_count", "layout_orientation"], requireAll = false)
    fun <T : RecyclerView.Adapter<*>> bindingXRecyclerViewAdapter(view: XRecyclerView, adapter: T, spanCount: Int?, @RecyclerView.Orientation orientation: Int?) {
        val validOrientation = orientation ?: RecyclerView.VERTICAL
        view.setAdapter(adapter, spanCount.toSafeInt(1), validOrientation)
    }

    /**
     * <com.xxx.XRecyclerView
     *     android:layout_width="match_parent"
     *     android:layout_height="match_parent"
     *     app:quick_adapter="@{viewModel.quickAdapter}"
     *     app:span_count="@{3}"
     *     app:horizontal_space="@{8}"
     *     app:vertical_space="@{8}"
     *     app:layout_orientation="@{androidx.recyclerview.widget.RecyclerView.HORIZONTAL}" />
     */
    @JvmStatic
    @BindingAdapter(value = ["quick_adapter", "span_count", "horizontal_space", "vertical_space", "layout_orientation"], requireAll = false)
    fun <T : BaseQuickAdapter<*, *>> bindingXRecyclerViewQuickAdapter(view: XRecyclerView, quickAdapter: T, spanCount: Int?, horizontalSpace: Int?, verticalSpace: Int?, @RecyclerView.Orientation orientation: Int?) {
        val validOrientation = orientation ?: RecyclerView.VERTICAL
        view.setQuickAdapter(quickAdapter, spanCount.toSafeInt(1), horizontalSpace.toSafeInt(), verticalSpace.toSafeInt(), validOrientation)
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="WebView绑定方法">
    /**
     * 加载网页
     */
    @JvmStatic
    @SuppressLint("SetJavaScriptEnabled", "JavascriptInterface", "AddJavascriptInterface")
    @BindingAdapter(value = ["web_load_network_url", "web_need_header"], requireAll = false)
    fun bindingWebViewLoadUrl(view: WebView, networkUrl: String?, needHeader: Boolean?) {
        view.load(networkUrl.orEmpty(), needHeader.orFalse)
    }

    /**
     * 加载本地网页
     */
    @JvmStatic
    @SuppressLint("SetJavaScriptEnabled", "JavascriptInterface", "AddJavascriptInterface")
    @BindingAdapter(value = ["web_load_asset_url", "web_need_header"], requireAll = false)
    fun bindingWebViewLoadAssetUrl(view: WebView, assetPath: String?, needHeader: Boolean?) {
        view.load("file:///android_asset/$assetPath", needHeader.orFalse)
    }
    // </editor-fold>

}