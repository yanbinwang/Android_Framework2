package com.example.common.widget.xrecyclerview.gesture

import android.animation.Animator
import android.animation.ValueAnimator
import androidx.recyclerview.widget.RecyclerView
import com.example.framework.utils.function.value.orZero

/**
 * 动画类 (处理手势结束后 Item 的「恢复 / 滑出」动画)
 * @mViewHolder (手势结束前，正在被操作的ViewHolder)
 * @mAnimationType (动画类型标识)
 * ANIMATION_TYPE_SWIPE_SUCCESS -> 滑动（Swipe）手势成功触发后的结束动画
 * ANIMATION_TYPE_SWIPE_CANCEL -> 滑动（Swipe）手势取消后的复位动画
 * ANIMATION_TYPE_DRAG -> 拖拽（Drag）手势结束后的复位动画
 * @mActionState (手势结束前，当前的手势状态（拖拽/滑动/闲置）)
 * prevActionState=ACTION_STATE_DRAG（拖拽手势）→ 无论结果如何，animationType=ANIMATION_TYPE_DRAG（拖拽复位动画）；
 * prevActionState=ACTION_STATE_SWIPE（滑动手势）+ 手势结果 = 成功 → animationType=ANIMATION_TYPE_SWIPE_SUCCESS；
 * prevActionState=ACTION_STATE_SWIPE（滑动手势）+ 手势结果 = 取消 → animationType=ANIMATION_TYPE_SWIPE_CANCEL。
 * @mStartDx/mStartDy/mTargetX/mTargetY (动画从哪开始，到哪结束 单位像素（px）)
 */
open class RecoverAnimation(val mViewHolder: RecyclerView.ViewHolder, val mAnimationType: Int, val mActionState: Int, private val mStartDx: Float, private val mStartDy: Float, private val mTargetX: Float, private val mTargetY: Float) : Animator.AnimatorListener {
    var mX = 0f
    var mY = 0f
    var mFraction = 0f
    var mEnded = false
    var mOverridden = false
    var mIsPendingCleanup = false
    private var mValueAnimator: ValueAnimator? = null

    init {
        mValueAnimator = ValueAnimator.ofFloat(0f, 1f)
        mValueAnimator?.addUpdateListener { animation: ValueAnimator? ->
            setFraction(animation?.animatedFraction.orZero)
        }
        mValueAnimator?.setTarget(mViewHolder.itemView)
        mValueAnimator?.addListener(this)
        setFraction(0f)
    }

    override fun onAnimationStart(animation: Animator) {
    }

    override fun onAnimationEnd(animation: Animator) {
        if (!mEnded) {
            mViewHolder.setIsRecyclable(true)
        }
        mEnded = true
    }

    override fun onAnimationCancel(animation: Animator) {
        setFraction(1f)
    }

    override fun onAnimationRepeat(animation: Animator) {
    }

    fun setDuration(duration: Long) {
        mValueAnimator?.setDuration(duration)
    }

    fun start() {
        mViewHolder.setIsRecyclable(false)
        mValueAnimator?.start()
    }

    fun cancel() {
        mValueAnimator?.cancel()
    }

    fun setFraction(fraction: Float) {
        mFraction = fraction
    }

    fun update() {
        mX = if (mStartDx == mTargetX) {
            mViewHolder.itemView.translationX
        } else {
            mStartDx + mFraction * (mTargetX - mStartDx)
        }
        mY = if (mStartDy == mTargetY) {
            mViewHolder.itemView.translationY
        } else {
            mStartDy + mFraction * (mTargetY - mStartDy)
        }
    }

}