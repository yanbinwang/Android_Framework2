package com.example.common.widget.i18n

import android.annotation.SuppressLint
import android.content.Context
import android.text.Spannable
import android.text.SpannableString
import android.util.AttributeSet
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.withStyledAttributes
import com.example.common.R
import com.example.common.utils.function.color
import com.example.common.utils.i18n.I18nUtil
import com.example.common.utils.i18n.i18String
import com.example.framework.utils.ColorSpan
import com.example.framework.utils.LinkClickSpan
import com.example.framework.utils.function.view.setSpannable
import com.example.framework.utils.setSpanFirst
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
    // 统一使用 Any 承载 Int(StringRes) 或 String
    private var linkSpans: MutableList<Triple<Any, Int, () -> Unit>> = mutableListOf()

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
        setSpannable(spannable)
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
     * 一步到位设置文本资源 + 链接（使用默认主题色）
     * 设置可点击的 Text 文案
     * tvNoAdmin.setClickableText(string(R.string.loginToRegister) to {
     *    JumpTo.register(this)
     *    finish()
     * })
     */
    fun setLinkSpans(@StringRes resId: Int, vararg keywords: Pair<Any, () -> Unit>) {
        this.i18nTextRes = resId
        setLinkSpans(*keywords)
    }

    /**
     * 仅设置链接（XML已配置text），可自定义颜色
     * 【注意】colorRes 必须放在 vararg 前面，否则无法编译
     */
    fun setLinkSpans(vararg keywords: Pair<Any, () -> Unit>, @ColorRes colorRes: Int = R.color.appTheme) {
        setLinkSpans(*keywords.map { (keywordSource, clickAction) ->
            Triple(keywordSource, colorRes, clickAction)
        }.toTypedArray())
    }

    /**
     * 底层核心方法，接收完整的 Triple 状态
     */
    fun setLinkSpans(vararg keywords: Triple<Any, Int, () -> Unit>) {
        this.formatArgs = arrayOf()
        this.linkSpans = keywords.toMutableList()
        applyI18n()
    }

    /**
     * 重置集合文字
     */
    fun resetI18n() {
        this.i18nTextRes = -1
        this.formatArgs = arrayOf()
        this.linkSpans.clear()
    }

    /**
     * 控件重写刷新方法
     */
    override fun applyI18n() {
        when {
            // 未设置 res 不做处理
            i18nTextRes < 0 -> {
            }
            // 跳转字符串
            linkSpans.isNotEmpty() -> {
                applySpans()
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

    private fun applySpans() {
        var spannable: Spannable = SpannableString.valueOf(i18String(i18nTextRes))
        linkSpans.forEach { (keywordSource, colorRes, clickAction) ->
            val target = when (keywordSource) {
                is Int -> i18String(keywordSource)
                is String -> keywordSource
                else -> ""
            }
            spannable = spannable.setSpanFirst(target, ColorSpan(color(colorRes)), LinkClickSpan(color(R.color.appTheme)) {
                clickAction.invoke()
            })
        }
        setSpannable(spannable)
    }

    override fun getI18nRef(): WeakReference<I18nImpl> {
        return selfRef
    }

}