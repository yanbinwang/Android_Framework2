package com.yanzhenjie.album.api

import android.content.Context
import androidx.annotation.IntRange
import com.yanzhenjie.album.ItemAction

/**
 * Created by YanZhenjie on 2017/8/19.
 */
abstract class BasicGalleryWrapper<Returner : BasicGalleryWrapper<Returner, Result, Cancel, Checked>, Result, Cancel, Checked>(context: Context) : BasicAlbumWrapper<Returner, ArrayList<Result>, Cancel, ArrayList<Checked>>(context) {
    var mCurrentPosition = 0
    var mCheckable = false
    var mItemClick: ItemAction<Checked>? = null
    var mItemLongClick: ItemAction<Checked>? = null

    /**
     * Set the list has been selected.
     *
     * @param checked the data list.
     */
    fun checkedList(checked: ArrayList<Checked>): Returner {
        this.mChecked = checked
        return this as Returner
    }

    /**
     * When the preview item is clicked.
     *
     * @param click action.
     */
    fun itemClick(click: ItemAction<Checked>): Returner {
        this.mItemClick = click
        return this as Returner
    }

    /**
     * When the preview item is clicked long.
     *
     * @param longClick action.
     */
    fun itemLongClick(longClick: ItemAction<Checked>): Returner {
        this.mItemLongClick = longClick
        return this as Returner
    }

    /**
     * Set the show position of List.
     *
     * @param currentPosition the current position.
     */
    fun currentPosition(@IntRange(from = 0, to = Integer.MAX_VALUE.toLong()) currentPosition: Int): Returner {
        this.mCurrentPosition = currentPosition
        return this as Returner
    }

    /**
     * The ability to select pictures.
     *
     * @param checkable checkBox is provided.
     */
    fun checkable(checkable: Boolean): Returner {
        this.mCheckable = checkable
        return this as Returner
    }

}