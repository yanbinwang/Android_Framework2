package com.example.common.widget.textview

import android.annotation.SuppressLint
import android.content.Context
import android.text.Editable
import android.text.Selection
import android.text.Spannable
import android.text.TextWatcher
import android.util.AttributeSet
import android.widget.EditText
import com.example.base.utils.EditTextUtil
import com.example.common.utils.builder.shortToast

/**
 * @description 禁用Emoji的edittext，只能避免部分
 * @author yan
 */
@SuppressLint("AppCompatCustomView")
class ContainsEmojiEditText @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : EditText(context, attrs, defStyleAttr) {
    private var cursorPos = 0//输入表情前的光标位置
    private var resetText = false //是否重置了EditText的内容
    private var inputAfterText: String? = null//输入表情前EditText中的文本

    init {
        cursorPos = selectionEnd
        addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (!resetText) {
                    cursorPos = selectionEnd
                    //这里用s.toString()而不直接用s是因为如果用s，
                    //那么，inputAfterText和s在内存中指向的是同一个地址，s改变了，
                    //inputAfterText也就改变了，那么表情过滤就失败了
                    inputAfterText = s.toString()
                }
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (!resetText) {
                    if (before != 0) return
                    if (count >= 2) { //表情符号的字符长度最小为2
                        val input = s?.subSequence(cursorPos, cursorPos + count)
                        if (containsEmoji(input.toString())) {
                            resetText = true
                            "不支持输入Emoji表情符号".shortToast()
                            //是表情符号就将文本还原为输入表情符号之前的内容
                            setText(inputAfterText)
                            if (text is Spannable) Selection.setSelection(text, text.length)
                        }
                    }
                } else {
                    resetText = false
                }
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })
    }

    /**
     * 检测是否有emoji表情
     */
    fun containsEmoji(source: String): Boolean {
        val len = source.length
        for (i in 0 until len) {
            val codePoint = source[i]
            //如果不能匹配,则该字符是Emoji表情
            if (!EditTextUtil.isEmojiCharacter(codePoint)) {
                return true
            }
        }
        return false
    }

}