package com.example.common.utils

import android.text.SpannableString
import android.text.Spanned
import android.text.TextUtils
import android.text.style.ForegroundColorSpan
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import com.example.base.utils.function.color
import com.example.base.utils.function.dip2px
import com.example.base.utils.function.px2dip
import com.example.base.utils.function.toSafeFloat
import com.example.common.BaseApplication
import com.example.common.R

//------------------------------------按钮，控件行为工具类------------------------------------
val Number?.dp: Int
    get() {
        return BaseApplication.instance!!.dip2px(this.toSafeFloat())
    }

val Number?.px: Int
    get() {
        return BaseApplication.instance!!.px2dip(this.toSafeFloat())
    }

/**
 * 设置按钮显影图片
 */
fun ImageView.setResource(triple: Triple<Boolean, Int, Int>) = setImageResource(if (!triple.first) triple.third else triple.second)

/**
 * 设置textview内容当中某一段的颜色
 */
@JvmOverloads
fun TextView.setSpan(textStr: String, keyword: String, colorRes: Int = R.color.blue_0d86ff) {
    val spannable = SpannableString(textStr)
    val index = textStr.indexOf(keyword)
    text = if (index != -1) {
        spannable.setSpan(ForegroundColorSpan(context.color(colorRes)), index, index + keyword.length, Spanned.SPAN_EXCLUSIVE_INCLUSIVE)
        spannable
    } else textStr
}

/**
 * 设置显示内容和对应文本颜色
 */
@JvmOverloads
fun TextView.setParameter(textStr: String = "", colorRes: Int = R.color.blue_0d86ff, resid: Int = 0) {
    if (!TextUtils.isEmpty(textStr)) text = textStr
    setTextColor(context.color(colorRes))
    setBackgroundResource(resid)
}

/**
 * 状态改变
 */
fun CheckBox.checked() {
    isChecked = !isChecked
}