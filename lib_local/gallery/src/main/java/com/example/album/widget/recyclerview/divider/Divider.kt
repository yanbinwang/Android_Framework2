package com.example.album.widget.recyclerview.divider

import androidx.recyclerview.widget.RecyclerView.ItemDecoration

/**
 * 列表分割线抽象基类
 * 继承自 RecyclerView.ItemDecoration
 * 定义：分割线的宽度、高度接口
 */
abstract class Divider : ItemDecoration() {
    /**
     * 获取分割线高度
     * @return 分割线高度
     */
    abstract fun getHeight(): Int

    /**
     * 获取分割线宽度
     * @return 分割线宽度
     */
    abstract fun getWidth(): Int
}