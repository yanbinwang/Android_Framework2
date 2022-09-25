package com.example.base.utils.function.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Paint
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.base.utils.function.string
import java.util.*

//------------------------------------view扩展函数类------------------------------------
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
 * 判断是否可见
 */
fun View?.isVisible(): Boolean {
    if (this == null) return false
    return this.visibility == View.VISIBLE
}

/**
 * 显示view
 */
fun View?.visible() {
    if (this == null) return
    if (visibility == View.VISIBLE) return
    this.visibility = View.VISIBLE
}

/**
 * 不显示view
 */
fun View?.invisible() {
    if (this == null) return
    if (visibility == View.INVISIBLE) return
    this.visibility = View.INVISIBLE
}

/**
 * 隐藏view
 */
fun View?.gone() {
    if (this == null) return
    if (visibility == View.GONE) return
    this.visibility = View.GONE
}

/**
 * 有效化
 */
fun View?.enable() {
    if (this == null) return
    if (isEnabled) return
    isEnabled = true
}

/**
 * 无效化
 */
fun View?.disable() {
    if (this == null) return
    if (!isEnabled) return
    isEnabled = false
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
 */
fun View?.size(width: Int? = null, height: Int? = null) {
    if (this == null) return
    val lp = layoutParams
    height?.let { layoutParams?.height = it }
    width?.let { layoutParams?.width = it }
    layoutParams = lp ?: ViewGroup.LayoutParams(width ?: ViewGroup.LayoutParams.WRAP_CONTENT, height ?: ViewGroup.LayoutParams.WRAP_CONTENT)
}

/**
 * 在layout完毕之后进行计算处理
 */
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
fun View?.text(): String? {
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
 */
fun ViewGroup.string(@StringRes res: Int) = context.string(res)

/**
 * 传入上下文获取绘制的item
 */
fun ViewGroup.inflate(@LayoutRes res: Int, attachToRoot: Boolean): View {
    return LayoutInflater.from(this.context).inflate(res, this, attachToRoot)
}

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