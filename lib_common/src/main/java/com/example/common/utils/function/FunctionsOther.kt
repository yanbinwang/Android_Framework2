package com.example.common.utils.function

import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Resources
import android.os.Bundle
import android.util.TypedValue
import android.widget.*
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import com.example.base.utils.ColorSpan
import com.example.base.utils.function.color
import com.example.base.utils.function.value.orZero
import com.example.base.utils.function.value.toNewList
import com.example.base.utils.function.view.background
import com.example.base.utils.function.view.textColor
import com.example.base.utils.setSpanFirst
import com.example.common.BaseApplication
import com.example.common.BuildConfig
import com.example.common.R
import com.example.common.constant.Constants
import com.example.common.utils.ScreenUtil
import com.example.common.utils.function.ExtraNumber.pt
import com.example.common.utils.function.ExtraNumber.ptFloat
import com.google.gson.Gson
import java.util.*

//------------------------------------按钮，控件行为工具类------------------------------------
/**
 * 是否是debug包
 */
val isDebug get() = BuildConfig.DEBUG

/**
 * 对应的拼接区分本地和测试
 */
val Int?.byHostUrl: String get() { return "${BuildConfig.LOCALHOST}${string(this.orZero)}" }

val String?.byHostUrl: String get() { return "${BuildConfig.LOCALHOST}${this}" }

/**
 * 设计图尺寸转换为实际尺寸
 */
val Number?.pt: Int
    get() = pt()

val Number?.ptFloat: Float
    get() = ptFloat()

/**
 * dp尺寸转换为实际尺寸
 */
val Number?.dp: Int
    get() {
        this ?: return 0
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this.toFloat(), BaseApplication.instance.resources.displayMetrics).toInt()
    }

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
 *  <string name="placeholderLevel">\V%1$s</string>
 *  字符串表达式的处理
 */
fun string(@StringRes res: Int, vararg param: Int): String {
    val paramString = param.toNewList { resString(it) }.toTypedArray()
    val result = resString(res)
    return String.format(result, paramString)
}

fun string(@StringRes res: Int, vararg param: String): String {
    val result = resString(res)
    return String.format(result, *param)
}

fun string(@StringRes res: Int): String {
    return resString(res)
}

/**
 * 获取资源文字
 */
fun resString(@StringRes res: Int): String {
    return try {
        BaseApplication.instance.getString(res)
    } catch (ignore: Exception) {
        ""
    }
}

/**
 * 获取Manifest中的参数
 */
fun getManifestString(name: String): String? {
    return BaseApplication.instance.packageManager.getApplicationInfo(BaseApplication.instance.packageName, PackageManager.GET_META_DATA).metaData.get(name)?.toString()
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
fun TextView?.setSpanFirst(txt: String, keyword: String, colorRes: Int = R.color.blue_3d81f2) {
    this ?: return
    text = txt.setSpanFirst(keyword, ColorSpan(context.color(colorRes)))
}

/**
 * 设置显示内容和对应文本颜色
 */
@JvmOverloads
fun TextView?.setArguments(txt: String ?= "", colorRes: Int = R.color.blue_3d81f2, resId: Int = 0) {
    this ?: return
    if (!txt.isNullOrEmpty()) text = txt
    textColor(colorRes)
    background(resId)
}

/**
 * 联动滑动时某个控件显影，传入对应控件的高度（dp）
 */
fun NestedScrollView?.addAlphaListener(menuHeight: Int, onAlphaChange: (alpha: Float) -> Unit?) {
    this ?: return
    setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { _, _, scrollY, _, _ ->
        onAlphaChange.invoke(if (scrollY <= menuHeight.pt / 2f) 0 + scrollY / (menuHeight.pt / 4f) else 1f)
    })
}

object ExtraNumber {
    /**
     * 设计图尺寸转换为实际尺寸
     */
    fun Number?.pt(): Int {
        if (this == null) return 0
        return ScreenUtil.getRealSize(this.toDouble())
    }

    /**
     * 设计图尺寸转换为实际尺寸
     */
    fun Number?.ptFloat(context: Context = BaseApplication.instance): Float {
        if (this == null) return 0f
        return ScreenUtil.getRealSizeFloat(context, this.toFloat())
    }

    /**
     * 获取顶栏高度
     * */
    fun getInternalDimensionSize(context: Context, key: String): Int {
        val result = 0
        try {
            val resourceId = Resources.getSystem().getIdentifier(key, "dimen", "android")
            if (resourceId > 0) {
                val size = context.resources.getDimensionPixelSize(resourceId)
                val size2 = Resources.getSystem().getDimensionPixelSize(resourceId)
                return if (size2 >= size) {
                    size2
                } else {
                    val densityOne = context.resources.displayMetrics.density
                    val densityTwo = Resources.getSystem().displayMetrics.density
                    val f = size * densityTwo / densityOne
                    (if (f >= 0) f + 0.5f else f - 0.5f).toInt()
                }
            }
        } catch (ignored: Resources.NotFoundException) {
            return 0
        }
        return result
    }

}