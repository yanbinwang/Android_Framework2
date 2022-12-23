package com.example.framework.utils.function.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Paint
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.*
import android.view.animation.AccelerateInterpolator
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.example.framework.utils.function.color
import com.example.framework.utils.function.string
import com.example.framework.utils.function.value.orZero
import com.google.android.material.appbar.AppBarLayout
import kotlin.math.abs

//------------------------------------view扩展函数类------------------------------------
/**
 * 防止重复点击
 * 默认500ms
 */
fun View?.click(time: Long = 500L, click: (v: View) -> Unit) {
    if (this == null) return
    this.setOnClickListener(object : OnMultiClickListener(time, click) {})
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

fun ((View) -> Unit).clicks(vararg v: View, time: Long = 500L) {
    val listener = object : OnMultiClickListener(time) {
        override fun onMultiClick(v: View) {
            this@clicks(v)
        }
    }
    v.forEach {
        it.setOnClickListener(listener)
    }
}

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
 * 清空点击
 */
fun View?.clearClick() {
    if (this == null) return
    this.setOnClickListener(null)
    this.isClickable = false
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
 * 背景
 */
fun View?.background(@DrawableRes bg: Int) {
    if (this == null) return
    this.setBackgroundResource(bg)
}

/**
 * 清除背景
 */
fun View?.clearBackground() {
    if (this == null) return
    this.background = null
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
 * view的weight（仅在LinearLayout下生效）
 */
var View?.weight: Float
    get() {
        return (this?.layoutParams as? LinearLayout.LayoutParams)?.weight.orZero
    }
    set(value) {
        this ?: return
        (layoutParams as? LinearLayout.LayoutParams)?.weight = value
    }

/**
 * view的horizontalWeight（仅在ConstraintLayout下生效）
 */
var View?.horizontalWeight: Float
    get() {
        return (this?.layoutParams as? ConstraintLayout.LayoutParams)?.horizontalWeight.orZero
    }
    set(value) {
        this ?: return
        (layoutParams as? ConstraintLayout.LayoutParams)?.horizontalWeight = value
    }

/**
 * view的verticalWeight（仅在ConstraintLayout下生效）
 */
var View?.verticalWeight: Float
    get() {
        return (this?.layoutParams as? ConstraintLayout.LayoutParams)?.verticalWeight.orZero
    }
    set(value) {
        this ?: return
        (layoutParams as? ConstraintLayout.LayoutParams)?.verticalWeight = value
    }

/**
 * 设置layoutGravity，只对LinearLayout和FrameLayout有效，有需要则自行添加其他view
 */
var View?.layoutGravity: Int
    get() {
        return when (this?.parent) {
            is LinearLayout -> {
                (this.layoutParams as LinearLayout.LayoutParams).gravity
            }
            is FrameLayout -> {
                (this.layoutParams as FrameLayout.LayoutParams).gravity
            }
            else -> {
                Gravity.NO_GRAVITY
            }
        }
    }
    set(value) {
        when (this?.parent) {
            is LinearLayout -> {
                val lp = this.layoutParams as LinearLayout.LayoutParams
                lp.gravity = value
                this.layoutParams = lp
            }
            is FrameLayout -> {
                val lp = this.layoutParams as FrameLayout.LayoutParams
                lp.gravity = value
                this.layoutParams = lp
            }
            else -> {
            }
        }
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
    focus()
    val inputMethodManager = this?.context?.getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS)
}

/**
 * 关闭软键盘
 */
fun View?.closeDecor() {
    val inputMethodManager = this?.context?.getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(windowToken, 0)
}

/**
 * 震动
 */
@SuppressLint("MissingPermission")
fun View?.vibrate(milliseconds: Long) {
    if (this == null) return
    val vibrator = (context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator)
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
        vibrator.vibrate(milliseconds)
    } else {
        vibrator.vibrate(VibrationEffect.createOneShot(milliseconds, VibrationEffect.DEFAULT_AMPLITUDE))
    }
}

/**
 * 动画隐藏view
 */
fun View?.fade(time: Long = 500, cancelAnim: Boolean = true) {
    if (this == null) return
    if (!this.isVisible()) return
    if (time <= 0) {
        gone()
        return
    }
    if (cancelAnim) {
        cancelAnim()
    } else if (animation != null) {
        if (animation.hasStarted() && !animation.hasEnded()) {
            return
        }
    }

    val anim = AlphaAnimation(1f, 0f)
    anim.fillAfter = false // 设置保持动画最后的状态
    anim.duration = time // 设置动画时间
    anim.interpolator = AccelerateInterpolator() // 设置插入器3
    anim.setAnimationListener(object : Animation.AnimationListener {
        override fun onAnimationEnd(animation: Animation?) {
            gone()
        }

        override fun onAnimationStart(animation: Animation?) {}
        override fun onAnimationRepeat(animation: Animation?) {}
    })
    startAnimation(anim)
}

/**
 * 动画显示view
 */
fun View?.appear(time: Long = 500, cancelAnim: Boolean = true) {
    if (this == null) return
    if (this.isVisible()) return
    if (time <= 0) {
        visible()
        return
    }
    if (cancelAnim) {
        cancelAnim()
    } else if (animation != null) {
        if (animation.hasStarted() && !animation.hasEnded()) {
            return
        }
    }
    visible()
    val anim = AlphaAnimation(0f, 1f)
    anim.fillAfter = false // 设置保持动画最后的状态
    anim.duration = time // 设置动画时间
    anim.interpolator = AccelerateInterpolator() // 设置插入器3
    anim.setAnimationListener(object : Animation.AnimationListener {
        override fun onAnimationEnd(animation: Animation?) {
            visible()
        }

        override fun onAnimationStart(animation: Animation?) {}
        override fun onAnimationRepeat(animation: Animation?) {}
    })
    startAnimation(anim)
}

/**
 * 取消View的动画
 */
fun View?.cancelAnim() {
    this ?: return
    animation?.setAnimationListener(null)
    animation?.cancel()
    animate()?.setUpdateListener(null)
    animate()?.setListener(null)
    animate()?.cancel()
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
 * trim { it <= ' ' }//避免某些特殊空格字符的被切除掉
 */
fun View?.text(): String {
    return when (this) {
        is EditText -> text.toString().trim { it <= ' ' }
        is TextView -> text.toString().trim { it <= ' ' }
        is CheckBox -> text.toString().trim { it <= ' ' }
        is RadioButton -> text.toString().trim { it <= ' ' }
        is Button -> text.toString().trim { it <= ' ' }
        else -> ""
    }
}

/**
 * 遍历父布局的child，批量隐藏或显示
 */
fun ViewGroup?.foreachChild(loop: (View) -> Unit) {
    if (this == null) return
    for (i in 0 until this.childCount) {
        loop(this.getChildAt(i))
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
fun ViewGroup.inflate(@LayoutRes res: Int, attachToRoot: Boolean) = LayoutInflater.from(context).inflate(res, this, attachToRoot)

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
 * 设置覆盖色
 */
fun ImageView?.tint(@ColorRes res: Int) {
    this ?: return
    setColorFilter(context.color(res))
}

/**
 * 图片src资源
 */
fun ImageView?.imageResource(@DrawableRes resId: Int) {
    this ?: return
    setImageResource(resId)
}

/**
 * 设置按钮显影图片
 */
fun ImageView?.setResource(triple: Triple<Boolean, Int, Int>) {
    this ?: return
    setImageResource(if (!triple.first) triple.third else triple.second)
}

/**
 * appbar监听
 */
fun AppBarLayout?.stateChanged(onStateChanged: (state: AppBarStateChangeListener.State?) -> Unit?) {
    this ?: return
    addOnOffsetChangedListener(object : AppBarStateChangeListener() {
        override fun onStateChanged(appBarLayout: AppBarLayout?, state: State?) {
            onStateChanged.invoke(state)
        }
    })
}

/**
 * appbar是否显示折叠的监听，用于解决刷新套广告套控件卡顿的问题，需要注意绘制时，底部如果不使用
 * NestedScrollView或者viewpager2等带有滑动事件传递的控件，会造成只有顶部套的部分可以滑动
 */
abstract class AppBarStateChangeListener : AppBarLayout.OnOffsetChangedListener {
    enum class State {
        EXPANDED, COLLAPSED, IDLE//展开，折叠，中间
    }

    private var mCurrentState = State.IDLE

    override fun onOffsetChanged(appBarLayout: AppBarLayout?, verticalOffset: Int) {
        mCurrentState = if (verticalOffset == 0) {
            if (mCurrentState != State.EXPANDED) onStateChanged(appBarLayout, State.EXPANDED)
            State.EXPANDED
        } else if (abs(verticalOffset) >= appBarLayout?.totalScrollRange.orZero) {
            if (mCurrentState != State.COLLAPSED) onStateChanged(appBarLayout, State.COLLAPSED)
            State.COLLAPSED
        } else {
            if (mCurrentState != State.IDLE) onStateChanged(appBarLayout, State.IDLE)
            State.IDLE
        }
    }

    abstract fun onStateChanged(appBarLayout: AppBarLayout?, state: State?)

}