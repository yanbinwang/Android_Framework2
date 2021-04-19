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
import android.widget.EditText
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.base.utils.DecimalInputFilter
import com.example.common.R
import java.text.DecimalFormat

/**
 *  按钮，控件行为工具类
 */

/**
 * 震动
 */
@SuppressLint("MissingPermission")
fun View.setVibrate(milliseconds: Long) {
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
fun View.openWebsite(url: String) {
    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
}

/**
 * 当小数位不超过两位时，补0
 */
fun TextView.setDecimalFormat(number: Double) {
    text = DecimalFormat("0.00").format(number)
}

/**
 * 当小数位超过两位时，只显示两位，但只有一位或没有，则不需要补0
 */
fun TextView.setDecimalFormat2(number: Double) {
    text = DecimalFormat("0.##").format(number)
}

/**
 * 设置textview内容当中某一段的颜色
 */
fun TextView.setSpannable(textStr: String, keyword: String, colorRes: Int = R.color.blue_0d86ff) {
    val spannable = SpannableString(textStr)
    val index = textStr.indexOf(keyword)
    if (index != -1) {
        spannable.setSpan(ForegroundColorSpan(ContextCompat.getColor(context, colorRes)), index, index + keyword.length, Spanned.SPAN_EXCLUSIVE_INCLUSIVE)
        text = spannable
    }
}

/**
 * EditText输入密码是否可见(显隐)
 */
fun EditText.inputTransformation(isDisplay: Boolean): Boolean {
    if (!isDisplay) {
        //display password text, for example "123456"
        transformationMethod = HideReturnsTransformationMethod.getInstance()
        try {
            setSelection(text.length)
        } catch (e: Exception) {
        }
    } else {
        //hide password, display "."
        transformationMethod = PasswordTransformationMethod.getInstance()
        try {
            setSelection(text.length)
        } catch (e: Exception) {
        }
    }
    postInvalidate()
    return !isDisplay
}

/**
 * EditText输入金额小数限制
 */
fun EditText.decimalFilter(decimalPoint: Int = 2) {
    val decimalInputFilter = DecimalInputFilter()
    decimalInputFilter.decimalPoint = decimalPoint
    filters = arrayOf<InputFilter>(decimalInputFilter)
}