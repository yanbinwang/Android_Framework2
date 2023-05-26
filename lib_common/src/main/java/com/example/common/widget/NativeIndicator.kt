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
    private var onBindView: ((binding: ItemTabBinding?, item: String?, selected: Boolean, index: Int) -> Unit)? = null//如需自定義，重寫此監聽

    override fun getBindView() = ItemTabBinding.bind(context.inflate(R.layout.item_tab))

    override fun onBindView(binding: ItemTabBinding?, item: String?, selected: Boolean, index: Int) {
        if(null == onBindView) {
            binding?.tvTitle.apply {
                setArguments(item.orEmpty(), if (selected) R.color.appTheme else R.color.textHint)
                textSize(if (selected) 16.pt else 14.pt)
                bold(selected)
            }
        } else onBindView?.invoke(binding, item, selected, index)
    }

    fun setBindViewListener(onBindView: ((binding: ItemTabBinding?, item: String?, selected: Boolean, index: Int) -> Unit)) {
        this.onBindView = onBindView
    }

}