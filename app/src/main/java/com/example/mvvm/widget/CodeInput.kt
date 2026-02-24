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
import com.example.framework.utils.function.value.orZero
import com.example.framework.utils.function.value.toSafeInt
import com.example.framework.utils.function.view.background
import com.example.framework.utils.function.view.color
import com.example.framework.utils.function.view.dimen
import com.example.framework.utils.function.view.doOnceAfterLayout
import com.example.framework.utils.function.view.inputType
import com.example.framework.utils.function.view.margin
import com.example.framework.utils.function.view.padding
import com.example.framework.utils.function.view.showInput
import com.example.framework.utils.function.view.size
import com.example.framework.utils.logWTF
import com.example.mvvm.R

/**
 * 自动编排密码输入框
 */
class CodeInput @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : LinearLayout(context, attrs, defStyleAttr) {
    private var boxCount = 4
    private var boxBgNormal: Int? = null
    private var boxBgFocus: Int? = null
    private var listener: OnCodeInputListener? = null

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
            boxBgNormal = getResourceId(R.styleable.CodeInput_boxBgNormal, R.drawable.shape_code)
            boxBgFocus = getResourceId(R.styleable.CodeInput_boxBgFocus, R.drawable.shape_code_pressed)
            // 初始化view
            val onKeyListener = OnKeyListener { _, keyCode, _ ->
                if (keyCode == KeyEvent.KEYCODE_DEL) {
                    back()
                }
                false
            }
            for (i in 0 until boxCount) {
                EditText(context).let { editText ->
                    editText.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize)
                    editText.setTextColor(textColor)
                    editText.inputType(inputType)
                    editText.size(boxWidth.toSafeInt(), boxHeight.toSafeInt())
                    editText.setGravity(Gravity.CENTER)
                    val padding = itemPadding.toSafeInt()
                    editText.padding(padding, padding, padding, padding)
                    editText.setEms(1)
                    editText.setOnKeyListener(onKeyListener)
                    editText.onFocusChangeListener = OnFocusChangeListener { v, hasFocus ->
                        setBoxBackground(v as? EditText, hasFocus)
                    }
                    editText.doAfterTextChanged { s ->
                        s?.takeIf { it.isNotEmpty() }?.let {
                            focus()
                            commit()
                        }
                    }
                    setBoxBackground(editText, false)
                    addView(editText)
                    val isHorizontal = orientation == HORIZONTAL
                    val start = if (isHorizontal) itemSpacing.toSafeInt() else 0
                    val top = if (isHorizontal) 0 else itemSpacing.toSafeInt()
                    val end = if (isHorizontal) itemSpacing.toSafeInt() else 0
                    val bottom = if (isHorizontal) 0 else itemSpacing.toSafeInt()
                    editText.margin(start, top, end, bottom)
                }
            }
        }
        // 撑满父容器
        setPadding(0, 0, 0, 0)
        // 默认居中
        gravity = Gravity.CENTER
    }

    /**
     * 获取焦点
     */
    private fun focus() {
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
    private fun commit() {
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
        "checkAndCommit:$stringBuilder".logWTF
        if (isFull) {
            listener?.onComplete(stringBuilder.toString())
//            setEnabled(false)
        }
    }

    /**
     * 回退
     */
    private fun back() {
        for (i in size - 1 downTo 0) {
            (getChildAt(i) as? EditText)?.takeIf { it.text.length == 1 }?.let {
                it.requestFocus()
                it.setSelection(1)
                return
            }
        }
    }

    /**
     * 清空
     */
    private fun clear() {
        for (i in size - 1 downTo 0) {
            (getChildAt(i) as? EditText)?.let {
                it.requestFocus()
                if (i == size - 1) {
                    setBoxBackground(it, false)
                } else {
                    setBoxBackground(it, true)
                    setBoxBackground(getChildAt(i + 1) as? EditText, false)
                }
                it.setText("")
            }
        }
    }

    /**
     * 设置输入框选中/未选择中的图片
     */
    private fun setBoxBackground(editText: EditText?, focus: Boolean) {
        when {
            // 聚焦且有focus背景 → 设置focus背景
            focus && boxBgFocus != null -> editText.background(boxBgFocus.orZero)
            // 非聚焦且有normal背景 → 设置normal背景
            !focus && boxBgNormal != null -> editText.background(boxBgNormal.orZero)
            // 其他情况：不修改背景（保留原有样式）
            else -> {}
        }
    }

    /**
     * 页面初次加载,弹出输入框并获取焦点 (需延迟)
     */
    fun focusNow(observer: LifecycleOwner) {
        doOnceAfterLayout {
            (getChildAt(0) as? EditText)?.let {
                it.showInput(observer)
                it.requestFocus()
            }
        }
    }

    /**
     * 获取去提交参数
     */
    fun commitNow(): String {
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
     * 清空
     */
    fun clearNow() {
        clear()
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
     * 设置监听回调
     */
    fun setOnCodeInputListener(listener: OnCodeInputListener) {
        this.listener = listener
    }

    interface OnCodeInputListener {
        fun onComplete(content: String?)
    }

}