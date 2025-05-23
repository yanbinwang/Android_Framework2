package com.example.mvvm.activity

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.os.Bundle
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.common.R
import com.example.common.base.BaseActivity
import com.example.common.config.ARouterPath
import com.example.common.utils.function.getStatusBarHeight
import com.example.common.utils.function.pt
import com.example.common.utils.function.setTheme
import com.example.framework.utils.function.value.orZero
import com.example.framework.utils.function.value.toSafeFloat
import com.example.framework.utils.function.view.appear
import com.example.framework.utils.function.view.applyConstraints
import com.example.framework.utils.function.view.bold
import com.example.framework.utils.function.view.bottomToBottomOf
import com.example.framework.utils.function.view.centerVertically
import com.example.framework.utils.function.view.fade
import com.example.framework.utils.function.view.gone
import com.example.framework.utils.function.view.padding
import com.example.framework.utils.function.view.setResource
import com.example.framework.utils.function.view.size
import com.example.framework.utils.function.view.startToEndOf
import com.example.framework.utils.function.view.startToStartOf
import com.example.framework.utils.function.view.textSize
import com.example.framework.utils.function.view.topToTopOf
import com.example.mvvm.databinding.ActivityTest2Binding
import com.google.android.material.appbar.AppBarLayout
import kotlin.math.abs

@Route(path = ARouterPath.TestActivity2)
class Test2Activity : BaseActivity<ActivityTest2Binding>() {
    private val rootView by lazy { ConstraintLayout(this) }
    private val ivAvatarId by lazy { View.generateViewId() }
    private val tvNickId by lazy { View.generateViewId() }

    //折叠后的小头像
    private val ivAvatar by lazy {
        ImageView(this).apply {
            id = ivAvatarId
            setResource(R.drawable.shape_glide_circular)
            size(44.pt, 44.pt)
            padding(10.pt, 10.pt, 10.pt, 10.pt)
            gone()
//            alpha = 0f
        }
    }

    //折叠后的标题
    private val tvNick by lazy {
        TextView(this).apply {
            id = tvNickId
            setTheme("老王", R.color.textPrimary)
            textSize(R.dimen.textSize12)
            bold(true)
            size(WRAP_CONTENT, WRAP_CONTENT)
            gone()
//            alpha = 0f
        }
    }

