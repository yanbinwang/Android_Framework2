package com.example.common.widget.textview.edit

import android.content.Context
import android.text.InputFilter
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.KeyEvent
import androidx.annotation.ColorInt
import androidx.annotation.StringRes
import com.example.common.R
import com.example.common.databinding.ViewPasswordEditBinding
import com.example.framework.utils.function.dimen
import com.example.framework.utils.function.inflate
import com.example.framework.utils.function.view.click
import com.example.framework.utils.function.view.color
import com.example.framework.utils.function.view.emojiLimit
import com.example.framework.utils.function.view.gone
import com.example.framework.utils.function.view.imeOptions
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
    private var isShowBtn = true
    private var hideRes = -1
    private var showRes = -1
    private val binding by lazy { ViewPasswordEditBinding.bind(context.inflate(R.layout.view_password_edit)) }
    val editText get() = binding.etClear

    init {
        binding.etClear.emojiLimit()
        binding.etClear.setOnKeyListener { _, keyCode, _ ->
            if (keyCode == KeyEvent.KEYCODE_DEL) binding.etClear.setText("")
            false
        }
        binding.ivShow.apply { click { setResource(Triple(binding.etClear.passwordDevelopment(), showRes, hideRes)) }}
        //以下属性在xml中前缀使用app:调取
        if (attrs != null) {
            val typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.PasswordEditText)
            //文本内容
            val text = typedArray.getResourceId(R.styleable.PasswordEditText_text, -1)
            if (text != -1) setText(text)
            //文字大小
            val textSize = typedArray.getDimension(R.styleable.PasswordEditText_textSize, context.dimen(R.dimen.textSize14))
            setTextSize(textSize)
            //文字颜色
            val textColor = typedArray.getColor(R.styleable.PasswordEditText_textColor, color(R.color.textPrimary))
            setTextColor(textColor)
            //无内容显示的文本内容
            val hint = typedArray.getResourceId(R.styleable.PasswordEditText_hint, -1)
            if (hint != -1) setHint(hint)
            //无为内容显示的文本内容颜色
            val hintColor = typedArray.getColor(R.styleable.PasswordEditText_textColorHint, color(R.color.textHint))
            setHintTextColor(hintColor)
            //文本方向
            val gravity = typedArray.getInt(R.styleable.PasswordEditText_gravity, Gravity.CENTER_VERTICAL or Gravity.START)
            setGravity(gravity)
            //睁眼闭眼图片资源
            hideRes = typedArray.getResourceId(R.styleable.PasswordEditText_btnImageHide, R.mipmap.ic_password_hide)
            showRes = typedArray.getResourceId(R.styleable.PasswordEditText_btnImageShow, R.mipmap.ic_password_show)
            //文案最大范围
            val maxLength = typedArray.getInt(R.styleable.PasswordEditText_maxLength, -1)
            if (maxLength != -1) setMaxLength(maxLength)
            //配置输入法右下角按钮的样式
            val imeOptions = typedArray.getInt(R.styleable.PasswordEditText_imeOptions, 0)
            binding.etClear.imeOptions(imeOptions)
            typedArray.recycle()
        }
    }

    override fun onInflateView() {
        if (isInflate()) addView(binding.root)
    }

    fun setText(@StringRes resid: Int) {
        binding.etClear.setText(resid)
    }

    fun setText(text: String) {
        binding.etClear.setText(text)
    }

    fun getText(): String {
        return binding.etClear.let { if (it.text == null) "" else it.text.toString() }
    }

    fun setTextSize(size: Float) {
        setTextSize(size, TypedValue.COMPLEX_UNIT_PX)
    }

    fun setTextSize(size: Float, unit: Int) {
        binding.etClear.setTextSize(unit, size)
    }

    fun setTextColor(@ColorInt color: Int) {
        binding.etClear.setTextColor(color)
    }

    fun setHint(@StringRes resid: Int) {
        binding.etClear.setHint(resid)
    }

    fun setHint(text: String) {
        binding.etClear.hint = text
    }

    fun setHintTextColor(@ColorInt color: Int) {
        binding.etClear.setHintTextColor(color)
    }

    fun setSelection(mCursor: Int) {
        binding.etClear.setSelection(mCursor)
    }

    fun addFilter(filter: InputFilter) {
        val filters = Arrays.copyOf(binding.etClear.filters, binding.etClear.filters.size + 1)
        filters[filters.size - 1] = filter
        binding.etClear.filters = filters
    }

    fun setGravity(gravity: Int) {
        binding.etClear.gravity = gravity
    }

    fun setMaxLength(maxLength: Int) {
        addFilter(InputFilter.LengthFilter(maxLength))
    }

    fun hideBtn() {
        isShowBtn = false
        binding.ivShow.gone()
    }

    fun showBtn() {
        isShowBtn = true
        binding.ivShow.visible()
    }

}

fun PasswordEditText?.text(): String {
    this ?: return ""
    return getText()
}