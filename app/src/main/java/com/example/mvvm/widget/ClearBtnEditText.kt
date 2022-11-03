package com.example.mvvm.widget

import android.content.Context
import android.text.Editable
import android.text.InputFilter
import android.text.InputFilter.LengthFilter
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.widget.EditText
import android.widget.TextView.OnEditorActionListener
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.example.base.utils.EditTextUtil
import com.example.base.utils.function.inflate
import com.example.base.utils.function.view.color
import com.example.base.utils.function.view.gone
import com.example.base.utils.function.view.visible
import com.example.base.widget.BaseViewGroup
import com.example.common.utils.dp
import com.example.mvvm.R
import com.example.mvvm.databinding.ViewClearEditBinding
import java.util.*

class ClearBtnEditText @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : BaseViewGroup(context, attrs, defStyleAttr) {
    private val binding by lazy { ViewClearEditBinding.bind(context.inflate(R.layout.view_clear_edit)) }
    private var isDisabled = false
    private var isShowBtn = true
    private var onTextEmptyListener: OnTextEmptyListener? = null

    init {
        EditTextUtil.setEmojiLimit(binding.etClear)
        binding.etClear.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                if (isDisabled) return
                if (!isShowBtn) return
                if (s != null && "" != s.toString()) {
                    binding.ivClear.visible()
                } else {
                    onTextEmptyListener?.onEmpty()
                    binding.ivClear.gone()
                }
            }
        })
        if (attrs != null) {
            val typedArray = context.obtainStyledAttributes(attrs, R.styleable.ClearBtnEditText)

            val text = typedArray.getResourceId(R.styleable.ClearBtnEditText_text, -1)
            if (text != -1) setText(text)

            val hint = typedArray.getResourceId(R.styleable.ClearBtnEditText_hint, -1)
            if (hint != -1) setHint(hint)

            val textSize = typedArray.getDimension(R.styleable.ClearBtnEditText_textSize, 14f)
            setTextSize(textSize)

            val maxLength = typedArray.getInt(R.styleable.ClearBtnEditText_maxLength, -1)
            if (maxLength != -1) setMaxLength(maxLength)

            val textColor = typedArray.getColor(R.styleable.ClearBtnEditText_textColor, color(R.color.black_111b34))
            setTextColor(textColor)

            val hintColor = typedArray.getColor(R.styleable.ClearBtnEditText_textColorHint, color(R.color.grey_c5cad5))
            setHintTextColor(hintColor)

            val disabled = typedArray.getBoolean(R.styleable.ClearBtnEditText_disabled, false)
            if (disabled) setDisabled()

            val gravity = typedArray.getInt(R.styleable.ClearBtnEditText_gravity, Gravity.CENTER_VERTICAL or Gravity.START)
            setGravity(gravity)

            val clearBtnImage = typedArray.getResourceId(R.styleable.ClearBtnEditText_clearBtnImage, R.mipmap.ic_text_clear)
            setImageResource(clearBtnImage)

            val minLine = typedArray.getInt(R.styleable.ClearBtnEditText_minLine, -1)
            val maxLine = typedArray.getInt(R.styleable.ClearBtnEditText_maxLine, -1)
            if (minLine > 0 || maxLine > 0) {
                binding.etClear.isSingleLine = false
                binding.etClear.setPaddingRelative(0, 12.dp, 0, 12.dp)
            } else {
                binding.etClear.isSingleLine = true
                binding.etClear.maxLines = 1
            }
            if (minLine > 0) binding.etClear.minLines = minLine
            if (maxLine > 0) binding.etClear.maxLines = maxLine
            val minHeight = typedArray.getDimension(R.styleable.ClearBtnEditText_android_minHeight, -1f)
            if (minHeight > 0) binding.etClear.minHeight = minHeight.toInt()

            EditTextUtil.apply {
                setInputType(binding.etClear, typedArray.getInt(R.styleable.ClearBtnEditText_inputType, 0))
                setImeOptions(binding.etClear, typedArray.getInt(R.styleable.ClearBtnEditText_imeOptions, 0))
            }

            typedArray.recycle()
        }
    }

    override fun onDrawView() {
        if (onFinishView()) addView(binding.root)
    }

    fun getEditText(): EditText {
        return binding.etClear
    }

    fun setText(@StringRes string: Int) {
        binding.etClear.setText(string)
    }

    fun setHint(@StringRes string: Int) {
        binding.etClear.setHint(string)
    }

    fun setHintTextColor(@ColorInt color: Int) {
        binding.etClear.setHintTextColor(color)
    }

    fun setInputType(type: Int) {
        binding.etClear.inputType = type
    }

    fun setTextColor(@ColorInt color: Int) {
        binding.etClear.setTextColor(color)
    }

    override fun setBackgroundColor(@ColorInt color: Int) {
        binding.etClear.setBackgroundColor(color)
    }

    //TypedValue
    //COMPLEX_UNIT_PX  COMPLEX_UNIT_DIP  COMPLEX_UNIT_SP  COMPLEX_UNIT_PT  COMPLEX_UNIT_IN  COMPLEX_UNIT_MM
    fun setTextSize(size: Float, unit: Int) {
        binding.etClear.setTextSize(unit, size)
    }

    fun setTextSize(size: Float) {
        binding.etClear.setTextSize(TypedValue.COMPLEX_UNIT_PX, size)
    }

    fun setGravity(gravity: Int) {
        binding.etClear.gravity = gravity
    }

    fun setImeOptions(imeOptions: Int) {
        binding.etClear.imeOptions = imeOptions
    }

    fun setImageResource(@DrawableRes res: Int) {
        binding.ivClear.setImageResource(res)
    }

    fun setMaxLength(maxLength: Int) {
        addFilter(LengthFilter(maxLength))
    }

    fun setFilter(filter: InputFilter) {
        binding.etClear.filters = arrayOf(filter)
    }

    fun addFilter(filter: InputFilter) {
        val filters: Array<InputFilter> = Arrays.copyOf(binding.etClear.filters, binding.etClear.filters.size + 1)
        filters[filters.size - 1] = filter
        binding.etClear.filters = filters
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        if (enabled) {
            setEnabled()
        } else {
            setDisabled()
        }
    }

    private fun setDisabled() {
        binding.etClear.isCursorVisible = false
        binding.etClear.isFocusable = false
        binding.etClear.isEnabled = false
        binding.etClear.isFocusableInTouchMode = false
        isDisabled = true
        isShowBtn = false
        binding.etClear.setTextColor(color(R.color.black_111b34))
        binding.ivClear.gone()
    }

    private fun setEnabled() {
        binding.etClear.isCursorVisible = true
        binding.etClear.isFocusable = true
        binding.etClear.isEnabled = true
        binding.etClear.isFocusableInTouchMode = true
        isDisabled = false
        isShowBtn = false
        binding.etClear.setTextColor(color(R.color.black_111b34))
        binding.ivClear.visible()
    }

    fun hideBtn() {
        binding.ivClear.gone()
        isShowBtn = false
    }

    fun showBtn() {
        isShowBtn = true
        if (binding.etClear.text.isNotEmpty()) {
            binding.ivClear.visible()
        }
    }

    fun setBtnWidth(width: Int) {
        binding.ivClear.layoutParams.width = width.dp
    }

    fun addTextChangedListener(textWatcher: TextWatcher?) {
        binding.etClear.addTextChangedListener(textWatcher)
    }

    fun setOnEditorActionListener(listener: OnEditorActionListener?) {
        binding.etClear.setOnEditorActionListener(listener)
    }

    fun setBtnOnClickListener(listener: OnClickListener?) {
        binding.ivClear.setOnClickListener(listener)
    }

    interface OnTextEmptyListener {
        fun onEmpty()
    }

}