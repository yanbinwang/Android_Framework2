package com.example.base.utils

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Color
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.view.animation.*
import android.widget.TextView
import androidx.annotation.ColorInt
import com.example.base.utils.function.view.gone
import com.example.base.utils.function.view.setPxTextSize
import com.example.base.utils.function.view.visible
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*

/**
 * @description 动画工具类
 * @author yan
 */
class AnimationUtil(private val view: View?, private val millisecond: Long) {
    private val animationList by lazy { ArrayList<Animator>() }
    private var interpolator: Interpolator = AccelerateDecelerateInterpolator()

    /**
     * 动画执行次数
     */
    fun setInterpolator(interpolator: Interpolator): AnimationUtil {
        this.interpolator = interpolator
        return this
    }

    /**
     * 显示的数字
     */
    fun textNum(startNum: Double, endNum: Double, endSuffix: String): AnimationUtil {
        if (view == null) return this
        if (view !is TextView) return this
        val df = DecimalFormat("0.00")
        df.decimalFormatSymbols = DecimalFormatSymbols(Locale.US)
        df.roundingMode = RoundingMode.HALF_UP
        if (millisecond == 0L) {
            view.text = df.format(endNum) + endSuffix
            return this
        }
        val animator = ValueAnimator.ofFloat(startNum.toFloat(), endNum.toFloat())
        //设置作用对象
        animator.setTarget(view)
        animator.interpolator = interpolator
        //设置执行时间(100ms)
        animator.duration = millisecond
        //添加动画更新监听
        animator.addUpdateListener { animation ->
            //获取当前值
            val mValue = animation.animatedValue as Float
            view.text = df.format(mValue) + endSuffix
        }
        animationList.add(animator)
        return this
    }

    /**
     * 设置字号动画
     */
    fun txtSize(start: Float, end: Float): AnimationUtil {
        if (view == null || view !is TextView) return this
        //设置颜色
        if (millisecond == 0L) {
            view.setPxTextSize(end)
            return this
        }
        val animator = ValueAnimator.ofFloat(start, end)
        //设置作用对象
        animator.setTarget(view)
        animator.interpolator = interpolator
        //设置执行时间(100ms)
        animator.duration = millisecond
        //添加动画更新监听
        animator.addUpdateListener { animation ->
            //获取当前值
            val mValue = animation.animatedValue as Float
            //设置字体大小
            view.setTextSize(TypedValue.COMPLEX_UNIT_PX, mValue)
        }
        animationList.add(animator)
        return this
    }

    /**
     * 设置字体颜色动画
     */
    fun txtColor(start: String, end: String): AnimationUtil {
        if (view == null || view !is TextView) return this
        val colorStart = Color.parseColor(start)
        val colorEnd = Color.parseColor(end)
        return txtColor(colorStart, colorEnd)
    }

    fun txtColor(@ColorInt colorStart: Int, @ColorInt colorEnd: Int): AnimationUtil {
        if (view == null || view !is TextView) return this
        val argbEvaluator = ArgbEvaluator() //渐变色计算类
        //设置颜色
        if (millisecond == 0L) {
            view.setTextColor(colorEnd)
            return this
        }
        val animator = ValueAnimator.ofFloat(0f, 1f)
        //设置作用对象
        animator.setTarget(view)
        animator.interpolator = interpolator
        //设置执行时间(100ms)
        animator.duration = millisecond
        //添加动画更新监听
        animator.addUpdateListener { animation ->
            //获取当前值
            val mValue = animation.animatedValue as Float
            //设置颜色
            view.setTextColor(argbEvaluator.evaluate(mValue, colorStart, colorEnd) as Int)
        }
        animationList.add(animator)
        return this
    }

    /**
     * 设置背景颜色动画
     */
    fun bgColor(@ColorInt colorStart: Int, @ColorInt colorEnd: Int): AnimationUtil {
        if (view == null) return this
        val argbEvaluator = ArgbEvaluator()
        if (millisecond == 0L) {
            view.setBackgroundColor(colorEnd)
            return this
        }
        val animator = ValueAnimator.ofFloat(0f, 1f)
        animator.setTarget(view)
        animator.interpolator = interpolator
        animator.duration = millisecond
        animator.addUpdateListener { animation ->
            val mValue = animation.animatedValue as Float
            view.setBackgroundColor(argbEvaluator.evaluate(mValue, colorStart, colorEnd) as Int)
        }
        animationList.add(animator)
        return this
    }

