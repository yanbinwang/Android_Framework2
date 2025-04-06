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
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.widget.addTextChangedListener
import com.example.common.R
import com.example.common.databinding.ViewClearEditBinding
import com.example.common.utils.function.pt
import com.example.common.utils.function.ptFloat
import com.example.common.widget.textview.SpecialEditText
import com.example.framework.utils.function.dimen
import com.example.framework.utils.function.inflate
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
import androidx.core.content.withStyledAttributes

/**
 * @description 带删除按钮的输入框
 * @author yan
 */
@SuppressLint("CustomViewStyleable")
class ClearEditText @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : BaseViewGroup(context, attrs, defStyleAttr), SpecialEditText {
    private var isDisabled = false//是否不可操作
    private var isShowBtn = true//是否显示清除按钮
    private var onTextChanged: ((s: Editable?) -> Unit)? = null
    private val mBinding by lazy { ViewClearEditBinding.bind(context.inflate(R.layout.view_clear_edit)) }
    val editText get() = mBinding.etClear

    init {
        mBinding.etClear.apply {
            emojiLimit()
            addTextChangedListener {
                if (isDisabled || !isShowBtn) return@addTextChangedListener
                mBinding.ivClear.visibility = if (it.toString().isEmpty()) View.GONE else View.VISIBLE
                onTextChanged?.invoke(it)
            }
        }
        mBinding.ivClear.click { mBinding.etClear.setTextString("") }
        //以下属性在xml中前缀使用app:调取
        context.withStyledAttributes(attrs, R.styleable.ClearEditText) {
            //文本内容
            val text = getResourceId(R.styleable.ClearEditText_text, -1)
            if (text != -1) setText(text)
            //文字大小
            val textSize = getDimension(R.styleable.ClearEditText_textSize, context.dimen(R.dimen.textSize14))
            setTextSize(textSize)
            //文字颜色
            val textColor = getColor(R.styleable.ClearEditText_textColor, color(R.color.textPrimary))
            setTextColor(textColor)
            //无内容显示的文本内容
            val hint = getResourceId(R.styleable.ClearEditText_hint, -1)
            if (hint != -1) setHint(hint)
            //无为内容显示的文本内容颜色
            val hintColor = getColor(R.styleable.ClearEditText_textColorHint, color(R.color.textHint))
            setHintTextColor(hintColor)
            //文本方向
            val gravity = getInt(R.styleable.ClearEditText_gravity, Gravity.CENTER_VERTICAL or Gravity.START)
            setGravity(gravity)
            //清除按钮图片资源
            val clearBtnImage = getResourceId(R.styleable.ClearEditText_clearBtnImage, R.mipmap.ic_clear)
            setImageResource(clearBtnImage)
            //文案最大范围
            val maxLength = getInt(R.styleable.ClearEditText_maxLength, -1)
            if (maxLength != -1) setMaxLength(maxLength)
            //最小和最大函数，不设置默认单行
            val minLine = getInt(R.styleable.ClearEditText_minLine, -1)
            val maxLine = getInt(R.styleable.ClearEditText_maxLine, -1)
            if (minLine > 0 || maxLine > 0) {
                mBinding.etClear.isSingleLine = false
                mBinding.etClear.setPaddingRelative(0, 10.pt, 0, 10.pt)
            } else {
                mBinding.etClear.isSingleLine = true
                mBinding.etClear.maxLines = 1
            }
            if (minLine > 0) mBinding.etClear.minLines = minLine
            if (maxLine > 0) mBinding.etClear.maxLines = maxLine
            val minHeight = getDimension(R.styleable.ClearEditText_android_minHeight, 40.ptFloat)
            mBinding.etClear.minHeight = minHeight.toInt()
            //当前控件是否可用
            val disabled = getBoolean(R.styleable.ClearEditText_disabled, false)
            if (disabled) setDisabled()
            //配置文案输入的格式
            val inputType = getInt(R.styleable.ClearEditText_inputType, 0)
            mBinding.etClear.inputType(inputType)
            //配置输入法右下角按钮的样式
            val imeOptions = getInt(R.styleable.ClearEditText_imeOptions, 0)
            mBinding.etClear.imeOptions(imeOptions)
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
    }

    fun setHintString(text: String) {
        if (isInEditMode) {
            mBinding.etClear.setHint(text)
        } else {
            mBinding.etClear.setHintString(text)
        }
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

    fun addFilter(filter: InputFilter) {
        val filters = Arrays.copyOf(mBinding.etClear.filters, mBinding.etClear.filters.size + 1)
        filters[filters.size - 1] = filter
        mBinding.etClear.filters = filters
    }

    fun addTextChangedListener(onTextChanged: ((s: Editable?) -> Unit)) {
        this.onTextChanged = onTextChanged
    }

    fun setOnFocusChangeListener(onFocusChange: ((v: View?, hasFocus: Boolean?) -> Unit)) {
        mBinding.etClear.onFocusChangeListener = OnFocusChangeListener { v, hasFocus -> onFocusChange.invoke(v, hasFocus) }
    }

    fun setOnEditorActionListener(listener: TextView.OnEditorActionListener) {
        mBinding.etClear.setOnEditorActionListener(listener)
    }

}