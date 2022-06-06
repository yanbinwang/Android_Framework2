package com.example.common.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.Context.VIBRATOR_SERVICE
import android.content.Intent
import android.graphics.Paint
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.text.InputFilter
import android.text.SpannableString
import android.text.Spanned
import android.text.TextUtils
import android.text.method.HideReturnsTransformationMethod
import android.text.method.LinkMovementMethod
import android.text.method.PasswordTransformationMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.*
import androidx.core.content.ContextCompat
import com.example.base.utils.DecimalInputFilter
import com.example.common.R
import com.example.common.constant.Constants
import java.lang.StringBuilder

//------------------------------------按钮，控件行为工具类------------------------------------

/**
 * 空出状态栏高度
 */
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

fun View.topStatusPadding() = run { if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) setPadding(0, Constants.STATUS_BAR_HEIGHT, 0, 0) }

fun View.topStatus() = run { if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, Constants.STATUS_BAR_HEIGHT) }

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
 * 文案添加点击事件（单一）
 */
@JvmOverloads
fun TextView.setClickableSpan(textStr: String, keyword: String, clickableSpan: ClickableSpan) {
    val spannable = SpannableString(textStr)
    val index = textStr.indexOf(keyword)
    text = if (index != -1) {
        spannable.setSpan(clickableSpan, index, index + keyword.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannable
    } else textStr
    movementMethod = LinkMovementMethod.getInstance()
}

/**
 * 设置显示内容和对应文本颜色
 */
@JvmOverloads
fun TextView.setState(textStr: String, colorRes: Int = R.color.blue_0d86ff) {
    text = textStr
    setTextColor(ContextCompat.getColor(context, colorRes))
}

/**
 * 设置下划线，并抗锯齿
 */
fun TextView.setUnderline() {
    paint.flags = Paint.UNDERLINE_TEXT_FLAG
    paint.isAntiAlias = true
}

/**
 * 设置中等加粗
 */
fun TextView.setMediumBold() {
    paint.strokeWidth = 1.0f
    paint.style = Paint.Style.FILL_AND_STROKE
}

/**
 * 设置撑满的文本内容
 */
fun TextView.setMatchText() {
    post {
        val rawText = text.toString()//原始文本
        val tvPaint = paint//paint包含字体等信息
        val tvWidth = width - paddingLeft - paddingRight//控件可用宽度
        val rawTextLines = rawText.replace("\r".toRegex(), "").split("\n").toTypedArray()//将原始文本按行拆分
        val sbNewText = StringBuilder()
        for (rawTextLine in rawTextLines) {
            if (tvPaint.measureText(rawTextLine) <= tvWidth) {
                //如果整行宽度在控件可用宽度之内，就不处理了
                sbNewText.append(rawTextLine)
            } else {
                //如果整行宽度超过控件可用宽度，则按字符测量，在超过可用宽度的前一个字符处手动换行
                var lineWidth = 0f
                var cnt = 0
                while (cnt != rawTextLine.length) {
                    val ch = rawTextLine[cnt]
                    lineWidth += tvPaint.measureText(ch.toString())
                    if (lineWidth <= tvWidth) {
                        sbNewText.append(ch)
                    } else {
                        sbNewText.append("\n")
                        lineWidth = 0f
                        --cnt
                    }
                    ++cnt
                }
            }
            sbNewText.append("\n")
        }
        //把结尾多余的\n去掉
        if (!rawText.endsWith("\n")) sbNewText.deleteCharAt(sbNewText.length - 1)
        text = sbNewText.toString()
    }
}

/**
 * EditText输入密码是否可见(显隐)
 */
fun EditText.inputTransformation(): Boolean {
    var display = false
    try {
        if (transformationMethod == HideReturnsTransformationMethod.getInstance()) {
            transformationMethod =  PasswordTransformationMethod.getInstance()
            display = false
        } else {
            transformationMethod =  HideReturnsTransformationMethod.getInstance()
            display = true
        }
        setSelection(text.length)
        postInvalidate()
    } catch (ignored: Exception) {
    }
    return display
}

/**
 * EditText输入金额小数限制
 */
fun EditText.decimalFilter(decimalPoint: Int = 2) {
    val decimalInputFilter = DecimalInputFilter()
    decimalInputFilter.decimalPoint = decimalPoint
    filters = arrayOf<InputFilter>(decimalInputFilter)
}

/**
 * EditText不允许输入空格
 */
fun EditText.inhibitInputSpace() {
    filters = arrayOf(object : InputFilter {
        override fun filter(source: CharSequence?, start: Int, end: Int, dest: Spanned?, dstart: Int, dend: Int): CharSequence? {
            val result = source ?: ""
            return if (result == " ") "" else null
        }
    })
}

/**
 * 检测
 */
fun Context.testingContent(vararg views: EditText?): Boolean {
    for (view in views) {
        if (view != null) {
            if (TextUtils.isEmpty(view.text.toString().trim { it <= ' ' })) return false
        }
    }
    return true
}