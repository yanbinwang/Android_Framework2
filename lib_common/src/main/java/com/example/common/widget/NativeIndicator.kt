package com.example.common.widget

import com.example.common.R
import com.example.common.databinding.ItemTabBinding
import com.example.common.utils.builder.TabLayoutBuilder
import com.example.common.utils.function.pt
import com.example.common.utils.function.setArguments
import com.example.framework.utils.function.inflate
import com.example.framework.utils.function.view.bold
import com.example.framework.utils.function.view.textSize
import com.google.android.material.tabs.TabLayout

/**
 * @description 菜单头工具类
 * 掏空系统tablayout，全部自定义
 * @author yan
 */
class NativeIndicator constructor(tab: TabLayout, tabTitle: List<String>) : TabLayoutBuilder<String, ItemTabBinding>(tab, tabTitle) {
    var onBindView: ((binding: ItemTabBinding?, item: String?, selected: Boolean, index: Int) -> Unit)? = null//如需自定義，重寫此監聽

    override fun getBindView() = ItemTabBinding.bind(context.inflate(R.layout.item_tab))

    override fun onBindView(binding: ItemTabBinding?, item: String?, selected: Boolean, index: Int) {
        binding?.tvTitle.apply {
            setArguments(item.orEmpty(), if (selected) R.color.blue_3d81f2 else R.color.grey_333333)
            textSize(if (selected) 16.pt else 14.pt)
            bold(selected)
        }
        onBindView?.invoke(binding, item, selected, index)
    }

}