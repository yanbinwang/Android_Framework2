package com.example.common.utils

import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.os.Looper
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.widget.*
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import com.example.base.utils.function.color
import com.example.base.utils.function.dip2px
import com.example.base.utils.function.px2dip
import com.example.base.utils.function.value.toSafeFloat
import com.example.common.BaseApplication
import com.example.common.R
import com.example.common.constant.Constants
import com.google.gson.Gson
import java.math.BigDecimal
import java.util.*

//------------------------------------按钮，控件行为工具类------------------------------------
/**
 * 当前是否是主线程
 */
val isMainThread get() = Looper.getMainLooper() == Looper.myLooper()

/**
 * 获取Color String中的color
 * eg: "#ffffff"
 */
@ColorInt
fun color(color: String?) = Color.parseColor(color ?: "#ffffff")

/**
 * 获取resources中的color
 */
@ColorInt
fun color(@ColorRes res: Int) = ContextCompat.getColor(BaseApplication.instance.applicationContext, res)

/**
 * 获取图片
 */
fun drawable(@DrawableRes res: Int) = ContextCompat.getDrawable(BaseApplication.instance.applicationContext, res)

/**
 * 获取Manifest中的参数
 */
fun getManifestString(name: String): String? {
    return BaseApplication.instance.packageManager.getApplicationInfo(BaseApplication.instance.packageName, PackageManager.GET_META_DATA).metaData.get(name)?.toString()
}

/**
 * 默认返回自身和自身class名小写，也可指定
 */
fun Class<*>.getPair(name: String? = null): Pair<Class<*>, String> {
    return this to (name ?: this.simpleName.lowercase(Locale.getDefault()))
}

/**
 * 默认返回自身和自身class名小写以及请求的id
 */
fun Class<*>.getTriple(pair: Pair<String, String>, name: String? = null): Triple<Class<*>, Pair<String, String>, String> {
    return Triple(this, pair, (name ?: this.simpleName.lowercase(Locale.getDefault())))
}

/**
 * 全局获取dp和px换算
 */
val Number?.dp: Int
    get() {
        return BaseApplication.instance.dip2px(this.toSafeFloat())
    }

val Number?.px: Int
    get() {
        return BaseApplication.instance.px2dip(this.toSafeFloat())
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
 * 判断某个对象上方是否具备某个注解
 * if (activity.hasAnnotation(SocketRequest::class.java)) {
 * SocketEventHelper.checkConnection(forceConnect = true)
 * }
 * //自定义一个注解
 * annotation class SocketRequest
 * @SocketRequest为注解，通过在application中做registerActivityLifecycleCallbacks监听回调，可以找到全局打了这个注解的activity，从而做一定的操作
 */
fun Any?.hasAnnotation(cls: Class<out Annotation>): Boolean {
    this ?: return false
    return this::class.java.isAnnotationPresent(cls)
}

/**
 * 获取对应大小的文字
 * File类直接取length
 */
fun Long.getFormatSize(): String {
    val byteResult = this / 1024
    if (byteResult < 1) return "<1K"
    val kiloByteResult = byteResult / 1024
    if (kiloByteResult < 1) return "${BigDecimal(byteResult.toString()).setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString()}K"
    val mByteResult = kiloByteResult / 1024
    if (mByteResult < 1) return "${BigDecimal(kiloByteResult.toString()).setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString()}M"
    val gigaByteResult = mByteResult / 1024
    if (gigaByteResult < 1) return "${BigDecimal(mByteResult.toString()).setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString()}GB"
    val teraByteResult = BigDecimal(gigaByteResult)
    return "${teraByteResult.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString()}TB"
}

/**
 *  backgroundColorSpan = new BackgroundImageSpan(R.drawable.bg_answer_wrong, getResources().getDrawable(R.drawable.bg_answer_wrong));
 */
fun String?.setBackgroundImageSpan(theme: BackgroundImageSpan, start: Int, end: Int): SpannableString {
    this ?: orEmpty()
    return SpannableString(this).apply { setSpan(theme, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE) }
}

/**
 * 设置覆盖色
 */
fun ImageView?.tint(@ColorRes res: Int) {
    this ?: return
    setColorFilter(context.color(res))
}

/**
 * 设置按钮显影图片
 */
fun ImageView?.setResource(triple: Triple<Boolean, Int, Int>) {
    this ?: return
    setImageResource(if (!triple.first) triple.third else triple.second)
}

/**
 * 图片宽度动态变为手机宽度
 */
fun ImageView?.setScreenWidth() {
    this ?: return
    layoutParams = when (parent) {
        is LinearLayout -> LinearLayout.LayoutParams(Constants.SCREEN_WIDTH, LinearLayout.LayoutParams.WRAP_CONTENT)
        is RelativeLayout -> RelativeLayout.LayoutParams(Constants.SCREEN_WIDTH, RelativeLayout.LayoutParams.WRAP_CONTENT)
        is FrameLayout -> FrameLayout.LayoutParams(Constants.SCREEN_WIDTH, FrameLayout.LayoutParams.WRAP_CONTENT)
        else -> ConstraintLayout.LayoutParams(Constants.SCREEN_WIDTH, ConstraintLayout.LayoutParams.WRAP_CONTENT)
    }
}

/**
 * 设置textview内容当中某一段的颜色
 */
@JvmOverloads
fun TextView?.setSpan(txt: String, keyword: String, colorRes: Int = R.color.blue_3d81f2) {
    this ?: return
    val spannable = SpannableString(txt)
    val index = txt.indexOf(keyword)
    text = if (index != -1) {
        spannable.setSpan(ForegroundColorSpan(context.color(colorRes)), index, index + keyword.length, Spanned.SPAN_EXCLUSIVE_INCLUSIVE)
        spannable
    } else txt
}

/**
 * 设置显示内容和对应文本颜色
 */
@JvmOverloads
fun TextView?.setArguments(txt: String ?= "", colorRes: Int = R.color.blue_3d81f2, resId: Int = 0) {
    this ?: return
    if (!txt.isNullOrEmpty()) text = txt
    setTextColor(context.color(colorRes))
    setBackgroundResource(resId)
}

/**
 * 联动滑动时某个控件显影，传入对应控件的高度（dp）
 */
fun NestedScrollView?.addAlphaListener(menuHeight: Int, onAlphaChange: (alpha: Float) -> Unit?) {
    this ?: return
    setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { _, _, scrollY, _, _ ->
        onAlphaChange.invoke(if (scrollY <= menuHeight.dp / 2f) 0 + scrollY / (menuHeight.dp / 4f) else 1f)
    })
}