package com.example.common.widget.textview.edittext

import android.content.Context
import android.text.Editable
import android.text.InputFilter
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.DimenRes
import androidx.annotation.StringRes
import androidx.core.content.withStyledAttributes
import androidx.core.widget.addTextChangedListener
import androidx.core.widget.doAfterTextChanged
import com.example.common.R
import com.example.common.databinding.ViewPasswordEditBinding
import com.example.framework.utils.function.inflate
import com.example.framework.utils.function.view.click
import com.example.framework.utils.function.view.color
import com.example.framework.utils.function.view.dimen
import com.example.framework.utils.function.view.emojiLimit
import com.example.framework.utils.function.view.gone
import com.example.framework.utils.function.view.imeOptions
import com.example.framework.utils.function.view.padding
import com.example.framework.utils.function.view.paddingAll
import com.example.framework.utils.function.view.paddingLtrb
import com.example.framework.utils.function.view.setResource
import com.example.framework.utils.function.view.textSize
import com.example.framework.utils.function.view.togglePasswordVisibility
import com.example.framework.utils.function.view.visible
import com.example.framework.widget.BaseViewGroup

/**
 * @description 密码显影输入框
 * @author yan
 */
class PasswordEditText @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : BaseViewGroup(context, attrs, defStyleAttr), SpecialEditText {
    private var showRes = -1
    private var hideRes = -1
    private var isShowBtn = true
    private val binding by lazy { ViewPasswordEditBinding.bind(context.inflate(R.layout.view_password_edit)) }
    val editText get() = binding.etClear

    init {
        binding.etClear.apply {
            emojiLimit()
            setOnKeyListener { _, keyCode, _ ->
                if (keyCode == KeyEvent.KEYCODE_DEL) {
                    binding.etClear.setPlainText("")
                }
                false
            }
        }
        binding.ivShow.click {
            binding.ivShow.setResource(Triple(binding.etClear.togglePasswordVisibility(), showRes, hideRes))
        }
        // 以下属性在xml中前缀使用app:调取
        context.withStyledAttributes(attrs, R.styleable.PasswordEditText) {
            // 文本内容
            val text = getResourceId(R.styleable.PasswordEditText_text, -1)
            if (text != -1) setText(text)
            // 文字大小
            val textSize = getDimension(R.styleable.PasswordEditText_textSize, dimen(R.dimen.textSize14))
            setTextSize(textSize)
            // 文字颜色
            val textColor = getColor(R.styleable.PasswordEditText_textColor, color(R.color.textPrimary))
            setTextColor(textColor)
            // 无内容显示的文本内容
            val hint = getResourceId(R.styleable.PasswordEditText_hint, -1)
            if (hint != -1) setHint(hint)
            // 无为内容显示的文本内容颜色
            val hintColor = getColor(R.styleable.PasswordEditText_textColorHint, color(R.color.textHint))
            setHintTextColor(hintColor)
            // 文本方向
            val gravity = getInt(R.styleable.PasswordEditText_gravity, Gravity.CENTER_VERTICAL or Gravity.START)
            setGravity(gravity)
            // 睁眼闭眼图片资源
            hideRes = getResourceId(R.styleable.PasswordEditText_btnImageHide, R.mipmap.ic_password_hide)
            showRes = getResourceId(R.styleable.PasswordEditText_btnImageShow, R.mipmap.ic_password_show)
            // 文案最大范围
            val maxLength = getInt(R.styleable.PasswordEditText_maxLength, -1)
            if (maxLength != -1) setMaxLength(maxLength)
            // 配置输入法右下角按钮的样式
            val imeOptions = getInt(R.styleable.PasswordEditText_imeOptions, 0)
            binding.etClear.imeOptions(imeOptions)
            // 内部容器修正
            val (resolvedStart, resolvedTop, resolvedEnd, resolvedBottom) = paddingLtrb()
            if (resolvedStart == 0  && resolvedTop == 0 && resolvedEnd == 0 &&  resolvedBottom == 0) return@withStyledAttributes
            // 撑满父容器
            paddingAll(0)
            // 子容器添加padding
            binding.root.padding(resolvedStart, resolvedTop, resolvedEnd, resolvedBottom)
        }
    }

    override fun onInflate() {
        if (shouldInflate) addView(binding.root)
    }

    fun setText(@StringRes resid: Int) {
        binding.etClear.setI18nRes(resid)
    }

    fun setTextString(text: String) {
        binding.etClear.setPlainText(text)
    }

    fun getText(): String {
        return binding.etClear.let { if (it.text == null) "" else it.text.toString() }
    }

    fun setTextSize(@DimenRes res: Int) {
        binding.etClear.textSize(res)
    }

    fun setTextSize(size: Float, unit: Int = TypedValue.COMPLEX_UNIT_PX) {
        binding.etClear.setTextSize(unit, size)
    }

    fun setTextColor(@ColorInt color: Int) {
        binding.etClear.setTextColor(color)
    }

    fun setHint(@StringRes resid: Int) {
        binding.etClear.setI18nHintRes(resid)
    }

    fun setHintString(text: String) {
        binding.etClear.setHintText(text)
    }

    fun setHintTextColor(@ColorInt color: Int) {
        binding.etClear.setHintTextColor(color)
    }

    fun setSelection(mCursor: Int) {
        binding.etClear.setSelection(mCursor)
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

    fun addFilter(filter: InputFilter) {
        val filters = binding.etClear.filters.copyOf(binding.etClear.filters.size + 1)
        filters[filters.size - 1] = filter
        binding.etClear.filters = filters
    }

    fun addTextChangedListener(listener: ((s: Editable?) -> Unit)) {
        binding.etClear.addTextChangedListener {
            listener.invoke(it)
        }
    }

    fun doAfterTextChanged(listener: ((s: Editable?) -> Unit)) {
        binding.etClear.doAfterTextChanged {
            listener.invoke(it)
        }
    }

    fun setOnFocusChangeListener(listener: ((v: View, hasFocus: Boolean) -> Unit)) {
        binding.etClear.onFocusChangeListener = OnFocusChangeListener { v, hasFocus ->
            listener.invoke(v, hasFocus)
        }
    }

    fun setOnEditorActionListener(listener: (v: TextView, actionId: Int, event: KeyEvent) -> Boolean) {
        binding.etClear.setOnEditorActionListener { textView, actionId, event ->
            listener(textView, actionId, event)
        }
    }

}