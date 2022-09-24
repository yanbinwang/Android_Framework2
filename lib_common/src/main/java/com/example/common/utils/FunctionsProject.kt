package com.example.common.utils

import android.os.Build
import android.text.SpannableString
import android.text.Spanned
import android.text.TextUtils
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.base.utils.function.color
import com.example.base.utils.function.dip2px
import com.example.base.utils.function.px2dip
import com.example.base.utils.function.toSafeFloat
import com.example.common.BaseApplication
import com.example.common.R
import com.example.common.constant.Constants

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
 * 空出状态栏高度
 */
fun View.statusBarHeight() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        layoutParams = when (parent) {
            is LinearLayout -> LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, Constants.STATUS_BAR_HEIGHT)
            is RelativeLayout -> RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, Constants.STATUS_BAR_HEIGHT)
            is FrameLayout -> FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, Constants.STATUS_BAR_HEIGHT)
            else -> ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, Constants.STATUS_BAR_HEIGHT)
        }
    }
}

fun View.statusBarPadding() = run {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) setPadding(0, Constants.STATUS_BAR_HEIGHT, 0, 0)
}

fun View.statusBarMargin(enable: Boolean = true) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M || enable) {
        val params = when (parent) {
            is LinearLayout -> layoutParams as LinearLayout.LayoutParams
            is RelativeLayout -> layoutParams as RelativeLayout.LayoutParams
            is FrameLayout -> layoutParams as FrameLayout.LayoutParams
            else -> layoutParams as ConstraintLayout.LayoutParams
        }
        params.topMargin = Constants.STATUS_BAR_HEIGHT
        layoutParams = params
    }
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