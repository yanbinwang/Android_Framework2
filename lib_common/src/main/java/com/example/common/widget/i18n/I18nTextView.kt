package com.example.common.widget.i18n

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.text.Spannable
import android.text.SpannableString
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.AttributeSet
import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.withStyledAttributes
import com.example.common.R
import com.example.common.utils.i18n.I18nUtil
import com.example.common.utils.i18n.i18String
import com.example.framework.utils.function.color
import com.example.framework.utils.function.value.fitRange
import com.example.framework.utils.function.value.toNewList
import com.example.framework.utils.logE
import java.lang.ref.WeakReference

/**
 * @description 全局文字替換文本
 * @author yan
 * 设置内容请务必使用 [setI18nRes] / [setI18nTextWithArgs] 系列方法
 */
@SuppressLint("CustomViewStyleable")
open class I18nTextView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : AppCompatTextView(context, attrs, defStyleAttr), I18nImpl {
    // 本地文本 res 路径 (xml绘制时 text 属性获取或外部传入)
    private var i18nTextRes = -1
    // 组合文本集合 (部分文案采取%n$s拼接的时候会传入多个字符串最终形成一个完整的文本)
    private var formatArgs: Array<out String>? = null
    // 弱持有当前 view 的回调监听
    private val selfRef: WeakReference<I18nImpl> by lazy { WeakReference(this) }
    // 文本是否可点击
    private var canSpanClick = true
    // 文本是否带下划线
    private var haveUnderline = false
    // 高亮文本颜色
    private var spanColor = intArrayOf(context.color(R.color.appTheme))
    // 高亮色块颜色
    @ColorInt
    private var highlightColorValue = context.color(android.R.color.transparent)
    // 本地 res 路径 / 文本 整体span点击样式集合
    private var clickableI18nSpans: ArrayList<Pair<Int, () -> Unit>> = arrayListOf()
    private var clickableSpans: ArrayList<Pair<String, () -> Unit>> = arrayListOf()

    companion object {
        /**
         * 无操作的占位回调，避免重复创建匿名对象
         */
        private val NO_OP_ACTION: () -> Unit = {}
    }

    init {
        context.withStyledAttributes(attrs, R.styleable.I18n) {
            val textRes = getResourceId(R.styleable.I18n_android_text, -1)
            if (textRes != -1) setI18nRes(textRes)
        }
    }

    /**
     * 全局监听注册/注销
     */
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        I18nUtil.register(this)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        I18nUtil.unregister(this)
    }

    /**
     * 设置纯文本，语言切换时不会自动刷新
     * @text 纯字符串
     */
    fun setPlainText(text: String) {
        this.formatArgs = arrayOf()
        this.i18nTextRes = -1
        this.text = text
    }

    /**
     * 设置 Spannable 文本，语言切换时不会自动刷新
     * @spannable TextSpan/ColorSpan等特殊字符
     */
    fun setSpannableText(spannable: Spannable) {
        this.formatArgs = arrayOf()
        this.i18nTextRes = -1
        this.text = spannable
    }

    /**
     * @resId 文字在 string.xml 中的資源地址
     */
    fun setI18nRes(@StringRes resId: Int) {
        this.i18nTextRes = resId
        applyI18n()
    }

    /**
     * 设置一组集合文字,并通过 String 类拼接
     */
    fun setFormatArgs(vararg args: String) {
        this.formatArgs = args
        applyI18n()
    }

    /**
     * 项目中部分文案采取 %n$s 方式拼接，在 xml 中配置對應文案，調用該方法直接替換 %n$s 的值
     */
    fun setI18nTextWithArgs(@StringRes resId: Int, vararg args: String) {
        this.i18nTextRes = resId
        this.formatArgs = args
        applyI18n()
    }

    /**
     * 设置可点击的 Text 文案
     * tvNoAdmin.setClickableText(string(R.string.loginToRegister) to {
     *    JumpTo.register(this)
     *    finish()
     * })
     */
    fun setClickableRes(vararg spans: Pair<Int, () -> Unit>) {
        this.clickableI18nSpans.clear()
        this.clickableSpans.clear()
        this.clickableI18nSpans.addAll(spans)
        applyI18n()
    }

    fun setClickableText(vararg spans: Pair<String, () -> Unit>) {
        this.clickableI18nSpans.clear()
        this.clickableSpans.clear()
        this.clickableSpans.addAll(spans)
        applyI18n()
    }

    fun setClickableTextString(vararg texts: String) {
        this.clickableI18nSpans.clear()
        this.clickableSpans.clear()
        this.clickableSpans.addAll(texts.toNewList { it to NO_OP_ACTION })
        applyI18n()
    }

    /**
     * 设置自定义 Span 样式
     */
    fun setSpanStyle(clickable: Boolean, haveUnderline: Boolean, @ColorInt vararg color: Int, @ColorInt pressHighlightColor: Int = Color.TRANSPARENT) {
        this.spanColor = color
        this.canSpanClick = clickable
        this.haveUnderline = haveUnderline
        this.highlightColorValue = pressHighlightColor
        applyI18n()
    }

    /**
     * 重置集合文字
     */
    fun resetI18n() {
        this.i18nTextRes = -1
        this.formatArgs = arrayOf()
        this.clickableI18nSpans.clear()
        this.clickableSpans.clear()
    }

    override fun applyI18n() {
        when {
            // 未设置 res 不做处理
            i18nTextRes < 0 -> {
            }
            // res 引用的集合不为空
            clickableI18nSpans.isNotEmpty() -> {
                // 高亮文本集合
                val spanString = clickableI18nSpans.toNewList { i18String(it.first) }
                // 完整文本
                val result = i18String(i18nTextRes, *(clickableI18nSpans.toNewList { it.first }.toIntArray()))
                applySpans(spanString, result)
            }
            // 字符串引用的集合不为空
            clickableSpans.isNotEmpty() -> {
                // 高亮文本集合
                val spanString = clickableSpans.toNewList { it.first }
                // 完整文本
                val result = i18String(i18nTextRes, *(spanString.toTypedArray()))
                applySpans(spanString, result)
            }
            // 未设置过多个字符串拼接的情况下,直接引用 i18nTextRes
            formatArgs.isNullOrEmpty() -> {
                text = i18String(i18nTextRes)
            }
            // 设置了多个字符串,引用拼接 (最终执行 String.format(result, *param))
            else -> {
                formatArgs?.let {
                    text = i18String(i18nTextRes, *it)
                }
            }
        }
    }

    private fun applySpans(spanString: List<String>, result: String) {
        val span = SpannableString(result)
        spanString.forEachIndexed { index, s ->
            val start = result.indexOf(s)
            try {
                span.setSpan(object : ClickableSpan() {
                    override fun onClick(widget: View) {
                        if (clickableI18nSpans.isNotEmpty()) {
                            clickableI18nSpans[index].second()
                        } else {
                            clickableSpans[index].second.invoke()
                        }
                    }

                    override fun updateDrawState(ds: TextPaint) {
                        ds.color = spanColor[index.fitRange(spanColor.indices)]
                        ds.isUnderlineText = haveUnderline
                    }
                }, start, start + s.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            } catch (e: Exception) {
                e.logE
            }
        }
        text = span
        movementMethod = if (canSpanClick) {
            isClickable = true
            isFocusableInTouchMode = true
            LinkMovementMethod.getInstance()
        } else {
            isClickable = false
            isFocusableInTouchMode = false
            defaultMovementMethod
        }
        highlightColor = highlightColorValue
    }

    override fun getI18nRef(): WeakReference<I18nImpl> {
        return selfRef
    }

}