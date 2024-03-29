package com.example.framework.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.recyclerview.widget.RecyclerView

/**
 * author: wyb
 * date: 2017/8/25.
 * 带检测数据是否为空的recyclerview
 */
class ObserverRecyclerView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : RecyclerView(context, attrs, defStyleAttr) {
    private var empty: View? = null//adapter没有数据的时候显示,类似于listView的emptyView
    private val observer by lazy { object : AdapterDataObserver() {
        override fun onChanged() {
            isEmpty()
        }

        override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
            isEmpty()
        }

        override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
            isEmpty()
        }
    }}

    /**
     * 当给与recyclerview内容时做检测
     * @param adapter
     */
    override fun setAdapter(adapter: Adapter<*>?) {
        val oldAdapter = getAdapter()
        oldAdapter?.unregisterAdapterDataObserver(observer)
        super.setAdapter(adapter)
        adapter?.registerAdapterDataObserver(observer)
        isEmpty()
    }

    /**
     * 检测内容是否为空
     */
    private fun isEmpty() {
        if (empty != null && adapter != null) {
            val emptyViewVisible = adapter?.itemCount == 0
            empty?.visibility = if (emptyViewVisible) VISIBLE else GONE
            visibility = if (emptyViewVisible) GONE else VISIBLE
        }
    }

    /**
     * 设置数据为空时候显示的view
     * @param empty
     */
    fun setEmptyView(empty: View?) {
        this.empty = empty
        isEmpty()
    }

}