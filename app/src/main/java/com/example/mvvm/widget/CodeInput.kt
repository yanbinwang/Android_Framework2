package com.example.mvvm.widget

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.KeyEvent
import android.widget.EditText
import android.widget.LinearLayout
import androidx.core.content.withStyledAttributes
import androidx.core.view.size
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.LifecycleOwner
import com.example.common.utils.function.ptFloat
import com.example.framework.utils.function.value.toSafeInt
import com.example.framework.utils.function.view.background
import com.example.framework.utils.function.view.color
import com.example.framework.utils.function.view.dimen
import com.example.framework.utils.function.view.doOnceAfterLayout
import com.example.framework.utils.function.view.inputType
import com.example.framework.utils.function.view.margin
import com.example.framework.utils.function.view.maxLimit
import com.example.framework.utils.function.view.paddingAll
import com.example.framework.utils.function.view.showInput
import com.example.framework.utils.function.view.size
import com.example.mvvm.R

/**
 * 自动编排密码输入框
 */
class CodeInput @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : LinearLayout(context, attrs, defStyleAttr) {
    // 输入框数量
    private var boxCount = 4
    // 未聚焦时的背景资源
    private var backgroundNormalRes = R.drawable.shape_code
    // 聚焦时的背景资源
    private var backgroundFocusedRes = R.drawable.shape_code_pressed
    // 输入完成监听器
    private var listener: ((content: String) -> Unit)? = null

