package com.example.base.utils

import android.content.Context
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.AnimationSet
import android.view.animation.ScaleAnimation

/**
 * 作者 : andy
 * 日期 : 15/11/16 12:23
 * 邮箱 : andyxialm@gmail.com
 * 描述 : 默认动画效果
 */
fun Context.getInAnimation(): AnimationSet {
    val inAnimation = AnimationSet(this, null)
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
    inAnimation.addAnimation(alpha)
    inAnimation.addAnimation(scale1)
    inAnimation.addAnimation(scale2)
    inAnimation.addAnimation(scale3)
    return inAnimation
}

fun Context.getOutAnimation(): AnimationSet {
    val outAnimation = AnimationSet(this, null)
    val alpha = AlphaAnimation(1.0f, 0.0f)
    alpha.duration = 150
    val scale = ScaleAnimation(1.0f, 0.6f, 1.0f, 0.6f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
    scale.duration = 150
    outAnimation.addAnimation(alpha)
    outAnimation.addAnimation(scale)
    return outAnimation
}