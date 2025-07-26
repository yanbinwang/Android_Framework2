package com.example.gallery.utils.album.api.choice

import android.content.Context
import android.content.Intent
import androidx.annotation.IntRange
import com.example.gallery.activity.AlbumActivity
import com.example.gallery.utils.album.Album
import com.yanzhenjie.album.AlbumFile

class ImageMultipleWrapper(context: Context?) : BasicChoiceWrapper<ImageMultipleWrapper, ArrayList<AlbumFile>, String, ArrayList<AlbumFile>>(context) {
    @IntRange(from = 1, to = Int.MAX_VALUE.toLong())
    private var mLimitCount = Int.MAX_VALUE

    fun checkedList(checked: ArrayList<AlbumFile>): ImageMultipleWrapper {
        this.mChecked = checked
        return this
    }

    /**
     * Set the maximum number to be selected.
     *
     * @param count the maximum number.
     */
    fun selectCount(@IntRange(from = 1, to = Int.MAX_VALUE.toLong()) count: Int): ImageMultipleWrapper {
        this.mLimitCount = count
        return this
    }

    override fun start() {
        AlbumActivity.sSizeFilter = mSizeFilter
        AlbumActivity.sMimeFilter = mMimeTypeFilter
        AlbumActivity.sResult = mResult
        AlbumActivity.sCancel = mCancel
        val intent = Intent(mContext, AlbumActivity::class.java)
        intent.putExtra(Album.KEY_INPUT_WIDGET, mWidget)
        intent.putParcelableArrayListExtra(Album.KEY_INPUT_CHECKED_LIST, mChecked)

        intent.putExtra(Album.KEY_INPUT_FUNCTION, Album.FUNCTION_CHOICE_IMAGE)
        intent.putExtra(Album.KEY_INPUT_CHOICE_MODE, Album.MODE_MULTIPLE)
        intent.putExtra(Album.KEY_INPUT_COLUMN_COUNT, mColumnCount)
        intent.putExtra(Album.KEY_INPUT_ALLOW_CAMERA, mHasCamera)
        intent.putExtra(Album.KEY_INPUT_LIMIT_COUNT, mLimitCount)
        intent.putExtra(Album.KEY_INPUT_FILTER_VISIBILITY, mFilterVisibility)
        mContext.startActivity(intent)
    }

}