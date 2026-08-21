package com.example.common.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.ViewFlipper
import com.example.common.R
import com.example.framework.utils.function.inflate
import com.example.framework.utils.function.view.loadAnimation

/**
 * Created by wangyanbin
 * 切换自定义
 * @setInAnimation：设置View进入屏幕时使用的动画
 * @setOutAnimation：设置View退出屏幕时使用的动画
 * @showNext：调用该方法来显示ViewFlipper里的下一个View
 * @showPrevious：调用该方法来显示ViewFlipper的上一个View
 * @setFilpInterval：设置View之间切换的时间间隔
 * @setFlipping：使用上面设置的时间间隔来开始切换所有的View，切换会循环进行
 * @stopFlipping：停止View切换
 * @displayedChild:获取当前选中的索引
 */
class SwitchFlipper @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : ViewFlipper(context, attrs) {
    // 缓存动画，避免每次切换都重新解析 XML
    private val rightInAnim by lazy { context.loadAnimation(R.anim.set_translate_right_in) }
    private val leftOutAnim by lazy { context.loadAnimation(R.anim.set_translate_left_out) }
    private val leftInAnim by lazy { context.loadAnimation(R.anim.set_translate_left_in) }
    private val rightOutAnim by lazy { context.loadAnimation(R.anim.set_translate_right_out) }

    override fun showNext() {
        inAnimation = rightInAnim
        outAnimation = leftOutAnim
        super.showNext()
    }

    override fun showPrevious() {
        inAnimation = leftInAnim
        outAnimation = rightOutAnim
        super.showPrevious()
    }

    /**
     * 批量添加布局资源对应的 View
     */
    fun addViews(vararg resources: Int) {
        resources.forEach { addView(context.inflate(it)) }
    }

    /**
     * 批量添加已创建的 View
     */
    fun addViews(vararg views: View) {
        views.forEach { addView(it) }
    }

    /**
     * 上一页
     */
    fun previousPage() {
        if (childCount <= 0 || displayedChild <= 0) return
        showPrevious()
    }

    /**
     * 下一页
     */
    fun nextPage() {
        if (childCount <= 0 || displayedChild >= childCount - 1) return
        showNext()
    }

    /**
     * 需保证只有2个view插入的情况下，调用此方法，实现左右切换
     */
    fun turnThePage() {
        if (childCount != 2) return
        if (displayedChild >= childCount - 1) {
            showPrevious()
        } else {
            showNext()
        }
    }

}