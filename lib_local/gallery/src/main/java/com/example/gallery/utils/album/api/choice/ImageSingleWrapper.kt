package com.example.gallery.utils.album.api.choice

import android.content.Context
import android.content.Intent
import com.example.gallery.activity.AlbumActivity
import com.example.gallery.utils.album.Album
import com.yanzhenjie.album.AlbumFile

class ImageSingleWrapper(context: Context?) : BasicChoiceWrapper<ImageSingleWrapper, ArrayList<AlbumFile>, String, AlbumFile>(context) {

    override fun start() {
        AlbumActivity.sSizeFilter = mSizeFilter
        AlbumActivity.sMimeFilter = mMimeTypeFilter
        AlbumActivity.sResult = mResult
        AlbumActivity.sCancel = mCancel
        val intent = Intent(mContext, AlbumActivity::class.java)
        intent.putExtra(Album.KEY_INPUT_WIDGET, mWidget)
        intent.putExtra(Album.KEY_INPUT_FUNCTION, Album.FUNCTION_CHOICE_IMAGE)
        intent.putExtra(Album.KEY_INPUT_CHOICE_MODE, Album.MODE_SINGLE)
        intent.putExtra(Album.KEY_INPUT_COLUMN_COUNT, mColumnCount)
        intent.putExtra(Album.KEY_INPUT_ALLOW_CAMERA, mHasCamera)
        intent.putExtra(Album.KEY_INPUT_LIMIT_COUNT, 1)
        intent.putExtra(Album.KEY_INPUT_FILTER_VISIBILITY, mFilterVisibility)
        mContext.startActivity(intent)
    }

}