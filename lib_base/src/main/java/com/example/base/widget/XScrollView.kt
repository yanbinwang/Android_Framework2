package com.example.base.widget

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ViewConfiguration
import androidx.core.widget.NestedScrollView
import com.example.base.utils.logI
import kotlin.math.abs

/**
 * @description
 * @author
 */
class XScrollView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : NestedScrollView(context, attrs, defStyleAttr) {
    private var downY = 0
    private var isTop = false
    private val touchSlop by lazy { ViewConfiguration.get(context).scaledTouchSlop }
    var onBottom: ((isBottom: Boolean) -> Unit)? = null

    init {
        overScrollMode = OVER_SCROLL_NEVER
    }

    override fun onOverScrolled(scrollX: Int, scrollY: Int, clampedX: Boolean, clampedY: Boolean) {
        super.onOverScrolled(scrollX, scrollY, clampedX, clampedY)
        if (scrollY != 0 && isTop) onBottom?.invoke(clampedY)
    }

    override fun onInterceptTouchEvent(e: MotionEvent): Boolean {
        when (e.action) {
            MotionEvent.ACTION_DOWN -> {
                isTop = false
                downY = e.rawY.toInt()
                "-----::----downY-----::$downY".logI
            }
            MotionEvent.ACTION_MOVE -> {
                val moveY: Int = e.rawY.toInt()
                "-----::----moveY-----::$moveY".logI
                //判断是向下滑动，才设置为true
                isTop = downY - moveY > 0
                if (abs(moveY - downY) > touchSlop) return true
            }
        }
        return super.onInterceptTouchEvent(e)
    }

}