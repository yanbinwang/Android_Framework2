package com.example.common.widget.i18n

import android.annotation.SuppressLint
import android.content.Context
import android.text.Spannable
import android.util.AttributeSet
import androidx.annotation.StringRes
import androidx.appcompat.widget.AppCompatRadioButton
import com.example.common.R
import com.example.common.utils.i18n.I18nUtil
import com.example.common.utils.i18n.string
import java.lang.ref.WeakReference

/**
 * @description 全局文字替換单选
 * 设置内容请务必使用 setContent / setI18nContent
 * @author yan
 */
@SuppressLint("CustomViewStyleable")
class I18nRadioButton @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : AppCompatRadioButton(context, attrs, defStyleAttr), I18nImpl {
    private var i18nTextRes: Int = -1
    private var contents: Array<out String>? = null
    private val weakReference: WeakReference<I18nImpl> by lazy { WeakReference(this) }

    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.I18n)
        //xml瀏覽的情況下
        if (isInEditMode) {
            val textRes = typedArray.getResourceId(R.styleable.I18n_android_text, -1)
            if (textRes != -1) setText(textRes)
        } else {
            setI18nRes(typedArray.getResourceId(R.styleable.I18n_android_text, -1))
        }
        typedArray.recycle()
    }

    /**
     * 設置text的string.xml資源地址
     */
    fun setI18nRes(@StringRes i18nTextRes: Int) {
        this.i18nTextRes = i18nTextRes
        refreshText()
    }

    /**
     * 项目中部分文案采取%n$s方式拼接，在xml中配置對應文案，然後調用該方法直接替換%n$s的值
     */
    fun setI18nContent(@StringRes i18nTextRes: Int, vararg contents: String) {
        setI18nRes(i18nTextRes)
        setContent(*contents)
    }

    fun setTextString(string: String) {
        this.contents = arrayOf()
        this.i18nTextRes = -1
        text = string
    }

    fun setTextString(span: Spannable) {
        this.contents = arrayOf()
        this.i18nTextRes = -1
        text = span
    }

    fun setContent(vararg contents: String) {
        this.contents = contents
        refreshText()
    }

    fun clearI18n() {
        this.contents = arrayOf()
        this.i18nTextRes = -1
    }

    override fun refreshText() {
        contents.let { contents ->
            when {
                i18nTextRes < 0 -> {
                }
                contents.isNullOrEmpty() -> {
                    text = string(i18nTextRes)
                }
                else -> {
                    text = string(i18nTextRes, *contents)
                }
            }
        }
    }

    override fun getWeakRef() = weakReference

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        I18nUtil.register(this)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        I18nUtil.unregister(this)
    }

}