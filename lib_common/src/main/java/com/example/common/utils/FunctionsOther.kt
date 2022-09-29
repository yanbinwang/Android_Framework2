package com.example.common.utils

import android.content.pm.PackageManager
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.TextUtils
import android.text.style.ForegroundColorSpan
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import com.example.base.utils.function.color
import com.example.base.utils.function.dip2px
import com.example.base.utils.function.px2dip
import com.example.base.utils.function.value.toSafeFloat
import com.example.base.utils.function.value.toSafeInt
import com.example.common.BaseApplication
import com.example.common.R
import com.google.gson.Gson
import java.math.BigDecimal

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
 * 获取resources中的color
 */
@ColorInt
fun color(@ColorRes res: Int) = BaseApplication.instance?.resources?.getColor(res).toSafeInt()

/**
 * 获取对应大小的文字
 */
fun Double.getFormatSize(): String {
    val byteResult = this / 1024
    if (byteResult < 1) return "<1K"
    val kiloByteResult = byteResult / 1024
    if (kiloByteResult < 1) return BigDecimal(byteResult.toString()).setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "K"
    val mByteResult = kiloByteResult / 1024
    if (mByteResult < 1) return BigDecimal(kiloByteResult.toString()).setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "M"
    val gigaByteResult = mByteResult / 1024
    if (gigaByteResult < 1) return BigDecimal(mByteResult.toString()).setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "GB"
    val teraByteResult = BigDecimal(gigaByteResult)
    return (teraByteResult.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "TB")
}

/**
 * 获取Manifest中的参数
 */
fun getManifestString(name: String): String? {
    return BaseApplication.instance!!.packageManager.getApplicationInfo(BaseApplication.instance!!.packageName, PackageManager.GET_META_DATA).metaData.get(name)?.toString()
}

/**
 * 清空fragment缓存
 */
fun Bundle?.clearFragmentSavedState() {
    this ?: return
    remove("android:support:fragments")
    remove("android:fragments")
}

/**
 * 对象转json
 */
fun Any?.toJsonString(): String {
    if (this == null) return "null"
    return Gson().toJson(this)
}

/**
 * 设置覆盖色
 */
fun ImageView?.tintColor(@ColorInt res: Int) {
    this ?: return
    setColorFilter(res)
}

/**
 * 设置覆盖色
 */
fun ImageView?.tint(@ColorRes res: Int) {
    this ?: return
    setColorFilter(color(res))
}

/**
 * 设置按钮显影图片
 */
fun ImageView.setResource(triple: Triple<Boolean, Int, Int>) =
    setImageResource(if (!triple.first) triple.third else triple.second)

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
 * 单选框状态改变
 */
fun CheckBox.checked() {
    isChecked = !isChecked
}