package com.example.gallery.album.widget.recyclerview

import android.view.View

/**
 * 列表条目点击事件监听器
 * 用于监听 RecyclerView / ListView 条目的点击回调
 */
interface OnItemClickListener {
    /**
     * 当条目被点击时回调
     * @param view     被点击的条目视图
     * @param position 被点击的条目位置
     */
    fun onItemClick(view: View?, position: Int)
}