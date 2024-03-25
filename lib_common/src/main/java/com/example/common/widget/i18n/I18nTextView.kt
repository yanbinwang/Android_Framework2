package com.example.common.widget.i18n

import android.annotation.SuppressLint
import android.content.Context
import android.text.Spannable
import android.text.SpannableString
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.AttributeSet
import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.StringRes
import androidx.appcompat.widget.AppCompatTextView
import com.example.common.R
import com.example.common.utils.i18n.I18nUtil
import com.example.common.utils.i18n.string
import com.example.framework.utils.function.color
import com.example.framework.utils.function.value.fitRange
import com.example.framework.utils.function.value.toNewList
import com.example.framework.utils.logE
import java.lang.ref.WeakReference

/**
 * @description 全局文字替換文本
 * 设置内容请务必使用 setContent / setI18nContent
 * @author yan
 */
@SuppressLint("CustomViewStyleable")
open class I18nTextView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : AppCompatTextView(context, attrs, defStyleAttr), I18nImpl {
    //本地res路径
    private var i18nTextRes: Int = -1
    private var contents: Array<out String>? = null
    //文字点击跳转
    private var canSpanClick = true
    private var haveUnderline = false
    private var spanArray: ArrayList<Pair<Int, () -> Unit>> = arrayListOf()
    private var spanArrayString: ArrayList<Pair<String, (() -> Unit)?>> = arrayListOf()
    private var spanColor = intArrayOf(context.color(R.color.appTheme))
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

    /**
     * tvNoAdmin.setClickableTextString(string(R.string.loginToRegister) to {
     * JumpTo.register(this)
     * finish()
     * })
     */
    fun setClickableTextString(vararg spanArray: Pair<String, (() -> Unit)?>) {
        this.spanArray.clear()
        this.spanArrayString.clear()
        this.spanArrayString.addAll(spanArray)
        refreshText()
    }

    fun setClickableTextRes(vararg spanArray: Pair<Int, () -> Unit>) {
        this.spanArray.clear()
        this.spanArrayString.clear()
        this.spanArray.addAll(spanArray)
        refreshText()
    }

    fun setClickableTextString(vararg spanArray: String) {
        this.spanArray.clear()
        this.spanArrayString.clear()
        this.spanArrayString.addAll(spanArray.toNewList { it to null })
        refreshText()
    }

    fun setSpanStyle(clickable: Boolean, haveUnderline: Boolean, @ColorInt vararg color: Int) {
        this.spanColor = color
        this.canSpanClick = clickable
        this.haveUnderline = haveUnderline
        refreshText()
    }

    fun clearI18n() {
        this.spanArray.clear()
        this.spanArrayString.clear()
        this.contents = arrayOf()
        this.i18nTextRes = -1
    }

    override fun refreshText() {
        contents.let { contents ->
            when {
                i18nTextRes < 0 -> {
                }
                spanArray.isNotEmpty() -> {
                    val result = string(i18nTextRes, *(spanArray.toNewList { it.first }.toIntArray()))
                    val spanString = spanArray.toNewList { string(it.first) }
                    onSpan(spanString, result)
                }
                spanArrayString.isNotEmpty() -> {
                    val spanString = spanArrayString.toNewList { it.first }
                    val result = string(i18nTextRes, *(spanString.toTypedArray()))
                    onSpan(spanString, result)
                }
                contents.isNullOrEmpty() -> text = string(i18nTextRes)
                else -> text = string(i18nTextRes, *contents)
            }
        }
    }

    private fun onSpan(spanString: List<String>, result: String) {
        val span = SpannableString(result)
        spanString.forEachIndexed { index, s ->
            val start = result.indexOf(s)
            try {
                span.setSpan(object : ClickableSpan() {
                    override fun onClick(widget: View) {
                        if (spanArray.isNotEmpty()) {
                            spanArray[index].second()
                        } else {
                            spanArrayString[index].second?.invoke()
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
        highlightColor = context.color(R.color.appTheme)
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