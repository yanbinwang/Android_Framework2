package com.example.common.utils.function

import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Resources
import android.text.Spannable
import android.text.SpannableString
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
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.NestedScrollView
import com.example.common.BaseApplication
import com.example.common.R
import com.example.common.config.Constants.NO_DATA
import com.example.common.config.ServerConfig
import com.example.common.utils.ScreenUtil.getRealSize
import com.example.common.utils.ScreenUtil.getRealSizeFloat
import com.example.common.utils.ScreenUtil.hasNavigationBar
import com.example.common.utils.function.ExtraNumber.pt
import com.example.common.utils.function.ExtraNumber.ptFloat
import com.example.common.utils.manager.AppManager
import com.example.framework.utils.ClickSpan
import com.example.framework.utils.ColorSpan
import com.example.framework.utils.function.color
import com.example.framework.utils.function.setPrimaryClip
import com.example.framework.utils.function.value.orZero
import com.example.framework.utils.function.value.toNewList
import com.example.framework.utils.function.view.background
import com.example.framework.utils.function.view.setSpannable
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
 * 获取Manifest中的参数
 */
fun getManifestString(name: String): String? {
    return BaseApplication.instance.packageManager.getApplicationInfo(BaseApplication.instance.packageName, PackageManager.GET_META_DATA).metaData.get(name)?.toString()
}

/**
 * 获取顶栏高度(静态默认值)
 * 设备出厂时定义的固定值（如大多数手机为 24dp~32dp），写死在系统资源文件中；
 * 不考虑当前窗口的状态（如是否全屏、是否隐藏状态栏、是否启用边缘到边缘模式）；
 * 不包含刘海屏（display cutout）等额外区域的高度（部分高版本手机可能优化，但本质仍是静态值）
 * BaseBindingAdapter
 * AppToolbar
 */
fun getStatusBarHeight(): Int {
//    return ExtraNumber.getInternalDimensionSize(BaseApplication.instance.applicationContext, "status_bar_height")
    val baseStatusBarHeight = ExtraNumber.getInternalDimensionSize(BaseApplication.instance.applicationContext, "status_bar_height")
    val currentActivity = AppManager.currentActivity()
    return if (null == currentActivity) {
        baseStatusBarHeight
    } else {
        val insets = ViewCompat.getRootWindowInsets(currentActivity.window.decorView)
        insets?.getInsets(WindowInsetsCompat.Type.statusBars())?.top ?: baseStatusBarHeight
    }
}

/**
 * 获取底栏高度(静态默认值)
 */
fun getNavigationBarHeight(): Int {
//    val mContext = BaseApplication.instance.applicationContext
//    if (!ScreenUtil.hasNavigationBar(mContext)) return 0
//    return ExtraNumber.getInternalDimensionSize(mContext, "navigation_bar_height")
    val baseNavigationBarHeight = ExtraNumber.getInternalDimensionSize(BaseApplication.instance.applicationContext, "navigation_bar_height")
    val currentActivity = AppManager.currentActivity()
    return if (null == currentActivity) {
        baseNavigationBarHeight
    } else {
        val decodeView = currentActivity.window.decorView
        if (decodeView.hasNavigationBar()) {
            val insets = ViewCompat.getRootWindowInsets(decodeView)
            insets?.getInsets(WindowInsetsCompat.Type.navigationBars())?.bottom ?: baseNavigationBarHeight
        } else {
            0
        }
    }
}

/**
 * 获取应用缓存大小并格式化为易读的字符串
 * @return 格式化后的缓存大小字符串，如 "2.5M"
 */
