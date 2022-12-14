package com.example.framework.widget

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.SeekBar

/**
 * 禁止拖动的seekbar
 */
@SuppressLint("AppCompatCustomView", "ClickableViewAccessibility")
class SeekBarCustom @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : SeekBar(context, attrs, defStyleAttr) {

    override fun onTouchEvent(event: MotionEvent?) = false

}