package com.example.mvvm.utils

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import com.example.framework.utils.EditTextUtil
import com.example.framework.utils.function.value.numberDigits
import com.example.framework.utils.function.value.orZero
import com.example.framework.utils.function.view.setSafeSelection
import com.example.framework.utils.logWTF

/**
 * @description 输入框帮助类
 * @author yan
 */
class NumberEditTextHelper(private val editText: EditText) {
    init {
//        //可在xml中实现
        EditTextUtil.setInputType(editText, 7)
        val numberEditTextHelper = object : NumberTextWatcher(editText) {
            override fun onEmpty() {
                "值为空".logWTF
                editText.setText("")
            }

            override fun onOutOfPrecision(before: String?, cursor: Int?) {
                "值超过小数位".logWTF
                editText.setText(before.orEmpty())
                editText.setSafeSelection(cursor.orZero)
            }

            override fun onResult(text: String) {
                "输入合法".logWTF
            }
        }
        editText.addTextChangedListener(numberEditTextHelper)
        numberEditTextHelper.precision = 2
    }
}

private abstract class NumberTextWatcher constructor(private val editText: EditText) : TextWatcher {
    private var textBefore: String? = null//用于记录变化前的文字
    private var textCursor = 0//用于记录变化时光标的位置
    var precision = 0

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
        if (text.numberDigits() > precision) {
            editText.removeTextChangedListener(this)
            onOutOfPrecision(textBefore, textCursor)
            editText.addTextChangedListener(this)
            return
        }
        onResult(text)
    }

    abstract fun onEmpty()

    abstract fun onOutOfPrecision(before: String?, cursor: Int?)

    abstract fun onResult(text: String)

}

