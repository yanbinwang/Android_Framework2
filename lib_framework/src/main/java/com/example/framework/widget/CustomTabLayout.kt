package com.example.framework.widget

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import com.example.framework.utils.function.value.orZero
import com.google.android.material.tabs.TabLayout

class CustomTabLayout @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : TabLayout(context, attrs, defStyleAttr) {

    override fun addTab(tab: Tab, setSelected: Boolean) {
        super.addTab(tab, setSelected)
        updateTabViews()
    }

    private fun updateTabViews() {
        val tabStrip = getChildAt(0) as? LinearLayout
        for (i in 0 until tabStrip?.childCount.orZero) {
            val tabView = tabStrip?.getChildAt(i)
            tabView?.setPadding(0, 0, 0, 0)
            tabView?.requestLayout()
        }
    }

}