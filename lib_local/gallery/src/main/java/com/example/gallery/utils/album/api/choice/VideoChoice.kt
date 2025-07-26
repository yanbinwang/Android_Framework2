package com.example.gallery.utils.album.api.choice

import android.content.Context
import com.yanzhenjie.album.api.choice.Choice

class VideoChoice(private val mContext: Context?) : Choice<VideoMultipleWrapper, VideoSingleWrapper> {

    override fun multipleChoice(): VideoMultipleWrapper {
        return VideoMultipleWrapper(mContext)
    }

    override fun singleChoice(): VideoSingleWrapper {
        return VideoSingleWrapper(mContext)
    }

}