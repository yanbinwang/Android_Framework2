package com.example.framework.utils.function.view

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.AnimationSet
import android.view.animation.AnimationUtils
import android.view.animation.Interpolator
import android.view.animation.ScaleAnimation
import android.view.animation.TranslateAnimation
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import androidx.core.graphics.toColorInt
import com.example.framework.utils.function.value.orZero
import com.example.framework.utils.function.value.toSafeFloat
import com.example.framework.utils.function.value.toSafeInt
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

/**
 * 弹性进入
 */
fun Context.elasticIn(): AnimationSet {
    return AnimationSet(this, null).apply {
        val alpha = AlphaAnimation(0.0f, 1.0f)
        alpha.duration = 90
        val scale1 = ScaleAnimation(0.8f, 1.05f, 0.8f, 1.05f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
        scale1.duration = 135
        val scale2 = ScaleAnimation(1.05f, 0.95f, 1.05f, 0.95f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
        scale2.duration = 105
        scale2.startOffset = 135
        val scale3 = ScaleAnimation(0.95f, 1f, 0.95f, 1.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
        scale3.duration = 60
        scale3.startOffset = 240
        addAnimation(alpha)
        addAnimation(scale1)
        addAnimation(scale2)
        addAnimation(scale3)
    }
}

/**
 * 弹性退出
 */
fun Context.elasticOut(): AnimationSet {
    return AnimationSet(this, null).apply {
        val alpha = AlphaAnimation(1.0f, 0.0f)
        alpha.duration = 150
        val scale = ScaleAnimation(1.0f, 0.6f, 1.0f, 0.6f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
        scale.duration = 150
        addAnimation(alpha)
        addAnimation(scale)
    }
}

/**
 * 底部弹出/隐藏动画（统一入口）
 * @param isShown true=弹出  false=隐藏
 */
fun bottomTranslate(isShown: Boolean = true, onEnd: (animation: Animation?) -> Unit = {}, onStart: (animation: Animation?) -> Unit = {}, onRepeat: (animation: Animation?) -> Unit = {}): Animation {
    return (if (isShown) createBottomSlideInAnimation() else createBottomSlideOutAnimation()).also { it.addListener(onEnd, onStart, onRepeat) }
}

/**
 * 创建【从底部弹出】的平移动画
 */
private fun createBottomSlideInAnimation(duration: Long = 300L): Animation {
    return createVerticalTranslateAnimation(1f, 0f, duration)
}

/**
 * 创建【向底部隐藏】的平移动画
 */
private fun createBottomSlideOutAnimation(duration: Long = 300L): Animation {
    return createVerticalTranslateAnimation(0f, 0.5f, duration)
}

/**
 * 创建垂直方向平移动画（基于父布局）
 */
private fun createVerticalTranslateAnimation(fromYDelta: Float, toYDelta: Float, duration: Long = 300): Animation {
    return TranslateAnimation(Animation.RELATIVE_TO_PARENT, 0f, Animation.RELATIVE_TO_PARENT, 0f, Animation.RELATIVE_TO_PARENT, fromYDelta, Animation.RELATIVE_TO_PARENT, toYDelta).apply {
        this.duration = duration
    }
}

/**
 * 加载动画
 */
fun Context.loadAnimation(id: Int, onEnd: (animation: Animation?) -> Unit = {}, onStart: (animation: Animation?) -> Unit = {}, onRepeat: (animation: Animation?) -> Unit = {}): Animation {
    return AnimationUtils.loadAnimation(this, id).also { it.addListener(onEnd, onStart, onRepeat) }
}

/**
 * 针对Animation添加扩展
 * 1) Animator 系列（有 KTX 扩展 , 支持anim.doOnStart {} anim.doOnEnd {}）
 * ValueAnimator / ObjectAnimator / ViewPropertyAnimator
 *
 * 2) Animation 系列 (旧视图动画，不支持)
 * AlphaAnimation / ScaleAnimation / TranslateAnimation / RotateAnimation / AnimationSet
 *
 * 3) Animation 只有 setListener，没有 addListener , 连续执行anim.doOnStart { } / anim.doOnEnd   { } 后面会覆盖前面
 * val alpha = AlphaAnimation(0f, 1f)
 * alpha.duration = 300
 * // 一次设置 start + end
 * alpha.addAnimationListener(
 *     onStart = {
 *         // 动画开始
 *     },
 *     onEnd = {
 *         // 动画结束
 *     }
 * )
 * view.startAnimation(alpha)
 */
inline fun Animation.doOnEnd(
    crossinline action: (animation: Animation?) -> Unit
): Animation.AnimationListener =
    addListener(onEnd = action)

inline fun Animation.doOnStart(
    crossinline action: (animation: Animation?) -> Unit
): Animation.AnimationListener =
    addListener(onStart = action)

inline fun Animation.doOnRepeat(
    crossinline action: (animation: Animation?) -> Unit
): Animation.AnimationListener =
    addListener(onRepeat = action)

inline fun Animation.addListener(
    crossinline onEnd: (animation: Animation?) -> Unit = {},
    crossinline onStart: (animation: Animation?) -> Unit = {},
    crossinline onRepeat: (animation: Animation?) -> Unit = {}
): Animation.AnimationListener {
    val listener = object : Animation.AnimationListener {
        override fun onAnimationEnd(animation: Animation?) {
            onEnd(animation)
        }

        override fun onAnimationRepeat(animation: Animation?) {
            onRepeat(animation)
        }

        override fun onAnimationStart(animation: Animation?) {
            onStart(animation)
        }
    }
    setAnimationListener(listener)
    return listener
}

/**
 * 【属性动画统一封装】
 * 针对 View 提供链式属性动画（基于 ValueAnimator / AnimatorSet）
 * 支持：文本数字、字号、颜色、背景色、宽高、边距、缩放等
 */
@SuppressLint("SetTextI18n")
class ViewAnimator(private val view: View?, private val duration: Long) {
    private var interpolator: Interpolator = AccelerateDecelerateInterpolator()
    private val animatorList = mutableListOf<Animator>()

    /**
     * 设置动画插值器（支持链式调用）
     * @param interpolator 动画速率插值器，如 [OvershootInterpolator]
     */
    fun withInterpolator(interpolator: Interpolator): ViewAnimator {
        this.interpolator = interpolator
        return this
    }

    /**
     * 文本数字滚动动画
     * @param start 起始数值
     * @param end 结束数值
     * @param suffix 结尾后缀（如 "%"）
     */
    fun animateTextNumber(start: Double, end: Double, suffix: String): ViewAnimator {
        // view !is TextView 已经包含了 view == null 的情况（因为 null 不属于任何非空类型）
        if (view !is TextView) return this
        val df = DecimalFormat("0.00").apply {
            decimalFormatSymbols = DecimalFormatSymbols(Locale.US)
            roundingMode = RoundingMode.HALF_UP
        }
        return createAnimator(start.toSafeFloat(), end.toSafeFloat()) { value ->
            view.text = "${df.format(value)}$suffix"
        }
    }

    /**
     * 字号渐变动画
     */
    fun animateTextSize(start: Float, end: Float): ViewAnimator {
        if (view !is TextView) return this
        return createAnimator(start, end) { value ->
            view.pxTextSize(value.orZero)
        }
    }

    /**
     * 文本颜色渐变动画（字符串 / ColorInt）
     */
    fun animateTextColor(start: String, end: String): ViewAnimator {
        if (view !is TextView) return this
        return animateTextColor(start.toColorInt(), end.toColorInt())
    }

    fun animateTextColor(@ColorInt start: Int, @ColorInt end: Int): ViewAnimator {
        if (view !is TextView) return this
        if (duration == 0L) {
            view.setTextColor(end)
            return this
        }
        return createAnimator(0f, 1f) { value ->
            val color = ArgbEvaluator().evaluate(value.orZero, start, end) as Int
            view.setTextColor(color)
        }
    }

    /**
     * 背景颜色渐变动画（字符串 / ColorInt）
     */
    fun animateBackgroundColor(start: String, end: String): ViewAnimator {
        if (view == null) return this
        return animateBackgroundColor(start.toColorInt(), end.toColorInt())
    }

    fun animateBackgroundColor(@ColorInt start: Int, @ColorInt end: Int): ViewAnimator {
        if (view == null) return this
        if (duration == 0L) {
            view.setBackgroundColor(end)
            return this
        }
        return createAnimator(0f, 1f) { value ->
            val color = ArgbEvaluator().evaluate(value.orZero, start, end) as Int
            view.setBackgroundColor(color)
        }
    }

    /**
     * 高度渐变动画 (px)
     */
    fun animateHeight(start: Int, end: Int): ViewAnimator {
        return createAnimator(start.toSafeFloat(), end.toSafeFloat()) { value ->
            view?.layoutParams?.apply { height = value.toSafeInt() }?.let { view.layoutParams = it }
        }
    }

    /**
     * 宽度渐变动画 (px)
     */
    fun animateWidth(start: Int, end: Int): ViewAnimator {
        return createAnimator(start.toSafeFloat(), end.toSafeFloat()) { value ->
            view?.layoutParams?.apply { width = value.toSafeInt() }?.let { view.layoutParams = it }
        }
    }

    /**
     * 左侧Margin渐变动画 (px)
     */
    fun animateMarginLeft(start: Int, end: Int): ViewAnimator {
        return createAnimator(start.toSafeFloat(), end.toSafeFloat()) { value ->
            (view?.layoutParams as? ViewGroup.MarginLayoutParams)?.apply {
                leftMargin = value.toSafeInt()
            }?.let { view.layoutParams = it }
        }
    }

    /**
     * 右侧Margin渐变动画 (px)
     */
    fun animateMarginRight(start: Int, end: Int): ViewAnimator {
        return createAnimator(start.toSafeFloat(), end.toSafeFloat()) { value ->
            (view?.layoutParams as? ViewGroup.MarginLayoutParams)?.apply {
                rightMargin = value.toSafeInt()
            }?.let { view.layoutParams = it }
        }
    }

    /**
     * 缩放动画（同时处理X/Y轴）
     */
    fun animateScale(start: Float, end: Float): ViewAnimator {
        if (view == null) return this
        if (duration == 0L || start == end) {
            view.scaleX = end
            view.scaleY = end
            return this
        }
        return createAnimator(start, end) { value ->
            view.scaleX = value.orZero
            view.scaleY = value.orZero
        }
    }

    /**
     * 统一动画方法
     */
    private fun createAnimator(start: Float, end: Float, update: (Float?) -> Unit): ViewAnimator {
        if (view == null) return this
        if (duration == 0L) {
            update(end)
            return this
        }
        ValueAnimator.ofFloat(start, end).also { animator ->
            animator.setTarget(view)
            animator.interpolator = interpolator
            animator.duration = duration
            animator.addUpdateListener {
                update(it.animatedValue as? Float)
            }
            animatorList.add(animator)
        }
        return this
    }

    /**
     * 开始执行动画
     */
    fun start(onStart: () -> Unit = {}, onEnd: () -> Unit = {}) {
        if (duration == 0L) {
            onEnd()
            return
        }
        (view?.tag as? AnimatorSet)?.apply {
            removeAllListeners()
            cancel()
        }
        val animSet = AnimatorSet().apply {
            setTarget(view)
            playTogether(animatorList)
            duration = duration
            doOnStart { onStart() }
            doOnEnd { onEnd() }
//            addListener(object : Animator.AnimatorListener {
//                override fun onAnimationStart(animation: Animator) {
//                    onStart.invoke()
//                }
//
//                override fun onAnimationEnd(animation: Animator) {
//                    onEnd.invoke()
//                }
//
//                override fun onAnimationCancel(animation: Animator) {
//                }
//
//                override fun onAnimationRepeat(animation: Animator) {
//                }
//            })
        }
        view?.tag = animSet
        animSet.start()
    }

}