    init {
        // 以下属性在xml中前缀使用app:调取
        context.withStyledAttributes(attrs, R.styleable.CodeInput) {
            // 文字大小 -> setTextSize(size, TypedValue.COMPLEX_UNIT_PX)
            val textSize = getDimension(R.styleable.CodeInput_textSize, dimen(R.dimen.textSize14))
            // 文字颜色 -> setTextColor(color)
            val textColor = getColor(R.styleable.CodeInput_textColor, color(R.color.textPrimary))
            // 配置文案输入的格式
            val inputType = getInt(R.styleable.CodeInput_inputType, 0)
            // 输入框宽/高
            val boxWidth = getDimension(R.styleable.CodeInput_boxWidth, 60.ptFloat)
            val boxHeight = getDimension(R.styleable.CodeInput_boxHeight, 60.ptFloat)
            // 内边距
            val itemPadding = getDimension(R.styleable.CodeInput_itemPadding, 0f)
            // 间距
            val itemSpacing = getDimension(R.styleable.CodeInput_itemSpacing, 5.ptFloat)
            // 输入框数量
            boxCount = getInt(R.styleable.CodeInput_boxCount, 4)
            // 选中/未选中图片资源
            backgroundNormalRes = getResourceId(R.styleable.CodeInput_boxBackgroundNormal, R.drawable.shape_code)
            backgroundFocusedRes = getResourceId(R.styleable.CodeInput_boxBackgroundFocused, R.drawable.shape_code_pressed)
            // 初始化输入框监听
            val onKeyListener = OnKeyListener { _, keyCode, _ ->
                if (keyCode == KeyEvent.KEYCODE_DEL) {
                    handleBackspace()
                }
                false
            }
            val onFocusChangeListener = OnFocusChangeListener { v, hasFocus ->
                // 正常情况才设置背景
                updateBoxBackground(v as? EditText, hasFocus)
            }
            for (i in 0 until boxCount) {
                EditText(context).apply {
                    setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize)
                    setTextColor(textColor)
                    inputType(inputType)
                    size(boxWidth.toSafeInt(), boxHeight.toSafeInt())
                    setGravity(Gravity.CENTER)
                    paddingAll(itemPadding.toSafeInt())
                    maxLimit(1)
                    setOnKeyListener(onKeyListener)
                    setOnFocusChangeListener(onFocusChangeListener)
                    doAfterTextChanged { s ->
                        s?.takeIf { it.isNotEmpty() }?.let {
                            moveToFirstEmptyBox()
                            checkAndNotifyComplete()
                        }
                    }
                    updateBoxBackground(this, false)
                }.also { editText ->
                    addView(editText)
                    val isHorizontal = orientation == HORIZONTAL
                    val resolvedStart = if (isHorizontal) itemSpacing.toSafeInt() else 0
                    val resolvedTop = if (isHorizontal) 0 else itemSpacing.toSafeInt()
                    val resolvedEnd = if (isHorizontal) itemSpacing.toSafeInt() else 0
                    val resolvedBottom = if (isHorizontal) 0 else itemSpacing.toSafeInt()
                    editText.margin(resolvedStart, resolvedTop, resolvedEnd, resolvedBottom)
                }
            }
        }
        // 撑满父容器
        paddingAll(0)
        // 默认居中
        gravity = Gravity.CENTER
    }

    /**
     * 设置enable时需批量设置
     */
    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        for (i in 0..<size) {
            val child = getChildAt(i)
            child.setEnabled(enabled)
        }
    }

    /**
     * 获取焦点
     */
    private fun moveToFirstEmptyBox() {
        for (i in 0..<size) {
            (getChildAt(i) as? EditText)?.takeIf { it.text.isEmpty() }?.let {
                it.requestFocus()
                return
            }
        }
    }

    /**
     * 提交
     */
    private fun checkAndNotifyComplete() {
        val stringBuilder = StringBuilder()
        var isFull = true
        for (i in 0..<boxCount) {
            (getChildAt(i) as? EditText)?.let {
                val content = it.getText().toString()
                if (content.isEmpty()) {
                    isFull = false
                    break
                } else {
                    stringBuilder.append(content)
                }
            }
        }
        if (isFull) {
            listener?.invoke(stringBuilder.toString())
        }
    }

    /**
     * 回退
     */
    private fun handleBackspace() {
        for (i in size - 1 downTo 0) {
            (getChildAt(i) as? EditText)?.takeIf { it.text.length == 1 }?.let {
                it.requestFocus()
                it.setSelection(1)
                return
            }
        }
    }

    /**
     * 设置输入框选中/未选择中的图片
     */
    private fun updateBoxBackground(editText: EditText?, hasFocus: Boolean) {
        val res = if (hasFocus) backgroundFocusedRes else backgroundNormalRes
        // 仅在有效资源 ID 时才设置，避免误清背景
        if (res != 0) {
            editText?.background(res)
        }
    }

    /**
     * 页面加载完成后自动弹出键盘并聚焦第一个输入框
     * @param owner LifecycleOwner，用于安全地延迟执行
     */
    fun focusNow(owner: LifecycleOwner) {
        doOnceAfterLayout {
            (getChildAt(0) as? EditText)?.let {
                it.showInput(owner)
                it.requestFocus()
            }
        }
    }

    /**
     * 立即获取当前已输入的完整内容（不校验是否填满）
     */
    fun getCode(): String {
        val stringBuilder = StringBuilder()
        for (i in 0..<boxCount) {
            (getChildAt(i) as? EditText)?.let {
                val content = it.getText().toString()
                if (content.isNotEmpty()) {
                    stringBuilder.append(content)
                }
            }
        }
        return stringBuilder.toString()
    }

    /**
     * 清空所有输入框并重置焦点
     */
    fun clearCode() {
        for (i in size - 1 downTo 0) {
            (getChildAt(i) as? EditText)?.let {
                it.requestFocus()
                // 手动补偿背景：因为 setText 不会触发 onFocusChange
                if (i == size - 1) {
                    // 最后一个格子：清空后设为非聚焦态
                    updateBoxBackground(it, false)
                } else {
                    // 其他格子：当前设为聚焦态，下一个设为非聚焦态，模拟焦点从右向左移动的视觉过程
                    updateBoxBackground(it, true)
                    updateBoxBackground(getChildAt(i + 1) as? EditText, false)
                }
                it.setText("")
            }
        }
    }

    /**
     * 设置输入完成监听器
     */
    fun setOnCodeCompleteListener(listener: ((content: String) -> Unit)) {
        this.listener = listener
    }

}