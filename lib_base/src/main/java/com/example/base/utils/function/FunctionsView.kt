package com.example.base.utils.function

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Paint
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.text.*
import android.text.method.HideReturnsTransformationMethod
import android.text.method.LinkMovementMethod
import android.text.method.PasswordTransformationMethod
import android.text.style.ClickableSpan
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.base.utils.DecimalInputFilter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import java.util.*

//------------------------------------view扩展函数类------------------------------------
/**
 * 设置按钮显影图片
 */
fun ImageView.setSwitchResource(display: Boolean, showId: Int, hideId: Int) = setImageResource(if (!display) showId else hideId)

/**
 * 文案添加点击事件（单一）
 */
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
            transformationMethod = PasswordTransformationMethod.getInstance()
            display = false
        } else {
            transformationMethod = HideReturnsTransformationMethod.getInstance()
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
 * 简易Edittext监听
 */
fun TextWatcher.textWatcher(vararg views: EditText) {
    for (view in views) {
        view.addTextChangedListener(this)
    }
}

/**
 * ViewPager2隐藏fadingEdge
 */
fun ViewPager2?.hideFadingEdge() {
    if (this == null) return
    try {
        getRecyclerView()?.overScrollMode = RecyclerView.OVER_SCROLL_NEVER
    } catch (ignore: Exception) {
    }
}

/**
 * ViewPager2获取内部RecyclerView
 */
fun ViewPager2?.getRecyclerView(): RecyclerView? {
    if (this == null) return null
    return try {
        (getChildAt(0) as RecyclerView)
    } catch (ignore: Exception) {
        null
    }
}

/**
 * ViewPager2向后翻页
 */
fun ViewPager2?.nextPage(isSmooth: Boolean = true) {
    if (this == null) return
    adapter?.let { adapter ->
        if (adapter.itemCount == 0) return
        setCurrentItem(currentItem + 1, isSmooth)
    }
}

/**
 * ViewPager2向前翻页
 */
fun ViewPager2?.prevPage(isSmooth: Boolean = true) {
    if (this == null) return
    adapter?.let { adapter ->
        if (adapter.itemCount == 0) return
        setCurrentItem(currentItem - 1, isSmooth)
    }
}

/**
 * 绑定vp和tab
 * */
fun ViewPager2?.bind(tab: TabLayout?, listener: TabLayoutMediator.TabConfigurationStrategy = TabLayoutMediator.TabConfigurationStrategy { _, _ -> }): TabLayoutMediator? {
    return TabLayoutMediator(tab ?: return null, this ?: return null) { _, _ -> }.apply { attach() }
}

/**
 * 绑定vp和tab
 * */
fun TabLayout?.bind(vp: ViewPager2?, listener: TabLayoutMediator.TabConfigurationStrategy = TabLayoutMediator.TabConfigurationStrategy { _, _ -> }): TabLayoutMediator? {
    return TabLayoutMediator(this ?: return null, vp ?: return null, listener).apply { attach() }
}

/**
 * 设置TabLayout的边距
 * */
fun TabLayout?.paddingEdge(start: Int? = null, top: Int? = null, end: Int? = null, bottom: Int? = null) {
    this ?: return
    val view = (getChildAt(0) as? ViewGroup)?.getChildAt(0) as? ViewGroup
    view?.padding(start, top, end, bottom)
    view?.clipToPadding = false
}

/**
 * 震动
 */
@SuppressLint("MissingPermission")
fun View.vibrate(milliseconds: Long) {
    val vibrator = (context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator)
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
        vibrator.vibrate(milliseconds)
    } else {
        vibrator.vibrate(VibrationEffect.createOneShot(milliseconds, VibrationEffect.DEFAULT_AMPLITUDE))
    }
}

/**
 * 开启硬件加速
 */
fun View?.byHardwareAccelerate(paint: Paint? = Paint()) {
    if (this == null) return
    setLayerType(View.LAYER_TYPE_HARDWARE, paint)
}

/**
 * 关闭硬件加速
 */
fun View?.stopHardwareAccelerate() {
    if (this == null) return
    setLayerType(View.LAYER_TYPE_SOFTWARE, Paint())
}

/**
 * 设置margin，单位px
 */
fun View?.margin(start: Int? = null, top: Int? = null, end: Int? = null, bottom: Int? = null) {
    if (this == null) return
    val lp = layoutParams as? ViewGroup.MarginLayoutParams ?: return
    start?.let {
        lp.marginStart = it
        lp.leftMargin = it
    }
    top?.let { lp.topMargin = it }
    end?.let {
        lp.marginEnd = it
        lp.rightMargin = it
    }
    bottom?.let { lp.bottomMargin = it }
    layoutParams = lp
}

/**
 * 设置padding，单位px
 */
