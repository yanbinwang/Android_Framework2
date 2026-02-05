package com.example.common.widget.xrecyclerview.gesture

import android.animation.Animator
import android.animation.ValueAnimator
import androidx.recyclerview.widget.RecyclerView
import com.example.framework.utils.function.value.orZero

open class RecoverAnimation(val mViewHolder: RecyclerView.ViewHolder, private val mAnimationType: Int, val mActionState: Int, private val mStartDx: Float, private val mStartDy: Float, private val mTargetX: Float, private val mTargetY: Float) : Animator.AnimatorListener {
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