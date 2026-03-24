package com.yanzhenjie.album.api;

import android.content.Context;

import androidx.annotation.IntRange;

import com.yanzhenjie.album.ItemAction;

import java.util.ArrayList;

/**
 * 预览功能 顶层抽象父类
 * 继承自：BasicAlbumWrapper
 * 作用：专门给【图片/视频预览页面】用的
 * 也就是 GalleryActivity 对应的外部调用封装
 * 功能：预览、当前位置、选中列表、点击/长按事件、是否可选择
 */
public abstract class BasicGalleryWrapper<Returner extends BasicGalleryWrapper, Result, Cancel, Checked> extends BasicAlbumWrapper<Returner, ArrayList<Result>, Cancel, ArrayList<Checked>> {
    // 当前预览的位置
    protected int mCurrentPosition;
    // 预览时是否可勾选（选择）
    protected boolean mCheckable;
    // 预览图片点击事件
    protected ItemAction<Checked> mItemClick;
    // 预览图片长按事件
    protected ItemAction<Checked> mItemLongClick;

    public BasicGalleryWrapper(Context context) {
        super(context);
    }

    /**
     * 设置已经选中的列表（预览时会标记勾选）
     */
    public final Returner checkedList(ArrayList<Checked> checked) {
        this.mChecked = checked;
        return (Returner) this;
    }

    /**
     * 预览图片单击事件
     */
    public Returner itemClick(ItemAction<Checked> click) {
        this.mItemClick = click;
        return (Returner) this;
    }

    /**
     * 预览图片长按事件
     */
    public Returner itemLongClick(ItemAction<Checked> longClick) {
        this.mItemLongClick = longClick;
        return (Returner) this;
    }

    /**
     * 设置从第几张开始预览
     */
    public Returner currentPosition(@IntRange(from = 0, to = Integer.MAX_VALUE) int currentPosition) {
        this.mCurrentPosition = currentPosition;
        return (Returner) this;
    }

    /**
     * 预览页面是否显示选择框
     */
    public Returner checkable(boolean checkable) {
        this.mCheckable = checkable;
        return (Returner) this;
    }

}