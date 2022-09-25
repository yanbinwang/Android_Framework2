package com.example.mvvm.adapter

import com.example.common.base.binding.BaseQuickAdapter
import com.example.common.base.binding.BaseViewDataBindingHolder
import com.example.mvvm.databinding.ItemTestBinding
import com.example.mvvm.model.TestListModel

/**
 * Created by WangYanBin on 2020/7/10.
 */
class TestListAdapter : BaseQuickAdapter<TestListModel, ItemTestBinding>() {

    override fun convert(
        holder: BaseViewDataBindingHolder,
        item: TestListModel?,
        payloads: MutableList<Any>?
    ) {
        holder.getBinding<ItemTestBinding>().apply {
            //赋值
            model = item
//            //设置事件
//            ivImg.setOnClickListener { v -> setOnItemChildClick(v, holder.absoluteAdapterPosition) }
//            tvTitle.setOnClickListener { v -> setOnItemChildClick(v, holder.absoluteAdapterPosition) }
        }
    }

}