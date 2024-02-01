package com.example.common.utils.helper

import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import com.example.common.widget.textview.edittext.EditTextImpl
import com.example.framework.utils.EditTextUtil
import com.example.framework.utils.function.value.numberCompareTo
import com.example.framework.utils.function.value.numberDigits
import com.example.framework.utils.function.value.orZero
import com.example.framework.utils.function.view.setSafeSelection
import com.example.framework.utils.function.view.text
import com.example.framework.utils.logWTF

/**
 * 用于输入框失去焦点时
 * 自动根据输入框内的值吸附最大最小值(处在范围内的值不会改变)
 */
class RangeHelper(private val editText: EditText) : EditTextImpl {
    private var min = ""
    private var max = ""

    init {
        editText.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                getText()
            }
        }
    }

    /**
     * 传入字符串避免小数位数比较的误差
     */
    fun setRange(min: String = "0", max: String = "0") {
        this.min = min
        this.max = max
    }

    /**
     * 服务器取值使用此方法
     */
    fun getText(): String {
        var text = editText.text()
        if (text.numberCompareTo(min) == -1) {
            text = min
        }
        if (text.numberCompareTo(max) == 1) {
            text = max
        }
        if (text != editText.text()) editText.setText(text)
        return text
    }

}


/**
 * 用于限制输入框在输入时候的小数位数
 * 禁止用户在打入小数后超过设定的位数，会自动回滚之前的值
 */
class DecimalHelper(private val editText: EditText) {
    private var listener: ((text: String) -> Unit)? = null

    private val watcher by lazy { object : DecimalTextWatcher(editText) {
        override fun onEmpty() {
            "值为空".logWTF
            editText.setText("")
        }

        override fun onOverstep(before: String?, cursor: Int?) {
            "值超过小数位".logWTF
            editText.setText(before.orEmpty())
            editText.setSafeSelection(cursor.orZero)
        }

        override fun onChanged(text: String) {
            "输入合法".logWTF
            listener?.invoke(text)
        }
    }}

    init {
        //可在xml中实现输入限制
        EditTextUtil.setInputType(editText, 7)
        //添加监听
        editText.addTextChangedListener(watcher)
    }

    /**
     * 设置小数位数限制
     */
    fun setDigits(digits: Int) {
        watcher.digits = digits
    }

    /**
     * 设置回调监听
     */
    fun setOnChangedListener(listener: ((text: String) -> Unit)) {
        this.listener = listener
    }

}

/**
 * 对应输入框限制输入的监听
 * 注：如果是输入范围限制，只能做最大值的限制
 */
private abstract class DecimalTextWatcher(private val editText: EditText) : TextWatcher {
    private var textBefore: String? = null//用于记录变化前的文字
    private var textCursor = 0//用于记录变化时光标的位置
    var digits = 0

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        textBefore = s.toString()
        textCursor = start
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
    }

    override fun afterTextChanged(s: Editable?) {
        val text = s.toString()
        if (text.isEmpty()) {
            editText.removeTextChangedListener(this)
            onEmpty()
            editText.addTextChangedListener(this)
            return
        }
        if (text.numberDigits() > digits) {
            editText.removeTextChangedListener(this)
            onOverstep(textBefore, textCursor)
            editText.addTextChangedListener(this)
            return
        }
        onChanged(text)
    }

    abstract fun onEmpty()

    abstract fun onOverstep(before: String?, cursor: Int?)

    abstract fun onChanged(text: String)

}