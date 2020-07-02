package com.example.common.base.binding

import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.common.widget.xrecyclerview.XRecyclerView
import com.example.common.widget.xrecyclerview.callback.OnRefreshListener

/**
 * Create by wyb at 20/4/18
 * 定义一些默认的属性，最好控制在一个传入变量，多了不如直接写方法调用
 */
object RecyclerViewBindingAdapter {

    @BindingAdapter(value = ["app:adapter"])
    fun setAdapter(recyclerView: XRecyclerView?, adapter: RecyclerView.Adapter<*>?) {
        if (recyclerView != null && adapter != null) {
            recyclerView.recyclerView.adapter = adapter
        }
    }

    @BindingAdapter(value = ["app:itemNormalSpace"])
    fun setNormalItemDecoration(recyclerView: XRecyclerView?, itemNormalSpace: Int) {
        recyclerView?.addItemDecoration(itemNormalSpace, 0, true, false)
    }

    @BindingAdapter(value = ["app:itemAroundSpace"])
    fun setAroundItemDecoration(recyclerView: XRecyclerView?, itemAroundSpace: Int) {
        recyclerView?.addItemDecoration(itemAroundSpace, itemAroundSpace, true, true)
    }

    @BindingAdapter(value = ["app:emptyBackgroundColor"])
    fun setEmptyBackgroundColor(recyclerView: XRecyclerView?, color: Int) {
        recyclerView?.setEmptyBackgroundColor(color)
    }

    @BindingAdapter(value = ["app:refreshListener"])
    fun setOnRefreshListener(recyclerView: XRecyclerView?, onRefreshListener: OnRefreshListener?) {
        recyclerView?.setOnRefreshListener(onRefreshListener)
    }

}