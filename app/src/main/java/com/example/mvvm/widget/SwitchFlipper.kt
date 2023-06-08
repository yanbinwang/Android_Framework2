package com.example.mvvm.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.ViewFlipper
import com.example.framework.utils.function.inflate
import com.example.mvvm.R

/**
 * Created by wangyanbin
 * 切换自定义->继承自FrameLayout
 * setInAnimation：设置View进入屏幕时使用的动画
 * setOutAnimation：设置View退出屏幕时使用的动画
 * showNext：调用该方法来显示ViewFlipper里的下一个View
 * showPrevious：调用该方法来显示ViewFlipper的上一个View
 * setFilpInterval：设置View之间切换的时间间隔
 * setFlipping：使用上面设置的时间间隔来开始切换所有的View，切换会循环进行
 * stopFlipping：停止View切换
 * displayedChild:获取当前选中的索引
 */
class SwitchFlipper @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : ViewFlipper(context, attrs) {

    init {
        inAnimation = AnimationUtils.loadAnimation(context, R.anim.set_translate_right_in)
        outAnimation = AnimationUtils.loadAnimation(context, R.anim.set_translate_left_out)
    }

    /**
     * 添加view
     */
    fun addViews(vararg resources: Int) {
        resources.forEach { addView(context.inflate(it)) }
    }

    /**
     * 存储进管理集合
     */
    fun addViews(vararg views: View) {
        views.forEach { addView(it) }
    }

    /**
     * 需保证只有2个view插入的情况下，调用此方法，实现左右切换
     */
    fun turnThePage() {
        if (displayedChild < childCount - 1) {
            showNext()
        } else {
            showPrevious()
        }
    }

}