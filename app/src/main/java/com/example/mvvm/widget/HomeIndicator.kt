package com.example.mvvm.widget

import com.example.common.utils.builder.TabLayoutBuilder
import com.example.common.utils.function.setArguments
import com.example.framework.utils.function.inflate
import com.example.framework.utils.function.value.orZero
import com.example.framework.utils.function.view.setResource
import com.example.mvvm.R
import com.example.mvvm.databinding.ItemHomeTabBinding
import com.google.android.material.tabs.TabLayout

/**
 * @description
 * @author
 */
class HomeIndicator constructor(tab: TabLayout, tabList: List<Triple<String, Int, Int>>) : TabLayoutBuilder<Triple<String, Int, Int>, ItemHomeTabBinding>(tab, tabList) {

    override fun getBindView() = ItemHomeTabBinding.bind(context.inflate(R.layout.item_home_tab))

    override fun onBindView(binding: ItemHomeTabBinding?, item: Triple<String, Int, Int>?, selected: Boolean, index: Int) {
        binding?.apply {
            ivTab.setResource(if (selected) item?.second.orZero else item?.third.orZero)
            tvTab.setArguments(item?.first.orEmpty(), if (selected) R.color.homeTabSelected else R.color.homeTabUnselected)
        }
    }

}