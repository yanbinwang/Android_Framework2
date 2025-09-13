package com.yanzhenjie.album.api;

import android.content.Context;

import androidx.annotation.IntRange;

import com.yanzhenjie.album.ItemAction;

import java.util.ArrayList;

/**
 * Created by YanZhenjie on 2017/8/19.
 */
public abstract class BasicGalleryWrapper<Returner extends BasicGalleryWrapper, Result, Cancel, Checked> extends BasicAlbumWrapper<Returner, ArrayList<Result>, Cancel, ArrayList<Checked>> {
    int mCurrentPosition;
    boolean mCheckable;
    ItemAction<Checked> mItemClick;
    ItemAction<Checked> mItemLongClick;

    public BasicGalleryWrapper(Context context) {
        super(context);
    }

    /**
     * Set the list has been selected.
     *
     * @param checked the data list.
     */
    public final Returner checkedList(ArrayList<Checked> checked) {
        this.mChecked = checked;
        return (Returner) this;
    }

    /**
     * When the preview item is clicked.
     *
     * @param click action.
     */
    public Returner itemClick(ItemAction<Checked> click) {
        this.mItemClick = click;
        return (Returner) this;
    }

    /**
     * When the preview item is clicked long.
     *
     * @param longClick action.
     */
    public Returner itemLongClick(ItemAction<Checked> longClick) {
        this.mItemLongClick = longClick;
        return (Returner) this;
    }

    /**
     * Set the show position of List.
     *
     * @param currentPosition the current position.
     */
    public Returner currentPosition(@IntRange(from = 0, to = Integer.MAX_VALUE) int currentPosition) {
        this.mCurrentPosition = currentPosition;
        return (Returner) this;
    }

    /**
     * The ability to select pictures.
     *
     * @param checkable checkBox is provided.
     */
    public Returner checkable(boolean checkable) {
        this.mCheckable = checkable;
        return (Returner) this;
    }

}