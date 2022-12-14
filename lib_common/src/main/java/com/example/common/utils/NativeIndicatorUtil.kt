package com.example.common.utils

import com.example.framework.utils.function.inflate
import com.example.common.R
import com.example.common.databinding.ItemTabBinding
import com.example.common.utils.builder.TabLayoutBuilder
import com.example.common.utils.function.setArguments
import com.google.android.material.tabs.TabLayout

/**
 * @description 菜单头工具类
 * 掏空系统tablayout，全部自定义
 * @author yan
 */
class NativeIndicatorUtil constructor(tab: TabLayout, tabTitle: MutableList<String>) : TabLayoutBuilder<String,ItemTabBinding>(tab, tabTitle) {

    override fun getBindView() = ItemTabBinding.bind(context.inflate(R.layout.item_tab))

    override fun onBindView(binding: ItemTabBinding?, item: String?, selected: Boolean, index: Int) {
        binding?.tvTitle.setArguments(item.orEmpty(), if (selected) R.color.blue_3d81f2 else R.color.grey_333333)
    }

}