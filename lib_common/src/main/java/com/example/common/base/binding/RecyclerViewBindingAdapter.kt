package com.example.common.base.binding

import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.listener.OnItemChildClickListener
import com.chad.library.adapter.base.listener.OnItemClickListener
import com.example.common.widget.xrecyclerview.XRecyclerView
import com.example.common.widget.xrecyclerview.callback.OnRefreshListener

/**
 * Create by wyb at 20/4/18
 * 定义一些默认的属性，最好控制在一个传入变量，多了不如直接写方法调用
 */
object RecyclerViewBindingAdapter {

    @JvmStatic
    @BindingAdapter(value = ["app:adapter"])
    fun setAdapter(recyclerView: XRecyclerView?, adapter: BaseQuickAdapter<*, *>?) {
        if (recyclerView != null && adapter != null) {
            recyclerView.recyclerView.adapter = adapter
        }
    }

    @JvmStatic
    @BindingAdapter(value = ["app:itemNormalSpace"])
    fun setNormalItemDecoration(recyclerView: XRecyclerView?, itemNormalSpace: Int) {
        recyclerView?.addItemDecoration(itemNormalSpace, 0, true, false)
    }

    @JvmStatic
    @BindingAdapter(value = ["app:itemAroundSpace"])
    fun setAroundItemDecoration(recyclerView: XRecyclerView?, itemAroundSpace: Int) {
        recyclerView?.addItemDecoration(itemAroundSpace, itemAroundSpace, true, true)
    }

    @JvmStatic
    @BindingAdapter(value = ["app:emptyBackgroundColor"])
    fun setEmptyBackgroundColor(recyclerView: XRecyclerView?, color: Int) {
        recyclerView?.setEmptyBackgroundColor(color)
    }

    @JvmStatic
    @BindingAdapter(value = ["app:itemClickListener"])
    fun setOnItemClickListener(recyclerView: XRecyclerView?, onItemClickListener: OnItemClickListener?) {
        val adapter: BaseQuickAdapter<*, *> = recyclerView?.recyclerView?.adapter as BaseQuickAdapter<*, *>
        adapter.setOnItemClickListener(onItemClickListener)
    }

    @JvmStatic
    @BindingAdapter(value = ["app:itemChildClickListener"])
    fun setOnItemClickListener(recyclerView: XRecyclerView?, onItemChildClickListener: OnItemChildClickListener?) {
        val adapter: BaseQuickAdapter<*, *> = recyclerView?.recyclerView?.adapter as BaseQuickAdapter<*, *>
        adapter.setOnItemChildClickListener(onItemChildClickListener)
    }

    @JvmStatic
    @BindingAdapter(value = ["app:refreshListener"])
    fun setOnRefreshListener(recyclerView: XRecyclerView?, onRefreshListener: OnRefreshListener?) {
        recyclerView?.setOnRefreshListener(onRefreshListener)
    }

}