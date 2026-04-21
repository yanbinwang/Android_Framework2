package com.example.gallery.feature.album.api.choice

import android.content.Context
import com.example.gallery.feature.album.api.VideoMultipleWrapper
import com.example.gallery.feature.album.api.VideoSingleWrapper

/**
 * 视频选择器总入口
 * 实现 Choice 接口，提供【视频多选 / 视频单选】
 */
class VideoChoice(private val context: Context) : Choice<VideoMultipleWrapper, VideoSingleWrapper> {

    /**
     * 视频多选
     */
    override fun multipleChoice(): VideoMultipleWrapper {
        return VideoMultipleWrapper(context)
    }

    /**
     * 视频单选
     */
    override fun singleChoice(): VideoSingleWrapper {
        return VideoSingleWrapper(context)
    }

}