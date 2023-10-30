package com.example.mvvm.widget

import com.example.common.utils.builder.TabLayoutBuilder
import com.example.framework.utils.function.inflate
import com.example.framework.utils.function.value.orZero
import com.example.framework.utils.function.view.background
import com.example.framework.utils.function.view.textColor
import com.example.mvvm.R
import com.example.mvvm.databinding.ItemHomeTabBinding
import com.google.android.material.tabs.TabLayout

class MainIndicator constructor(tab: TabLayout, tabTitle: List<Triple<Int, Int, Int>>) : TabLayoutBuilder<Triple<Int, Int, Int>, ItemHomeTabBinding>(tab, tabTitle) {

    override fun getBindView() = ItemHomeTabBinding.bind(context.inflate(R.layout.item_home_tab))

    override fun onBindView(binding: ItemHomeTabBinding?, item: Triple<Int, Int, Int>?, selected: Boolean, index: Int) {
        binding?.apply {
            ivIcon.background(if (selected) item?.first.orZero else item?.second.orZero)
            tvLabel.setI18nRes(item?.third.orZero)
            tvLabel.textColor(if (selected) R.color.mainTabSelected else R.color.mainTabUnselected)
        }
    }

}