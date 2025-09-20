package com.yanzhenjie.album.api.choice

import android.content.Context
import com.yanzhenjie.album.api.AlbumMultipleWrapper
import com.yanzhenjie.album.api.AlbumSingleWrapper

/**
 * Created by YanZhenjie on 2017/8/16.
 */
class AlbumChoice(private val mContext: Context) : Choice<AlbumMultipleWrapper, AlbumSingleWrapper> {

    override fun multipleChoice(): AlbumMultipleWrapper {
        return AlbumMultipleWrapper(mContext)
    }

    override fun singleChoice(): AlbumSingleWrapper {
        return AlbumSingleWrapper(mContext)
    }

}