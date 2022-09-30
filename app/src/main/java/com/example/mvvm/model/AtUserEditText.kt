package com.example.mvvm.model

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.text.style.ForegroundColorSpan
import android.util.AttributeSet
import android.widget.EditText


/**
 * @description
 * @author
 */
@SuppressLint("SetTextI18n", "AppCompatCustomView")
class AtUserEditText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : EditText(context, attrs, defStyleAttr) {


    /**
     * 自定义一个ForegroundColorSpan
     */
    class MentionSpan(val userName: String, val userIcon: String, val userId: Long) : ForegroundColorSpan(Color.BLUE)

//    init {
////        addTextChangedListener(object : TextWatcher {
////            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
////                "beforeTextChanged:${s.toString()}".logWTF
////                //只记录一次
////                if (!beforeFlag) {
////                    beforeFlag = true
////                    lastContent = s.toString()
////                }
////            }
////
////            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
////                "onTextChanged:${s.toString()}".logWTF
////                var changeContent = s.toString()
////            }
////
////            override fun afterTextChanged(s: Editable?) {
////                "afterTextChanged:${s.toString()}".logWTF
////                if(beforeFlag) setText(lastContent)
////            }
////        })
////        addTextChangedListener {
////
////        }
//    }
//
//    fun setAt(mention: MentionSpan) {
//        val spanBuilder = SpannableStringBuilder()
//        spanBuilder.append(text)
//        val mentionSpan = " @${mention.userName} ".toSpannable()
//        val index = selectionStart //获取光标所在位置
//        val edit = editableText//获取EditText的文字
//        if (index < 0 || index >= edit.length) {
//            append(mention, spanBuilder, mentionSpan)
//            setText(spanBuilder.toSpannable(), BufferType.SPANNABLE)
//            setSelection(text.length)
//        } else {
//            //光标所在位置插入文字
//            append(mention, spanBuilder, mentionSpan)
//            edit.insert(index, mentionSpan)
//            setSelection(index)
//        }
//        atList.add(selectionStart - mentionSpan.length to selectionStart)
//        Gson().toJson(atList).logWTF
//    }
//
//    /**
//     * 添加文字的方法
//     */
//    private fun append(
//        mention: MentionSpan,
//        spanBuilder: SpannableStringBuilder,
//        mentionSpan: Spannable
//    ) {
//        mentionSpan.setSpan(mention, 0, mentionSpan.length, SPAN_EXCLUSIVE_EXCLUSIVE)
//        spanBuilder.append(mentionSpan)
//    }
//
//    class MentionSpan(val userName: String, val userIcon: String, val userId: Long) :
//        ForegroundColorSpan(Color.BLUE)

    }