package com.yanzhenjie.album.widget.divider;

import androidx.recyclerview.widget.RecyclerView;

/**
 * 列表分割线抽象基类
 * 继承自 RecyclerView.ItemDecoration
 * 定义：分割线的宽度、高度接口
 */
public abstract class Divider extends RecyclerView.ItemDecoration {

    /**
     * 获取分割线高度
     * @return 分割线高度
     */
    public abstract int getHeight();

    /**
     * 获取分割线宽度
     * @return 分割线宽度
     */
    public abstract int getWidth();

}