fun getFormattedCacheSize(): String {
    val mContext = BaseApplication.instance.applicationContext
    var value = "0M"
    mContext?.cacheDir?.apply {
        value = getTotalSize().let { if (it > 0) it.getSizeFormat() else value }
    }
    return value
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

fun drawable(@DrawableRes res: Int, width: Int, height: Int) = drawable(res)?.apply { setBounds(0, 0, width, height) }

/**
 *  <string name="dollar">\$%1$s</string>
 *  string(R.string.dollar, "10086")
 *  $10086
 *  字符串表达式的处理
 *  %n$ms：代表输出的是字符串，n代表是第几个参数，设置m的值可以在输出之前放置空格
 *  %n$md：代表输出的是整数，n代表是第几个参数，设置m的值可以在输出之前放置空格，也可以设为0m,在输出之前放置m个0
 *  %n$mf：代表输出的是浮点数，n代表是第几个参数，设置m的值可以控制小数位数，如m=2.2时，输出格式为00.00
 *  也可简单写成：
 *  %d   （表示整数）
 *  %f   （表示浮点数）
 *  %s   （表示字符串）
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

fun resString(@StringRes res: Int): String {
    return try {
        BaseApplication.instance.getString(res)
    } catch (e: Exception) {
        e.printStackTrace()
        ""
    }
}

/**
 * 针对string的一些默认值操作
 */
fun String?.orNoData(): String {
    return if (isNullOrEmpty()) NO_DATA else this
}

/**
 * 复制字符串
 */
fun String?.setPrimaryClip(label: String = "Label") {
    if (this == null) return
    BaseApplication.instance.setPrimaryClip(label, this)
}

/**
 * 设置textview内容当中某一段的颜色
 */
fun TextView?.setSpan(txt: Any, keyword: Any, colorRes: Int = R.color.appTheme, spanAll: Boolean = false) {
    this ?: return
    val textToProcess = when (txt) {
        is Int -> string(txt)
        is String -> txt
        else -> ""
    }
    val keywordToProcess = when (keyword) {
        is Int -> string(keyword)
        is String -> keyword
        else -> ""
    }
    val span = ColorSpan(context.color(colorRes))
    text = if (spanAll) {
        textToProcess.setSpanAll(keywordToProcess, span)
    } else {
        textToProcess.setSpanFirst(keywordToProcess, span)
    }
}

/**
 * 设置点击跳转
 */
fun TextView?.setSpan(txt: Any, vararg keywords: Triple<Any, Int, () -> Unit>) {
    this ?: return
    val textToProcess = when (txt) {
        is Int -> string(txt)
        is String -> txt
        else -> ""
    }
    var content: Spannable = SpannableString.valueOf(textToProcess)
    keywords.forEach {
        val keyword = when (val res = it.first) {
            is Int -> string(res)
            is String -> res
            else -> ""
        }
        content = content.setSpanFirst(keyword, ColorSpan(context.color(it.second)), ClickSpan(object : XClickableSpan() {
            override fun onLinkClick(widget: View) {
                it.third.invoke()
            }
        }))
    }
    setSpannable(content)
}

fun TextView?.setSpan(txt: Any, vararg keywords: Pair<Any, () -> Unit>, colorRes: Int = R.color.appTheme) {
    setSpan(txt, *keywords.map {
        Triple(it.first, colorRes, it.second)
    }.toTypedArray())
}

/**
 * 设置显示内容和对应文本颜色
 */
fun TextView?.setTheme(txt: String = "", colorRes: Int = R.color.appTheme, resId: Int = -1) {
    this ?: return
    text = txt
    textColor(colorRes)
    if (-1 != resId) background(resId)
}

/**
 * 联动滑动时某个控件显影，传入对应控件的高度（dp）
 */
fun NestedScrollView?.addAlphaListener(menuHeight: Int, func: (alpha: Float) -> Unit?) {
    this ?: return
    setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { _, _, scrollY, _, _ ->
        func.invoke(if (scrollY <= menuHeight.pt / 2f) 0 + scrollY / (menuHeight.pt / 4f) else 1f)
    })
}

///**
// * 自定义反向动画
// */
//fun Context.translate(onStart: () -> Unit = {}, onEnd: () -> Unit = {}, onRepeat: () -> Unit = {}, isShown: Boolean = true): Animation {
//    return loadAnimation(if (isShown) R.anim.set_translate_bottom_in else R.anim.set_translate_bottom_out, onStart, onEnd, onRepeat)
//}

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
 *  textView.movementMethod = android.text.method.LinkMovementMethod.getInstance()
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