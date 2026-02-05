package com.example.common.widget.xrecyclerview.gesture.callback

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

/**
 * 用于RecyclerView的适配器
 */
class ItemTouchCallBack(mAdapter: RecyclerView.Adapter<*>?) : BaseTouchCallback() {
    private var mSwipeEnable = true // 是否允许左滑删除
    private val mCallBack = mAdapter as? OnItemTouchListener // 适配器需继承OnItemTouchListener重写方法

    /**
     * 返回可以滑动的方向,一般使用makeMovementFlags(int,int)
     * 或makeFlag(int, int)来构造我们的返回值
     */
    override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
        // 允许上下拖拽
        val drag = ItemTouchHelper.UP or ItemTouchHelper.DOWN
        // 允许向左滑动
        val swipe = ItemTouchHelper.LEFT
        // 设置
        return makeMovementFlags(drag, swipe)
    }

    /**
     * 上下拖动item时回调,可以调用Adapter的notifyItemMoved方法来交换两个ViewHolder的位置，
     * 最后返回true，
     * 表示被拖动的ViewHolder已经移动到了目的位置
     */
    override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
        // 通知适配器,两个子条目位置发生改变
        mCallBack?.onItemMove(viewHolder.getBindingAdapterPosition(), target.getBindingAdapterPosition())
        return true
    }

    /**
     * 当用户左右滑动item时达到删除条件就会调用,一般为一半,条目继续滑动删除,否则弹回
     */
    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        mCallBack?.onItemDelete(viewHolder.getBindingAdapterPosition())
    }

    /**
     * 支持长按拖动,默认是true
     */
    override fun isLongPressDragEnabled(): Boolean {
        return super.isLongPressDragEnabled()
    }

    /**
     * 支持滑动,即可以调用到onSwiped()方法,默认是true
     */
    override fun isItemViewSwipeEnabled(): Boolean {
        return mSwipeEnable
    }

    /**
     * 设置是否支持左滑删除
     */
    fun setmSwipeEnable(enable: Boolean) {
        this.mSwipeEnable = enable
    }

    /**
     * RecyclerView的Adapter适配器使用,继承并重写
     */
    interface OnItemTouchListener {
        /**
         * 数据交换 -> move(fromPosition, toPosition)
         */
        fun onItemMove(fromPosition: Int, toPosition: Int)

        /**
         * 数据删除 -> removed(position)
         */
        fun onItemDelete(position: Int)
    }

}