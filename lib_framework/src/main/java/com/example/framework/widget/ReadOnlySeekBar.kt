package com.example.framework.widget

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.SeekBar

/**
 * 禁止拖动的 seekbar
 */
@SuppressLint("ClickableViewAccessibility")
class ReadOnlySeekBar @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : SeekBar(context, attrs, defStyleAttr) {

    init {
        // 直接设为不可点击 + 不可聚焦，从根源上消除交互和无障碍问题
        isClickable = false
        isFocusable = false
        importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_NO
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        // 消费掉此次手势传递,不继续传递后后面的 View
        return true
    }

}