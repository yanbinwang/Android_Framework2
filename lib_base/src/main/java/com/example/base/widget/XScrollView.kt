package com.example.base.widget

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ViewConfiguration
import androidx.core.widget.NestedScrollView
import com.example.base.utils.LogUtil.i
import kotlin.math.abs

/**
 * @description
 * @author
 */
class XScrollView(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : NestedScrollView(context, attrs, defStyleAttr) {
    private var touchSlop = 0
    private var downY = 0
    var isTop = false //是否滑动到顶端
    var onBottom: ((isBottom: Boolean) -> Unit)? = null

    init {
        initialize()
    }

    override fun onOverScrolled(scrollX: Int, scrollY: Int, clampedX: Boolean, clampedY: Boolean) {
        super.onOverScrolled(scrollX, scrollY, clampedX, clampedY)
        if (scrollY != 0 && null != onBottom && isTop) onBottom?.invoke(clampedY)
    }

    override fun onInterceptTouchEvent(e: MotionEvent?): Boolean {
        when (e?.action) {
            MotionEvent.ACTION_DOWN -> {
                isTop = false
                downY = e.rawY.toInt()
                i("-----::----downY-----::$downY")
            }
            MotionEvent.ACTION_MOVE -> {
                val moveY: Int = e.rawY.toInt()
                i("-----::----moveY-----::$moveY")
                //判断是向下滑动，才设置为true
                isTop = downY - moveY > 0
                if (abs(moveY - downY) > touchSlop) return true
            }
        }
        return super.onInterceptTouchEvent(e)
    }

    private fun initialize() {
        overScrollMode = OVER_SCROLL_NEVER
        touchSlop = ViewConfiguration.get(context).scaledTouchSlop
    }

}