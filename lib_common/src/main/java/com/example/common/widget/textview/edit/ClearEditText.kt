package com.example.common.widget.textview.edit

import android.annotation.SuppressLint
import android.content.Context
import android.text.InputFilter
import android.text.InputFilter.LengthFilter
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.widget.addTextChangedListener
import com.example.common.R
import com.example.common.databinding.ViewClearEditBinding
import com.example.common.utils.function.ptFloat
import com.example.common.widget.textview.edit.callback.SpecialEditTextImpl
import com.example.framework.utils.function.dimen
import com.example.framework.utils.function.inflate
import com.example.framework.utils.function.view.*
import com.example.framework.widget.BaseViewGroup
import java.util.*

/**
 * @description 带删除按钮的输入框
 * @author yan
 */
@SuppressLint("CustomViewStyleable")
class ClearEditTextImpl @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : BaseViewGroup(context, attrs, defStyleAttr),
    SpecialEditTextImpl {
    private var isDisabled = false//是否不可操作
    private var isShowBtn = true//是否显示清除按钮
    private val binding by lazy { ViewClearEditBinding.bind(context.inflate(R.layout.view_clear_edit)) }
    val editText get() = binding.etClear

    init {
        binding.etClear.apply {
            emojiLimit()
            addTextChangedListener {
                if (isDisabled || !isShowBtn) return@addTextChangedListener
                binding.ivClear.visibility = if(it.toString().isEmpty()) View.GONE else View.VISIBLE
            }
        }
        binding.ivClear.click { binding.etClear.setText("") }
        //以下属性在xml中前缀使用app:调取
        if (attrs != null) {
            val typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.ClearEditText)
            //文本内容
            val text = typedArray.getResourceId(R.styleable.ClearEditText_text, -1)
            if (text != -1) setText(text)
            //文字大小
            val textSize = typedArray.getDimension(R.styleable.ClearEditText_textSize, context.dimen(R.dimen.textSize14))
            setTextSize(textSize)
            //文字颜色
            val textColor = typedArray.getColor(R.styleable.ClearEditText_textColor, color(R.color.textPrimary))
            setTextColor(textColor)
            //无内容显示的文本内容
            val hint = typedArray.getResourceId(R.styleable.ClearEditText_hint, -1)
            if (hint != -1) setHint(hint)
            //无为内容显示的文本内容颜色
            val hintColor = typedArray.getColor(R.styleable.ClearEditText_textColorHint, color(R.color.textHint))
            setHintTextColor(hintColor)
            //文本方向
            val gravity = typedArray.getInt(R.styleable.ClearEditText_gravity, Gravity.CENTER_VERTICAL or Gravity.START)
            setGravity(gravity)
            //清除按钮图片资源
            val clearBtnImage = typedArray.getResourceId(R.styleable.ClearEditText_clearBtnImage, R.mipmap.ic_clear)
            setImageResource(clearBtnImage)
            //文案最大范围
            val maxLength = typedArray.getInt(R.styleable.ClearEditText_maxLength, -1)
            if (maxLength != -1) setMaxLength(maxLength)
            //最小和最大函数，不设置默认单行
            val minLine = typedArray.getInt(R.styleable.ClearEditText_minLine, -1)
            val maxLine = typedArray.getInt(R.styleable.ClearEditText_maxLine, -1)
            if (minLine > 0 || maxLine > 0) {
                binding.etClear.isSingleLine = false
            } else {
                binding.etClear.isSingleLine = true
                binding.etClear.maxLines = 1
            }
            if (minLine > 0) binding.etClear.minLines = minLine
            if (maxLine > 0) binding.etClear.maxLines = maxLine
            val minHeight = typedArray.getDimension(R.styleable.ClearEditText_android_minHeight, 40.ptFloat)
            binding.etClear.minHeight = minHeight.toInt()
            //当前控件是否可用
            val disabled = typedArray.getBoolean(R.styleable.ClearEditText_disabled, false)
            if (disabled) setDisabled()
            //配置文案输入的格式
            val inputType = typedArray.getInt(R.styleable.ClearEditText_inputType, 0)
            binding.etClear.inputType(inputType)
            //配置输入法右下角按钮的样式
            val imeOptions = typedArray.getInt(R.styleable.ClearEditText_imeOptions, 0)
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

    fun setImageResource(@DrawableRes res: Int) {
        binding.ivClear.setImageResource(res)
    }

    fun setMaxLength(maxLength: Int) {
        addFilter(LengthFilter(maxLength))
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

    fun hideBtn() {
        isShowBtn = false
        binding.ivClear.gone()
    }

    fun showBtn() {
        isShowBtn = true
        binding.etClear.apply { if (text.isNotEmpty()) visible() }
    }

}

fun ClearEditTextImpl?.text(): String {
    this ?: return ""
    return getText()
}