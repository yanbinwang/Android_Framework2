package com.example.mvvm.widget

import android.text.Editable
import android.text.SpannableStringBuilder
import android.text.TextUtils

/**
 * @description 输入帮助类
 * @author yan
 */
object AtUserHelper {
    //定义好正则
//    private const val AT_PATTERN = "@\\(name:([^\\n\\\\r`~\\!@#\\\$%\\^&\\*\\(\\)\\+=\\|'\\:;'\\,\\[\\]\\.\\<\\>/\\?！@#￥%……（）——\\{\\}【】‘；：”“’。，、？]+),id:([A-Za-z0-9]+)\\)"
//    private const val AT_PATTERN = "@\\(name:([\\s\\S]*?),id:([A-Za-z0-9]+)\\)"
//    private const val AT_PATTERN = "@\\(([\\s\\S]*?)\\)"
    private const val AT_PATTERN = "@([\\s\\S]*?)"

    /**
     * @return 是否输入了At
     */
    fun isInputAt(beforeStr: String, afterStr: String, editSelectionEnd: Int): Boolean {
        if (!TextUtils.isEmpty(afterStr)) {
            //输入内容的操作
            if (TextUtils.isEmpty(beforeStr) || afterStr.length > beforeStr.length) {
                if (afterStr.isNotEmpty() && editSelectionEnd - 1 >= 0 && afterStr.subSequence(
                        editSelectionEnd - 1,
                        editSelectionEnd
                    ) == "@"
                ) {
                    return true
                }
            }
        }
        return false
    }

//    /**
//     * AtUser解析
//     */
//    fun toAtUser(editable: Editable): Editable? {
//        if (TextUtils.isEmpty(editable)) {
//            return null
//        }
//        if (editable is SpannableStringBuilder) {
//            val beforeSpans = editable.getSpans(0, editable.length, AtUserSpan::class.java)
//            for (span in beforeSpans) {
//                val start = editable.getSpanStart(span)
//                val end = editable.getSpanEnd(span)
//                editable.replace(start, end, span.atContent)
//            }
//        }
//        return editable
//    }

}