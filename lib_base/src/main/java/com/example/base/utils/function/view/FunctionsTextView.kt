package com.example.base.utils.function.view

import android.graphics.Paint
import android.text.*
import android.text.method.HideReturnsTransformationMethod
import android.text.method.LinkMovementMethod
import android.text.method.PasswordTransformationMethod
import android.text.style.ClickableSpan
import android.widget.EditText
import android.widget.TextView
import com.example.base.utils.DecimalInputFilter

//------------------------------------textview扩展函数类------------------------------------
/**
 * 文案添加点击事件（单一）
 */
fun TextView.setClickableSpan(textStr: String, keyword: String, clickableSpan: ClickableSpan) {
    val spannable = SpannableString(textStr)
    val index = textStr.indexOf(keyword)
    text = if (index != -1) {
        spannable.setSpan(clickableSpan, index, index + keyword.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannable
    } else textStr
    movementMethod = LinkMovementMethod.getInstance()
}

/**
 * 设置下划线，并抗锯齿
 */
fun TextView.setUnderline() {
    paint.flags = Paint.UNDERLINE_TEXT_FLAG
    paint.isAntiAlias = true
}

/**
 * 设置中等加粗
 */
fun TextView.setMediumBold() {
    paint.strokeWidth = 1.0f
    paint.style = Paint.Style.FILL_AND_STROKE
}

/**
 * 设置撑满的文本内容
 */
fun TextView.setMatchText() {
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
 * EditText输入密码是否可见(显隐)
 */
fun EditText.inputTransformation(): Boolean {
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
    } catch (ignored: Exception) {
    }
    return display
}

/**
 * EditText输入金额小数限制
 */
fun EditText.decimalFilter(decimalPoint: Int = 2) {
    val decimalInputFilter = DecimalInputFilter()
    decimalInputFilter.decimalPoint = decimalPoint
    filters = arrayOf<InputFilter>(decimalInputFilter)
}

/**
 * EditText不允许输入空格
 */
fun EditText.inhibitInputSpace() {
    filters = arrayOf(object : InputFilter {
        override fun filter(source: CharSequence?, start: Int, end: Int, dest: Spanned?, dstart: Int, dend: Int): CharSequence? {
            val result = source ?: ""
            return if (result == " ") "" else null
        }
    })
}

/**
 * 简易Edittext监听
 */
fun TextWatcher.textWatcher(vararg views: EditText) {
    for (view in views) {
        view.addTextChangedListener(this)
    }
}

fun textWatcher(listener:SimpleTextWatcher, vararg views: EditText) {
    for (view in views) {
        view.addTextChangedListener(listener)
    }
}

/**
 * 简易的输入监听
 */
abstract class SimpleTextWatcher : TextWatcher {
    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
    }

    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
    }

    override fun afterTextChanged(s: Editable) {
    }
}