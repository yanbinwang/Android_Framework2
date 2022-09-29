package com.example.mvvm.widget

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.text.SpannableStringBuilder
import android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
import android.text.style.ForegroundColorSpan
import android.util.AttributeSet
import android.widget.EditText
import androidx.core.text.toSpannable
import androidx.core.widget.addTextChangedListener
import com.example.base.utils.function.color
import com.example.mvvm.R

/**
 * @description
 * @author
 */
@SuppressLint("SetTextI18n", "AppCompatCustomView")
class CommunityEditText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : EditText(context, attrs, defStyleAttr) {

    init {
//        addTextChangedListener(object : TextWatcher {
//            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
//
//            }
//
//            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
//
//            }
//
//            override fun afterTextChanged(s: Editable?) {
//            }
//        })
        addTextChangedListener {
            setTextColor(context.color(R.color.black))
        }
    }

    fun setAt(mention: MentionSpan) {
        val spanBuilder = SpannableStringBuilder()
        spanBuilder.append(text)
        spanBuilder.append(" ")
        val mentionSpan = "@${mention.userName}".toSpannable()
//        val begin = selectionStart//当前光标位置
//        val end = begin + mentionSpan.length
//        mentionSpan.setSpan(mention, 0, mentionSpan.length, SPAN_EXCLUSIVE_EXCLUSIVE)
        mentionSpan.setSpan(
            mention,
            if (selectionStart - 1 < 0) 0 else selectionStart - 1,
            mentionSpan.length,
            SPAN_EXCLUSIVE_EXCLUSIVE
        )
        spanBuilder.append(mentionSpan)
        spanBuilder.append(" ")
        setText(spanBuilder.toSpannable(), BufferType.SPANNABLE)
        setSelection(text.length)
    }

    fun setAt2(mention: MentionSpan) {
        val spanBuilder = SpannableStringBuilder()
        spanBuilder.append(text)
        spanBuilder.append(" ")
        val mentionSpan = "@${mention.userName}".toSpannable()
        mentionSpan.setSpan(mention, 3, mentionSpan.length, SPAN_EXCLUSIVE_EXCLUSIVE)
        spanBuilder.append(mentionSpan)
        spanBuilder.append(" ")
        setText(spanBuilder.toSpannable(), BufferType.SPANNABLE)
        setSelection(text.length)
    }

    class MentionSpan(val userName: String, val userIcon: String, val userId: Long) :
        ForegroundColorSpan(Color.BLUE)

}