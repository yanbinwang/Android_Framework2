package com.example.common.widget.textview.edittext

import android.content.Context
import android.text.Editable
import android.text.InputFilter
import android.text.InputFilter.LengthFilter
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.DimenRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.content.withStyledAttributes
import androidx.core.widget.addTextChangedListener
import androidx.core.widget.doAfterTextChanged
import com.example.common.R
import com.example.common.databinding.ViewClearEditBinding
import com.example.common.utils.function.pt
import com.example.common.utils.function.ptFloat
import com.example.framework.utils.function.inflate
import com.example.framework.utils.function.view.click
import com.example.framework.utils.function.view.color
import com.example.framework.utils.function.view.dimen
import com.example.framework.utils.function.view.emojiLimit
import com.example.framework.utils.function.view.gone
import com.example.framework.utils.function.view.imeOptions
import com.example.framework.utils.function.view.inputType
import com.example.framework.utils.function.view.padding
import com.example.framework.utils.function.view.paddingAll
import com.example.framework.utils.function.view.paddingLtrb
import com.example.framework.utils.function.view.textColor
import com.example.framework.utils.function.view.textSize
import com.example.framework.utils.function.view.visible
import com.example.framework.widget.BaseViewGroup

/**
 * @description 带删除按钮的输入框
 * @author yan
 */
class ClearEditText @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : BaseViewGroup(context, attrs, defStyleAttr), SpecialEditText {
    private var isDisabled = false // 是否不可操作
    private var isShowBtn = true // 是否可操作清除按钮
    private var onTextChanged: ((s: Editable?) -> Unit)? = null
    private val binding by lazy { ViewClearEditBinding.bind(context.inflate(R.layout.view_clear_edit)) }
    val editText get() = binding.etClear

    init {
        binding.etClear.apply {
            emojiLimit()
            addTextChangedListener {
                if (isDisabled || !isShowBtn) return@addTextChangedListener
                binding.ivClear.visibility = if (it.isNullOrEmpty()) GONE else VISIBLE
                onTextChanged?.invoke(it)
            }
        }
        binding.ivClear.click {
            if (isShowBtn) {
                binding.etClear.setPlainText("")
            }
        }
        // 以下属性在xml中前缀使用app:调取
        context.withStyledAttributes(attrs, R.styleable.ClearEditText) {
            // 文本内容
            val text = getResourceId(R.styleable.ClearEditText_text, -1)
            if (text != -1) setText(text)
            // 文字大小
            val textSize = getDimension(R.styleable.ClearEditText_textSize, dimen(R.dimen.textSize14))
            setTextSize(textSize)
            // 文字颜色
            val textColor = getColor(R.styleable.ClearEditText_textColor, color(R.color.textPrimary))
            setTextColor(textColor)
            // 无内容显示的文本内容
            val hint = getResourceId(R.styleable.ClearEditText_hint, -1)
            if (hint != -1) setHint(hint)
            // 无为内容显示的文本内容颜色
            val hintColor = getColor(R.styleable.ClearEditText_textColorHint, color(R.color.textHint))
            setHintTextColor(hintColor)
            // 文本方向
            val gravity = getInt(R.styleable.ClearEditText_gravity, Gravity.CENTER_VERTICAL or Gravity.START)
            setGravity(gravity)
            // 清除按钮图片资源
            val clearBtnImage = getResourceId(R.styleable.ClearEditText_clearBtnImage, R.mipmap.ic_clear)
            setImageResource(clearBtnImage)
            // 文案最大范围
            val maxLength = getInt(R.styleable.ClearEditText_maxLength, -1)
            if (maxLength != -1) setMaxLength(maxLength)
            // 最小和最大函数，不设置默认单行
            val minLine = getInt(R.styleable.ClearEditText_minLine, -1)
            val maxLine = getInt(R.styleable.ClearEditText_maxLine, -1)
            if (minLine > 0 || maxLine > 0) {
                binding.etClear.isSingleLine = false
                binding.etClear.setPaddingRelative(0, 10.pt, 0, 10.pt)
            } else {
                binding.etClear.isSingleLine = true
                binding.etClear.maxLines = 1
            }
            if (minLine > 0) binding.etClear.minLines = minLine
            if (maxLine > 0) binding.etClear.maxLines = maxLine
            val minHeight = getDimension(R.styleable.ClearEditText_android_minHeight, 40.ptFloat)
            binding.etClear.minHeight = minHeight.toInt()
            // 当前控件是否可用
            val disabled = getBoolean(R.styleable.ClearEditText_disabled, false)
            if (disabled) setDisabled()
            // 配置文案输入的格式
            val inputType = getInt(R.styleable.ClearEditText_inputType, 0)
            binding.etClear.inputType(inputType)
            // 配置输入法右下角按钮的样式
            val imeOptions = getInt(R.styleable.ClearEditText_imeOptions, 0)
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

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        if (enabled) {
            setEnabled()
        } else {
            setDisabled()
        }
    }

    private fun setEnabled() {
        isDisabled = false
        isShowBtn = true
        binding.etClear.apply {
            isCursorVisible = true
            isFocusable = true
            isEnabled = true
            isFocusableInTouchMode = true
            textColor(R.color.textPrimary)
        }
        showBtn()
    }

    private fun setDisabled() {
        isDisabled = true
        isShowBtn = false
        binding.etClear.apply {
            isCursorVisible = false
            isFocusable = false
            isEnabled = false
            isFocusableInTouchMode = false
            textColor(R.color.textDisabled)
        }
        hideBtn()
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

    fun setInputType(type: Int) {
        binding.etClear.inputType = type
    }

    fun setImageResource(@DrawableRes res: Int) {
        binding.ivClear.setImageResource(res)
    }

    fun setMaxLength(maxLength: Int) {
        addFilter(LengthFilter(maxLength))
    }

    fun hideBtn() {
        isShowBtn = false
        binding.ivClear.gone()
    }

    fun showBtn() {
        isShowBtn = true
        binding.ivClear.visibility = if (binding.etClear.text.isNullOrEmpty()) GONE else VISIBLE
    }

    fun addFilter(filter: InputFilter) {
        val filters = binding.etClear.filters.copyOf(binding.etClear.filters.size + 1)
        filters[filters.size - 1] = filter
        binding.etClear.filters = filters
    }

    fun addTextChangedListener(listener: ((s: Editable?) -> Unit)) {
        this.onTextChanged = listener
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