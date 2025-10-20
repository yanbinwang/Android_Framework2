package com.example.common.utils.function

import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.VectorDrawable
import android.text.Spannable
import android.text.SpannableString
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.NestedScrollView
import com.example.common.BaseApplication
import com.example.common.R
import com.example.common.config.Constants.NO_DATA
import com.example.common.config.ServerConfig
import com.example.common.utils.NavigationBarDrawable
import com.example.common.utils.ScreenUtil.getRealSize
import com.example.common.utils.ScreenUtil.getRealSizeFloat
import com.example.common.utils.ScreenUtil.hasNavigationBar
import com.example.common.utils.function.ExtraNumber.pt
import com.example.common.utils.function.ExtraNumber.ptFloat
import com.example.common.utils.manager.AppManager
import com.example.common.widget.textview.edittext.ClearEditText
import com.example.common.widget.textview.edittext.PasswordEditText
import com.example.framework.utils.ClickSpan
import com.example.framework.utils.ColorSpan
import com.example.framework.utils.function.color
import com.example.framework.utils.function.setPrimaryClip
import com.example.framework.utils.function.value.orZero
import com.example.framework.utils.function.value.toNewList
import com.example.framework.utils.function.view.background
import com.example.framework.utils.function.view.getScreenLocation
import com.example.framework.utils.function.view.setSpannable
import com.example.framework.utils.function.view.size
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
 */
fun getStatusBarHeight(): Int {
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
    val mContext = BaseApplication.instance.applicationContext
    val baseNavigationBarHeight = ExtraNumber.getInternalDimensionSize(mContext, "navigation_bar_height")
    val currentActivity = AppManager.currentActivity()
    return if (null == currentActivity) {
        if (hasNavigationBar(mContext)) {
            baseNavigationBarHeight
        } else {
            0
        }
    } else {
        val decorView = currentActivity.window.decorView
        if (decorView.hasNavigationBar()) {
            val insets = ViewCompat.getRootWindowInsets(decorView)
            insets?.getInsets(WindowInsetsCompat.Type.navigationBars())?.bottom ?: baseNavigationBarHeight
        } else {
            0
        }
    }
}

/**
 * 读取layer-list的xml内的图片数据
 * <layer-list xmlns:android="http://schemas.android.com/apk/res/android">
 *
 *     <item android:drawable="@color/bgWhite" />
 *
 *     <item android:top="104dp">
 *         <bitmap
 *             android:antialias="true"
 *             android:gravity="top|center_horizontal"
 *             android:scaleType="fitXY"
 *             android:width="300dp"
 *             android:height="354dp"
 *             android:src="@mipmap/bg_splash"
 *             android:tileMode="disabled" />
 *     </item>
 *
 * </layer-list>
 * // 1. 目标 item 下标（这里假设是第二个 item，索引为1）
 *  val targetItemIndex = 1
 *  val drawableInfo = context.layerDrawable(R.drawable.layout_list_splash, targetItemIndex)
 *  // 2. 解析 layer-list 资源
 *  val layerDrawable = drawableInfo?.first
 *  // 3. 获取目标 item
 *  val bitmapDrawable = drawableInfo?.second
 *  // 4. 获取XML中定义的item偏移（margin），单位是dp，需转为px
 *  val marginTopDp = layerDrawable?.getLayerInsetTop(targetItemIndex)
 * // val marginLeftDp = layerDrawable?.getLayerInsetLeft(targetItemIndex)
 * // val marginRightDp = layerDrawable?.getLayerInsetRight(targetItemIndex)
 * // val marginBottomDp = layerDrawable?.getLayerInsetBottom(targetItemIndex)
 *  // 5. 获取XML中定义的bitmap宽高（android:width/android:height）注意：如果XML中是wrap_content，需用bitmap自身尺寸
 *  val xmlWidthPx = try {
 *      // 从drawable的固有宽高中获取XML定义的尺寸（仅对显式设置了宽高的有效）
 *      bitmapDrawable?.intrinsicWidth
 *  } catch (e: Exception) {
 *      e.printStackTrace()
 *      0
 *  }.orZero
 *  val xmlHeightPx = try {
 *      bitmapDrawable?.intrinsicHeight
 *  } catch (e: Exception) {
 *      e.printStackTrace()
 *      0
 *  }.orZero
 */
