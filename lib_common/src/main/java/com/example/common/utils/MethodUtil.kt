package com.example.common.utils

import android.annotation.SuppressLint
import android.content.Context.VIBRATOR_SERVICE
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.text.InputFilter
import android.text.SpannableString
import android.text.Spanned
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.*
import androidx.core.content.ContextCompat
import com.example.base.utils.DecimalInputFilter
import com.example.common.R
import com.example.common.constant.Constants

//------------------------------------按钮，控件行为工具类------------------------------------

/**
 * 空出状态栏高度
 */
fun RelativeLayout.topMargin() {
    val params = layoutParams as RelativeLayout.LayoutParams
    params.topMargin = Constants.STATUS_BAR_HEIGHT
    layoutParams = params
}

fun LinearLayout.topMargin() {
    val params = layoutParams as LinearLayout.LayoutParams
    params.topMargin = Constants.STATUS_BAR_HEIGHT
    layoutParams = params
}

/**
 * 震动
 */
@SuppressLint("MissingPermission")
fun View.vibrate(milliseconds: Long) {
    val vibrator = (context.getSystemService(VIBRATOR_SERVICE) as Vibrator)
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
        vibrator.vibrate(milliseconds)
    } else {
        vibrator.vibrate(VibrationEffect.createOneShot(milliseconds, VibrationEffect.DEFAULT_AMPLITUDE))
    }
}

/**
 * 开启一个网页
 */
fun View.openWebsite(url: String) = context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))

/**
 * 设置按钮显影图片
 */
fun ImageView.setDisplayResource(display: Boolean, showId: Int, hideId: Int) = setBackgroundResource(if (!display) showId else hideId)

/**
 * 设置textview内容当中某一段的颜色
 */
fun TextView.setSpan(textStr: String, keyword: String, colorRes: Int = R.color.blue_0d86ff) {
    val spannable = SpannableString(textStr)
    val index = textStr.indexOf(keyword)
    if (index != -1) {
        spannable.setSpan(ForegroundColorSpan(ContextCompat.getColor(context, colorRes)), index, index + keyword.length, Spanned.SPAN_EXCLUSIVE_INCLUSIVE)
        text = spannable
    }
}

/**
 * 设置显示内容和对应文本颜色
 */
fun TextView.setState(textStr: String, colorRes: Int = R.color.blue_0d86ff) {
    text = textStr
    setTextColor(ContextCompat.getColor(context, colorRes))
}

/**
 * EditText输入密码是否可见(显隐)
 */
fun EditText.inputTransformation(): Boolean {
    try {
        transformationMethod = if(transformationMethod == HideReturnsTransformationMethod.getInstance()) {
            PasswordTransformationMethod.getInstance()
        } else {
            HideReturnsTransformationMethod.getInstance()
        }
        setSelection(text.length)
        postInvalidate()
    } catch (ignored: Exception) {
    } finally {
        return transformationMethod == HideReturnsTransformationMethod.getInstance()
    }
}

/**
 * EditText输入金额小数限制
 */
fun EditText.decimalFilter(decimalPoint: Int = 2) {
    val decimalInputFilter = DecimalInputFilter()
    decimalInputFilter.decimalPoint = decimalPoint
    filters = arrayOf<InputFilter>(decimalInputFilter)
}