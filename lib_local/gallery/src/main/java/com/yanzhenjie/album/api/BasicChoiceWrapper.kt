package com.yanzhenjie.album.api

import android.content.Context
import androidx.annotation.IntRange
import com.yanzhenjie.album.Filter

/**
 * Created by YanZhenjie on 2017/8/16.
 */
abstract class BasicChoiceWrapper<Returner : BasicChoiceWrapper<Returner, Result, Cancel, Checked>, Result, Cancel, Checked>(context: Context) : BasicAlbumWrapper<Returner, Result, Cancel, Checked>(context) {
    var mColumnCount = 2
    var mHasCamera = true
    var mFilterVisibility = true
    var mSizeFilter: Filter<Long>? = null
    var mMimeTypeFilter: Filter<String>? = null

    /**
     * Turn on the camera function.
     */
    fun camera(hasCamera: Boolean): Returner {
        this.mHasCamera = hasCamera
        return this as Returner
    }

    /**
     * Sets the number of columns for the page.
     *
     * @param count the number of columns.
     */
    fun columnCount(@IntRange(from = 2, to = 4) count: Int): Returner {
        this.mColumnCount = count
        return this as Returner
    }

    /**
     * Filter the file size.
     *
     * @param filter filter.
     */
    fun filterSize(filter: Filter<Long>): Returner {
        this.mSizeFilter = filter
        return this as Returner
    }

    /**
     * Filter the file extension.
     *
     * @param filter filter.
     */
    fun filterMimeType(filter: Filter<String>): Returner {
        this.mMimeTypeFilter = filter
        return this as Returner
    }

    /**
     * The visibility of the filtered file.
     *
     * @param visibility true is displayed, false is not displayed.
     */
    fun afterFilterVisibility(visibility: Boolean): Returner {
        this.mFilterVisibility = visibility
        return this as Returner
    }

}