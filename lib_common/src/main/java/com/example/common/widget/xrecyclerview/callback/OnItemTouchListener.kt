package com.example.common.widget.xrecyclerview.callback

/**
 * author:wyb
 */
interface OnItemTouchListener {

    //数据交换
    fun onItemMove(fromPosition: Int, toPosition: Int)

    //数据删除
    fun onItemDelete(position: Int)

}
