package com.example.gallery.album.widget.recyclerview

import android.widget.CompoundButton

/**
 * 列表条目内复选框/开关的点击监听器
 * 专门用于监听 CheckBox、Switch 等按钮的选中事件
 */
interface OnCheckedClickListener {
    /**
     * 当复合按钮（CheckBox/Switch）被点击时回调
     * @param button   被点击的复合按钮对象
     * @param position 条目在列表中的位置
     */
    fun onCheckedClick(button: CompoundButton?, position: Int)
}