package com.example.mvvm.utils

import com.example.mvvm.bean.VideoSnapBean

interface VideoSnapImpl {
    /**
     * 释放的数据bean
     */
    fun releaseVideo(bean: VideoSnapBean?)

    /**
     * 播放的数据
     */
    fun playVideo(bean: VideoSnapBean?)
}