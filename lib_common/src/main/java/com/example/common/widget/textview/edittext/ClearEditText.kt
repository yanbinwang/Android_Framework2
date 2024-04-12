package com.example.common.widget.textview.edittext

import android.annotation.SuppressLint
import android.content.Context
import android.text.Editable
import android.text.InputFilter
import android.text.InputFilter.LengthFilter
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.View.OnFocusChangeListener
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.widget.addTextChangedListener
import com.example.common.R
import com.example.common.databinding.ViewClearEditBinding
import com.example.common.utils.function.ptFloat
import com.example.common.widget.textview.SpecialEditText
import com.example.framework.utils.function.dimen
import com.example.framework.utils.function.inflate
import com.example.framework.utils.function.view.background
import com.example.framework.utils.function.view.click
import com.example.framework.utils.function.view.color
import com.example.framework.utils.function.view.emojiLimit
import com.example.framework.utils.function.view.gone
import com.example.framework.utils.function.view.imeOptions
import com.example.framework.utils.function.view.inputType
import com.example.framework.utils.function.view.textColor
import com.example.framework.utils.function.view.visible
import com.example.framework.widget.BaseViewGroup
import java.util.Arrays

/**
 * @description 带删除按钮的输入框
 * @author yan
 */
@SuppressLint("CustomViewStyleable")
class ClearEditText @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : BaseViewGroup(context, attrs, defStyleAttr), SpecialEditText {
    private var isDisabled = false//是否不可操作
    private var isShowBtn = true//是否显示清除按钮
    private var onTextChanged: ((s: Editable?) -> Unit)? = null
    private var onFocusChange: ((v: View?, hasFocus: Boolean?) -> Unit)? = null
    private val mBinding by lazy { ViewClearEditBinding.bind(context.inflate(R.layout.view_clear_edit)) }
    val editText get() = mBinding.etClear

    init {
        normal()
        mBinding.etClear.apply {
            emojiLimit()
            addTextChangedListener {
                if (isDisabled || !isShowBtn) return@addTextChangedListener
                mBinding.ivClear.visibility = if (it.toString().isEmpty()) View.GONE else View.VISIBLE
                onTextChanged?.invoke(it)
            }
            onFocusChangeListener = OnFocusChangeListener { v, hasFocus ->
                if (hasFocus) focused() else normal()
                onFocusChange?.invoke(v, hasFocus)
            }
        }
        mBinding.ivClear.click { mBinding.etClear.setTextString("") }
        //以下属性在xml中前缀使用app:调取
        if (attrs != null) {
            val typedArray = context.obtainStyledAttributes(attrs, R.styleable.ClearEditText)
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
                mBinding.etClear.isSingleLine = false
            } else {
                mBinding.etClear.isSingleLine = true
                mBinding.etClear.maxLines = 1
            }
            if (minLine > 0) mBinding.etClear.minLines = minLine
            if (maxLine > 0) mBinding.etClear.maxLines = maxLine
            val minHeight = typedArray.getDimension(R.styleable.ClearEditText_android_minHeight, 40.ptFloat)
            mBinding.etClear.minHeight = minHeight.toInt()
            //当前控件是否可用
            val disabled = typedArray.getBoolean(R.styleable.ClearEditText_disabled, false)
            if (disabled) setDisabled()
            //配置文案输入的格式
            val inputType = typedArray.getInt(R.styleable.ClearEditText_inputType, 0)
            mBinding.etClear.inputType(inputType)
            //配置输入法右下角按钮的样式
            val imeOptions = typedArray.getInt(R.styleable.ClearEditText_imeOptions, 0)
            mBinding.etClear.imeOptions(imeOptions)
            typedArray.recycle()
        }
    }

    override fun onInflate() {
        if (isInflate) addView(mBinding.root)
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
        mBinding.etClear.apply {
            isCursorVisible = false
            isFocusable = false
            isEnabled = false
            isFocusableInTouchMode = false
            textColor(R.color.textDisabled)
        }
        mBinding.ivClear.gone()
    }

    private fun setEnabled() {
        isDisabled = false
        isShowBtn = false
        mBinding.etClear.apply {
            isCursorVisible = true
            isFocusable = true
            isEnabled = true
            isFocusableInTouchMode = true
            textColor(R.color.textPrimary)
        }
        mBinding.ivClear.visible()
    }

    fun setText(@StringRes resid: Int) {
        if (isInEditMode) {
            mBinding.etClear.setText(resid)
        } else {
            mBinding.etClear.setI18nRes(resid)
        }
//        mBinding.etClear.setI18nRes(resid)
    }

    fun setTextString(text: String) {
        mBinding.etClear.setTextString(text)
//        mBinding.etClear.setText(text)
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
        if (isInEditMode) {
            mBinding.etClear.setHint(resid)
        } else {
            mBinding.etClear.setI18nHintRes(resid)
        }
//        mBinding.etClear.setHint(resid)
    }

    fun setHintString(text: String) {
        if (isInEditMode) {
            mBinding.etClear.setHint(text)
        } else {
            mBinding.etClear.setHintString(text)
        }
//        mBinding.etClear.hint = text
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

    fun setInputType(type: Int) {
        mBinding.etClear.inputType = type
    }

    fun setImageResource(@DrawableRes res: Int) {
        mBinding.ivClear.setImageResource(res)
    }

    fun setMaxLength(maxLength: Int) {
        addFilter(LengthFilter(maxLength))
    }

    fun hideBtn() {
        isShowBtn = false
        mBinding.ivClear.gone()
    }

    fun showBtn() {
        isShowBtn = true
        mBinding.etClear.apply { if (text.toString().isNotEmpty()) visible() }
    }

    fun normal() {
//        mBinding.root.background(R.drawable.shape_input)
    }

    fun focused() {
//        mBinding.root.background(R.drawable.shape_input_focused)
    }

    fun error() {
//        mBinding.root.background(R.drawable.shape_input_error)
    }

    fun addFilter(filter: InputFilter) {
        val filters = Arrays.copyOf(mBinding.etClear.filters, mBinding.etClear.filters.size + 1)
        filters[filters.size - 1] = filter
        mBinding.etClear.filters = filters
    }

    fun addTextChangedListener(onTextChanged: ((s: Editable?) -> Unit)) {
        this.onTextChanged = onTextChanged
    }

    fun setOnFocusChangeListener(onFocusChange: ((v: View?, hasFocus: Boolean?) -> Unit)) {
        this.onFocusChange = onFocusChange
    }

}