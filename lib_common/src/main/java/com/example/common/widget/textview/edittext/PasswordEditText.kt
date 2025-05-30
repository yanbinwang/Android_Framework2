package com.example.common.widget.textview.edittext

import android.content.Context
import android.text.Editable
import android.text.InputFilter
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.View.OnFocusChangeListener
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.StringRes
import androidx.core.content.withStyledAttributes
import androidx.core.widget.addTextChangedListener
import com.example.common.R
import com.example.common.databinding.ViewPasswordEditBinding
import com.example.framework.utils.function.dimen
import com.example.framework.utils.function.inflate
import com.example.framework.utils.function.value.toSafeInt
import com.example.framework.utils.function.view.click
import com.example.framework.utils.function.view.color
import com.example.framework.utils.function.view.emojiLimit
import com.example.framework.utils.function.view.gone
import com.example.framework.utils.function.view.imeOptions
import com.example.framework.utils.function.view.padding
import com.example.framework.utils.function.view.passwordDevelopment
import com.example.framework.utils.function.view.setResource
import com.example.framework.utils.function.view.visible
import com.example.framework.widget.BaseViewGroup
import java.util.Arrays

/**
 * @description 密码显影输入框
 * @author yan
 */
class PasswordEditText @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : BaseViewGroup(context, attrs, defStyleAttr), SpecialEditText {
    private var hideRes = -1
    private var showRes = -1
    private var isShowBtn = true
    private val mBinding by lazy { ViewPasswordEditBinding.bind(context.inflate(R.layout.view_password_edit)) }
    val editText get() = mBinding.etClear

    init {
        mBinding.etClear.emojiLimit()
        mBinding.etClear.apply {
            setOnKeyListener { _, keyCode, _ ->
                if (keyCode == KeyEvent.KEYCODE_DEL) mBinding.etClear.setText("")
                false
            }
        }
        mBinding.ivShow.apply { click { setResource(Triple(mBinding.etClear.passwordDevelopment(), showRes, hideRes)) }}
        //以下属性在xml中前缀使用app:调取
        context.withStyledAttributes(attrs, R.styleable.PasswordEditText) {
            //文本内容
            val text = getResourceId(R.styleable.PasswordEditText_text, -1)
            if (text != -1) setText(text)
            //文字大小
            val textSize = getDimension(R.styleable.PasswordEditText_textSize, context.dimen(R.dimen.textSize14))
            setTextSize(textSize)
            //文字颜色
            val textColor = getColor(R.styleable.PasswordEditText_textColor, color(R.color.textPrimary))
            setTextColor(textColor)
            //无内容显示的文本内容
            val hint = getResourceId(R.styleable.PasswordEditText_hint, -1)
            if (hint != -1) setHint(hint)
            //无为内容显示的文本内容颜色
            val hintColor = getColor(R.styleable.PasswordEditText_textColorHint, color(R.color.textHint))
            setHintTextColor(hintColor)
            //文本方向
            val gravity = getInt(R.styleable.PasswordEditText_gravity, Gravity.CENTER_VERTICAL or Gravity.START)
            setGravity(gravity)
            //睁眼闭眼图片资源
            hideRes = getResourceId(R.styleable.PasswordEditText_btnImageHide, R.mipmap.ic_password_hide)
            showRes = getResourceId(R.styleable.PasswordEditText_btnImageShow, R.mipmap.ic_password_show)
            //文案最大范围
            val maxLength = getInt(R.styleable.PasswordEditText_maxLength, -1)
            if (maxLength != -1) setMaxLength(maxLength)
            //配置输入法右下角按钮的样式
            val imeOptions = getInt(R.styleable.PasswordEditText_imeOptions, 0)
            mBinding.etClear.imeOptions(imeOptions)
            //内部容器修正
            val resolvedStart = if (paddingStart != 0) paddingStart else paddingLeft
            val resolvedEnd = if (paddingEnd != 0) paddingEnd else paddingRight
            if (resolvedStart == 0  && paddingTop == 0 && resolvedEnd == 0 &&  paddingBottom == 0) return@withStyledAttributes
            //撑满父容器
            setPadding(0, 0, 0, 0)
            //子容器添加padding
            mBinding.root.padding(resolvedStart.toSafeInt(), paddingTop.toSafeInt(), resolvedEnd.toSafeInt(), paddingBottom.toSafeInt())
        }
    }

    override fun onInflate() {
        if (isInflate) addView(mBinding.root)
    }

    fun setText(@StringRes resid: Int) {
        mBinding.etClear.setText(resid)
    }

    fun setText(text: String) {
        mBinding.etClear.setText(text)
    }

    fun getText(): String {
        return mBinding.etClear.let { if (it.text == null) "" else it.text.toString() }
    }

    fun setTextSize(size: Float) {
        setTextSize(size, TypedValue.COMPLEX_UNIT_PX)
    }

    fun setTextSize(size: Float, unit: Int) {
        mBinding.etClear.setTextSize(unit, size)
    }

    fun setTextColor(@ColorInt color: Int) {
        mBinding.etClear.setTextColor(color)
    }

    fun setHint(@StringRes resid: Int) {
        mBinding.etClear.setHint(resid)
    }

    fun setHint(text: String) {
        mBinding.etClear.hint = text
    }

    fun setHintTextColor(@ColorInt color: Int) {
        mBinding.etClear.setHintTextColor(color)
    }

    fun setSelection(mCursor: Int) {
        mBinding.etClear.setSelection(mCursor)
    }

    fun setGravity(gravity: Int) {
        mBinding.etClear.gravity = gravity
    }

    fun setMaxLength(maxLength: Int) {
        addFilter(InputFilter.LengthFilter(maxLength))
    }

    fun hideBtn() {
        isShowBtn = false
        mBinding.ivShow.gone()
    }

    fun showBtn() {
        isShowBtn = true
        mBinding.ivShow.visible()
    }

    fun addFilter(filter: InputFilter) {
        val filters = Arrays.copyOf(mBinding.etClear.filters, mBinding.etClear.filters.size + 1)
        filters[filters.size - 1] = filter
        mBinding.etClear.filters = filters
    }

    fun addTextChangedListener(onTextChanged: ((s: Editable?) -> Unit)) {
        mBinding.etClear.addTextChangedListener { onTextChanged.invoke(it) }
    }

    fun setOnFocusChangeListener(onFocusChange: ((v: View?, hasFocus: Boolean?) -> Unit)) {
        mBinding.etClear.onFocusChangeListener = OnFocusChangeListener { v, hasFocus -> onFocusChange.invoke(v, hasFocus) }
    }

    fun setOnEditorActionListener(listener: TextView.OnEditorActionListener) {
        mBinding.etClear.setOnEditorActionListener(listener)
    }

}