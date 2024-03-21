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
    private var columns = 4//默认一行4个
    private var list = ArrayList<List<Pair<String, String>>>()
    private var listener: ((position: Int) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(RecyclerView(parent.context))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val absolutePosition = holder.absoluteAdapterPosition
        val mList = list.safeGet(absolutePosition)
        (holder.itemView as? RecyclerView).initGridHorizontal(CustomTabAdapter().apply {
            refresh(mList)
            setOnItemClickListener { _, index ->
                listener?.invoke(if (absolutePosition == 0) index else index + mList.safeSize * absolutePosition - 1)
            }
        }, columns)
    }

    override fun getItemCount(): Int {
        return list.safeSize
    }

    fun refresh(mList: List<List<Pair<String, String>>>) {
        this.list.clear()
        this.list.addAll(mList)
        notifyDataSetChanged()
    }

    fun setColumns(columns: Int) {
        this.columns = columns
    }

    fun setOnItemClickListener(listener: ((position: Int) -> Unit)) {
        this.listener = listener
    }

    class ViewHolder(itemView: RecyclerView) : RecyclerView.ViewHolder(itemView) {
        init {
            itemView.layoutParams = RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT)
        }
    }

}