fun View?.padding(start: Int? = null, top: Int? = null, end: Int? = null, bottom: Int? = null) {
    if (this == null) return
    setPaddingRelative(start ?: paddingStart, top ?: paddingTop, end ?: paddingEnd, bottom ?: paddingBottom)
}

/**
 * 设置padding，单位px
 */
fun View?.paddingAll(padding: Int) {
    if (this == null) return
    setPaddingRelative(padding, padding, padding, padding)
}

/**
 * 调整view大小
 * @param width  可使用MATCH_PARENT和WRAP_CONTENT，传null或者不传为不变
 * @param height 可使用MATCH_PARENT和WRAP_CONTENT，传null或者不传为不变
 * */
fun View?.size(width: Int? = null, height: Int? = null) {
    if (this == null) return
    val lp = layoutParams
    height?.let {
        layoutParams?.height = it
    }
    width?.let {
        layoutParams?.width = it
    }
    layoutParams = lp ?: ViewGroup.LayoutParams(width ?: ViewGroup.LayoutParams.WRAP_CONTENT, height ?: ViewGroup.LayoutParams.WRAP_CONTENT)
}

/**
 * 在layout完毕之后进行计算处理
 * */
fun <T : View> T?.doOnceAfterLayout(listener: (T) -> Unit) {
    if (this == null) return
    viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
        override fun onGlobalLayout() {
            viewTreeObserver.removeOnGlobalLayoutListener(this)
            listener(this@doOnceAfterLayout)
        }
    })
}

/**
 * 开启软键盘
 */
fun View?.openDecor() {
    closeDecor()
    val view = this
    Timer().schedule(object : TimerTask() {
        override fun run() {
            (view?.context?.getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE) as InputMethodManager).toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS)
        }
    }, 200)
    val inputMethodManager = this?.context?.getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.showSoftInput(this, 2)
}

/**
 * 关闭软键盘
 */
fun View?.closeDecor() {
    val inputMethodManager = this?.context?.getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(windowToken, 0)
}

/**
 * 控件获取焦点
 */
fun View?.focus() {
    this?.isFocusable = true //设置输入框可聚集
    this?.isFocusableInTouchMode = true //设置触摸聚焦
    this?.requestFocus() //请求焦点
    this?.findFocus() //获取焦点
}

/**
 * 控件获取默认值
 */
fun View?.parameters(): String? {
    return when (this) {
        is EditText -> text.toString().trim { it <= ' ' }
        is TextView -> text.toString().trim { it <= ' ' }
        is CheckBox -> text.toString().trim { it <= ' ' }
        is RadioButton -> text.toString().trim { it <= ' ' }
        is Button -> text.toString().trim { it <= ' ' }
        else -> null
    }
}

/**
 * 清空点击
 */
fun View?.clearClick() {
    if (this == null) return
    this.setOnClickListener(null)
    this.isClickable = false
}

/**
 * 防止重复点击
 */
fun View?.click(click: ((v: View) -> Unit)?) {
    if (click == null) {
        clearClick()
    } else {
        click(500L, click)
    }
}

/**
 * 防止重复点击
 * 默认500ms
 */
fun View?.click(time: Long = 500L, click: (v: View) -> Unit) {
    if (this == null) return
    this.setOnClickListener(object : OnMultiClickListener(time, click) {})
}

/**
 * 半秒不可重复点击
 */
fun View.OnClickListener.clicks(vararg v: View, time: Long = 500L) {
    val listener = object : OnMultiClickListener(time) {
        override fun onMultiClick(v: View) {
            this@clicks.onClick(v)
        }
    }
    v.forEach {
        it.setOnClickListener(listener)
    }
}

/**
 * 获取resources中的color
 */
fun ViewGroup.color(@ColorRes res: Int) = ContextCompat.getColor(context, res)

/**
 * 获取resources中的drawable
 */
fun ViewGroup.drawable(@DrawableRes res: Int) = ContextCompat.getDrawable(context, res)

/**
 * 获取Resources中的String
 * */
fun ViewGroup.string(@StringRes res: Int) = context.string(res)

/**
 * 防止多次点击, 至少要500毫秒的间隔
 */
abstract class OnMultiClickListener(private val time: Long = 500, var click: (v: View) -> Unit = {}) : View.OnClickListener {
    private var lastClickTime: Long = 0

    open fun onMultiClick(v: View) {
        click(v)
    }

    @Deprecated("请勿覆写此方法")
    override fun onClick(v: View) {
        val currentTimeNano = System.nanoTime() / 1000000L
        // 超过点击间隔后再将lastClickTime重置为当前点击时间
        if (currentTimeNano - lastClickTime >= time) {
            lastClickTime = currentTimeNano
            onMultiClick(v)
        }
    }
}

/**
 * 简易的输入监听
 */
abstract class SimpleTextWatcher : TextWatcher {
    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
    }

    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
    }

    override fun afterTextChanged(s: Editable) {
    }
}