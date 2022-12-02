package com.example.common.utils

import android.app.Activity
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets

/**
 * description 表单形的页面，设置软键盘弹出后自动resize
 * author yan
 */
object FormActivityUtil {

    fun setAct(activity: Activity, extra: (bottom: Int) -> Unit = {}) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            val rootView = (activity.findViewById<View>(android.R.id.content) as ViewGroup).getChildAt(0)
            rootView.fitsSystemWindows = true
            rootView.setOnApplyWindowInsetsListener(object : View.OnApplyWindowInsetsListener {
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

    fun setView(view: View, extra: (bottom: Int) -> Unit = {}) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
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

}