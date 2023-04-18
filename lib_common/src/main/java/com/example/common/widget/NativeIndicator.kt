package com.example.common.widget

import com.example.common.R
import com.example.common.databinding.ItemTabBinding
import com.example.common.utils.builder.TabLayoutBuilder
import com.example.common.utils.function.pt
import com.example.common.utils.function.setArguments
import com.example.framework.utils.function.inflate
import com.example.framework.utils.function.value.orZero
import com.example.framework.utils.function.view.bold
import com.example.framework.utils.function.view.textSize
import com.google.android.material.tabs.TabLayout

/**
 * @description 菜单头工具类
 * 掏空系统tablayout，全部自定义
 * @author yan
 */
class NativeIndicator constructor(tab: TabLayout, tabTitle: List<String>) : TabLayoutBuilder<String, ItemTabBinding>(tab, tabTitle) {
    private var paramsMap = HashMap<String, Int>()

    init {
        setParams()
    }

    override fun getBindView() = ItemTabBinding.bind(context.inflate(R.layout.item_tab))

    override fun onBindView(binding: ItemTabBinding?, item: String?, selected: Boolean, index: Int) {
        binding?.tvTitle.apply {
            setArguments(item.orEmpty(), if (selected) paramsMap["selectedColor"].orZero else paramsMap["oriSelectedColor"].orZero)
            textSize(if (selected) paramsMap["selectedSize"].orZero else paramsMap["oriSelectedSize"].orZero)
            when (paramsMap["isBold"]) {
                0 -> bold(false)
                1 -> bold(true)
                else -> bold(selected)
            }
        }
    }

    /**
     * 在bind前可重寫
     * isBold->0都不加粗，1都加粗，2選中的加粗未選擇不加粗
     */
    fun setParams(selectedSize: Int = 16.pt, oriSelectedSize: Int = 14.pt, selectedColor: Int = R.color.blue_3d81f2, oriSelectedColor: Int = R.color.grey_333333, isBold: Int = 0) {
        paramsMap["selectedSize"] = selectedSize
        paramsMap["oriSelectedSize"] = oriSelectedSize
        paramsMap["selectedColor"] = selectedColor
        paramsMap["oriSelectedColor"] = oriSelectedColor
        paramsMap["isBold"] = isBold
    }

}