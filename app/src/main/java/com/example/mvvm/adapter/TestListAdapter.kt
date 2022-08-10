package com.example.mvvm.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.example.mvvm.R
import com.example.mvvm.databinding.ItemTestBinding
import com.example.mvvm.model.TestListModel

/**
 * Created by WangYanBin on 2020/7/10.
 */
class TestListAdapter : BaseQuickAdapter<TestListModel, BaseDataBindingHolder<ItemTestBinding>>(R.layout.item_test) {

    override fun convert(holder: BaseDataBindingHolder<ItemTestBinding>, item: TestListModel) {
        holder.dataBinding?.apply {
            //赋值
            model = item
            //设置事件
            ivImg.setOnClickListener { v -> setOnItemChildClick(v, holder.absoluteAdapterPosition) }
            tvTitle.setOnClickListener { v -> setOnItemChildClick(v, holder.absoluteAdapterPosition) }
        }
    }

}