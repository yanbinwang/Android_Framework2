package com.example.gallery.feature.album.api

import android.content.Context
import androidx.annotation.IntRange
import com.example.gallery.feature.album.callback.ItemAction

/**
 * 预览功能 顶层抽象父类
 * 继承自：BasicAlbumWrapper
 * 作用：专门给【图片/视频预览页面】用的 , 也就是 GalleryActivity 对应的外部调用封装
 * 功能：预览、当前位置、选中列表、点击/长按事件、是否可选择
 */
abstract class BasicGalleryWrapper<Returner : BasicGalleryWrapper<Returner, Result, Cancel, Checked>, Result, Cancel, Checked>(context: Context) : BasicAlbumWrapper<Returner, ArrayList<Result>, Cancel, ArrayList<Checked>>(context) {
    // 当前预览的位置
    protected var mCurrentPosition = 0
    // 预览时是否可勾选（选择）
    protected var mCheckable = false
    // 预览图片点击事件
    protected var mItemClick: ItemAction<Checked>? = null
    // 预览图片长按事件
    protected var mItemLongClick: ItemAction<Checked>? = null

    /**
     * 设置已经选中的列表（预览时会标记勾选）
     */
    fun checkedList(checked: ArrayList<Checked>): Returner {
        this.mChecked = checked
        return this as Returner
    }

    /**
     * 预览图片单击事件
     */
    fun itemClick(click: ItemAction<Checked>): Returner {
        this.mItemClick = click
        return this as Returner
    }

    /**
     * 预览图片长按事件
     */
    fun itemLongClick(longClick: ItemAction<Checked>): Returner {
        this.mItemLongClick = longClick
        return this as Returner
    }

    /**
     * 设置从第几张开始预览
     */
    fun currentPosition(@IntRange(from = 0, to = Long.MAX_VALUE) currentPosition: Int): Returner {
        this.mCurrentPosition = currentPosition
        return this as Returner
    }

    /**
     * 预览页面是否显示选择框
     */
    fun checkable(checkable: Boolean): Returner {
        this.mCheckable = checkable
        return this as Returner
    }

}