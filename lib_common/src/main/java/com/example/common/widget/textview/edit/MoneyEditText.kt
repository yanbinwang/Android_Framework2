package com.example.common.widget.textview.edit

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import com.example.framework.utils.function.view.inputType

/**
 * @description 金幣輸入
 * @author yan
 */
class MoneyEditText @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : ClearEditText(context, attrs, defStyleAttr) {
    var afterTextChanged: ((mBefore: String?, mCursor: Int, mContent: String) -> Unit)? = null

    init {
        editText.inputType(7)
        reset()
    }

    private val limitTextWatch = object : TextWatcher {
        private var mBefore: String? = null// 用于记录变化前的文字
        private var mCursor = 0// 用于记录变化时光标的位置
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            mBefore = s.toString()
            mCursor = start
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        }

        override fun afterTextChanged(s: Editable?) {
            //排除空的情況
            val mContent = s.toString()
            if (mContent.isEmpty()) {
                removeTextChangedListener()
                setText("")
                addTextChangedListener()
                return
            }
            afterTextChanged?.invoke(mBefore, mCursor, mContent)
        }
    }

    fun reset() {
        editText.removeTextChangedListener(limitTextWatch)
        editText.addTextChangedListener(limitTextWatch)
    }

    fun addTextChangedListener() {
        editText.removeTextChangedListener(limitTextWatch)
    }

    fun removeTextChangedListener() {
        editText.addTextChangedListener(limitTextWatch)
    }

}