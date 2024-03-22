package com.example.mvvm.widget

import com.example.common.utils.builder.TabLayoutBuilder
import com.example.framework.utils.function.inflate
import com.example.framework.utils.function.value.orZero
import com.example.framework.utils.function.view.background
import com.example.framework.utils.function.view.textColor
import com.example.mvvm.R
import com.example.mvvm.databinding.ItemHomeTabBinding
import com.google.android.material.tabs.TabLayout

/**
 * 首页底部切换
 * tabTitle:
 * first->选中图片
 * second->未选图片
 * third->文字res
 */
class MainIndicator(tab: TabLayout?) : TabLayoutBuilder<Triple<Int, Int, Int>, ItemHomeTabBinding>(tab) {

    override fun getBindView() = ItemHomeTabBinding.bind(mContext.inflate(R.layout.item_home_tab))

    override fun onBindView(mBinding: ItemHomeTabBinding?, item: Triple<Int, Int, Int>?, selected: Boolean, index: Int) {
        mBinding?.apply {
            ivIcon.background(if (selected) item?.first.orZero else item?.second.orZero)
            tvLabel.setI18nRes(item?.third.orZero)
            tvLabel.textColor(if (selected) R.color.homeTextSelected else R.color.homeTextUnselected)
        }
    }

    fun init() {
        bind(listOf(
            Triple(R.mipmap.ic_main_home_on, R.mipmap.ic_main_home, R.string.mainHome),
            Triple(R.mipmap.ic_main_market_on, R.mipmap.ic_main_market, R.string.mainMarket),
            Triple(R.mipmap.ic_main_balance_on, R.mipmap.ic_main_balance, R.string.mainBalance),
            Triple(R.mipmap.ic_main_user_on, R.mipmap.ic_main_user, R.string.mainUser)))
    }

}