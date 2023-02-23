package com.example.common.widget.textview

import android.content.Context
import android.text.Spannable
import android.util.AttributeSet
import android.util.TypedValue
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.StringRes
import com.example.common.R
import com.example.common.databinding.ViewCheckLabelBinding
import com.example.common.utils.function.pt
import com.example.framework.utils.function.dimen
import com.example.framework.utils.function.inflate
import com.example.framework.utils.function.view.*
import com.example.framework.widget.BaseViewGroup

/**
 * @description 自定义选框容器
 * @author yan
 */
class CheckBoxLabel @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : BaseViewGroup(context, attrs, defStyleAttr) {
    private val binding by lazy { ViewCheckLabelBinding.bind(context.inflate(R.layout.view_check_label)) }

    init {
        if (attrs != null) {
            val typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.CheckBoxLabel)
            //选框大小
            val checkSize = typedArray.getInt(R.styleable.CheckBoxLabel_checkSize, 20.pt)
            binding.ck.size(checkSize, checkSize)
            //选框样式
            val checkStyle = typedArray.getInt(R.styleable.CheckBoxLabel_checkStyle, R.style.CheckBoxStyle)
            binding.ck.setTextAppearance(getContext(), checkStyle)
            //文字距左侧边距
            val marginChild = typedArray.getInt(R.styleable.CheckBoxLabel_marginChild, 5.pt)
            binding.tvLabel.margin(start = marginChild)
            //文本内容
            val text = typedArray.getResourceId(R.styleable.CheckBoxLabel_text, -1)
            if (text != -1) setText(text)
            //文字大小
            val textSize = typedArray.getDimension(R.styleable.CheckBoxLabel_textSize, context.dimen(R.dimen.textSize14))
            setTextSize(textSize)
            //文字颜色
            val textColor = typedArray.getColor(R.styleable.CheckBoxLabel_textColor, color(R.color.textPrimary))
            setTextColor(textColor)
            typedArray.recycle()
            //全局范围内点击都会让checkbox选中或未选中
            binding.clContainer.click { binding.ck.checked() }
        }
    }

    override fun onInflateView() {
        if (isInflate()) addView(binding.root)
    }

    fun setText(@StringRes resid: Int) {
        binding.tvLabel.setText(resid)
    }

    fun setText(text: String) {
        binding.tvLabel.text = text
    }

    fun setText(text: Spannable) {
        binding.tvLabel.text = text
    }

    fun getText(): String {
        return binding.tvLabel.let { if (it.text == null) "" else it.text.toString() }
    }

    fun setTextSize(size: Float) {
        setTextSize(size, TypedValue.COMPLEX_UNIT_PX)
    }

    fun setTextSize(size: Float, unit: Int) {
        binding.tvLabel.setTextSize(unit, size)
    }

    fun setTextColor(@ColorInt color: Int) {
        binding.tvLabel.setTextColor(color)
    }

    /**
     * 当前是否选择
     */
    fun isChecked(): Boolean {
        return binding.ck.isChecked
    }

    /**
     * 获取textview，可能textview本身内部文案需要做点击
     */
    fun getTextView(): TextView {
        return binding.tvLabel
    }

}