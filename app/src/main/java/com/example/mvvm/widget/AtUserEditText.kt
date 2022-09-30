package com.example.mvvm.widget

import android.annotation.SuppressLint
import android.content.Context
import android.text.*
import android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
import android.text.style.DrawableMarginSpan
import android.util.AttributeSet
import android.widget.EditText
import androidx.core.text.toSpannable
import com.example.base.utils.logWTF

/**
 * @description
 * @author
 */
@SuppressLint("AppCompatCustomView")
class AtUserEditText @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : EditText(context, attrs, defStyleAttr) {

    init {
//        addTextChangedListener(object : TextWatcher {
//            private var beforeEditStart = 0
//            private var beforeEditEnd = 0
//            private var beforeText: SpannableStringBuilder? = null
//            private var afterText: SpannableStringBuilder? = null
//
//            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
//                "beforeTextChanged:$s".logWTF
//                beforeText = SpannableStringBuilder(s)
//                beforeEditStart = selectionStart
//                beforeEditEnd = selectionEnd
//            }
//
//            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
//                "onTextChanged:$s".logWTF
//                afterText = SpannableStringBuilder(s)
//            }
//
//            override fun afterTextChanged(s: Editable?) {
//                "afterTextChanged:$s".logWTF
//                if (null != beforeText && null != afterText && null != s) {
//                    //删除At整体
//                    isRemoveAt(beforeText!!, afterText!!, s, beforeEditStart, beforeEditEnd)
//                }
//            }
//        })
    }

    fun isRemoveAt(beforeStr: CharSequence?, afterStr: CharSequence, s: Editable, editSelectionStart: Int, editSelectionEnd: Int): Boolean {
        if (TextUtils.isEmpty(afterStr) || TextUtils.isEmpty(beforeStr) || afterStr !is SpannableStringBuilder || beforeStr !is SpannableStringBuilder) {
            return false
        }
        if (afterStr.length < beforeStr.length) { //删除内容的操作
            val beforeSpans = beforeStr.getSpans(0, beforeStr.length, AtUserSpan::class.java)
            var mReturn = false
            for (span in beforeSpans) {
                val start = beforeStr.getSpanStart(span)
                val end = beforeStr.getSpanEnd(span)
                var isRemove = false
                if (editSelectionStart == editSelectionEnd && editSelectionEnd == end) {
                    isRemove = true
                    s.delete(start, end - 1)
                } else if (editSelectionStart <= start && editSelectionEnd >= end) {
                    return false
                } else if (start in editSelectionStart until editSelectionEnd) {
                    isRemove = true
                    s.delete(editSelectionStart, end - editSelectionEnd)
                } else if (end in (editSelectionStart + 1)..editSelectionEnd) {
                    isRemove = true
                    s.delete(start, editSelectionStart)
                }
                if (isRemove) {
                    mReturn = true
                    beforeStr.removeSpan(span)
                }
            }
            return mReturn
        }
        return false
    }

    fun parseAtUser(mention: AtUserSpan) {
        val spanBuilder = SpannableStringBuilder()
        spanBuilder.append(text)
        val mentionSpan = " @${mention.name} ".toSpannable()
        val index = selectionStart //获取光标所在位置
        val edit = editableText//获取EditText的文字
        if (index < 0 || index >= edit.length) {
            append(mention, spanBuilder, mentionSpan)
            setText(spanBuilder.toSpannable(), BufferType.SPANNABLE)
            setSelection(text.length)
        } else {
            //光标所在位置插入文字
            append(mention, spanBuilder, mentionSpan)
            edit.insert(index, mentionSpan)
            setSelection(index)
        }
    }

    /**
     * 添加文字的方法
     */
    private fun append(mention: AtUserSpan, spanBuilder: SpannableStringBuilder, mentionSpan: Spannable) {
        mentionSpan.setSpan(mention, 0, mentionSpan.length, SPAN_EXCLUSIVE_EXCLUSIVE)
        spanBuilder.append(mentionSpan)
    }

}