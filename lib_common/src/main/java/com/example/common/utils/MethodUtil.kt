package com.example.common.utils

import android.os.Build
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.common.R
import com.example.common.constant.Constants

//------------------------------------按钮，控件行为工具类------------------------------------
/**
 * 空出状态栏高度
 */
fun View.topStatus() = run { if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, Constants.STATUS_BAR_HEIGHT) }

fun View.topStatusPadding() = run { if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) setPadding(0, Constants.STATUS_BAR_HEIGHT, 0, 0) }

fun RelativeLayout.topStatusMargin(arrow: Boolean = true) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M || arrow) {
        val params = layoutParams as RelativeLayout.LayoutParams
        params.topMargin = Constants.STATUS_BAR_HEIGHT
        layoutParams = params
    }
}

fun LinearLayout.topStatusMargin(arrow: Boolean = true) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M || arrow) {
        val params = layoutParams as LinearLayout.LayoutParams
        params.topMargin = Constants.STATUS_BAR_HEIGHT
        layoutParams = params
    }
}

/**
 * 图片宽屏
 */
fun ImageView.setRelativeScreenWidth() {
    val rLayoutParams = layoutParams as RelativeLayout.LayoutParams
    rLayoutParams.width = Constants.SCREEN_WIDTH
    layoutParams = rLayoutParams
}

/**
 * 图片宽屏
 */
fun ImageView.setLinearScreenWidth() {
    val lLayoutParams = layoutParams as LinearLayout.LayoutParams
    lLayoutParams.width = Constants.SCREEN_WIDTH
    layoutParams = lLayoutParams
}

/**
 * 设置textview内容当中某一段的颜色
 */
@JvmOverloads
fun TextView.setSpan(textStr: String, keyword: String, colorRes: Int = R.color.blue_0d86ff) {
    val spannable = SpannableString(textStr)
    val index = textStr.indexOf(keyword)
    text = if (index != -1) {
        spannable.setSpan(ForegroundColorSpan(ContextCompat.getColor(context, colorRes)), index, index + keyword.length, Spanned.SPAN_EXCLUSIVE_INCLUSIVE)
        spannable
    } else textStr
}

/**
 * 设置显示内容和对应文本颜色
 */
@JvmOverloads
fun TextView.setState(textStr: String, colorRes: Int = R.color.blue_0d86ff) {
    text = textStr
    setTextColor(ContextCompat.getColor(context, colorRes))
}