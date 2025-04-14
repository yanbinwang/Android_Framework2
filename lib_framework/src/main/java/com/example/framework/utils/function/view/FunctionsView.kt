package com.example.framework.utils.function.view

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.GradientDrawable.OVAL
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AccelerateInterpolator
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.Interpolator
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import android.view.animation.TranslateAnimation
import android.view.inputmethod.InputMethodManager
import android.widget.BaseExpandableListAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ExpandableListView
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.TextView
import androidx.annotation.AnimRes
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.annotation.DrawableRes
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.lifecycle.LifecycleOwner
import com.example.framework.utils.function.color
import com.example.framework.utils.function.doOnDestroy
import com.example.framework.utils.function.string
import com.example.framework.utils.function.value.orZero
import com.example.framework.utils.function.value.parseColor
import com.example.framework.utils.logE
import com.google.android.material.appbar.AppBarLayout
import java.util.WeakHashMap
import java.util.concurrent.atomic.AtomicBoolean
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

fun ((View) -> Unit).clicks(vararg v: View?, time: Long = 500L) {
    val listener = object : OnMultiClickListener(time) {
        override fun onMultiClick(v: View) {
            this@clicks(v)
        }
    }
    v.forEach {
        it?.setOnClickListener(listener)
    }
}

fun View.OnClickListener.clicks(vararg v: View?, time: Long = 500L) {
    val listener = object : OnMultiClickListener(time) {
        override fun onMultiClick(v: View) {
            this@clicks.onClick(v)
        }
    }
    v.forEach {
        it?.setOnClickListener(listener)
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

///**
// * 判断是否可见
// */
//fun View?.isVisible(): Boolean {
//    if (this == null) return false
//    return this.visibility == View.VISIBLE
//}

/**
 * 显示view
 */
fun View?.visible() {
    if (this == null) return
//    if (visibility == View.VISIBLE) return
    if (isVisible) return
    this.visibility = View.VISIBLE
}

/**
 * 不显示view
 */
fun View?.invisible() {
    if (this == null) return
//    if (visibility == View.INVISIBLE) return
    if (isInvisible) return
    this.visibility = View.INVISIBLE
}

/**
 * 隐藏view
 */
fun View?.gone() {
    if (this == null) return
//    if (visibility == View.GONE) return
    if (isGone) return
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
 * 获取resources中的drawable
 */
fun View?.dimen(@DimenRes res: Int): Float {
    this ?: return 0f
    return context.resources.getDimension(res)
}

/**
 * 使用视图的 id + 业务后缀生成唯一键（hashCode 可能冲突，但概率极低）
 */
fun View?.generateTagKey(keySuffix: String): Int {
    this ?: return 0
    return ("${this.hashCode()}::$keySuffix").hashCode()
}

/**
 * 背景
 */
fun View?.background(@DrawableRes bg: Int) {
    if (this == null) return
    this.setBackgroundResource(bg)
}

/**
 * 减少本地背景文件的绘制，直接代码绘制
 * colorString 颜色字符 -> "#cf111111"
 * radius 圆角 -> 传入X.ptFloat,代码添加一个对应圆角的背景
 */
fun View?.backgroundCorner(colorString: String, radius: Float) {
    if (this == null) return
    this.background = GradientDrawable().apply {
        setColor(colorString.parseColor())
        cornerRadius = radius
    }
}

fun View?.backgroundOval(colorString: String) {
    if (this == null) return
    this.background = GradientDrawable().apply {
        shape = OVAL
        setColor(colorString.parseColor())
    }
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
inline fun <T : View> T?.doOnceAfterLayout(crossinline listener: (T) -> Unit) {
    if (this == null) return
    viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
        override fun onGlobalLayout() {
            viewTreeObserver.removeOnGlobalLayoutListener(this)
            listener(this@doOnceAfterLayout)
        }
    })
}

/**
 * 列表频繁刷新时除外层重写equals和hashcode方法外，内部赋值再嵌套一层做比较
 */
inline fun <T> View?.setItem(any: Any?, crossinline listener: (View, T?) -> Unit) {
    if (this == null) return
    if (null == tag) tag = any
    listener.invoke(this, tag as? T)
}

/**
 * 开启软键盘
 * 某些页面底部需要有留言版
 */
fun View?.openDecor() {
    if (this == null) return
    focus()
    val inputMethodManager = context?.getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE) as? InputMethodManager
    inputMethodManager?.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS)
}

/**
 * 关闭软键盘
 */
fun View?.closeDecor() {
    if (this == null) return
    val inputMethodManager = context?.getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE) as? InputMethodManager
    inputMethodManager?.hideSoftInputFromWindow(windowToken, 0)
}

/**
 * 震动
 */
