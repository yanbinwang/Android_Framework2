package com.example.framework.utils.function.view

import android.app.Activity
import android.content.Context
import android.graphics.Paint
import android.graphics.Typeface
import android.text.*
import android.text.method.HideReturnsTransformationMethod
import android.text.method.LinkMovementMethod
import android.text.method.PasswordTransformationMethod
import android.text.style.ClickableSpan
import android.util.TypedValue
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.core.content.ContextCompat
import com.example.framework.utils.DecimalInputFilter
import com.example.framework.utils.function.view.ExtraTextViewFunctions.hideSoftKeyboard
import com.example.framework.utils.function.view.ExtraTextViewFunctions.insertAtFocusedPosition

//------------------------------------textview扩展函数类------------------------------------
/**
 * 下划线
 */
fun TextView?.underLine(haveLine: Boolean) {
    if (this == null) return
    paintFlags = if (haveLine) {
        paintFlags or Paint.UNDERLINE_TEXT_FLAG
    } else {
        paintFlags xor Paint.UNDERLINE_TEXT_FLAG
    }
}

/**
 * 加粗
 */
fun TextView?.bold(isBold: Boolean) {
    if (this == null) return
    typeface = if (isBold) {
        Typeface.defaultFromStyle(Typeface.BOLD)
    } else {
        Typeface.defaultFromStyle(Typeface.NORMAL)
    }
}

/**
 * 字体颜色
 */
fun TextView?.textColor(@ColorRes color: Int) {
    if (this == null) return
    this.setTextColor(ContextCompat.getColor(context, color))
}

/**
 * 以res设置textSize
 */
fun TextView?.textSize(@DimenRes size: Int) {
    if (this == null) return
    this.setTextSize(TypedValue.COMPLEX_UNIT_PX, context.resources.getDimension(size))
}

/**
 * 以px做单位设置textSize
 */
fun TextView?.setPxTextSize(size: Float) {
    if (this == null) return
    this.setTextSize(TypedValue.COMPLEX_UNIT_PX, size)
}

/**
 * 设置撑满的文本内容
 */
fun TextView?.setMatchText() {
    if (this == null) return
    post {
        val rawText = text.toString()//原始文本
        val tvPaint = paint//paint包含字体等信息
        val tvWidth = width - paddingLeft - paddingRight//控件可用宽度
        val rawTextLines = rawText.replace("\r".toRegex(), "").split("\n").toTypedArray()//将原始文本按行拆分
        val sbNewText = StringBuilder()
        for (rawTextLine in rawTextLines) {
            if (tvPaint.measureText(rawTextLine) <= tvWidth) {
                //如果整行宽度在控件可用宽度之内，就不处理了
                sbNewText.append(rawTextLine)
            } else {
                //如果整行宽度超过控件可用宽度，则按字符测量，在超过可用宽度的前一个字符处手动换行
                var lineWidth = 0f
                var cnt = 0
                while (cnt != rawTextLine.length) {
                    val ch = rawTextLine[cnt]
                    lineWidth += tvPaint.measureText(ch.toString())
                    if (lineWidth <= tvWidth) {
                        sbNewText.append(ch)
                    } else {
                        sbNewText.append("\n")
                        lineWidth = 0f
                        --cnt
                    }
                    ++cnt
                }
            }
            sbNewText.append("\n")
        }
        //把结尾多余的\n去掉
        if (!rawText.endsWith("\n")) sbNewText.deleteCharAt(sbNewText.length - 1)
        text = sbNewText.toString()
    }
}

/**
 * 文案添加点击事件（单一）
 */
