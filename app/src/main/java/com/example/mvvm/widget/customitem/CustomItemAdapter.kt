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
    private var count = -1
    private var columns = -1
    private var list = ArrayList<List<Triple<Boolean, String, String>>>()
    private var listener: ((position: Int) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(RecyclerView(parent.context))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        //当前页数（0开始）
        val absolutePosition = holder.absoluteAdapterPosition
        //每一页的容器
        val recycler = (holder.itemView as? RecyclerView)
        //每一页容器的适配器
        val adapter = CustomTabAdapter()
        //根据设置的一行加载的数量，加载当前页数
        recycler.initGridHorizontal(adapter, columns)
        //每一页的整体集合数据
        val item = list.safeGet(absolutePosition)
        //刷新一下按钮
        adapter.refresh(item)
        //添加对应按钮的点击回调
        adapter.setOnItemClickListener { t, index ->
            //下标值计算：每页总按钮数*当前页数+下标（默认0）
            val mPosition = count * absolutePosition + index
            listener?.invoke(mPosition)
        }
    }

    override fun getItemCount(): Int {
        return list.safeSize
    }

    /**
     * columns：设置子page一行加载的数量
     * count:子页一共容纳的数量，比如一页加载满默认应该是8个按钮（上下各4个）
     */
    fun setConfiguration(count: Int = 8, columns: Int = 4) {
        this.count = count
        this.columns = columns
    }

    /**
     * 刷新按钮
     */
    fun refresh(mList: List<List<Triple<Boolean, String, String>>>) {
        this.list.clear()
        this.list.addAll(mList)
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