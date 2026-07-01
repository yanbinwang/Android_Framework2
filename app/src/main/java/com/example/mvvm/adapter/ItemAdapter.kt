package com.example.mvvm.adapter

import com.example.common.base.binding.adapter.BaseQuickAdapter
import com.example.common.base.binding.adapter.BaseViewDataBindingHolder
import com.example.common.widget.xrecyclerview.gesture.ItemDecorationCallBack
import com.example.mvvm.BR
import com.example.mvvm.bean.TestBean
import com.example.mvvm.databinding.ItemTestBinding

class ItemAdapter : BaseQuickAdapter<TestBean, ItemTestBinding>() , ItemDecorationCallBack.OnItemDecorationListener {

    override fun onConvert(holder: BaseViewDataBindingHolder, item: TestBean?, payloads: MutableList<Any>?) {
        super.onConvert(holder, item, payloads)
        setExecutePendingVariable(BR.bean, item)
    }

    override fun onItemMove(fromPosition: Int, toPosition: Int) {
//        // 交换位置
//        Collections.swap(list(), fromPosition, toPosition)
//        // 局部刷新(移动)
//        notifyItemMoved(fromPosition, toPosition)
        move(fromPosition, toPosition)
    }

    override fun onItemDelete(position: Int) {
//        // 删除数据
//        list().removeAt(position)
//        // 局部刷新(移除)
//        notifyItemRemoved(position)
        removed(position)
    }

}