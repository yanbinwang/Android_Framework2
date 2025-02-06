package com.example.mvvm.widget

import com.example.common.utils.builder.TabLayoutBuilder
import com.example.common.widget.setTabTheme
import com.example.framework.utils.function.inflate
import com.example.framework.utils.function.value.orZero
import com.example.framework.utils.function.view.background
import com.example.mvvm.R
import com.example.mvvm.databinding.ItemMainTabBinding
import com.google.android.material.tabs.TabLayout

/**
 * 首页底部切换
 * tabTitle:
 * first->选中图片
 * second->未选图片
 * third->文字
 */
class MainIndicator(tab: TabLayout?) : TabLayoutBuilder<Triple<Int, Int, String>, ItemMainTabBinding>(tab) {

    override fun getBindView() = ItemMainTabBinding.bind(mContext.inflate(R.layout.item_main_tab))

    override fun onBindView(mBinding: ItemMainTabBinding?, item: Triple<Int, Int, String>?, selected: Boolean, index: Int) {
        mBinding?.apply {
            ivIcon.background(if (selected) item?.first.orZero else item?.second.orZero)
            tvLabel.setTabTheme(item?.third, selected, R.color.homeTextSelected to R.color.homeTextUnselected, sizeRes = R.dimen.textSize10 to R.dimen.textSize10, padding = 0 to 0)
        }
    }

    fun init() {
        bind(listOf(
            Triple(R.mipmap.ic_main_bookshelf_on, R.mipmap.ic_main_bookshelf, "书架"),
            Triple(R.mipmap.ic_main_discovery_on, R.mipmap.ic_main_discovery, "发现"),
            Triple(R.mipmap.ic_main_more_on, R.mipmap.ic_main_more, "更多")))
    }

}