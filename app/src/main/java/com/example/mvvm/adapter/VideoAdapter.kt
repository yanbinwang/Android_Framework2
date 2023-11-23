package com.example.mvvm.adapter

import com.example.common.base.binding.adapter.BaseQuickAdapter
import com.example.common.base.binding.adapter.BaseViewDataBindingHolder
import com.example.mvvm.databinding.ItemImageBinding

/**
 * 适配器中的list不会被清空，会持续增长
 * 向上滑则插到当前集合前方，向下滑则插入当前集合后方
 */
class VideoAdapter : BaseQuickAdapter<Int, ItemImageBinding>() {

    override fun convert(
        holder: BaseViewDataBindingHolder,
        item: Int?,
        payloads: MutableList<Any>?
    ) {
        super.convert(holder, item, payloads)
//        binding.root.layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
//        val index = holder.absoluteAdapterPosition.mod(list().safeSize)
//        val bean = list().safeGet(index)
    }

    /**
     * 通过当前方法调取刷新
     */
    fun refresh(list: List<Int>, isBottom: Boolean) {
        if (isBottom) {
            insert(list)
        } else {
            insert(0, list)
        }
    }

    override fun getItemCount(): Int {
        return Int.MAX_VALUE
    }

}