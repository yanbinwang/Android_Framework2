package com.example.common.widget.edittext

import android.annotation.SuppressLint
import android.content.Context
import android.text.Editable
import android.text.InputFilter
import android.text.InputFilter.LengthFilter
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.widget.EditText
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.example.common.R
import com.example.common.databinding.ViewClearEditBinding
import com.example.common.utils.function.pt
import com.example.common.utils.function.ptFloat
import com.example.framework.utils.emojiLimit
import com.example.framework.utils.function.inflate
import com.example.framework.utils.function.view.*
import com.example.framework.utils.imeOptions
import com.example.framework.utils.inputType
import com.example.framework.widget.BaseViewGroup
import java.util.*

/**
 * @description 带删除按钮的输入框
 * @author yan
 */
@SuppressLint("CustomViewStyleable")
class ClearEditText @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : BaseViewGroup(context, attrs, defStyleAttr) {
    private var isDisabled = false
    private var isShowBtn = true
    private val binding by lazy { ViewClearEditBinding.bind(context.inflate(R.layout.view_clear_edit)) }

    init {
        binding.etClear.emojiLimit()
        binding.etClear.addTextChangedListener(object : OnMultiTextWatcher {
            override fun afterTextChanged(s: Editable) {
                super.afterTextChanged(s)
                if (isDisabled) return
                if (!isShowBtn) return
                if ("" != s.toString()) {
                    binding.ivClear.visible()
                } else {
                    binding.ivClear.gone()
                }
            }
        })
        binding.ivClear.click { binding.etClear.setText("") }
        if (attrs != null) {
            val ta = getContext().obtainStyledAttributes(attrs, R.styleable.ClearBtnEditText)

            val text = ta.getResourceId(R.styleable.ClearBtnEditText_text, -1)
            if (text != -1) setText(text)

            val textSize = ta.getDimension(R.styleable.ClearBtnEditText_textSize, 32.ptFloat)
            setTextSize(textSize)

            val textColor = ta.getColor(R.styleable.ClearBtnEditText_textColor, color(R.color.textPrimary))
            setTextColor(textColor)

            val hint = ta.getResourceId(R.styleable.ClearBtnEditText_hint, -1)
            if (hint != -1) setHint(hint)

            val hintColor = ta.getColor(R.styleable.ClearBtnEditText_textColorHint, color(R.color.textHint))
            setHintTextColor(hintColor)

            val gravity = ta.getInt(R.styleable.ClearBtnEditText_gravity, Gravity.CENTER_VERTICAL or Gravity.START)
            setGravity(gravity)

            val clearBtnImage = ta.getResourceId(R.styleable.ClearBtnEditText_clearBtnImage, R.mipmap.ic_text_clear)
            setImageResource(clearBtnImage)

            val maxLength = ta.getInt(R.styleable.ClearBtnEditText_maxLength, -1)
            if (maxLength != -1) setMaxLength(maxLength)
            val minLine = ta.getInt(R.styleable.ClearBtnEditText_minLine, -1)
            val maxLine = ta.getInt(R.styleable.ClearBtnEditText_maxLine, -1)
            if (minLine > 0 || maxLine > 0) {
                binding.etClear.isSingleLine = false
                binding.etClear.setPaddingRelative(0, 12.pt, 0, 12.pt)
            } else {
                binding.etClear.isSingleLine = true
                binding.etClear.maxLines = 1
            }
            if (minLine > 0) binding.etClear.minLines = minLine
            if (maxLine > 0) binding.etClear.maxLines = maxLine
            val minHeight = ta.getDimension(R.styleable.ClearBtnEditText_android_minHeight, -1f)
            if (minHeight > 0) binding.etClear.minHeight = minHeight.toInt()

            val disabled = ta.getBoolean(R.styleable.ClearBtnEditText_disabled, false)
            if (disabled) setDisabled()

            val inputType = ta.getInt(R.styleable.ClearBtnEditText_inputType, 0)
            binding.etClear.inputType(inputType)

            val imeOptions = ta.getInt(R.styleable.ClearBtnEditText_imeOptions, 0)
            binding.etClear.imeOptions(imeOptions)

            ta.recycle()
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

    fun addFilter(filter: InputFilter) {
        val filters = Arrays.copyOf(binding.etClear.filters, binding.etClear.filters.size + 1)
        filters[filters.size - 1] = filter
        binding.etClear.filters = filters
    }

    fun setGravity(gravity: Int) {
        binding.etClear.gravity = gravity
    }

    fun setImageResource(@DrawableRes res: Int) {
        binding.ivClear.setImageResource(res)
    }

    fun setMaxLength(maxLength: Int) {
        addFilter(LengthFilter(maxLength))
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
        binding.ivClear.gone()
    }

    private fun setEnabled() {
        isDisabled = false
        isShowBtn = false
        binding.etClear.apply {
            isCursorVisible = true
            isFocusable = true
            isEnabled = true
            isFocusableInTouchMode = true
            textColor(R.color.textPrimary)
        }
        binding.ivClear.visible()
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        if (enabled) {
            setEnabled()
        } else {
            setDisabled()
        }
    }

    fun hideBtn() {
        isShowBtn = false
        binding.ivClear.gone()
    }

    fun showBtn() {
        isShowBtn = true
        binding.etClear.apply { if (text.isNotEmpty()) visible() }
    }

    fun getEditText(): EditText {
        return binding.etClear
    }

}