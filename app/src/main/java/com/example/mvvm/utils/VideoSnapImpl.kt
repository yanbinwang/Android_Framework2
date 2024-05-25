package com.example.mvvm.utils

import com.example.mvvm.bean.VideoSnap

interface VideoSnapImpl {
    /**
     * 释放的数据bean
     */
    fun releaseVideo(bean: VideoSnap?)

    /**
     * 播放的数据
     */
    fun playVideo(bean: VideoSnap?)
}