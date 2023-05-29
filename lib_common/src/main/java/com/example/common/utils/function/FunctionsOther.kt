package com.example.common.utils.function

import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Resources
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.util.TypedValue
import android.view.View
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import com.example.common.BaseApplication
import com.example.common.R
import com.example.common.config.Constants.NO_DATA
import com.example.common.config.ServerConfig
import com.example.common.utils.GsonUtil
import com.example.common.utils.ScreenUtil
import com.example.common.utils.ScreenUtil.getRealSize
import com.example.common.utils.ScreenUtil.getRealSizeFloat
import com.example.common.utils.function.ExtraNumber.pt
import com.example.common.utils.function.ExtraNumber.ptFloat
import com.example.common.utils.i18n.string
import com.example.framework.utils.ColorSpan
import com.example.framework.utils.function.color
import com.example.framework.utils.function.value.orZero
import com.example.framework.utils.function.view.background
import com.example.framework.utils.function.view.textColor
import com.example.framework.utils.setSpanAll
import com.example.framework.utils.setSpanFirst

//------------------------------------按钮，控件行为工具类------------------------------------
/**
 * 对应的拼接区分本地和测试
 */
val Int?.byServerUrl get() = string(this.orZero).byServerUrl

val String?.byServerUrl get() = "${ServerConfig.serverUrl()}${this}"

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
 * 获取Resources中的String
 */
fun resString(@StringRes res: Int): String {
    return try {
        BaseApplication.instance.getString(res)
    } catch (ignore: Exception) {
        ""
    }
}

/**
 * 针对string的一些默认值操作
 */
fun String?.orNoData(): String {
    return if (isNullOrEmpty()) NO_DATA else this
}

//fun String?.orNoDollar(): String {
//    return if (isNullOrEmpty()) NO_DATA_DOLLAR else this
//}
//
//fun String?.orNoPercent(): String {
//    return if (isNullOrEmpty()) NO_DATA_PERCENT else this
//}

/**
 * 对象转json
 */
fun Any?.toJsonString(): String {
    if (this == null) return ""
    return GsonUtil.objToJson(this).orEmpty()
}

/**
 * 后端请求如果data是JsonArray的话，使用该方法得到一个集合
 */
fun <T> String?.toList(clazz: Class<T>): List<T>? {
    if (this == null) return emptyList()
    return GsonUtil.jsonToList(this, clazz)
}

/**
 * 将json转换为对象
 */
fun <T> String?.toObj(clazz: Class<T>): T? {
    if (this == null) return null
    return GsonUtil.jsonToObj(this, clazz)
}

/**
 * 获取Manifest中的参数
 */
fun getManifestString(name: String): String? {
    return BaseApplication.instance.packageManager.getApplicationInfo(BaseApplication.instance.packageName, PackageManager.GET_META_DATA).metaData.get(name)?.toString()
}

/**
 * 获取顶栏高度
 */
fun getStatusBarHeight(): Int {
    return ExtraNumber.getInternalDimensionSize(BaseApplication.instance, "status_bar_height")
}

/**
 * 获取底栏高度
 */
fun getNavigationBarHeight(context: Context): Int {
    if (!ScreenUtil.hasNavigationBar(context)) return 0
    return ExtraNumber.getInternalDimensionSize(context, "navigation_bar_height")
}

/**
 * 设置textview内容当中某一段的颜色
 */
fun TextView?.setSpanFirst(txt: String, keyword: String, colorRes: Int = R.color.appTheme) {
    this ?: return
    text = txt.setSpanFirst(keyword, ColorSpan(context.color(colorRes)))
}

fun TextView?.setSpanFirst(@StringRes res: Int, @StringRes resKeyword: Int, colorRes: Int = R.color.appTheme) {
    this ?: return
    setSpanFirst(string(res), string(resKeyword), colorRes)
}

fun TextView?.setSpanAll(txt: String, keyword: String, colorRes: Int = R.color.appTheme) {
    this ?: return
    text = txt.setSpanAll(keyword, ColorSpan(context.color(colorRes)))
}

fun TextView?.setSpanAll(@StringRes res: Int, @StringRes resKeyword: Int, colorRes: Int = R.color.appTheme) {
    this ?: return
    setSpanAll(string(res), string(resKeyword), colorRes)
}

/**
 * 设置显示内容和对应文本颜色
 */
fun TextView?.setArguments(txt: String = "", colorRes: Int = R.color.appTheme, resId: Int = -1) {
    this ?: return
    text = txt
    textColor(colorRes)
    if (-1 != resId) background(resId)
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

/**
 * 点击链接的span
 * "我已阅读《用户协议》和《隐私政策》".setSpanFirst("《用户协议》",ClickSpan(object :XClickableSpan(){
 *      override fun onLinkClick(widget: View) {
 *          "点击用户协议".logWTF
 *      }
 *  })).setSpanFirst("《隐私政策》",ClickSpan(object :XClickableSpan(){
 *      override fun onLinkClick(widget: View) {
 *          "点击隐私政策".logWTF
 *      }
 *  }))
 */
abstract class XClickableSpan(private val colorRes: Int = R.color.appTheme) : ClickableSpan() {

    abstract fun onLinkClick(widget: View)

    override fun onClick(widget: View) {
        onLinkClick(widget)
    }

    override fun updateDrawState(ds: TextPaint) {
        super.updateDrawState(ds)
        ds.color = color(colorRes)
        ds.isUnderlineText = false
    }
}

object ExtraNumber {
    /**
     * 设计图尺寸转换为实际尺寸
     */
    fun Number?.pt(): Int {
        if (this == null) return 0
        return getRealSize(this.toDouble())
    }

    /**
     * 设计图尺寸转换为实际尺寸
     */
    fun Number?.ptFloat(context: Context = BaseApplication.instance): Float {
        if (this == null) return 0f
        return getRealSizeFloat(context, this.toFloat())
    }

    /**
     * 获取顶栏高度
     */
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