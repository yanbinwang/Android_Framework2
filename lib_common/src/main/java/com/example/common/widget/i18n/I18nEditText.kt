package com.example.common.widget.i18n

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import androidx.annotation.StringRes
import androidx.appcompat.widget.AppCompatEditText
import com.example.common.R
import com.example.common.utils.i18n.I18nUtil
import com.example.common.utils.i18n.i18String
import java.lang.ref.WeakReference
import androidx.core.content.withStyledAttributes

/**
 * @description 全局文字替換輸入框
 * @author yan
 * 设置内容请务必使用 [setI18nRes] / [setI18nTextWithArgs] 系列方法
 */
@SuppressLint("CustomViewStyleable")
class I18nEditText @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : AppCompatEditText(context, attrs, defStyleAttr), I18nImpl {
    private var i18nTextRes = -1
    private var i18nHintRes = -1
    private var formatArgs: Array<out String>? = null
    private val selfRef: WeakReference<I18nImpl> by lazy { WeakReference(this) }

    init {
        context.withStyledAttributes(attrs, R.styleable.I18n) {
            val textRes = getResourceId(R.styleable.I18n_android_text, -1)
            if (textRes != -1) setI18nRes(textRes)
            val hintRes = getResourceId(R.styleable.I18n_android_hint, -1)
            if (hintRes != -1) setI18nHintRes(hintRes)
        }
        // 设置输入框可聚集
        isFocusable = true
        // 设置触摸聚焦
        isFocusableInTouchMode = true
    }

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
        setText(text)
    }

    /**
     * @text 纯字符串
     */
    fun setHintText(text: String) {
        this.i18nHintRes = -1
        hint = text
    }

    /**
     * @resId 文字在 string.xml 中的資源地址
     */
    fun setI18nRes(@StringRes resId: Int) {
        this.i18nTextRes = resId
        applyI18n()
    }

    /**
     * @resId 提示文字在 string.xml 中的資源地址
     */
    fun setI18nHintRes(@StringRes resId: Int) {
        this.i18nHintRes = resId
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
     * 项目中部分文案采取%n$s方式拼接，在xml中配置對應文案，調用該方法直接替換%n$s的值
     */
    fun setI18nTextWithArgs(@StringRes resId: Int, vararg args: String) {
        this.i18nTextRes = resId
        this.formatArgs = args
        applyI18n()
    }

    /**
     * 重置集合文字
     */
    fun resetI18n() {
        this.formatArgs = arrayOf()
        this.i18nTextRes = -1
        this.i18nHintRes = -1
    }

    override fun applyI18n() {
        when {
            i18nTextRes < 0 -> {
            }
            formatArgs.isNullOrEmpty() -> {
                setText(i18String(i18nTextRes))
            }
            else -> {
                formatArgs?.let {
                    setText(i18String(i18nTextRes, *it))
                }
            }
        }
        if (i18nHintRes != -1) {
            hint = i18String(i18nHintRes)
        }
    }

    override fun getI18nRef(): WeakReference<I18nImpl> {
        return selfRef
    }

}