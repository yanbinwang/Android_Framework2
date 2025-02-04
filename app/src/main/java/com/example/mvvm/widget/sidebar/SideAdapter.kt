package com.example.mvvm.widget.sidebar

import com.example.common.base.binding.adapter.BaseQuickAdapter
import com.example.common.base.binding.adapter.BaseViewDataBindingHolder
import com.example.common.utils.function.orNoData
import com.example.framework.utils.function.value.orFalse
import com.example.framework.utils.function.value.orZero
import com.example.framework.utils.function.view.gone
import com.example.framework.utils.function.view.visible
import com.example.mvvm.databinding.ItemSideBinding
import com.example.mvvm.widget.sidebar.bean.SortBean
import java.util.Locale

/**
 * 城市适配器
 */
class SideAdapter : BaseQuickAdapter<SortBean, ItemSideBinding>() {

    override fun onConvert(holder: BaseViewDataBindingHolder, item: SortBean?, payloads: MutableList<Any>?) {
        super.onConvert(holder, item, payloads)
        mBinding?.apply {
            val position = holder.absoluteAdapterPosition
            val section = getSectionForPosition(item).orZero
            //如果当前下标和选中下标的第一个相等，则显示标签
            if (position == getPositionForSection(section)) {
                tvLetter.visible()
                tvLetter.text = item?.sortLetters.orNoData()
            } else {
                tvLetter.gone()
            }
            val title = item?.name
            if (title?.contains("/").orFalse) {
                val index = title?.indexOf("/").orZero
                tvTitle.text = title?.substring(0, index).orNoData()
                tvInfo.text = title?.subSequence(index, title.length).toString().orNoData()
            } else {
                tvTitle.text = title.orNoData()
            }
        }
    }

    /**
     * 获取传入对象标题ABCD的ascii码（java使用chatAt，kotlin使用code）
     */
    fun getSectionForPosition(bean: SortBean?): Int? {
        return bean?.sortLetters?.get(0)?.code
    }

    /**
     * 如果当前位置等于该分类首字母的Char的位置 ，则认为是第一次出现
     * position == getPositionForSection(section)
     */
    fun getPositionForSection(section: Int): Int {
        for (i in 0 until itemCount) {
            val sortStr = item(i)?.sortLetters.orEmpty()
            val firstChar = sortStr.uppercase(Locale.getDefault())[0]
            if (firstChar.code == section) {
                return i
            }
        }
        return -1
    }

}