package com.example.common.utils

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets

/**
 * description 表单形的页面，设置软键盘弹出后自动resize
 * author yan
 */
@Deprecated("使用基类的setOnWindowInsetsChanged方法更精确")
object FormActivityUtil {

    fun setAct(activity: Activity, extra: (bottom: Int) -> Unit = {}) {
        val rootView = (activity.findViewById<View>(android.R.id.content) as? ViewGroup)?.getChildAt(0)
        // 让视图自动根据系统 insets 调整自身 padding，从而避免内容被系统窗口（状态栏、导航栏等）遮挡
        rootView?.fitsSystemWindows = true
        rootView?.setOnApplyWindowInsetsListener(object : View.OnApplyWindowInsetsListener {
            var bottomBefore = 0
            override fun onApplyWindowInsets(v: View, insets: WindowInsets): WindowInsets {
                val bottom = insets.systemWindowInsetBottom
                if (bottomBefore != bottom) {
                    bottomBefore = bottom
                    extra(bottom)
                }
                return v.onApplyWindowInsets(insets.replaceSystemWindowInsets(0, 0, 0, bottom))
            }
        })
    }

    fun setView(view: View, extra: (bottom: Int) -> Unit = {}) {
        view.fitsSystemWindows = true
        view.setOnApplyWindowInsetsListener(object : View.OnApplyWindowInsetsListener {
            var bottomBefore = 0
            override fun onApplyWindowInsets(v: View, insets: WindowInsets): WindowInsets {
                val bottom = insets.systemWindowInsetBottom
                if (bottomBefore != bottom) {
                    bottomBefore = bottom
                    extra(bottom)
                }
                return v.onApplyWindowInsets(insets.replaceSystemWindowInsets(0, 0, 0, bottom))
            }
        })
    }

}