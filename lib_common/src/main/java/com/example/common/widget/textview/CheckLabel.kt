package com.example.common.widget.textview

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.example.common.R
import com.example.common.databinding.ViewCheckLabelBinding
import com.example.common.utils.function.pt
import com.example.framework.utils.function.dimen
import com.example.framework.utils.function.inflate
import com.example.framework.utils.function.view.color
import com.example.framework.utils.function.view.margin
import com.example.framework.utils.function.view.size
import com.example.framework.utils.logWTF
import com.example.framework.widget.BaseViewGroup

/**
 * @description 用於確認協議等自定義控件
 * @author yan
 */
class CheckLabel @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : BaseViewGroup(context, attrs, defStyleAttr) {
    private val binding by lazy { ViewCheckLabelBinding.bind(context.inflate(R.layout.view_check_label)) }

    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.CheckLabel)
        //單選按鈕大小
        val size = typedArray.getInt(R.styleable.CheckLabel_checkSize, 20.pt)
        binding.ckClause.size(size, size)
        //單選按鈕樣式
        val background = typedArray.getResourceId(R.styleable.CheckLabel_checkBackground, R.drawable.selector_check_box)
        setBackground(background)
        //文本距左側邊距
        val marginChild = typedArray.getInt(R.styleable.CheckLabel_labelTextSize, 5.pt)
        binding.tvLabel.margin(marginChild + size)
        //文本内容
        val text = typedArray.getResourceId(R.styleable.CheckLabel_labelText, -1)
        if (text != -1) setText(text)
        //文字大小
        val textSize = typedArray.getDimension(R.styleable.CheckLabel_labelTextSize, context.dimen(R.dimen.textSize14))
        setTextSize(textSize)
        //文字颜色
        val textColor = typedArray.getColor(R.styleable.CheckLabel_labelTextColor, color(R.color.textPrimary))
        setTextColor(textColor)
        typedArray.recycle()
    }

    override fun onInflateView() {
        if (isInflate()) addView(binding.root)
    }

    fun setBackground(@DrawableRes resid: Int) {
        binding.ckClause.setBackgroundResource(resid)
    }

    fun setText(@StringRes resid: Int) {
        binding.tvLabel.setText(resid)
    }

    fun setText(text: String) {
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

}