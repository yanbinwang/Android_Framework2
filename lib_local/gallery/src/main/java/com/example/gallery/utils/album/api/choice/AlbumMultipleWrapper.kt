package com.example.gallery.utils.album.api.choice

import android.content.Context
import android.content.Intent
import androidx.annotation.IntRange
import com.example.gallery.activity.AlbumActivity
import com.example.gallery.utils.album.Album
import com.yanzhenjie.album.AlbumFile
import com.yanzhenjie.album.Filter

class AlbumMultipleWrapper(context: Context?) : BasicChoiceAlbumWrapper<AlbumMultipleWrapper, ArrayList<AlbumFile>, String, ArrayList<AlbumFile>>(context) {
    private var mLimitCount = Int.MAX_VALUE
    private var mDurationFilter: Filter<Long>? = null

    fun checkedList(checked: ArrayList<AlbumFile>): AlbumMultipleWrapper {
        this.mChecked = checked
        return this
    }

    /**
     * Set the maximum number to be selected.
     *
     * @param count the maximum number.
     */
    fun selectCount(@IntRange(from = 1, to = Int.MAX_VALUE.toLong()) count: Int): AlbumMultipleWrapper {
        this.mLimitCount = count
        return this
    }

    /**
     * Filter video duration.
     *
     * @param filter filter.
     */
    fun filterDuration(filter: Filter<Long>): AlbumMultipleWrapper {
        this.mDurationFilter = filter
        return this
    }

    override fun start() {
        AlbumActivity.sSizeFilter = mSizeFilter
        AlbumActivity.sMimeFilter = mMimeTypeFilter
        AlbumActivity.sDurationFilter = mDurationFilter
        AlbumActivity.sResult = mResult
        AlbumActivity.sCancel = mCancel
        val intent = Intent(mContext, AlbumActivity::class.java)
        intent.putExtra(Album.KEY_INPUT_WIDGET, mWidget)
        intent.putParcelableArrayListExtra(Album.KEY_INPUT_CHECKED_LIST, mChecked)

        intent.putExtra(Album.KEY_INPUT_FUNCTION, Album.FUNCTION_CHOICE_ALBUM)
        intent.putExtra(Album.KEY_INPUT_CHOICE_MODE, Album.MODE_MULTIPLE)
        intent.putExtra(Album.KEY_INPUT_COLUMN_COUNT, mColumnCount)
        intent.putExtra(Album.KEY_INPUT_ALLOW_CAMERA, mHasCamera)
        intent.putExtra(Album.KEY_INPUT_LIMIT_COUNT, mLimitCount)
        intent.putExtra(Album.KEY_INPUT_FILTER_VISIBILITY, mFilterVisibility)
        intent.putExtra(Album.KEY_INPUT_CAMERA_QUALITY, mQuality)
        intent.putExtra(Album.KEY_INPUT_CAMERA_DURATION, mLimitDuration)
        intent.putExtra(Album.KEY_INPUT_CAMERA_BYTES, mLimitBytes)
        mContext.startActivity(intent)
    }

}