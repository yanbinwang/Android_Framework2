package com.example.common.widget

import android.view.View
import android.widget.TextView
import com.example.base.utils.builder.TabLayoutBuilder
import com.example.base.utils.function.view.setMediumBold
import com.example.common.R
import com.example.common.utils.setArguments
import com.google.android.material.tabs.TabLayout

/**
 * @description 菜单头工具类
 * 掏空系统tablayout，全部自定义
 * @author yan
 */
class IndicatorLayout constructor(tab: TabLayout, tabTitle: MutableList<String>) : TabLayoutBuilder<String>(tab, tabTitle) {

    override fun getLayoutRes() = R.layout.item_tab

    override fun onBindView(view: View, item: String?, selected: Boolean) {
        view.findViewById<TextView>(R.id.tv_title).apply {
            setMediumBold(selected)
            setArguments(item.orEmpty(), if (selected) R.color.blue_3d81f2 else R.color.grey_333333)
        }
    }

}