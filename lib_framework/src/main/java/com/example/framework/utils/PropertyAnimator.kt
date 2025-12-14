package com.example.framework.utils

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
import com.example.framework.utils.function.view.pxTextSize
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

/**
 * @description 动画工具类
 * @author yan
 */
@SuppressLint("SetTextI18n")
class PropertyAnimator(private val view: View?, private val millisecond: Long) {
    private var interpolator: Interpolator = AccelerateDecelerateInterpolator()
    private val animationList by lazy { ArrayList<Animator>() }

    companion object {
        /**
         * 进入
         */
        @JvmStatic
        fun Context.elasticityEnter(): AnimationSet {
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
         * 退出
         */
        @JvmStatic
        fun Context.elasticityExit(): AnimationSet {
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
         * 加载
         */
        @JvmStatic
        fun Context.loadAnimation(id: Int, onStart: () -> Unit = {}, onEnd: () -> Unit = {}, onRepeat: () -> Unit = {}): Animation {
            return AnimationUtils.loadAnimation(this, id).loadAnimation(onStart, onEnd, onRepeat)
        }

        @JvmStatic
        fun Animation.loadAnimation(onStart: () -> Unit = {}, onEnd: () -> Unit = {}, onRepeat: () -> Unit = {}): Animation {
            return this.apply {
                setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationStart(animation: Animation?) {
                        onStart.invoke()
                    }

                    override fun onAnimationEnd(animation: Animation?) {
                        onEnd.invoke()
                    }

                    override fun onAnimationRepeat(animation: Animation?) {
                        onRepeat.invoke()
                    }
                })
            }
        }

        /**
         * 自定义反向动画
         */
        @JvmStatic
        fun translate(onStart: () -> Unit = {}, onEnd: () -> Unit = {}, onRepeat: () -> Unit = {}, isShown: Boolean = true): Animation {
            return (if (isShown) createBottomPopUpAnimation() else createBottomHideAnimation()).loadAnimation(onStart, onEnd, onRepeat)
        }

        /**
         * 创建底部弹出动画
         * @param duration 动画持续时间，单位为毫秒，默认为 300 毫秒
         * @return 创建好的底部弹出动画
         */
        private fun createBottomPopUpAnimation(duration: Long = 300): Animation {
            return createVerticalTranslateAnimation(1f, 0f, duration)
        }

        /**
         * 创建底部隐藏动画
         * @param duration 动画持续时间，单位为毫秒，默认为 300 毫秒
         * @return 创建好的底部隐藏动画
         */
        private fun createBottomHideAnimation(duration: Long = 300): Animation {
            return createVerticalTranslateAnimation(0f, 0.5f, duration)
        }

        /**
         * 创建一个垂直方向的平移动画
         * @param fromYDelta 动画起始的 Y 轴相对位置
         * @param toYDelta 动画结束的 Y 轴相对位置
         * @param duration 动画持续时间，单位为毫秒
         * @return 创建好的 TranslateAnimation 对象
         */
        private fun createVerticalTranslateAnimation(fromYDelta: Float, toYDelta: Float, duration: Long = 300): Animation {
            val translateAnimation = TranslateAnimation(
                Animation.RELATIVE_TO_PARENT, 0f,
                Animation.RELATIVE_TO_PARENT, 0f,
                Animation.RELATIVE_TO_PARENT, fromYDelta,
                Animation.RELATIVE_TO_PARENT, toYDelta
            )
            translateAnimation.duration = duration
            return translateAnimation
        }
    }

    /**
     * 设置动画插值器（支持链式调用）
     * @param interpolator 动画速率插值器，如 [OvershootInterpolator]
     */
    fun withInterpolator(interpolator: Interpolator): PropertyAnimator {
        this.interpolator = interpolator
        return this
    }

    /**
     * 创建文本数字渐变动画
     * @param startNum 起始数值
     * @param endNum 结束数值
     * @param endSuffix 结尾后缀（如 "%"）
     */
    fun animateTextNumber(startNum: Double, endNum: Double, endSuffix: String): PropertyAnimator {
        //view !is TextView 已经包含了 view == null 的情况（因为 null 不属于任何非空类型）
        if (view !is TextView) return this
        val df = DecimalFormat("0.00").apply {
            decimalFormatSymbols = DecimalFormatSymbols(Locale.US)
            roundingMode = RoundingMode.HALF_UP
        }
        return createAnimator(startNum.toSafeFloat(), endNum.toSafeFloat()) { value ->
            view.text = "${df.format(value)}$endSuffix"
        }
    }

    /**
     * 字号渐变动画
     */
    fun animateTextSize(start: Float, end: Float): PropertyAnimator {
        if (view !is TextView) return this
        return createAnimator(start, end) { value ->
            view.pxTextSize(value.orZero)
        }
    }

