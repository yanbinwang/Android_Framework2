package com.yanzhenjie.album.api;

import android.content.Context;

import androidx.annotation.IntRange;

import com.yanzhenjie.album.Filter;

/**
 * Created by YanZhenjie on 2017/8/16.
 */
public abstract class BasicChoiceWrapper<Returner extends BasicChoiceWrapper, Result, Cancel, Checked> extends BasicAlbumWrapper<Returner, Result, Cancel, Checked> {
    int mColumnCount = 2;
    boolean mHasCamera = true;
    boolean mFilterVisibility = true;
    Filter<Long> mSizeFilter;
    Filter<String> mMimeTypeFilter;

    BasicChoiceWrapper(Context context) {
        super(context);
    }

    /**
     * Turn on the camera function.
     */
    public Returner camera(boolean hasCamera) {
        this.mHasCamera = hasCamera;
        return (Returner) this;
    }

    /**
     * Sets the number of columns for the page.
     *
     * @param count the number of columns.
     */
    public Returner columnCount(@IntRange(from = 2, to = 4) int count) {
        this.mColumnCount = count;
        return (Returner) this;
    }

    /**
     * Filter the file size.
     *
     * @param filter filter.
     */
    public Returner filterSize(Filter<Long> filter) {
        this.mSizeFilter = filter;
        return (Returner) this;
    }

    /**
     * Filter the file extension.
     *
     * @param filter filter.
     */
    public Returner filterMimeType(Filter<String> filter) {
        this.mMimeTypeFilter = filter;
        return (Returner) this;
    }

    /**
     * The visibility of the filtered file.
     *
     * @param visibility true is displayed, false is not displayed.
     */
    public Returner afterFilterVisibility(boolean visibility) {
        this.mFilterVisibility = visibility;
        return (Returner) this;
    }

}