inline fun <reified T : Drawable> getTypedDrawable(@DrawableRes res: Int): T? {
    val mContext = BaseApplication.instance.applicationContext
    val drawable = ResourcesCompat.getDrawable(mContext.resources, res, mContext.theme)
    return drawable as? T
}

inline fun <reified T : Drawable> Context?.getTypedDrawable(@DrawableRes res: Int): T? {
    this ?: return null
    val drawable = ResourcesCompat.getDrawable(resources, res, theme)
    return drawable as? T
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
 * 使用CardView时,4个角都会带有弧度,但是有些xml在绘制时,底部是不需要的
 * 可以外层套一个FrameLayout,背景设为透明,内部套CardView,cardBackgroundColor设为对应纯色或图片,然后调用该扩展
 */
fun View?.adjustRadiusDrawable(@ColorRes color: Int, radius: Int) {
    this ?: return
    val windowBackground = when (background) {
        is ColorDrawable -> background
        is BitmapDrawable, is VectorDrawable -> background
        else -> null
    } ?: color(R.color.appWindowBackground).toDrawable()
    val bottomColor = color(color)
    val bottomDrawable = NavigationBarDrawable(bottomColor)
    bottomDrawable.paint.color = bottomColor
    bottomDrawable.updateNavigationBarHeight(radius)
    val combinedDrawable = LayerDrawable(arrayOf(windowBackground, bottomDrawable))
    background = combinedDrawable
}

/**
 * 设置textview内容当中某一段的颜色
 */
fun TextView?.setSpan(txt: Any, keyword: Any, @ColorRes colorRes: Int = R.color.appTheme, spanAll: Boolean = false) {
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
    setSpannable(if (spanAll) {
        textToProcess.setSpanAll(keywordToProcess, span)
    } else {
        textToProcess.setSpanFirst(keywordToProcess, span)
    })
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

fun TextView?.setSpan(txt: Any, vararg keywords: Pair<Any, () -> Unit>, @ColorRes colorRes: Int = R.color.appTheme) {
    setSpan(txt, *keywords.map {
        Triple(it.first, colorRes, it.second)
    }.toTypedArray())
}

/**
 * 设置显示内容和对应文本颜色
 */
fun TextView?.setTheme(txt: String = "", @ColorRes colorRes: Int = R.color.appTheme, resId: Int = -1) {
    this ?: return
    text = txt
    textColor(colorRes)
    if (-1 != resId) background(resId)
}

/**
 * 列表滑动时移动到某个输入框下
 * 对应页面设置:
 * android:windowSoftInputMode="stateHidden|adjustResize"
 */
fun NestedScrollView?.setScrollTo(insets: WindowInsetsCompat, root: View?, list: List<View?>, onImeShow: () -> Unit = {}, onImeDismiss: () -> Unit = {}) {
    this ?: return
    // 获取软键盘高度及显示状态
    val imeType = WindowInsetsCompat.Type.ime()
    val isImeVisible = insets.isVisible(imeType)
    // 输入法的高度包含了底部导航栏
    val imeHeight = if (isImeVisible) insets.getInsets(imeType).bottom else 0
    // 获取状态栏/导航栏高度
    val statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
    val navigationBarHeight = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
    // 0的情况有些特殊,可能是用户手动隐藏导航栏
    val lastNavigationHeight = getTag(R.id.theme_ime_navigation_tag) as? Int ?: 0
    if (navigationBarHeight != lastNavigationHeight) {
        setTag(R.id.theme_ime_navigation_tag, navigationBarHeight)
        root.size(MATCH_PARENT, MATCH_PARENT)
    }
    // 从 tag 中获取当前实例的 lastImeBottom，默认为 0
    val lastImeHeight = getTag(R.id.theme_ime_height_tag) as? Int ?: 0
    // 只有当任意值变化时，才触发回调（避免频繁调用）
    if (imeHeight != lastImeHeight) {
        // 保存当前值到 tag 中
        setTag(R.id.theme_ime_height_tag, imeHeight)
        // 最外层父类布局的高度是不变的,此处拉取一次作为复位的计算值
        val parentHeight = (root?.parent as? ViewGroup)?.height ?: return
        // 此次是否是显示输入法
        val isShow = imeHeight != 0
        // 此处的root是mBinding.root,我们设置它缩小/放大就可以让NestedScrollView被挤压从而实现滚动
        // 重设外层父类大小/页面采用的是EdgeToEdge,一整个屏幕都是手机app的操作空间,所以要把系统弹出的输入法的底部高度加回去
        if (isShow) {
            root.size(height = parentHeight - imeHeight + navigationBarHeight)
        } else {
            root.size(height = parentHeight)
        }
        // 开始循环传入的输入框集合,判断对应输入框是否处于有焦点状态,并获取y轴高度(滚动距离)
        for (v in list) {
//            if (!v?.isFocused.orFalse) continue
            val actualInputView = getActualInputView(v)
            // 若不是输入类控件，或输入控件未聚焦，直接跳过
            if (actualInputView == null || !actualInputView.isFocused) continue
            val topY = v?.getScreenLocation()?.get(1).orZero
            val top = (topY - scrollY.orZero)
            val bottom = top + v?.height.orZero
            if (top < statusBarHeight) {
                scrollTo(0, topY - statusBarHeight)
            } else if (bottom > height.orZero) {
                scrollTo(0, bottom - height.orZero)
            }
        }
        if (isShow) {
            onImeShow.invoke()
        } else {
            onImeDismiss.invoke()
        }
    }
}

/**
 * 统一获取控件内部的“实际输入 View”（EditText）
 */
private fun getActualInputView(v: View?): EditText? {
    return when (v) {
        is ClearEditText -> v.editText
        is PasswordEditText -> v.editText
        is EditText -> v
        // 后续新增自定义输入控件，只需在这里加分支
        // is SearchEditText -> v.editText
        else -> null // 非输入类控件，返回null
    }
}

/**
 * 联动滑动时某个控件显影，传入对应控件的高度（dp）
 */
fun NestedScrollView?.addAlphaListener(menuHeight: Int, func: (alpha: Float) -> Unit?) {
    this ?: return
    setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { _, _, scrollY, _, _ ->
        // 确保menuHeight不为0，避免除零异常
        if (menuHeight <= 0) {
            func(0f)
            return@OnScrollChangeListener
        }
        // 计算透明度：在0到menuHeight范围内从0平滑过渡到1
        val alpha = (scrollY.toFloat() / menuHeight).coerceIn(0f, 1f)
        func(alpha)
//        func.invoke(if (scrollY <= menuHeight / 2f) 0 + scrollY / (menuHeight / 4f) else 1f)
    })
}

//private static final int SCROLL_THRESHOLD = 500;
//
//scrollView.setOnScrollChangeListener(new ViewTreeObserver.OnScrollChangedListener() {
//    @Override
//    public void onScrollChanged() {
//        // 获取当前滚动的垂直距离（像素）
//        int scrollY = scrollView.getScrollY();
//        // 限制范围并计算透明度
//        int clampedScrollY = Math.max(0, Math.min(scrollY, SCROLL_THRESHOLD));
//        float alpha = (float) clampedScrollY / SCROLL_THRESHOLD;
//        // 应用透明度
//        backgroundBlock.setAlpha(alpha);
//    }
//})

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
     * 获取状态栏/导航栏高度
     * 直接取系统层配置的像素值作为保底措施
     */
    @JvmStatic
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
        } catch (_: Resources.NotFoundException) {
            return 0
        }
        return result
    }

}