package com.yanzhenjie.album.api.choice

import android.content.Context
import com.yanzhenjie.album.api.VideoMultipleWrapper
import com.yanzhenjie.album.api.VideoSingleWrapper

/**
 * Created by YanZhenjie on 2017/8/16.
 */
class VideoChoice(private val mContext: Context) : Choice<VideoMultipleWrapper, VideoSingleWrapper> {

    override fun multipleChoice(): VideoMultipleWrapper {
        return VideoMultipleWrapper(mContext)
    }

    override fun singleChoice(): VideoSingleWrapper {
        return VideoSingleWrapper(mContext)
    }

}