package com.example.mvvm.widget.customitem

import android.annotation.SuppressLint
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.framework.utils.function.value.safeGet
import com.example.framework.utils.function.value.safeSize
import com.example.framework.utils.function.view.initGridHorizontal

/**
 *  Created by wangyanbin
 *  按钮适配器
 */
@SuppressLint("NotifyDataSetChanged")
class CustomItemAdapter : RecyclerView.Adapter<CustomItemAdapter.ViewHolder>() {
    private var columns = -1
    private var tabCount = -1
    private var list = ArrayList<List<Triple<Boolean, String, String>>>()
    private var listener: ((position: Int) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(RecyclerView(parent.context))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        //当前页数（0开始）
        val page = holder.absoluteAdapterPosition
        //每一页的容器
        val recycler = (holder.itemView as? RecyclerView)
        //每一页容器的适配器
        val adapter = CustomTabAdapter()
        //每一页容器的按钮点击回调
        adapter.setOnItemClickListener { _, _, index ->
            //下标值计算：每页总按钮数*当前页数+下标（默认0）
            val mPosition = tabCount * page + index
            listener?.invoke(mPosition)
        }
        //设置一行加载按钮的数量，加载当前页
        recycler.initGridHorizontal(adapter, columns)
        //每一页容器的整体数据
        val item = list.safeGet(page)
        //刷新一下按钮数据
        adapter.refresh(item)
    }

    override fun getItemCount(): Int {
        return list.safeSize
    }

    /**
     * columns：设置子page一行加载的数量--4
     * tabCount:子页一共容纳的数量，比如一页加载满默认应该是8个按钮（上下各4个）--8
     */
    fun setConfiguration(columns: Int, tabCount: Int) {
        this.columns = columns
        this.tabCount = tabCount
    }

    /**
     * 刷新按钮
     */
    fun refresh(data: List<List<Triple<Boolean, String, String>>>?) {
        data ?: return
        this.list.clear()
        this.list.addAll(data)
        notifyDataSetChanged()
    }

    /**
     * 设置按钮的点击
     */
    fun setOnItemClickListener(listener: ((position: Int) -> Unit)) {
        this.listener = listener
    }

    class ViewHolder(itemView: RecyclerView) : RecyclerView.ViewHolder(itemView) {
        init {
            itemView.layoutParams = RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT)
        }
    }

}