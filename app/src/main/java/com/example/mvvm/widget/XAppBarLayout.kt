package com.example.mvvm.widget

import android.content.Context
import android.util.AttributeSet
import android.view.ViewTreeObserver
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import com.google.android.material.appbar.AppBarLayout

/**
 * @description 禁止/允许appbar滑动
 * @author yan
 */
class XAppBarLayout @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : AppBarLayout(context, attrs, defStyleAttr) {
    private var forbidAppBarScroll = true

    fun forbidAppBarScroll(forbid: Boolean) {
        if (forbid == forbidAppBarScroll) {
            return
        }
        if (forbid) {
            forbidAppBarScroll = true
            if (ViewCompat.isLaidOut(this)) {
                setAppBarDragCallback(object : Behavior.DragCallback() {
                    override fun canDrag(appBarLayout: AppBarLayout): Boolean {
                        return false
                    }
                })
            } else {
                viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                    override fun onGlobalLayout() {
                        viewTreeObserver.removeOnGlobalLayoutListener(this)
                        setAppBarDragCallback(object : Behavior.DragCallback() {
                            override fun canDrag(appBarLayout: AppBarLayout): Boolean {
                                return false
                            }
                        })
                    }
                })
            }
        } else {
            forbidAppBarScroll = false
            if (ViewCompat.isLaidOut(this)) {
                setAppBarDragCallback(null)
            } else {
                viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                    override fun onGlobalLayout() {
                        viewTreeObserver.removeOnGlobalLayoutListener(this)
                        setAppBarDragCallback(null)
                    }
                })
            }
        }
    }

    private fun setAppBarDragCallback(dragCallback: Behavior.DragCallback?) {
        val params = layoutParams as? CoordinatorLayout.LayoutParams
        val behavior = params?.behavior as? Behavior
        behavior?.setDragCallback(dragCallback)
    }

}