    /**
     * 高度动画
     */
    fun height(startPX: Int, endPX: Int): AnimationUtil {
        if (view == null) return this
        if (millisecond == 0L) {
            val layoutParams = view.layoutParams
            layoutParams.height = endPX
            view.layoutParams = layoutParams
            return this
        }
        val animator = ValueAnimator.ofFloat(startPX.toFloat(), endPX.toFloat())
        //设置作用对象
        animator.setTarget(view)
        animator.interpolator = interpolator
        //设置执行时间(100ms)
        animator.duration = millisecond
        //添加动画更新监听
        animator.addUpdateListener { animation ->
            //获取当前值
            val mValue = (animation.animatedValue as Float).toInt()
            //设置高度
            val layoutParams = view.layoutParams
            layoutParams.height = mValue
            view.layoutParams = layoutParams
        }
        animationList.add(animator)
        return this
    }

    /**
     * 宽度度动画
     */
    fun width(startPX: Int, endPX: Int): AnimationUtil {
        if (view == null) return this
        if (millisecond == 0L) {
            val layoutParams = view.layoutParams
            layoutParams.width = endPX
            view.layoutParams = layoutParams
            return this
        }
        val animator = ValueAnimator.ofFloat(startPX.toFloat(), endPX.toFloat())
        animator.setTarget(view)
        animator.interpolator = interpolator
        animator.duration = millisecond
        animator.addUpdateListener { animation ->
            val mValue = (animation.animatedValue as Float).toInt()
            val layoutParams = view.layoutParams
            layoutParams.width = mValue
            view.layoutParams = layoutParams
        }
        animationList.add(animator)
        return this
    }

    /**
     *  左侧margin动画
     */
    fun marginLeft(startPX: Int, endPX: Int): AnimationUtil {
        if (view == null) return this
        if (millisecond == 0L) {
            val layoutParams = view.layoutParams as ViewGroup.MarginLayoutParams
            layoutParams.leftMargin = endPX
            view.layoutParams = layoutParams
            return this
        }
        val animator = ValueAnimator.ofFloat(startPX.toFloat(), endPX.toFloat())
        //设置作用对象
        animator.setTarget(view)
        animator.interpolator = interpolator
        //设置执行时间(100ms)
        animator.duration = millisecond
        //添加动画更新监听
        animator.addUpdateListener { animation ->
            //获取当前值
            val mValue = (animation.animatedValue as Float).toInt()
            //设置高度
            val layoutParams = view.layoutParams as ViewGroup.MarginLayoutParams
            layoutParams.leftMargin = mValue
            view.layoutParams = layoutParams
        }
        animationList.add(animator)
        return this
    }

    /**
     *  右侧margin动画
     */
    fun marginRight(startPX: Int, endPX: Int): AnimationUtil {
        if (view == null) return this
        if (millisecond == 0L) {
            val layoutParams = view.layoutParams as ViewGroup.MarginLayoutParams
            layoutParams.leftMargin = endPX
            view.layoutParams = layoutParams
            return this
        }
        val animator = ValueAnimator.ofFloat(startPX.toFloat(), endPX.toFloat())
        animator.setTarget(view)
        animator.interpolator = interpolator
        animator.duration = millisecond
        animator.addUpdateListener { animation ->
            val mValue = (animation.animatedValue as Float).toInt()
            val layoutParams = view.layoutParams as ViewGroup.MarginLayoutParams
            layoutParams.rightMargin = mValue
            view.layoutParams = layoutParams
        }
        animationList.add(animator)
        return this
    }

