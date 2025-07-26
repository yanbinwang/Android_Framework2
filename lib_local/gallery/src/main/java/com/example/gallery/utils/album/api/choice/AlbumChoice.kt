package com.example.gallery.utils.album.api.choice

import android.content.Context
import com.yanzhenjie.album.api.choice.Choice

class AlbumChoice(private val mContext: Context?) : Choice<AlbumMultipleWrapper, AlbumSingleWrapper> {

    override fun multipleChoice(): AlbumMultipleWrapper {
        return AlbumMultipleWrapper(mContext)
    }

    override fun singleChoice(): AlbumSingleWrapper {
        return AlbumSingleWrapper(mContext)
    }

}