@SuppressLint("MissingPermission")
fun View?.vibrate(milliseconds: Long) {
    if (this == null) return
    val vibrator = (context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator)
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
        vibrator?.vibrate(milliseconds)
    } else {
        vibrator?.vibrate(VibrationEffect.createOneShot(milliseconds, VibrationEffect.DEFAULT_AMPLITUDE))
    }
}

/**
 * 动画隐藏view
 */
fun View?.fade(time: Long = 500, cancelAnim: Boolean = true) {
    if (this == null) return
    if (!this.isVisible) return
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
 * 透明度
 * @param from 0f-1f
 * @param to 0f-1f
 */
fun View?.alpha(from: Float, to: Float, timeMS: Long, endListener: (() -> Unit)? = null) {
    this ?: return
    animation?.setAnimationListener(null)
    animation?.cancel()
    val anim = AlphaAnimation(from, to)
    if (to == 1f) visible()
    anim.fillAfter = false // 设置保持动画最后的状态
    anim.duration = timeMS // 设置动画时间
    anim.interpolator = AccelerateInterpolator() // 设置插入器3
    anim.setAnimationListener(object : Animation.AnimationListener {
        override fun onAnimationEnd(animation: Animation?) {
            endListener?.invoke() ?: if (to == 0f) {
                gone()
            } else {
                visible()
            }
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
    if (this.isVisible) return
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
 * 展开页面按钮的动画，传入是否是展开状态
 */
fun View?.rotate(default: Boolean = true): Boolean {
    if (this == null) return false
    if (animation != null) {
        if (animation.hasStarted() && !animation.hasEnded()) return false
    }
    val isRotate = tag as? Boolean ?: default
    val startRot = if (isRotate) 180f else 0f
    val endRot = if (isRotate) 0f else 180f
    tag = !isRotate
    val anim = AnimatorSet()
    anim.playTogether(ObjectAnimator.ofFloat(this, "rotation", startRot, endRot))
    anim.duration = 500
    anim.start()
    return isRotate
}

/**
 * 旋轉
 */
fun View?.rotate(time: Long = 500, cancelAnim: Boolean = true) {
    if (this == null) return
    if (cancelAnim) {
        cancelAnim()
    } else if (animation != null) {
        if (animation.hasStarted() && !animation.hasEnded()) {
            return
        }
    }
    val anim = AnimatorSet()
    anim.playTogether(ObjectAnimator.ofFloat(this, "rotation", 0f, 360f))
    anim.duration = time
    anim.start()
}

/**
 * 旋转
 */
fun View?.rotate(from: Float, to: Float, timeMS: Long, interpolator: Interpolator = AccelerateDecelerateInterpolator(), repeat: Boolean = false) {
    this ?: return
    animation?.cancel()
    val anim = RotateAnimation(from, to, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
    anim.fillAfter = true // 设置保持动画最后的状态
    anim.duration = timeMS // 设置动画时间3
    if (repeat) anim.repeatCount = -1
    anim.interpolator = interpolator // 设置插入器
    startAnimation(anim)
}

/**
 * 移动
 * @param type Animation.ABSOLUTE, Animation.RELATIVE_TO_SELF, or Animation.RELATIVE_TO_PARENT.
 */
fun View?.move(xFrom: Float, xTo: Float, yFrom: Float, yTo: Float, timeMS: Long, fillAfter: Boolean = true, onStart: (() -> Unit)? = null, onEnd: (() -> Unit)? = null, type: Int = Animation.RELATIVE_TO_SELF, interpolator: Interpolator = LinearInterpolator()) {
    this ?: return
    animation?.setAnimationListener(null)
    animation?.cancel()
    val anim = TranslateAnimation(type, xFrom, type, xTo, type, yFrom, type, yTo)
    if (fillAfter) anim.fillAfter = true //设置保持动画最后的状态
    anim.duration = timeMS //设置动画时间
    anim.interpolator = interpolator //设置插入器
    if (onEnd != null || onStart != null) {
        anim.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationEnd(animation: Animation?) {
                onEnd?.invoke()
            }
            override fun onAnimationStart(animation: Animation?) {
                onStart?.invoke()
            }
            override fun onAnimationRepeat(animation: Animation?) {}
        })
    }
    startAnimation(anim)
}

/**
 * 平移动画
 * 参数：0f, ScreenUtils.getScreenW(context).toFloat()
 * .doOnEnd->动画结束后
 */
fun View?.translationX(vararg values: Float): ObjectAnimator? {
    this ?: return null
    return ObjectAnimator.ofFloat(this, "translationX", *values)
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
 * 动画循环
 */
fun View?.loopAnimation(anim: Animation) {
    this ?: return
    try {
        clearAnimation()
        anim.apply {
            repeatMode = Animation.RESTART
            repeatCount = Animation.INFINITE
        }
        startAnimation(anim)
        animation?.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {
            }

            override fun onAnimationEnd(animation: Animation?) {
                startAnimation(anim)
            }

            override fun onAnimationRepeat(animation: Animation?) {
            }
        })
    } catch (e: Exception) {
        e.logE
    }
}

/**
 * 动画循环
 */
fun View?.loopAnimation(ctx: Context?, @AnimRes animRes: Int) {
    this ?: return
    ctx ?: return
    val anim = AnimationUtils.loadAnimation(ctx, animRes)
    loopAnimation(anim)
}

/**
 * 动画循环
 */
fun View?.startAnimation(@AnimRes animRes: Int) {
    this ?: return
    val anim = AnimationUtils.loadAnimation(context, animRes)
    clearAnimation()
    startAnimation(anim)
}

/**
 * 动画停止
 */
fun View?.cancelAnimation() {
    this ?: return
    try {
        animation?.setAnimationListener(null)
        animation?.cancel()
    } catch (e: Exception) {
        e.logE
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
 * 控件获取焦点
 */
fun View?.focus() {
    if (this == null) return
    isFocusable = true //设置输入框可聚集
    isFocusableInTouchMode = true //设置触摸聚焦
    requestFocus() //请求焦点
    findFocus() //获取焦点
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
 * 当一个容器内的view在被滑动时，如果执行取消刷新的操作，并不会执行，故而先传递一个取消的事件（模拟手指离开屏幕）
 * header的onDragListener中关闭refresh的刷新前，先调用取消的action，告知系统手势离开屏幕，然后再关闭
 * ->(refresh.parent as? ViewGroup)?.dispatchTouchEvent....
 */
fun ViewGroup?.actionCancel() {
    if (this == null) return
    dispatchTouchEvent(MotionEvent.obtain(System.currentTimeMillis(), System.currentTimeMillis(), MotionEvent.ACTION_CANCEL, 0f, 0f, 0))
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
 * 如果是context调用inflate，因为并未插入父布局，所以layoutParams是获取不到的，会造成宽高绘制不正常
 * 故而使用父布局的inflate可以确保获取到准确的layoutParams
 *  fun addToParent(parent: ViewGroup) {
 *  val view = getView()
 *  parent.addView(view)
 *  view.size(MATCH_PARENT, WRAP_CONTENT)
 *  }
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
 * setImageResource()里面是int类型 无法使用setImageResource来清空图片,不过Bitmap可以设置为nul从而达到设置为空的效果
 * 设置setImageDrawable(null)
 */
fun ImageView?.setDrawable(resId: Drawable?) {
    this ?: return
    setImageDrawable(resId)
}

fun ImageView?.setResource(@DrawableRes resId: Int) {
    this ?: return
    //‌调用setImageResource(0)会导致ImageView显示一个默认的占位符图片，而不是显示任何有效的图像资源‌
    //-1则会闪退报错
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
 * 设置一个新的bitmap
 */
//用于存储每个 ImageView 的订阅状态
private val subscriptionMap by lazy { WeakHashMap<ImageView, AtomicBoolean>() }

fun ImageView?.setBitmap(observer: LifecycleOwner, bit: Bitmap?) {
    if (this == null || bit == null) return
    //检查是否已经订阅过
    val isSubscribed = subscriptionMap.getOrPut(this) { AtomicBoolean(false) }
    if (!isSubscribed.getAndSet(true)) {
        observer.doOnDestroy {
            recycle()
            //移除订阅状态标记
            subscriptionMap.remove(this)
        }
    }
    recycle()
    setImageBitmap(bit)
}

/**
 * imageview回收
 */
fun ImageView?.recycle() {
    this ?: return
    val mDrawable = this.drawable
    if (mDrawable is BitmapDrawable) {
        val bitmap = mDrawable.bitmap
        if (!bitmap.isRecycled) {
            bitmap.recycle()
        }
    }
}

/**
 * appbar监听
 */
fun AppBarLayout?.stateChanged(func: (state: AppBarStateChangeListener.State?) -> Unit?) {
    this ?: return
    addOnOffsetChangedListener(object : AppBarStateChangeListener() {
        override fun onStateChanged(appBarLayout: AppBarLayout?, state: State?) {
            func.invoke(state)
        }
    })
}

/**
 * 可折叠list初始化
 */
fun ExpandableListView?.init(adapter: BaseExpandableListAdapter) {
    this ?: return
    setGroupIndicator(null)//去除右侧箭头
    setOnGroupClickListener { _, _, _, _ -> true }//使列表不能点击收缩
    setAdapter(adapter)
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