    /**
     * 伸缩动画
     */
    fun scale(startScale: Float, endScale: Float): AnimationUtil {
        if (view == null) return this
        if (millisecond == 0L || startScale == endScale) {
            view.scaleX = endScale
            view.scaleY = endScale
            return this
        }
        val animator = ValueAnimator.ofFloat(startScale, endScale)
        //设置作用对象
        animator.setTarget(view)
        animator.interpolator = interpolator
        //设置执行时间(100ms)
        animator.duration = millisecond
        //添加动画更新监听
        animator.addUpdateListener { animation ->
            //获取当前值
            val mValue = animation.animatedValue as Float
            //设置缩放
            view.scaleX = mValue
            view.scaleY = mValue
        }
        animationList.add(animator)
        return this
    }

    /**
     * 开始执行动画
     */
    fun start(onStart: (() -> Unit)? = null, onEnd: (() -> Unit)? = null) {
        if (millisecond == 0L) {
            onEnd?.invoke()
            return
        }
        (view?.tag as? AnimatorSet)?.apply {
            removeAllListeners()
            cancel()
        }
        val animSet = AnimatorSet()
        animSet.setTarget(view)
        animSet.playTogether(animationList)
        animSet.duration = millisecond
        animSet.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator?) {
                onStart?.invoke()
            }

            override fun onAnimationEnd(animation: Animator?) {
                onEnd?.invoke()
            }

            override fun onAnimationCancel(animation: Animator?) {
            }

            override fun onAnimationRepeat(animation: Animator?) {
            }
        })
        view?.tag = animSet
        animSet.start()
    }

    companion object {
        /**
         * 旋转
         */
        fun rotate(view: View, from: Float, to: Float, timeMS: Long, interpolator: Interpolator = AccelerateDecelerateInterpolator(), repeat: Boolean = false) {
            view.animation?.cancel()
            val anim = RotateAnimation(from, to, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
            anim.fillAfter = true // 设置保持动画最后的状态
            anim.duration = timeMS // 设置动画时间3
            if (repeat) anim.repeatCount = -1
            anim.interpolator = interpolator // 设置插入器
            view.startAnimation(anim)
        }

        /**
         * 移动
         * @param type Animation.ABSOLUTE, Animation.RELATIVE_TO_SELF, or Animation.RELATIVE_TO_PARENT.
         */
        fun move(view: View, xFrom: Float, xTo: Float, yFrom: Float, yTo: Float, timeMS: Long, fillAfter: Boolean = true, onStart: (() -> Unit)? = null, onEnd: (() -> Unit)? = null, type: Int = Animation.RELATIVE_TO_SELF, interpolator: Interpolator = LinearInterpolator(), ) {
            view.animation?.setAnimationListener(null)
            view.animation?.cancel()
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

                    override fun onAnimationRepeat(animation: Animation?) {
                    }
                })
            }
            view.startAnimation(anim)
        }

        /**
         * 透明度
         * @param from 0f-1f
         * @param to 0f-1f
         */
        fun alpha(view: View, from: Float, to: Float, timeMS: Long, endListener: (() -> Unit)? = null) {
            view.animation?.setAnimationListener(null)
            view.animation?.cancel()
            val anim = AlphaAnimation(from, to)
            if (to == 1f) view.visible()
            anim.fillAfter = false // 设置保持动画最后的状态
            anim.duration = timeMS // 设置动画时间
            anim.interpolator = AccelerateInterpolator() // 设置插入器3
            anim.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationEnd(animation: Animation?) {
                    endListener?.invoke() ?: if (to == 0f) {
                        view.gone()
                    }
                }

                override fun onAnimationStart(animation: Animation?) {
                }

                override fun onAnimationRepeat(animation: Animation?) {
                }
            })
            view.startAnimation(anim)
        }
    }

}

/**
 * 进入
 */
fun Context.shownAnim(): AnimationSet {
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
fun Context.hiddenAnim(): AnimationSet {
    return AnimationSet(this, null).apply {
        val alpha = AlphaAnimation(1.0f, 0.0f)
        alpha.duration = 150
        val scale = ScaleAnimation(1.0f, 0.6f, 1.0f, 0.6f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
        scale.duration = 150
        addAnimation(alpha)
        addAnimation(scale)
    }
}