    /**
     * 文本颜色渐变动画（字符串版）
     */
    fun animateTextColor(start: String, end: String): PropertyAnimator {
        if (view !is TextView) return this
        val startColor = start.toColorInt()
        val endColor = end.toColorInt()
        return animateTextColor(startColor, endColor)
    }

    /**
     * 文本颜色渐变动画（Int版）
     */
    fun animateTextColor(@ColorInt startColor: Int, @ColorInt endColor: Int): PropertyAnimator {
        if (view !is TextView) return this
        if (millisecond == 0L) {
            view.setTextColor(endColor)
            return this
        }
        return createAnimator(0f, 1f) { value ->
            val color = ArgbEvaluator().evaluate(value.orZero, startColor, endColor) as Int
            view.setTextColor(color)
        }
    }

    /**
     * 背景颜色渐变动画（字符串版）
     */
    fun animateBackgroundColor(start: String, end: String): PropertyAnimator {
        if (view == null) return this
        val startColor = start.toColorInt()
        val endColor = end.toColorInt()
        return animateBackgroundColor(startColor, endColor)
    }

    /**
     * 背景颜色渐变动画（Int版）
     */
    fun animateBackgroundColor(@ColorInt startColor: Int, @ColorInt endColor: Int): PropertyAnimator {
        if (view == null) return this
        if (millisecond == 0L) {
            view.setBackgroundColor(endColor)
            return this
        }
        return createAnimator(0f, 1f) { value ->
            val color = ArgbEvaluator().evaluate(value.orZero, startColor, endColor) as Int
            view.setBackgroundColor(color)
        }
    }

    /**
     * 高度渐变动画
     */
    fun animateHeight(startPX: Int, endPX: Int): PropertyAnimator {
        return createAnimator(startPX.toSafeFloat(), endPX.toSafeFloat()) { value ->
            view?.layoutParams?.apply { height = value.toSafeInt() }?.let { view.layoutParams = it }
        }
    }

    /**
     * 宽度渐变动画
     */
    fun animateWidth(startPX: Int, endPX: Int): PropertyAnimator {
        return createAnimator(startPX.toSafeFloat(), endPX.toSafeFloat()) { value ->
            view?.layoutParams?.apply { width = value.toSafeInt() }?.let { view.layoutParams = it }
        }
    }

    /**
     * 左侧Margin渐变动画
     */
    fun animateMarginLeft(startPX: Int, endPX: Int): PropertyAnimator {
        return createAnimator(startPX.toSafeFloat(), endPX.toSafeFloat()) { value ->
            (view?.layoutParams as? ViewGroup.MarginLayoutParams)?.apply {
                leftMargin = value.toSafeInt()
            }?.let { view.layoutParams = it }
        }
    }

    /**
     * 右侧Margin渐变动画
     */
    fun animateMarginRight(startPX: Int, endPX: Int): PropertyAnimator {
        return createAnimator(startPX.toSafeFloat(), endPX.toSafeFloat()) { value ->
            (view?.layoutParams as? ViewGroup.MarginLayoutParams)?.apply {
                rightMargin = value.toSafeInt()
            }?.let { view.layoutParams = it }
        }
    }

    /**
     * 缩放动画（同时处理X/Y轴）
     */
    fun animateScale(startScale: Float, endScale: Float): PropertyAnimator {
        if (view == null) return this
        if (millisecond == 0L || startScale == endScale) {
            view.scaleX = endScale
            view.scaleY = endScale
            return this
        }
        return createAnimator(startScale, endScale) { value ->
            view.scaleX = value.orZero
            view.scaleY = value.orZero
        }
    }

    /**
     * 统一动画方法
     */
    private fun createAnimator(start: Float, end: Float, update: (Float?) -> Unit): PropertyAnimator {
        if (view == null) return this
        if (millisecond == 0L) {
            update(end)
            return this
        }
        ValueAnimator.ofFloat(start, end).also { animator ->
            animator.setTarget(view)
            animator.interpolator = interpolator
            animator.duration = millisecond
            animator.addUpdateListener {
                update(it.animatedValue as? Float)
            }
            animationList.add(animator)
        }
        return this
    }

    /**
     * 开始执行动画
     */
    fun start(onStart: () -> Unit = {}, onEnd: () -> Unit = {}) {
        if (millisecond == 0L) {
            onEnd()
            return
        }
        (view?.tag as? AnimatorSet)?.apply {
            removeAllListeners()
            cancel()
        }
        val animSet = AnimatorSet().apply {
            setTarget(view)
            playTogether(animationList)
            duration = millisecond
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