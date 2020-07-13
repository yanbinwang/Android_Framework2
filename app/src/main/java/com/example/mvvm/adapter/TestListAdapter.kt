package com.example.mvvm.adapter

import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.listener.OnItemClickListener
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.example.mvvm.R
import com.example.mvvm.databinding.ItemTestBinding
import com.example.mvvm.model.TestListModel

/**
 * Created by WangYanBin on 2020/7/10.
 */
class TestListAdapter :
    BaseQuickAdapter<TestListModel?, BaseDataBindingHolder<*>>(R.layout.item_test) {

    override fun onItemViewHolderCreated(viewHolder: BaseDataBindingHolder<*>, viewType: Int) {
        super.onItemViewHolderCreated(viewHolder, viewType)
        DataBindingUtil.bind<ViewDataBinding>(viewHolder.itemView)
    }

    override fun convert(holder: BaseDataBindingHolder<*>, item: TestListModel?) {
        if (item == null) {
            return
        }
        //设置数据
        val binding: ItemTestBinding = holder.dataBinding as ItemTestBinding
        binding.model = item
        binding.ivImg.setOnClickListener { v -> setOnItemChildClick(v, holder.adapterPosition); }
        binding.tvTitle.setOnClickListener { v -> setOnItemChildClick(v,holder.adapterPosition) }
    }

}