    override fun isImmersionBarEnabled() = false

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        initImmersionBar(false)
        initMenu()
    }

    /**
     * 初始化顶部菜单
     */
    private fun initMenu() {
        mBinding?.toolbar.size(height = 44.pt + getStatusBarHeight())
        mBinding?.toolbar?.clearToolbarCompletely()
        rootView.size(MATCH_PARENT, MATCH_PARENT)
        rootView.padding(top = getStatusBarHeight())
        mBinding?.toolbar?.addView(rootView)
        rootView.addView(ivAvatar)
        rootView.applyConstraints {
            startToStartOf(ivAvatarId)
            centerVertically(ivAvatarId)
        }
        rootView.addView(tvNick)
        rootView.applyConstraints {
            startToEndOf(tvNickId, ivAvatarId)
            topToTopOf(tvNickId, ivAvatarId)
            bottomToBottomOf(tvNickId, ivAvatarId)
        }
    }

    private var isAnimating = false
    private val minScale = 0.3f // 最小缩放比例
    private val maxScale = 1.0f  // 最大缩放比例
    private val maxTranslationX = -70f // 最大水平偏移量(向左为负)
    private val maxTranslationY = 50f   // 最大垂直偏移量(向上为负)
    private var initialScale = maxScale
    private var initialTranslationX = 0f
    private var initialTranslationY = 0f

    override fun initEvent() {
        super.initEvent()
        initialScale = mBinding?.llInfo?.scaleX.orZero
        initialTranslationX = mBinding?.llInfo?.translationX.orZero
        initialTranslationY = mBinding?.llInfo?.translationY.orZero
        mBinding?.appbar?.addOnOffsetChangedListener(object : AppBarLayout.OnOffsetChangedListener {
            private var isHide = false
            override fun onOffsetChanged(appBarLayout: AppBarLayout?, verticalOffset: Int) {
                //UI缩放动画
                val totalScrollRange = appBarLayout?.totalScrollRange.toSafeFloat()
                val offset = abs(verticalOffset).toSafeFloat()
                val percentage = offset / totalScrollRange
//                // 计算目标缩放比例 (根据滚动百分比在minScale和maxScale之间线性插值)
//                val targetScale = maxScale - (maxScale - minScale) * percentage
//                // 计算目标平移量 (向上滚动时向左上方平移，向下滚动时复位)
//                val targetTranslationX = initialTranslationX + (maxTranslationX * percentage)
//                val targetTranslationY = initialTranslationY + (maxTranslationY * percentage)
//                // 使用动画使变化过程更平滑
//                if (!isAnimating && (mBinding?.llInfo?.scaleX != targetScale ||
//                            mBinding?.llInfo?.translationX != targetTranslationX ||
//                            mBinding?.llInfo?.translationY != targetTranslationY)) {
//                    animateProperties(mBinding?.llInfo?.scaleX.orZero, targetScale,
//                        mBinding?.llInfo?.translationX.orZero, targetTranslationX,
//                        mBinding?.llInfo?.translationY.orZero, targetTranslationY)
//                }
                mBinding?.llInfo?.alpha = 1 - percentage
                mBinding?.llInfo?.scaleX = 1 - percentage
                mBinding?.llInfo?.scaleY = 1 - percentage
                //折叠/显式状态动画
                val needHide = offset < totalScrollRange
                if (needHide != isHide) {
                    isHide = needHide
                    initImmersionBar(!needHide)
                    if (needHide) {
                        mBinding?.llInfo.appear(100)
                        ivAvatar.fade(100)
                        tvNick.fade(100)
                    } else {
                        mBinding?.llInfo.fade(100)
                        ivAvatar.appear(100)
                        tvNick.appear(100)
                    }
                }
            }
        })
    }

    /**
     * 立即将视图重置为初始状态
     */
    fun resetWithoutAnimation() {
        mBinding?.llInfo?.clearAnimation()
        mBinding?.llInfo?.scaleX = initialScale
        mBinding?.llInfo?.scaleY = initialScale
        mBinding?.llInfo?.translationX = initialTranslationX
        mBinding?.llInfo?.translationY = initialTranslationY
    }

    /**
     * 立即将视图设置为最小缩放状态(无动画)
     */
    fun applyMinimumScaleWithoutAnimation() {
        mBinding?.llInfo?.scaleX = minScale
        mBinding?.llInfo?.scaleY = minScale
        mBinding?.llInfo?.translationX = initialTranslationX + maxTranslationX
        mBinding?.llInfo?.translationY = initialTranslationY + maxTranslationY
    }

    //    private fun animateScale(from: Float, to: Float) {
//        val animator = ValueAnimator.ofFloat(from, to).apply {
//            duration = 150
//            addUpdateListener { animation ->
//                val scale = animation.animatedValue as Float
//                mBinding?.llInfo?.scaleX = scale
//                mBinding?.llInfo?.scaleY = scale
//            }
//            addListener(object : AnimatorListenerAdapter() {
//                override fun onAnimationStart(animation: Animator) {
//                    isAnimating = true
//                }
//                override fun onAnimationEnd(animation: Animator) {
//                    isAnimating = false
//                }
//            })
//        }
//        animator.start()
//    }

    private fun animateProperties(
        fromScale: Float, toScale: Float,
        fromTranslationX: Float, toTranslationX: Float,
        fromTranslationY: Float, toTranslationY: Float
    ) {
        val scaleAnimator = ValueAnimator.ofFloat(fromScale, toScale).apply {
            addUpdateListener { animation ->
                val scale = animation.animatedValue as Float
                mBinding?.llInfo?.scaleX = scale
                mBinding?.llInfo?.scaleY = scale
            }
        }

        val translationXAnimator = ValueAnimator.ofFloat(fromTranslationX, toTranslationX).apply {
            addUpdateListener { animation ->
                val translation = animation.animatedValue as Float
                mBinding?.llInfo?.translationX = translation
            }
        }

        val translationYAnimator = ValueAnimator.ofFloat(fromTranslationY, toTranslationY).apply {
            addUpdateListener { animation ->
                val translation = animation.animatedValue as Float
                mBinding?.llInfo?.translationY = translation
            }
        }
        // 使用AnimatorSet确保动画同步进行
        val animatorSet = AnimatorSet().apply {
            playTogether(scaleAnimator, translationXAnimator, translationYAnimator)
            duration = 150
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator) {
                    isAnimating = true
                }

                override fun onAnimationEnd(animation: Animator) {
                    isAnimating = false
                }
            })
        }
        animatorSet.start()
    }

}

fun Toolbar.clearToolbarCompletely() {
    removeAllViews()
    setLogo(null)
    setTitle(null)
    getMenu().clear()
    // 如果有导航图标，也移除
    navigationIcon = null
    // 某些情况下可能需要强制设置padding
    setPadding(0, 0, 0, 0)
}