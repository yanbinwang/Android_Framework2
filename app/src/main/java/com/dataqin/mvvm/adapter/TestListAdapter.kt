package com.dataqin.mvvm.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.dataqin.mvvm.R
import com.dataqin.mvvm.databinding.ItemTestBinding
import com.dataqin.mvvm.model.TestListModel

/**
 * Created by WangYanBin on 2020/7/10.
 */
class TestListAdapter : BaseQuickAdapter<TestListModel, BaseDataBindingHolder<ItemTestBinding>>(R.layout.item_test) {

    override fun convert(holder: BaseDataBindingHolder<ItemTestBinding>, item: TestListModel) {
        holder.dataBinding?.apply {
            //赋值
            model = item
            //设置事件
            ivImg.setOnClickListener { v -> setOnItemChildClick(v, holder.adapterPosition) }
            tvTitle.setOnClickListener { v -> setOnItemChildClick(v, holder.adapterPosition) }
        }
    }

}