fun TextView?.setClickableSpan(textStr: String, keyword: String, clickableSpan: ClickableSpan) {
    if (this == null) return
    val spannable = SpannableString(textStr)
    val index = textStr.indexOf(keyword)
    text = if (index != -1) {
        spannable.setSpan(clickableSpan, index, index + keyword.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannable
    } else textStr
    movementMethod = LinkMovementMethod.getInstance()
}

/**
 * EditText输入密码是否可见(显隐)
 */
fun EditText?.inputTransformation(): Boolean {
    if (this == null) return false
    var display = false
    try {
        if (transformationMethod == HideReturnsTransformationMethod.getInstance()) {
            transformationMethod = PasswordTransformationMethod.getInstance()
            display = false
        } else {
            transformationMethod = HideReturnsTransformationMethod.getInstance()
            display = true
        }
        setSelection(text.length)
        postInvalidate()
    } catch (_: Exception) {
    }
    return display
}

/**
 * EditText输入金额小数限制
 */
fun EditText?.decimalFilter(decimalPoint: Int = 2) {
    if (this == null) return
    val decimalInputFilter = DecimalInputFilter()
    decimalInputFilter.decimalPoint = decimalPoint
    filters = arrayOf<InputFilter>(decimalInputFilter)
}

/**
 * EditText不允许输入空格
 */
fun EditText?.inhibitInputSpace() {
    if (this == null) return
    filters = arrayOf(object : InputFilter {
        override fun filter(source: CharSequence?, start: Int, end: Int, dest: Spanned?, dstart: Int, dend: Int): CharSequence? {
            val result = source ?: ""
            return if (result == " ") "" else null
        }
    })
}

/**
 * 添加EditText的InputFilter
 */
fun EditText?.addFilter(vararg filterList: InputFilter) {
    if (this == null) return
    filters = filters.plus(filterList)
}

/**
 * 去除EditText的InputFilter
 */
fun EditText?.removeFilter(vararg filterList: InputFilter) {
    if (this == null) return
    filters = arrayOf<InputFilter>().plus(filters.filter { !filterList.contains(it) })
}

/**
 * 添加回车时的处理
 */
fun EditText?.onDone(listener: () -> Unit) {
    if (this == null) return
    setOnEditorActionListener { _, id, _ ->
        if (id == EditorInfo.IME_ACTION_SEARCH || id == EditorInfo.IME_ACTION_UNSPECIFIED || id == EditorInfo.IME_ACTION_DONE) {
            listener()
            hideKeyboard()
            return@setOnEditorActionListener true
        }
        return@setOnEditorActionListener false
    }
}

/**
 * 弹出软键盘并获取焦点
 */
fun EditText?.showInput() {
    if (this == null) return
    focus()
    openDecor()
}

/**
 * 弹出软键盘
 */
fun EditText?.doInput() {
    if (this == null) return
    requestFocus()
    val inputManager = this.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    inputManager.showSoftInput(this, 0)
}

/**
 * 隐藏软键盘
 */
fun EditText?.hideKeyboard() {
    if (this == null) return
    hideSoftKeyboard(context, this)
    clearFocus()
}

/**
 * 清除
 */
fun EditText?.clear() {
    if (this == null) return
    setText("")
    hideKeyboard()
}

/**
 * 指定光标插入字符串
 */
fun EditText?.insertAtFocus(string: String) {
    this ?: return
    insertAtFocusedPosition(this, string)
}

/**
 * 安全选中某个光标
 */
fun EditText?.setSafeSelection(start: Int, stop: Int? = null) {
    this ?: return
    if (start !in 0..text.length) return
    try {
        if (stop == null) {
            setSelection(start)
        } else {
            setSelection(start, stop)
        }
    } catch (_: Exception) {
    }
}

/**
 * 单选框状态改变
 */
fun CheckBox?.checked() {
    this ?: return
    isChecked = !isChecked
}

fun CheckBox?.checked(checked: Boolean) {
    this ?: return
    isChecked = checked
}

/**
 * 简易Edittext监听
 */
fun OnMultiTextWatcher.textWatcher(vararg views: EditText) {
    for (view in views) {
        view.addTextChangedListener(this)
    }
}

object ExtraTextViewFunctions {
    /**
     * 隐藏软键盘(可用于Activity，Fragment)
     */
    fun hideSoftKeyboard(context: Context, view: View) {
        val inputMethodManager: InputMethodManager = context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
    }

    /**
     * 实现将扫描结果插入在EditText光标处
     * @param editText
     * @param index 获取光标所在位置
     * @param text 插入的文本
     */
    fun insertAtFocusedPosition(editText: EditText, text: String) {
        val index = editText.selectionStart //获取光标所在位置
        val edit = editText.editableText //获取EditText的文字
        if (index in edit.indices) {
            edit.insert(index, text) //光标所在位置插入文字
        } else {
            edit.append(text)
        }
    }
}

/**
 * 简易的输入监听
 */
interface OnMultiTextWatcher : TextWatcher {
    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
    }

    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
    }

    override fun afterTextChanged(s: Editable) {
    }
}